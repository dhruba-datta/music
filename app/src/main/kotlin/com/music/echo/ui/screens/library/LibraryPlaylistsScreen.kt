package echo.music.iad1tya.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.CONTENT_TYPE_HEADER
import echo.music.iad1tya.constants.CONTENT_TYPE_PLAYLIST
import echo.music.iad1tya.constants.GridItemSize
import echo.music.iad1tya.constants.GridItemsSizeKey
import echo.music.iad1tya.constants.GridThumbnailHeight
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.LibraryViewType
import echo.music.iad1tya.constants.PlaylistSortDescendingKey
import echo.music.iad1tya.constants.PlaylistSortType
import echo.music.iad1tya.constants.PlaylistSortTypeKey
import echo.music.iad1tya.constants.PlaylistViewTypeKey
import echo.music.iad1tya.constants.YtmSyncKey
import echo.music.iad1tya.ui.component.EmptyPlaceholder
import echo.music.iad1tya.ui.component.LibraryPlaylistGridItem
import echo.music.iad1tya.ui.component.LibraryPlaylistListItem
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.SortHeader
import echo.music.iad1tya.ui.component.StageLibraryListHeader
import echo.music.iad1tya.ui.component.StageSectionTitle
import echo.music.iad1tya.ui.component.stageGridFullWidth
import echo.music.iad1tya.ui.component.stageGridPadding
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.LibraryPlaylistsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun LibraryPlaylistsScreen(
  navController: NavController,
  onBack: () -> Unit,
  viewModel: LibraryPlaylistsViewModel = hiltViewModel(),
  initialTextFieldValue: String? = null,
  allowSyncing: Boolean = true,
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current

  val coroutineScope = rememberCoroutineScope()

  var viewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.GRID)
  val (sortType, onSortTypeChange) =
    rememberEnumPreference(PlaylistSortTypeKey, PlaylistSortType.CREATE_DATE)
  val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSortDescendingKey, true)
  val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

  val playlists by viewModel.allPlaylists.collectAsState()

  val lazyListState = rememberLazyListState()
  val lazyGridState = rememberLazyGridState()

  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

  val (ytmSync) = rememberPreference(YtmSyncKey, true)

  LaunchedEffect(Unit) {
    if (ytmSync) {
      withContext(Dispatchers.IO) { viewModel.sync() }
    }
  }

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      when (viewType) {
        LibraryViewType.LIST -> lazyListState.animateScrollToItem(0)
        LibraryViewType.GRID -> lazyGridState.animateScrollToItem(0)
      }
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  val headerContent =
    @Composable {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 8.dp),
      ) {
        StageSectionTitle(text = pluralStringResource(R.plurals.n_playlist, playlists.size, playlists.size))

        Spacer(Modifier.weight(1f))

        SortHeader(
          sortType = sortType,
          sortDescending = sortDescending,
          onSortTypeChange = onSortTypeChange,
          onSortDescendingChange = onSortDescendingChange,
          sortTypeText = { sortType ->
            when (sortType) {
              PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
              PlaylistSortType.NAME -> R.string.sort_by_name
              PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
              PlaylistSortType.LAST_UPDATED -> R.string.sort_by_last_updated
            }
          },
        )
      }
    }

  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    when (viewType) {
      LibraryViewType.LIST -> {
        LazyColumn(
          state = lazyListState,
          contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
        ) {
          item(
            key = "filter",
            contentType = CONTENT_TYPE_HEADER,
          ) {
            StageLibraryListHeader(title = stringResource(R.string.filter_playlists), onBack = onBack)
          }

          item(
            key = "header",
            contentType = CONTENT_TYPE_HEADER,
          ) {
            headerContent()
          }


          playlists.let { playlists ->
            if (playlists.isEmpty()) {
              item(key = "empty_placeholder") {
                EmptyPlaceholder(
                  icon = R.drawable.playlist_play,
                  text = stringResource(R.string.library_playlist_empty),
                )
              }
            }

            items(
              items = playlists.distinctBy { it.id },
              key = { it.id },
              contentType = { CONTENT_TYPE_PLAYLIST },
            ) { playlist ->
              LibraryPlaylistListItem(
                navController = navController,
                menuState = menuState,
                coroutineScope = coroutineScope,
                playlist = playlist,
                modifier = Modifier.animateItem()
              )
            }
          }
        }
      }
      LibraryViewType.GRID -> {
        LazyVerticalGrid(
          state = lazyGridState,
          columns =
            GridCells.Adaptive(
              minSize =
                GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp,
            ),
          contentPadding = stageGridPadding(),
        ) {
          item(
            key = "filter",
            span = { GridItemSpan(maxLineSpan) },
            contentType = CONTENT_TYPE_HEADER,
          ) {
            Box(Modifier.stageGridFullWidth()) {
              StageLibraryListHeader(title = stringResource(R.string.filter_playlists), onBack = onBack)
            }
          }

          item(
            key = "header",
            span = { GridItemSpan(maxLineSpan) },
            contentType = CONTENT_TYPE_HEADER,
          ) {
            Box(Modifier.stageGridFullWidth()) { headerContent() }
          }


          playlists.let { playlists ->
            if (playlists.isEmpty()) {
              item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyPlaceholder(
                  icon = R.drawable.playlist_play,
                  text = stringResource(R.string.library_playlist_empty),
                )
              }
            }

            items(
              items = playlists.distinctBy { it.id },
              key = { it.id },
              contentType = { CONTENT_TYPE_PLAYLIST },
            ) { playlist ->
              LibraryPlaylistGridItem(
                navController = navController,
                menuState = menuState,
                coroutineScope = coroutineScope,
                playlist = playlist,
                modifier = Modifier.animateItem()
              )
            }
          }
        }
      }
    }
  }
}
