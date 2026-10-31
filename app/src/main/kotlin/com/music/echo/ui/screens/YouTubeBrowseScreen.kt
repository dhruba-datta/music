package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.GridItemSize
import echo.music.iad1tya.constants.GridItemsSizeKey
import echo.music.iad1tya.constants.GridThumbnailHeight
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StageSectionHeader
import echo.music.iad1tya.ui.component.YouTubeGridItem
import echo.music.iad1tya.ui.component.YouTubeListItem
import echo.music.iad1tya.ui.component.shimmer.GridItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.component.stageGridFullWidth
import echo.music.iad1tya.ui.component.stageGridPadding
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.viewmodels.YouTubeBrowseViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun YouTubeBrowseScreen(
  navController: NavController,
  viewModel: YouTubeBrowseViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val browseResult by viewModel.result.collectAsState()

  val coroutineScope = rememberCoroutineScope()
  val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

  val sections =
    remember(browseResult) {
      val seen = mutableSetOf<String>()
      browseResult?.items.orEmpty().map { section ->
        section.title to section.items.filter { seen.add(it.id) }
      }
    }

  val onItemClick: (YTItem) -> Unit = { item ->
    when (item) {
      is SongItem -> {
        if (item.id == mediaMetadata?.id) {
          playerConnection.togglePlayPause()
        } else {
          playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
        }
      }
      is AlbumItem -> navController.navigate("album/${item.id}")
      is ArtistItem -> navController.navigate("artist/${item.id}")
      is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
    }
  }

  val showItemMenu: (YTItem) -> Unit = { item ->
    menuState.show {
      when (item) {
        is SongItem ->
          YouTubeSongMenu(
            song = item,
            navController = navController,
            onDismiss = menuState::dismiss,
          )
        is AlbumItem ->
          YouTubeAlbumMenu(
            albumItem = item,
            navController = navController,
            onDismiss = menuState::dismiss,
          )
        is ArtistItem ->
          YouTubeArtistMenu(
            artist = item,
            onDismiss = menuState::dismiss,
          )
        is PlaylistItem ->
          YouTubePlaylistMenu(
            playlist = item,
            coroutineScope = coroutineScope,
            onDismiss = menuState::dismiss,
          )
      }
    }
  }

  LazyVerticalGrid(
    columns =
      GridCells.Adaptive(
        minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp
      ),
    contentPadding = stageGridPadding(),
  ) {
    if (browseResult == null) {
      items(8) { ShimmerHost { GridItemPlaceHolder(fillMaxWidth = true) } }
    }

    sections.forEachIndexed { index, (sectionTitle, sectionItems) ->
      if (!sectionTitle.isNullOrEmpty() && sectionItems.isNotEmpty()) {
        item(key = "section_$index", span = { GridItemSpan(maxLineSpan) }) {
          StageSectionHeader(title = sectionTitle, modifier = Modifier.stageGridFullWidth())
        }
      }

      items(
        items = sectionItems,
        key = { it.id },
        span = { GridItemSpan(if (it is SongItem) maxLineSpan else 1) },
      ) { item ->
        val itemModifier =
          Modifier.combinedClickable(
            onClick = { onItemClick(item) },
            onLongClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              showItemMenu(item)
            },
          )

        if (item is SongItem) {
          YouTubeListItem(
            item = item,
            isActive = mediaMetadata?.id == item.id,
            isPlaying = isPlaying,
            trailingContent = {
              IconButton(onClick = { showItemMenu(item) }, onLongClick = {}) {
                Icon(painterResource(R.drawable.more_vert), contentDescription = null)
              }
            },
            modifier = Modifier.stageGridFullWidth().animateItem().then(itemModifier),
          )
        } else {
          YouTubeGridItem(
            item = item,
            isActive = item is AlbumItem && mediaMetadata?.album?.id == item.id,
            isPlaying = isPlaying,
            fillMaxWidth = true,
            coroutineScope = coroutineScope,
            modifier = itemModifier.animateItem(),
          )
        }
      }
    }
  }

  TopAppBar(
    title = { StageBarTitle(browseResult?.title.orEmpty()) },
    navigationIcon = {
      IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
      }
    }
  )
}
