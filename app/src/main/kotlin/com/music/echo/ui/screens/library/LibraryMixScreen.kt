package echo.music.iad1tya.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.HideExplicitKey
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.LibraryFilter
import echo.music.iad1tya.constants.ShowBottomPlaylistKey
import echo.music.iad1tya.constants.ShowCachedPlaylistKey
import echo.music.iad1tya.constants.ShowDownloadedPlaylistKey
import echo.music.iad1tya.constants.ShowExportedPlaylistKey
import echo.music.iad1tya.constants.ShowLikedPlaylistKey
import echo.music.iad1tya.constants.ShowTopPlaylistKey
import echo.music.iad1tya.constants.YtmSyncKey
import echo.music.iad1tya.extensions.toMediaItem
import echo.music.iad1tya.playback.queues.ListQueue
import echo.music.iad1tya.ui.component.AutoPlaylistGrid
import echo.music.iad1tya.ui.component.AutoPlaylistTile
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.SongListItem
import echo.music.iad1tya.ui.component.StageServiceButton
import echo.music.iad1tya.ui.component.StageSourceCard
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.ui.component.stageSourceSection
import echo.music.iad1tya.ui.menu.AlbumMenu
import echo.music.iad1tya.ui.menu.ArtistMenu
import echo.music.iad1tya.ui.menu.PlaylistMenu
import echo.music.iad1tya.ui.menu.SongMenu
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.LibraryMixViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun LibraryMixScreen(
  navController: NavController,
  onOpenList: (LibraryFilter) -> Unit,
  viewModel: LibraryMixViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val (ytmSync) = rememberPreference(YtmSyncKey, true)
  val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
  val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

  val topSize by viewModel.topValue.collectAsState(initial = 50)

  val (showLiked) = rememberPreference(ShowLikedPlaylistKey, true)
  val (showDownloaded) = rememberPreference(ShowDownloadedPlaylistKey, true)
  val (showExported) = rememberPreference(ShowExportedPlaylistKey, true)
  val (showTop) = rememberPreference(ShowTopPlaylistKey, true)
  val (showBottom) = rememberPreference(ShowBottomPlaylistKey, true)
  val (showCached) = rememberPreference(ShowCachedPlaylistKey, true)

  val albums by viewModel.albums.collectAsState()
  val artists by viewModel.artists.collectAsState()
  val playlists by viewModel.playlists.collectAsState()
  val allSongs by viewModel.allSongs.collectAsState()

  val orderedPlaylists =
    remember(playlists) {
      playlists.distinctBy { it.id }.sortedByDescending { it.playlist.isPinned }
    }
  val songs =
    remember(allSongs, hideExplicit) {
      if (hideExplicit) allSongs.filter { !it.song.explicit } else allSongs
    }

  val coroutineScope = rememberCoroutineScope()

  val lazyListState = rememberLazyListState()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      lazyListState.animateScrollToItem(0)
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  LaunchedEffect(Unit) {
    if (ytmSync) {
      withContext(Dispatchers.IO) { viewModel.syncAllLibrary() }
    }
  }

  val autoPlaylistTiles =
    buildList {
      if (showLiked) add(AutoPlaylistTile(stringResource(R.string.liked), R.drawable.favorite, onClick = { navController.navigate("auto_playlist/liked") }))
      if (showDownloaded) add(AutoPlaylistTile(stringResource(R.string.offline), R.drawable.offline, onClick = { navController.navigate("auto_playlist/downloaded") }))
      add(AutoPlaylistTile(stringResource(R.string.filter_local), R.drawable.snippet_folder, onClick = { navController.navigate("local_songs") }))
      if (showCached) add(AutoPlaylistTile(stringResource(R.string.cached_playlist), R.drawable.cached, onClick = { navController.navigate("cache_playlist/cached") }))
      if (showTop) add(AutoPlaylistTile(stringResource(R.string.my_top) + " $topSize", R.drawable.trending_up, onClick = { navController.navigate("top_playlist/$topSize") }))
      if (showBottom) add(AutoPlaylistTile(stringResource(R.string.my_bottom) + " 50", R.drawable.trending_down, onClick = { navController.navigate("bottom_playlist/50") }))
      if (showExported) add(AutoPlaylistTile(stringResource(R.string.action_exported), R.drawable.download, onClick = { navController.navigate("auto_playlist/exported") }))
      add(AutoPlaylistTile("Taste Mix", R.drawable.playlist_play, onClick = { navController.navigate("generate") }))
    }

  val seeAllLabel = stringResource(R.string.stage_see_all)
  val playlistsTitle = stringResource(R.string.filter_playlists)
  val artistsTitle = stringResource(R.string.filter_artists)
  val albumsTitle = stringResource(R.string.filter_albums)
  val songsTitle = stringResource(R.string.filter_songs)

  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val pullRefreshState = rememberPullToRefreshState()

  PullToRefreshBox(
    state = pullRefreshState,
    isRefreshing = isRefreshing,
    onRefresh = viewModel::refresh,
    indicator = {
      PullToRefreshDefaults.LoadingIndicator(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    }
  ) {
    LazyColumn(
      state = lazyListState,
      contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
      item(key = "services") {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier =
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 16.dp),
        ) {
          StageServiceButton(
            title = stringResource(R.string.stage_youtube_music),
            iconRes = R.drawable.stage_youtube_music,
            colors = listOf(Color(0xFFFF4E45), Color(0xFF7A0F1E)),
            onClick = { navController.navigate(if (isLoggedIn) "account" else "login") },
            modifier = Modifier.weight(1f),
          )
          StageServiceButton(
            title = stringResource(R.string.stage_spotify),
            iconRes = R.drawable.ic_spotify,
            colors = listOf(Color(0xFF1DB954), Color(0xFF0B3B20)),
            onClick = { navController.navigate("spotify_library") },
            modifier = Modifier.weight(1f),
          )
        }
      }

      item(key = "playlists_title") {
        NavigationTitle(
          title = playlistsTitle,
          onClick = { onOpenList(LibraryFilter.PLAYLISTS) },
          endText = seeAllLabel,
        )
      }

      item(key = "auto_playlists_grid") {
        AutoPlaylistGrid(
          autoPlaylistTiles,
          columns = 4,
          artHeight = 62.dp,
          modifier = Modifier.padding(bottom = 12.dp),
        )
      }

      stageSourceSection(
        key = "playlists",
        title = playlistsTitle,
        items = orderedPlaylists,
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { onOpenList(LibraryFilter.PLAYLISTS) },
        showTitle = false,
      ) { item ->
        StageSourceCard(
          title = item.playlist.name,
          subtitle = pluralStringResource(R.plurals.n_song, item.songCount, item.songCount),
          imageUrl = item.thumbnails.firstOrNull() ?: item.playlist.thumbnailUrl,
          onClick = { navController.navigate("local_playlist/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              PlaylistMenu(
                playlist = item,
                coroutineScope = coroutineScope,
                onDismiss = menuState::dismiss,
              )
            }
          },
        )
      }

      stageSourceSection(
        key = "artists",
        title = artistsTitle,
        items = artists.distinctBy { it.id },
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { onOpenList(LibraryFilter.ARTISTS) },
      ) { item ->
        StageSourceCard(
          title = item.artist.name,
          subtitle = null,
          imageUrl = item.artist.thumbnailUrl,
          circular = true,
          onClick = { navController.navigate("artist/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              ArtistMenu(
                originalArtist = item,
                coroutineScope = coroutineScope,
                onDismiss = menuState::dismiss,
              )
            }
          },
        )
      }

      stageSourceSection(
        key = "albums",
        title = albumsTitle,
        items = albums.distinctBy { it.id },
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { onOpenList(LibraryFilter.ALBUMS) },
      ) { item ->
        StageSourceCard(
          title = item.album.title,
          subtitle = item.artists.joinToString { it.name },
          imageUrl = item.album.thumbnailUrl,
          onClick = { navController.navigate("album/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              AlbumMenu(
                originalAlbum = item,
                navController = navController,
                onDismiss = menuState::dismiss,
              )
            }
          },
        )
      }

      item(key = "songs_title") {
        NavigationTitle(
          title = songsTitle,
          onClick = { onOpenList(LibraryFilter.SONGS) },
          endText = seeAllLabel,
        )
      }

      itemsIndexed(
        items = songs,
        key = { _, item -> "song_${item.song.id}" },
      ) { index, song ->
        SongListItem(
          song = song,
          showInLibraryIcon = true,
          isActive = song.id == mediaMetadata?.id,
          isPlaying = isPlaying,
          showLikedIcon = true,
          shape = listItemShape(index, songs.size),
          trailingContent = {
            IconButton(
              onClick = {
                menuState.show {
                  SongMenu(
                    originalSong = song,
                    navController = navController,
                    onDismiss = menuState::dismiss,
                  )
                }
              },
            ) {
              Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
            }
          },
          modifier =
            Modifier.fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (song.id == mediaMetadata?.id) {
                    playerConnection.togglePlayPause()
                  } else {
                    playerConnection.playQueue(
                      ListQueue(
                        title = context.getString(R.string.queue_all_songs),
                        items = songs.map { it.toMediaItem() },
                        startIndex = index,
                      ),
                    )
                  }
                },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    SongMenu(
                      originalSong = song,
                      navController = navController,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              )
              .animateItem(),
        )
      }
    }
  }
}
