package echo.music.iad1tya.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.font.FontWeight
import echo.music.iad1tya.viewmodels.LocalFilter
import echo.music.iad1tya.viewmodels.LocalSearchViewModel
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import echo.music.iad1tya.spotify.SpotifyMapper
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.ui.component.ListItem
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.PauseSearchHistoryKey
import echo.music.iad1tya.db.entities.SearchHistory
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.ChipsRow
import echo.music.iad1tya.ui.component.EmptyPlaceholder
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.YouTubeListItem
import echo.music.iad1tya.ui.component.shimmer.ListItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.OnlineSearchViewModel
import echo.music.iad1tya.viewmodels.SearchResultSource
import echo.music.iad1tya.viewmodels.SearchRow
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
  navController: NavController,
  viewModel: OnlineSearchViewModel = hiltViewModel(),
  pureBlack: Boolean = false
) {
  val database = LocalDatabase.current
  val menuState = LocalMenuState.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }

  var isSearchFocused by remember { mutableStateOf(false) }

  val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)

  BackHandler(enabled = isSearchFocused) {
    isSearchFocused = false
    focusManager.clearFocus()
  }

  val encodedQuery = navController.currentBackStackEntry?.arguments?.getString("query") ?: ""
  val decodedQuery =
    remember(encodedQuery) {
      try {
        URLDecoder.decode(encodedQuery, "UTF-8")
      } catch (e: Exception) {
        encodedQuery
      }
    }

  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue(decodedQuery, TextRange(decodedQuery.length)))
    }

  val keyboardController = LocalSoftwareKeyboardController.current

  val onSearch: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        isSearchFocused = false
        focusManager.clearFocus()
        keyboardController?.hide()

        navController.navigate("search/${URLEncoder.encode(searchQuery.trim(), "UTF-8")}") {
          popUpTo("search/${URLEncoder.encode(decodedQuery, "UTF-8")}") { inclusive = true }

          if (!pauseSearchHistory) {
            coroutineScope.launch(Dispatchers.IO) {
              database.query { insert(SearchHistory(query = searchQuery)) }
            }
          }
        }
      }
    }
  }

  LaunchedEffect(decodedQuery) {
    query = TextFieldValue(decodedQuery, TextRange(decodedQuery.length))
  }

  val searchFilter by viewModel.filter.collectAsState()
  val searchSummary = viewModel.summaryPage
  val itemsPage by
    remember(searchFilter) {
      derivedStateOf { searchFilter?.value?.let { viewModel.viewStateMap[it] } }
    }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" } }
      .collect { shouldLoadMore ->
        if (!shouldLoadMore) return@collect
        viewModel.loadMore()
      }
  }

  val ytItemContent: @Composable LazyItemScope.(YTItem, Int, Int, Boolean) -> Unit =
    { item: YTItem, index: Int, size: Int, showSource: Boolean ->
      val longClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
      YouTubeListItem(
        item = item,
        isActive =
          when (item) {
            is SongItem -> mediaMetadata?.id == item.id
            is AlbumItem -> mediaMetadata?.album?.id == item.id
            else -> false
          },
        isPlaying = isPlaying,
        shape = listItemShape(index, size),
        trailingContent = {
          if (showSource) {
            SearchSourceIcon(R.drawable.stage_youtube_music, Color(0xFFFF4E45))
          }
          IconButton(
            onClick = longClick,
          ) {
            Icon(
              painter = painterResource(R.drawable.more_vert),
              contentDescription = null,
            )
          }
        },
        modifier =
          Modifier.combinedClickable(
              onClick = {
                when (item) {
                  is SongItem -> {
                    if (item.id == mediaMetadata?.id) {
                      playerConnection.togglePlayPause()
                    } else {
                      playerConnection.playQueue(
                        YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata())
                      )
                    }
                  }
                  is AlbumItem -> navController.navigate("album/${item.id}")
                  is ArtistItem -> navController.navigate("artist/${item.id}")
                  is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                }
              },
              onLongClick = longClick,
            )
            .animateItem(),
      )
    }

  val context = LocalContext.current
  val notFound = stringResource(R.string.no_results_found)

  fun withResolved(track: SpotifyTrack, action: (SongItem) -> Unit) {
    viewModel.resolveTrack(track) { song ->
      if (song != null) {
        action(song)
      } else {
        Toast.makeText(context, notFound, Toast.LENGTH_SHORT).show()
      }
    }
  }

  val navigateTo: (String) -> Unit = { route -> navController.navigate(route) }

  val rowContent: @Composable LazyItemScope.(SearchRow, Int, Int, Boolean) -> Unit =
    { row, index, size, showSource ->
      when (row) {
        is SearchRow.YouTubeRow -> ytItemContent(row.item, index, size, showSource)
        is SearchRow.TrackRow -> {
          val track = row.track
          val showMenu = {
            withResolved(track) { song ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              menuState.show {
                YouTubeSongMenu(
                  song = song,
                  navController = navController,
                  onDismiss = menuState::dismiss,
                )
              }
            }
          }
          SpotifySearchRow(
            track.name,
            track.artists.joinToString { it.name },
            track.album?.images?.firstOrNull()?.url,
            false,
            index,
            size,
            showSource,
            {
              withResolved(track) { song ->
                playerConnection.playQueue(
                  YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata())
                )
              }
            },
            showMenu,
          )
        }
        is SearchRow.AlbumRow ->
          SpotifySearchRow(
            row.album.name,
            row.album.artists.joinToString { it.name },
            row.album.images.firstOrNull()?.url,
            false,
            index,
            size,
            showSource,
            { viewModel.openAlbum(row.album, navigateTo) },
            null,
          )
        is SearchRow.ArtistRow ->
          SpotifySearchRow(
            row.artist.name,
            null,
            row.artist.images.firstOrNull()?.url,
            true,
            index,
            size,
            showSource,
            { viewModel.openArtist(row.artist, navigateTo) },
            null,
          )
        is SearchRow.PlaylistRow -> {
          val cover = SpotifyMapper.getPlaylistThumbnail(row.playlist)
          SpotifySearchRow(
            row.playlist.name,
            row.playlist.owner?.displayName,
            cover,
            false,
            index,
            size,
            showSource,
            {
              val title = Uri.encode(row.playlist.name)
              val coverArg = Uri.encode(cover.orEmpty())
              navController.navigate(
                "spotify_playlist/${row.playlist.id}?title=$title&cover=$coverArg"
              )
            },
            null,
          )
        }
      }
    }

  val searchSource by viewModel.source.collectAsState()
  val localSearchViewModel: LocalSearchViewModel = hiltViewModel()
  val localFilter by localSearchViewModel.filter.collectAsState()
  val showSource = searchSource == SearchResultSource.ALL && viewModel.spotifyConnected
  val youtubeLoading =
    searchSource != SearchResultSource.SPOTIFY &&
      (searchFilter?.let { itemsPage == null && !viewModel.filterFailed(it) }
        ?: (searchSummary == null && !viewModel.summaryFailed))
  val spotifyLoading = searchSource != SearchResultSource.YOUTUBE && viewModel.spotifyLoading

  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color =
        if (pureBlack) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceContainerHighest,
      modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp),
    ) {
      BasicTextField(
        value = query,
        onValueChange = { newQuery -> query = newQuery },
        modifier =
          Modifier.fillMaxWidth()
            .height(48.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
              if (focusState.isFocused) {
                isSearchFocused = true
              }
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp),
          ) {
            IconButton(onClick = { navController.navigateUp() }) {
              Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = stringResource(R.string.dismiss),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
              if (query.text.isEmpty()) {
                Text(
                  text = stringResource(R.string.stage_search_music),
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                )
              }
              innerTextField()
            }
            if (query.text.isNotEmpty()) {
              IconButton(onClick = { query = TextFieldValue("") }) {
                Icon(
                  painter = painterResource(R.drawable.close),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        },
      )
    }

    Box(modifier = Modifier.weight(1f)) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          SearchSourcePicker(
            selected = searchSource,
            sources =
              buildList {
                add(SearchResultSource.ALL to stringResource(R.string.stage_all_sources))
                add(SearchResultSource.YOUTUBE to stringResource(R.string.stage_youtube_music))
                if (viewModel.spotifyConnected) {
                  add(SearchResultSource.SPOTIFY to stringResource(R.string.stage_spotify))
                }
                add(SearchResultSource.LIBRARY to stringResource(R.string.stage_my_library))
              },
            onSelect = {
              viewModel.source.value = it
              if (it == SearchResultSource.SPOTIFY && searchFilter == FILTER_VIDEO) {
                viewModel.filter.value = null
              }
              coroutineScope.launch { lazyListState.scrollToItem(0) }
            },
          )

          if (searchSource == SearchResultSource.LIBRARY) {
            ChipsRow(
              chips =
                listOf(
                  LocalFilter.ALL to stringResource(R.string.filter_all),
                  LocalFilter.SONG to stringResource(R.string.filter_songs),
                  LocalFilter.ALBUM to stringResource(R.string.filter_albums),
                  LocalFilter.ARTIST to stringResource(R.string.filter_artists),
                  LocalFilter.PLAYLIST to stringResource(R.string.filter_playlists),
                ),
              currentValue = localFilter,
              onValueUpdate = { localSearchViewModel.filter.value = it },
              startPadding = 10.dp,
              modifier = Modifier.weight(1f),
            )
          } else {
            ChipsRow(
              chips =
                buildList {
                  add(null to stringResource(R.string.filter_all))
                  add(FILTER_SONG to stringResource(R.string.filter_songs))
                  add(FILTER_ALBUM to stringResource(R.string.filter_albums))
                  add(FILTER_ARTIST to stringResource(R.string.filter_artists))
                  add(FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_playlists))
                  if (searchSource != SearchResultSource.SPOTIFY) {
                    add(FILTER_VIDEO to stringResource(R.string.filter_videos))
                  }
                },
              currentValue = searchFilter,
              onValueUpdate = {
                if (viewModel.filter.value != it) {
                  viewModel.filter.value = it
                }
                coroutineScope.launch { lazyListState.animateScrollToItem(0) }
              },
              startPadding = 10.dp,
              modifier = Modifier.weight(1f),
            )
          }
        }

        if (searchSource == SearchResultSource.LIBRARY) {
          LocalSearchScreen(
            query = decodedQuery,
            navController = navController,
            onDismiss = {},
            showFilters = false,
            pureBlack = pureBlack,
          )
        } else {
          LazyColumn(
            state = lazyListState,
            contentPadding =
              WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
            modifier = Modifier.fillMaxWidth()
          ) {
            val hasResults: Boolean
            val filter = searchFilter
            if (filter == null) {
              val sections = viewModel.sections(searchSource)
              hasResults = sections.isNotEmpty()
              sections.forEach { section ->
                item(key = "title_${section.title}") {
                  NavigationTitle(stringResource(section.title))
                }

                itemsIndexed(
                  items = section.rows,
                  key = { _, row -> "${section.title}/${row.key}" },
                ) { index, row ->
                  rowContent(row, index, section.rows.size, showSource)
                }
              }
            } else {
              val rows = viewModel.filteredRows(filter, searchSource)
              hasResults = rows.isNotEmpty()
              itemsIndexed(
                items = rows,
                key = { _, row -> "filtered_${row.key}" },
              ) { index, row ->
                rowContent(row, index, rows.size, showSource)
              }

              if (searchSource != SearchResultSource.SPOTIFY && itemsPage?.continuation != null) {
                item(key = "loading") { ShimmerHost { repeat(3) { ListItemPlaceHolder() } } }
              }
            }

            if (!hasResults) {
              if (youtubeLoading || spotifyLoading) {
                item(key = "shimmer") { ShimmerHost { repeat(8) { ListItemPlaceHolder() } } }
              } else {
                item(key = "no_results") {
                  EmptyPlaceholder(
                    icon = R.drawable.search,
                    text = stringResource(R.string.no_results_found),
                  )
                }
              }
            }

            item(key = "bottom_spacer") {
              Spacer(
                modifier =
                  Modifier.windowInsetsBottomHeight(LocalPlayerAwareWindowInsets.current)
              )
            }
          }
        }
      }
      if (isSearchFocused) {
        OnlineSearchScreen(
          query = query.text,
          onQueryChange = { query = it },
          navController = navController,
          onSearch = onSearch,
          onDismiss = {
            isSearchFocused = false
            focusManager.clearFocus()
          },
          pureBlack = pureBlack
        )
      }
    }
  }
}

