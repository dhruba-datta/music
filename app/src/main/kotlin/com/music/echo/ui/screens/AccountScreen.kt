package echo.music.iad1tya.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.music.innertube.models.PlaylistItem
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.ui.component.StageSourceCard
import echo.music.iad1tya.ui.component.StageSourcePinnedCard
import echo.music.iad1tya.ui.component.stageSourceSection
import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.GridItemSize
import echo.music.iad1tya.constants.GridItemsSizeKey
import echo.music.iad1tya.constants.GridThumbnailHeight
import echo.music.iad1tya.ui.component.ChipsRow
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.YouTubeGridItem
import echo.music.iad1tya.ui.component.shimmer.GridItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.component.stageGridFullWidth
import echo.music.iad1tya.ui.component.stageGridPadding
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.viewmodels.AccountContentType
import echo.music.iad1tya.viewmodels.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  viewModel: AccountViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current

  val coroutineScope = rememberCoroutineScope()

  val playlists by viewModel.playlists.collectAsState()
  val albums by viewModel.albums.collectAsState()
  val artists by viewModel.artists.collectAsState()
  val selectedContentType by viewModel.selectedContentType.collectAsState()
  val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

  var showAll by rememberSaveable { mutableStateOf(false) }
  val seeAllLabel = stringResource(R.string.stage_see_all)
  val playlistsTitle = stringResource(R.string.filter_playlists)
  val albumsTitle = stringResource(R.string.filter_albums)
  val artistsTitle = stringResource(R.string.filter_artists)

  fun openAll(type: AccountContentType) {
    viewModel.setSelectedContentType(type)
    showAll = true
  }

  fun playlistMenu(item: PlaylistItem) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    menuState.show {
      YouTubePlaylistMenu(
        playlist = item,
        coroutineScope = coroutineScope,
        onDismiss = menuState::dismiss,
      )
    }
  }

  BackHandler(enabled = showAll) { showAll = false }

  if (!showAll) {
    LazyColumn(contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()) {
      item(key = "liked") {
        StageSourcePinnedCard(
          title = stringResource(R.string.liked),
          subtitle = stringResource(R.string.stage_youtube_music),
          iconRes = R.drawable.favorite,
          colors = listOf(Color(0xFFFF4E45), Color(0xFF7A0F1E)),
          onClick = { navController.navigate("auto_playlist/liked") },
          modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp),
        )
      }
      stageSourceSection(
        key = "playlists",
        title = playlistsTitle,
        items = playlists.orEmpty().distinctBy { it.id },
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { openAll(AccountContentType.PLAYLISTS) },
      ) { item ->
        StageSourceCard(
          title = item.title,
          subtitle = item.author?.name ?: item.songCountText,
          imageUrl = item.thumbnail,
          onClick = { navController.navigate("online_playlist/${item.id}") },
          onLongClick = { playlistMenu(item) },
        )
      }
      stageSourceSection(
        key = "albums",
        title = albumsTitle,
        items = albums.orEmpty().distinctBy { it.id },
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { openAll(AccountContentType.ALBUMS) },
      ) { item ->
        StageSourceCard(
          title = item.title,
          subtitle = item.artists?.joinToString { it.name },
          imageUrl = item.thumbnail,
          onClick = { navController.navigate("album/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              YouTubeAlbumMenu(
                albumItem = item,
                navController = navController,
                onDismiss = menuState::dismiss
              )
            }
          },
        )
      }
      stageSourceSection(
        key = "artists",
        title = artistsTitle,
        items = artists.orEmpty().distinctBy { it.id },
        itemKey = { it.id },
        seeAllLabel = seeAllLabel,
        onSeeAll = { openAll(AccountContentType.ARTISTS) },
      ) { item ->
        StageSourceCard(
          title = item.title,
          subtitle = null,
          imageUrl = item.thumbnail,
          circular = true,
          onClick = { navController.navigate("artist/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show { YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss) }
          },
        )
      }
      if (playlists == null) {
        item(key = "loading") {
          Box(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }
  } else {
  LazyVerticalGrid(
    columns =
      GridCells.Adaptive(
        minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp
      ),
    contentPadding = stageGridPadding(),
  ) {
    item(span = { GridItemSpan(maxLineSpan) }) {
      ChipsRow(
        chips =
          listOf(
            AccountContentType.PLAYLISTS to stringResource(R.string.filter_playlists),
            AccountContentType.ALBUMS to stringResource(R.string.filter_albums),
            AccountContentType.ARTISTS to stringResource(R.string.filter_artists),
          ),
        currentValue = selectedContentType,
        onValueUpdate = { viewModel.setSelectedContentType(it) },
        modifier = Modifier.stageGridFullWidth(),
      )
    }

    when (selectedContentType) {
      AccountContentType.PLAYLISTS -> {
        items(
          items = playlists.orEmpty().distinctBy { it.id },
          key = { it.id },
        ) { item ->
          YouTubeGridItem(
            item = item,
            fillMaxWidth = true,
            modifier =
              Modifier.combinedClickable(
                onClick = { navController.navigate("online_playlist/${item.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    YouTubePlaylistMenu(
                      playlist = item,
                      coroutineScope = coroutineScope,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              ),
          )
        }

        if (playlists == null) {
          items(8) { ShimmerHost { GridItemPlaceHolder(fillMaxWidth = true) } }
        }
      }
      AccountContentType.ALBUMS -> {
        items(items = albums.orEmpty().distinctBy { it.id }, key = { it.id }) { item ->
          YouTubeGridItem(
            item = item,
            fillMaxWidth = true,
            modifier =
              Modifier.combinedClickable(
                onClick = { navController.navigate("album/${item.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    YouTubeAlbumMenu(
                      albumItem = item,
                      navController = navController,
                      onDismiss = menuState::dismiss
                    )
                  }
                }
              )
          )
        }

        if (albums == null) {
          items(8) { ShimmerHost { GridItemPlaceHolder(fillMaxWidth = true) } }
        }
      }
      AccountContentType.ARTISTS -> {
        items(items = artists.orEmpty().distinctBy { it.id }, key = { it.id }) { item ->
          YouTubeGridItem(
            item = item,
            fillMaxWidth = true,
            modifier =
              Modifier.combinedClickable(
                onClick = { navController.navigate("artist/${item.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
                  }
                }
              )
          )
        }

        if (artists == null) {
          items(8) { ShimmerHost { GridItemPlaceHolder(fillMaxWidth = true) } }
        }
      }
    }
  }

  }

  StageTopBar(
    title = { Text(stringResource(R.string.stage_youtube_music)) },
    navigationIcon = {
      IconButton(
        onClick = { if (showAll) showAll = false else navController.navigateUp() },
        onLongClick = navController::backToMain,
      ) {
        Icon(
          painterResource(R.drawable.arrow_back),
          contentDescription = null,
        )
      }
    }
)
}
