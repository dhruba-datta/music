package echo.music.iad1tya.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.echo.spotify.SpotifyPlaybackResolver
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.filterYoutubeShorts
import com.music.innertube.pages.SearchSummaryPage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.HideExplicitKey
import echo.music.iad1tya.constants.HideVideoSongsKey
import echo.music.iad1tya.constants.HideYoutubeShortsKey
import echo.music.iad1tya.models.ItemsPage
import echo.music.iad1tya.spotify.Spotify
import echo.music.iad1tya.spotify.models.SpotifyAlbum
import echo.music.iad1tya.spotify.models.SpotifyArtist
import echo.music.iad1tya.spotify.models.SpotifyPlaylist
import echo.music.iad1tya.spotify.models.SpotifySearchResult
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.spotifyimport.SpotifyImportRepository
import echo.music.iad1tya.utils.dataStore
import echo.music.iad1tya.utils.get
import echo.music.iad1tya.utils.reportException
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SONG_SECTION_SIZE = 10
private const val OTHER_SECTION_SIZE = 6

enum class SearchResultSource {
  ALL,
  YOUTUBE,
  SPOTIFY,
  LIBRARY,
}

/** One line of the search results, from either service. */
sealed class SearchRow {
  abstract val key: String

  data class YouTubeRow(val item: YTItem) : SearchRow() {
    override val key = "yt_${item.id}"
  }

  data class TrackRow(val track: SpotifyTrack) : SearchRow() {
    override val key = "sp_track_${track.id}"
  }

  data class AlbumRow(val album: SpotifyAlbum) : SearchRow() {
    override val key = "sp_album_${album.id}"
  }

  data class ArtistRow(val artist: SpotifyArtist) : SearchRow() {
    override val key = "sp_artist_${artist.id}"
  }

  data class PlaylistRow(val playlist: SpotifyPlaylist) : SearchRow() {
    override val key = "sp_playlist_${playlist.id}"
  }
}

data class SearchSection(val title: Int, val rows: List<SearchRow>)

private fun matchKey(title: String, artist: String?): String =
  "$title|${artist.orEmpty()}".lowercase().filter { it.isLetterOrDigit() || it == '|' }

private fun SearchRow.matchKey(): String? =
  when (this) {
    is SearchRow.YouTubeRow ->
      when (val item = item) {
        is SongItem -> matchKey(item.title, item.artists.firstOrNull()?.name)
        is AlbumItem -> matchKey(item.title, item.artists?.firstOrNull()?.name)
        is ArtistItem -> matchKey(item.title, null)
        else -> null
      }
    is SearchRow.TrackRow -> matchKey(track.name, track.artists.firstOrNull()?.name)
    is SearchRow.AlbumRow -> matchKey(album.name, album.artists.firstOrNull()?.name)
    is SearchRow.ArtistRow -> matchKey(artist.name, null)
    is SearchRow.PlaylistRow -> null
  }

/** Takes turns between the two services, dropping anything both of them found. */
private fun alternate(first: List<SearchRow>, second: List<SearchRow>): List<SearchRow> {
  val seen = mutableSetOf<String>()
  return (0 until maxOf(first.size, second.size))
    .flatMap { index -> listOfNotNull(first.getOrNull(index), second.getOrNull(index)) }
    .filter { row -> row.matchKey()?.let(seen::add) ?: true }
    .distinctBy { it.key }
}