/** The small service logo at the end of a result row. */
@Composable
private fun SearchSourceIcon(icon: Int, tint: Color) {
  Icon(
    painter = painterResource(icon),
    contentDescription = null,
    tint = tint,
    modifier = Modifier.size(16.dp),
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.SpotifySearchRow(
  title: String,
  subtitle: String?,
  imageUrl: String?,
  circular: Boolean,
  index: Int,
  size: Int,
  showSource: Boolean,
  onClick: () -> Unit,
  onMenu: (() -> Unit)?,
) {
  ListItem(
    title = title,
    subtitle = {
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    },
    thumbnailContent = {
      AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
          Modifier.size(ListThumbnailSize)
            .clip(if (circular) CircleShape else RoundedCornerShape(ThumbnailCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
      )
    },
    trailingContent = {
      if (showSource) {
        SearchSourceIcon(R.drawable.ic_spotify, Color(0xFF1DB954))
      }
      if (onMenu != null) {
        IconButton(onClick = onMenu) {
          Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
        }
      }
    },
    shape = listItemShape(index, size),
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onMenu)
        .animateItem(),
  )
}

/** The pill that chooses where to search, at the start of the filter row. */
@Composable
private fun SearchSourcePicker(
  selected: SearchResultSource,
  sources: List<Pair<SearchResultSource, String>>,
  onSelect: (SearchResultSource) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = Modifier.padding(start = 18.dp, top = 12.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier.clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHighest)
          .clickable { expanded = true }
          .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
    ) {
      Text(
        text = sources.firstOrNull { it.first == selected }?.second.orEmpty(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
      )
      Icon(
        painter = painterResource(R.drawable.expand_more),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      sources.forEach { (source, label) ->
        DropdownMenuItem(
          text = { Text(label) },
          onClick = {
            expanded = false
            onSelect(source)
          },
        )
      }
    }
  }
}
