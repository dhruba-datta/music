package echo.music.iad1tya.ui.screens.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.echo.spotify.SpotifyPlaybackResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.playback.SpotifyDownloadQueue
import echo.music.iad1tya.playback.SpotifyQueue
import echo.music.iad1tya.playback.queues.Queue
import echo.music.iad1tya.spotify.Spotify
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.spotifyimport.SpotifyImportRepository
import echo.music.iad1tya.utils.reportException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SpotifyPlaylistDetailUiState(
  val playlistId: String = "",
  val title: String = "",
  val coverUrl: String? = null,
  val tracks: List<SpotifyTrack> = emptyList(),
  val totalCount: Int = 0,
  val isLoading: Boolean = false,
  val resolvingTrackId: String? = null,
  val downloadSongIds: List<String> = emptyList(),
  val isPreparingDownload: Boolean = false,
  val isDownloadStopped: Boolean = false,
  val error: String? = null,
)

@HiltViewModel
class SpotifyPlaylistDetailViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val spotifyImportRepository: SpotifyImportRepository,
  private val spotifyPlaybackResolver: SpotifyPlaybackResolver,
  private val spotifyDownloadQueue: SpotifyDownloadQueue,
  database: MusicDatabase,
) : ViewModel() {

  private val playlistId: String = savedStateHandle.get<String>("playlistId").orEmpty()
  private val initialTitle: String = savedStateHandle.get<String>("title") ?: "Playlist"
  private val initialCover: String? = savedStateHandle.get<String>("cover")?.takeIf { it.isNotBlank() }

  private val _uiState = MutableStateFlow(
    SpotifyPlaylistDetailUiState(
      playlistId = playlistId,
      title = initialTitle,
      coverUrl = initialCover,
      isLoading = true,
    )
  )
  val uiState: StateFlow<SpotifyPlaylistDetailUiState> = _uiState.asStateFlow()

  /** The tracks here that are in the app's Liked songs, by matched song or by title and artist. */
  val likedTrackIds: StateFlow<Set<String>> =
    combine(_uiState, database.likedSongsByCreateDateAsc()) { state, likedSongs ->
        val likedIds = likedSongs.mapTo(HashSet()) { it.id }
        val likedByTitle =
          likedSongs.groupBy(
            keySelector = { matchableTitle(it.song.title) },
            valueTransform = { song -> song.artists.map { it.name } },
          )
        state.tracks
          .filter { track ->
            songIdFor(track) in likedIds ||
              likedByTitle[matchableTitle(track.name)].orEmpty().any { artists ->
                sharesArtist(track.artists.map { it.name }, artists)
              }
          }
          .mapTo(HashSet()) { it.id }
      }
      .flowOn(Dispatchers.Default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  private var pendingDownload: Job? = null

  init {
    spotifyDownloadQueue.load(playlistId)
    loadTracks()
    viewModelScope.launch {
      combine(
          spotifyDownloadQueue.songIds,
          spotifyDownloadQueue.preparing,
          spotifyDownloadQueue.stopped,
        ) { songIds, preparing, stopped ->
          Triple(songIds[playlistId].orEmpty().toList(), playlistId in preparing, playlistId in stopped)
        }
        .collect { (ids, preparing, stopped) ->
          _uiState.update {
            it.copy(
              downloadSongIds = ids,
              isPreparingDownload = preparing,
              isDownloadStopped = stopped,
            )
          }
        }
    }
  }

  fun refresh() {
    loadTracks()
  }

  fun loadTracks() {
    viewModelScope.launch(Dispatchers.IO) {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        spotifyImportRepository.restoreSession()

        val tracksList = mutableListOf<SpotifyTrack>()
        var total = 0

        var offset = 0
        do {
          val pageSize: Int
          if (playlistId == "liked") {
            val page = Spotify.likedSongs(limit = PAGE_SIZE, offset = offset).getOrThrow()
            tracksList.addAll(page.items.map { it.track })
            total = page.total
            pageSize = page.rawCount
          } else {
            val page =
              Spotify.playlistTracks(playlistId = playlistId, limit = PAGE_SIZE, offset = offset)
                .getOrThrow()
            tracksList.addAll(page.items.mapNotNull { it.track })
            total = page.total
            pageSize = page.rawCount
          }
          offset += pageSize
          _uiState.update { it.copy(tracks = tracksList.toList(), totalCount = total) }
        } while (pageSize > 0 && offset < total)

        _uiState.update {
          it.copy(
            tracks = tracksList,
            totalCount = total,
            isLoading = false,
            error = null,
          )
        }
      } catch (e: Exception) {
        reportException(e)
        _uiState.update {
          it.copy(
            isLoading = false,
            error = e.message ?: "Failed to load tracks",
          )
        }
      }
    }
  }

  /** The YouTube song matched to [track], if a download or playback has matched it yet. */
  fun songIdFor(track: SpotifyTrack): String? =
    spotifyDownloadQueue.songIdFor(track.id) ?: spotifyPlaybackResolver.getCached(track.id)?.id

  /** Waits for the track list if it is still loading, then hands it to the download queue. */
  fun downloadAll(onFinished: (queued: Int, missing: Int) -> Unit) {
    if (_uiState.value.isPreparingDownload) return
    _uiState.update { it.copy(isPreparingDownload = true) }
    pendingDownload =
      viewModelScope.launch {
        val tracks = _uiState.first { !it.isLoading }.tracks
        spotifyDownloadQueue.download(playlistId, tracks, onFinished)
        syncPreparingDownload()
      }
  }

  /** Stops what is still in progress and keeps the songs that have finished. */
  fun cancelDownloads() {
    pendingDownload?.cancel()
    spotifyDownloadQueue.cancel(playlistId)
    syncPreparingDownload()
  }

  fun removeDownloads() {
    pendingDownload?.cancel()
    spotifyDownloadQueue.remove(playlistId)
    syncPreparingDownload()
  }

  private fun syncPreparingDownload() {
    _uiState.update {
      it.copy(isPreparingDownload = playlistId in spotifyDownloadQueue.preparing.value)
    }
  }

  /** Plays the list in order from [startIndex], skipping ahead past tracks YouTube doesn't have. */
  fun playFrom(
    startIndex: Int,
    shuffle: Boolean,
    onSuccess: (Queue) -> Unit,
    onError: () -> Unit = {},
  ) {
    if (!shuffle) {
      play({ _uiState.value.tracks }, startIndex, onSuccess, onError)
      return
    }
    // The queue only matches a few songs ahead, so with shuffle on the order is drawn up here.
    val picked = _uiState.value.tracks.getOrNull(startIndex) ?: return
    play(shuffledOrder(picked), 0, onSuccess, onError)
  }

  fun playShuffled(onSuccess: (Queue) -> Unit, onError: () -> Unit = {}) {
    play(shuffledOrder(), 0, onSuccess, onError)
  }

  /** A random order that takes in tracks loaded later, without moving those it already holds. */
  private fun shuffledOrder(first: SpotifyTrack? = null): () -> List<SpotifyTrack> {
    val order = ArrayList<SpotifyTrack>(listOfNotNull(first))
    val seen = order.mapTo(HashSet()) { it.id }
    return {
      synchronized(order) {
        order += _uiState.value.tracks.filter { seen.add(it.id) }.shuffled()
        order.toList()
      }
    }
  }

  private fun play(
    tracks: () -> List<SpotifyTrack>,
    startIndex: Int,
    onSuccess: (Queue) -> Unit,
    onError: () -> Unit,
  ) {
    val list = tracks()
    val startTrack = list.getOrNull(startIndex) ?: return
    viewModelScope.launch {
      _uiState.update { it.copy(resolvingTrackId = startTrack.id) }
      val queue =
        withContext(Dispatchers.IO) {
          (startIndex until minOf(startIndex + MAX_START_ATTEMPTS, list.size)).firstNotNullOfOrNull {
            index ->
            spotifyPlaybackResolver.resolveTrack(list[index])?.let { song ->
              SpotifyQueue(_uiState.value.title, tracks, index + 1, song, spotifyPlaybackResolver)
            }
          }
        }
      _uiState.update { it.copy(resolvingTrackId = null) }
      if (queue != null) onSuccess(queue) else onError()
    }
  }
}

private const val MAX_START_ATTEMPTS = 5
private const val PAGE_SIZE = 50

private val TitleExtras = Regex("""\(.*?\)|\[.*?]""")

/** A title without the extras the same song carries on one service only, like "(Remastered)". */
internal fun matchableTitle(title: String): String {
  val plain = title.lowercase().replace(TitleExtras, "").substringBefore(" - ").trim()
  return plain.ifEmpty { title.lowercase().trim() }
}

/** Whether two artist lists share a name. One may join several artists, as in "A & B". */
internal fun sharesArtist(first: List<String>, second: List<String>): Boolean =
  first.any { a ->
    second.any { b ->
      a.isNotBlank() && b.isNotBlank() &&
        (a.contains(b, ignoreCase = true) || b.contains(a, ignoreCase = true))
    }
  }