@HiltViewModel
class OnlineSearchViewModel
@Inject
constructor(
  @ApplicationContext val context: Context,
  savedStateHandle: SavedStateHandle,
  val extensionManager: com.music.echo.extensions.ExtensionManager,
  private val spotifyImportRepository: SpotifyImportRepository,
  private val spotifyPlaybackResolver: SpotifyPlaybackResolver,
) : ViewModel() {
  val query =
    try {
      URLDecoder.decode(savedStateHandle.get<String>("query")!!, "UTF-8")
    } catch (e: IllegalArgumentException) {
      savedStateHandle.get<String>("query")!!
    }
  val filter = MutableStateFlow<YouTube.SearchFilter?>(null)
  var summaryPage by mutableStateOf<SearchSummaryPage?>(null)
  val viewStateMap = mutableStateMapOf<String, ItemsPage?>()
  val source = MutableStateFlow(SearchResultSource.ALL)

  var spotifyConnected by mutableStateOf(false)
    private set

  var spotifyLoading by mutableStateOf(true)
    private set

  private var spotifyResult by mutableStateOf<SpotifySearchResult?>(null)

  var summaryFailed by mutableStateOf(false)
    private set

  private val failedFilters = mutableStateMapOf<String, Boolean>()

  fun filterFailed(filter: YouTube.SearchFilter): Boolean = failedFilters[filter.value] == true

  init {
    viewModelScope.launch {
      spotifyConnected =
        try {
          spotifyImportRepository.restoreSession().isAuthenticated
        } catch (e: Exception) {
          if (e is kotlinx.coroutines.CancellationException) throw e
          false
        }
      if (spotifyConnected) {
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        spotifyResult =
          withContext(Dispatchers.IO) {
            Spotify.search(query, listOf("track", "album", "artist", "playlist")).getOrNull()
          }
            ?.let { result ->
              if (hideExplicit) {
                result.copy(
                  tracks =
                    result.tracks?.let { page -> page.copy(items = page.items.filter { !it.explicit }) }
                )
              } else {
                result
              }
            }
      }
      spotifyLoading = false
    }

    viewModelScope.launch {
      filter.collect { filter ->
        if (filter == null) {
          if (summaryPage == null) {
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
            
            // Fetch Addons first
            var addonItems: List<com.music.innertube.models.SongItem> = emptyList()
            try {
                addonItems = extensionManager.search(query)
            } catch (e: Exception) {}

            YouTube.searchSummary(query)
              .onSuccess {
                val filtered = it.filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
                    .filterYoutubeShorts(hideYoutubeShorts)
                    
                // Append addons if they exist
                if (addonItems.isNotEmpty()) {
                    val modSummaries = filtered.summaries.toMutableList()
                    modSummaries.add(0, com.music.innertube.pages.SearchSummary(title = "Addons", items = addonItems))
                    summaryPage = com.music.innertube.pages.SearchSummaryPage(summaries = modSummaries)
                } else {
                    summaryPage = filtered
                }
              }
              .onFailure {
                summaryFailed = true
                reportException(it)
              }
          }
        } else {
          if (viewStateMap[filter.value] == null) {
            YouTube.search(query, filter)
              .onSuccess { result ->
                val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                val featured =
                  if (filter == YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST) {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST)
                      .getOrNull()
                      ?.items
                      .orEmpty()
                  } else {
                    emptyList()
                  }
                viewStateMap[filter.value] =
                  ItemsPage(
                    (featured + result.items)
                      .distinctBy { it.id }
                      .filterExplicit(
                        hideExplicit,
                      )
                      .let { items ->
                        if (filter.value == YouTube.SearchFilter.FILTER_VIDEO.value) items
                        else items.filterVideoSongs(hideVideoSongs)
                      }
                      .filterYoutubeShorts(hideYoutubeShorts),
                    result.continuation,
                  )
              }
              .onFailure {
                failedFilters[filter.value] = true
                reportException(it)
              }
          }
        }
      }
    }
  }

  private fun spotifyTracks(): List<SearchRow> =
    spotifyResult?.tracks?.items.orEmpty().map { SearchRow.TrackRow(it) }

  private fun spotifyArtists(): List<SearchRow> =
    spotifyResult?.artists?.items.orEmpty().map { SearchRow.ArtistRow(it) }

  private fun spotifyAlbums(): List<SearchRow> =
    spotifyResult?.albums?.items.orEmpty().map { SearchRow.AlbumRow(it) }

  private fun spotifyPlaylists(): List<SearchRow> =
    spotifyResult?.playlists?.items.orEmpty().map { SearchRow.PlaylistRow(it) }

  /** The sections of the All view for [source]. */
  fun sections(source: SearchResultSource): List<SearchSection> {
    val youtube =
      if (source == SearchResultSource.SPOTIFY) {
        emptyList()
      } else {
        summaryPage?.summaries.orEmpty().flatMap { it.items }.distinctBy { it.id }
      }
    val useSpotify = source != SearchResultSource.YOUTUBE

    fun section(
      title: Int,
      size: Int,
      items: List<YTItem>,
      spotify: List<SearchRow>,
    ): SearchSection =
      SearchSection(
        title,
        alternate(items.map { SearchRow.YouTubeRow(it) }, if (useSpotify) spotify else emptyList())
          .take(size),
      )

    val top =
      youtube.firstOrNull()?.let { SearchRow.YouTubeRow(it) }
        ?: spotifyTracks().firstOrNull().takeIf { useSpotify }
    val topKey = top?.key
    return listOfNotNull(top?.let { SearchSection(R.string.top_result, listOf(it)) })
      .plus(
        listOf(
          section(
            R.string.filter_songs,
            SONG_SECTION_SIZE,
            youtube.filterIsInstance<SongItem>(),
            spotifyTracks(),
          ),
          section(
            R.string.filter_artists,
            OTHER_SECTION_SIZE,
            youtube.filterIsInstance<ArtistItem>(),
            spotifyArtists(),
          ),
          section(
            R.string.filter_albums,
            OTHER_SECTION_SIZE,
            youtube.filterIsInstance<AlbumItem>(),
            spotifyAlbums(),
          ),
          section(
            R.string.filter_playlists,
            OTHER_SECTION_SIZE,
            youtube.filterIsInstance<PlaylistItem>(),
            spotifyPlaylists(),
          ),
        )
          .map { it.copy(rows = it.rows.filter { row -> row.key != topKey }) }
      )
      .filter { it.rows.isNotEmpty() }
  }

  /** The list for one type chip. */
  fun filteredRows(filter: YouTube.SearchFilter, source: SearchResultSource): List<SearchRow> {
    val youtube =
      if (source == SearchResultSource.SPOTIFY) {
        emptyList()
      } else {
        viewStateMap[filter.value]?.items.orEmpty().distinctBy { it.id }.map {
          SearchRow.YouTubeRow(it)
        }
      }
    val spotify =
      if (source == SearchResultSource.YOUTUBE) {
        emptyList()
      } else {
        when (filter) {
          YouTube.SearchFilter.FILTER_SONG -> spotifyTracks()
          YouTube.SearchFilter.FILTER_ALBUM -> spotifyAlbums()
          YouTube.SearchFilter.FILTER_ARTIST -> spotifyArtists()
          YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST -> spotifyPlaylists()
          else -> emptyList()
        }
      }
    return alternate(youtube, spotify)
  }

  fun resolveTrack(track: SpotifyTrack, onResolved: (SongItem?) -> Unit) {
    viewModelScope.launch {
      onResolved(withContext(Dispatchers.IO) { spotifyPlaybackResolver.resolveTrack(track) })
    }
  }

  fun openAlbum(album: SpotifyAlbum, navigate: (String) -> Unit) {
    val name = "${album.name} ${album.artists.firstOrNull()?.name.orEmpty()}".trim()
    viewModelScope.launch {
      val match =
        withContext(Dispatchers.IO) {
          YouTube.search(name, YouTube.SearchFilter.FILTER_ALBUM).getOrNull()?.items
            ?.filterIsInstance<AlbumItem>()
            ?.firstOrNull()
        }
      navigate(match?.let { "album/${it.id}" } ?: "search/${URLEncoder.encode(name, "UTF-8")}")
    }
  }

  fun openArtist(artist: SpotifyArtist, navigate: (String) -> Unit) {
    viewModelScope.launch {
      val match =
        withContext(Dispatchers.IO) {
          YouTube.search(artist.name, YouTube.SearchFilter.FILTER_ARTIST).getOrNull()?.items
            ?.filterIsInstance<ArtistItem>()
            ?.firstOrNull()
        }
      navigate(
        match?.let { "artist/${it.id}" } ?: "search/${URLEncoder.encode(artist.name, "UTF-8")}"
      )
    }
  }

  fun loadMore() {
    val filter = filter.value?.value
    viewModelScope.launch {
      if (filter == null) return@launch
      val viewState = viewStateMap[filter] ?: return@launch
      val continuation = viewState.continuation
      if (continuation != null) {
        val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@launch
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
        val newItems =
          searchResult.items
            .filterExplicit(hideExplicit)
            .let { items ->
              if (filter == YouTube.SearchFilter.FILTER_VIDEO.value) items
              else items.filterVideoSongs(hideVideoSongs)
            }
            .filterYoutubeShorts(hideYoutubeShorts)
        viewStateMap[filter] =
          ItemsPage((viewState.items + newItems).distinctBy { it.id }, searchResult.continuation)
      }
    }
  }
}
