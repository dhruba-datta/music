package echo.music.iad1tya.ui.screens.playlist

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.music.echo.spotify.SpotifyPlaybackResolver
import com.music.innertube.models.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.ExoDownloadService
import echo.music.iad1tya.spotify.Spotify
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.spotifyimport.SpotifyImportRepository
import echo.music.iad1tya.utils.reportException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
  val error: String? = null,
)

@HiltViewModel
class SpotifyPlaylistDetailViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val spotifyImportRepository: SpotifyImportRepository,
  private val spotifyPlaybackResolver: SpotifyPlaybackResolver,
  private val database: MusicDatabase,
  @ApplicationContext private val context: Context,
) : ViewModel() {

  private val playlistId: String = savedStateHandle.get<String>("playlistId").orEmpty()
  private val initialTitle: String = savedStateHandle.get<String>("title") ?: "Playlist"
  private val initialCover: String? = savedStateHandle.get<String>("cover")?.takeIf { it.isNotBlank() }

  private val downloadPrefs = context.getSharedPreferences("spotify_downloads", Context.MODE_PRIVATE)

  private val _uiState = MutableStateFlow(
    SpotifyPlaylistDetailUiState(
      playlistId = playlistId,
      title = initialTitle,
      coverUrl = initialCover,
      isLoading = true,
      downloadSongIds = downloadPrefs.getStringSet(playlistId, emptySet()).orEmpty().toList(),
    )
  )
  val uiState: StateFlow<SpotifyPlaylistDetailUiState> = _uiState.asStateFlow()

  init {
    loadTracks()
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
            pageSize = page.items.size
          } else {
            val page =
              Spotify.playlistTracks(playlistId = playlistId, limit = PAGE_SIZE, offset = offset)
                .getOrThrow()
            tracksList.addAll(page.items.mapNotNull { it.track })
            total = page.total
            pageSize = page.items.size
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

  fun downloadAll(onError: () -> Unit) {
    if (_uiState.value.isPreparingDownload) return
    viewModelScope.launch(Dispatchers.IO) {
      _uiState.update { it.copy(isPreparingDownload = true) }
      val queuedIds = mutableListOf<String>()
      try {
        val songs =
          spotifyPlaybackResolver
            .resolveAndFilter(_uiState.value.tracks, hideExplicit = false)
            .distinctBy { it.id }
        songs.forEach { song ->
          database.withTransaction { insert(song.toMediaMetadata()) }
          val downloadRequest =
            DownloadRequest.Builder(song.id, song.id.toUri())
              .setCustomCacheKey(song.id)
              .setData(song.title.toByteArray())
              .build()
          DownloadService.sendAddDownload(
            context,
            ExoDownloadService::class.java,
            downloadRequest,
            false,
          )
          queuedIds.add(song.id)
        }
      } catch (e: Exception) {
        reportException(e)
      } finally {
        saveDownloadIds(queuedIds)
        _uiState.update { it.copy(isPreparingDownload = false, downloadSongIds = queuedIds.toList()) }
      }
      if (queuedIds.isEmpty()) withContext(Dispatchers.Main) { onError() }
    }
  }

  private fun saveDownloadIds(ids: List<String>) {
    downloadPrefs.edit().putStringSet(playlistId, ids.toSet()).apply()
  }

  fun removeDownloads() {
    _uiState.value.downloadSongIds.forEach { id ->
      DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, id, false)
    }
    saveDownloadIds(emptyList())
    _uiState.update { it.copy(downloadSongIds = emptyList()) }
  }

  fun playTrack(
    track: SpotifyTrack,
    onSuccess: (SongItem) -> Unit,
    onError: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(resolvingTrackId = track.id) }
      val resolved = withContext(Dispatchers.IO) {
        spotifyPlaybackResolver.resolveTrack(track)
      }
      _uiState.update { it.copy(resolvingTrackId = null) }

      if (resolved != null) {
        onSuccess(resolved)
      } else {
        onError()
      }
    }
  }
}

private const val PAGE_SIZE = 50
