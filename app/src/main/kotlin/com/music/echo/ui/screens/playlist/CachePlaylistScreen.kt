package echo.music.iad1tya.ui.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.fastSumBy
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.LocalDownloadUtil
import echo.music.iad1tya.constants.HideExplicitKey
import echo.music.iad1tya.constants.SongSortDescendingKey
import echo.music.iad1tya.constants.SongSortType
import echo.music.iad1tya.constants.SongSortTypeKey
import echo.music.iad1tya.db.entities.Song
import echo.music.iad1tya.extensions.toMediaItem
import echo.music.iad1tya.playback.ExoDownloadService
import echo.music.iad1tya.playback.queues.ListQueue
import echo.music.iad1tya.ui.component.DraggableScrollbar
import echo.music.iad1tya.ui.component.EmptyPlaceholder
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.SongListItem
import echo.music.iad1tya.ui.component.SortHeader
import echo.music.iad1tya.ui.component.StageHero
import echo.music.iad1tya.ui.component.StageHeroBlendedArtwork
import echo.music.iad1tya.ui.component.StageHeroDownloadButton
import echo.music.iad1tya.ui.component.StageHeroPlayButton
import echo.music.iad1tya.ui.component.StageHeroShuffleButton
import echo.music.iad1tya.ui.component.rememberStageHeroUnderBar
import echo.music.iad1tya.ui.component.stageBarButton
import echo.music.iad1tya.ui.component.stageHeroBarColors
import echo.music.iad1tya.ui.menu.CachePlaylistMenu
import echo.music.iad1tya.ui.menu.SelectionSongMenu
import echo.music.iad1tya.ui.menu.SongMenu
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.ui.utils.resize
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.makeTimeString
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.CachePlaylistViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CachePlaylistScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  viewModel: CachePlaylistViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val menuState = LocalMenuState.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
  val cachedSongs by viewModel.cachedSongs.collectAsState()

  val (sortType, onSortTypeChange) =
    rememberEnumPreference(SongSortTypeKey, SongSortType.CREATE_DATE)
  val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)
  val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

  val sortedSongs =
    remember(cachedSongs, sortType, sortDescending) {
      val sorted =
        when (sortType) {
          SongSortType.CREATE_DATE ->
            cachedSongs.sortedBy { it.song.dateDownload ?: LocalDateTime.MIN }
          SongSortType.NAME -> cachedSongs.sortedBy { it.song.title }
          SongSortType.ARTIST ->
            cachedSongs.sortedBy { song -> song.artists.joinToString(separator = "") { it.name } }
          SongSortType.PLAY_TIME -> cachedSongs.sortedBy { it.song.totalPlayTime }
        }
      if (sortDescending) sorted.reversed() else sorted
    }

  var inSelectMode by rememberSaveable { mutableStateOf(false) }
  val selection =
    rememberSaveable(
      saver =
        listSaver<MutableList<String>, String>(
          save = { it.toList() },
          restore = { it.toMutableStateList() }
        )
    ) {
      mutableStateListOf()
    }
  val onExitSelectionMode = {
    inSelectMode = false
    selection.clear()
  }

  var isSearching by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf(TextFieldValue()) }
  val focusRequester = remember { FocusRequester() }
  val lazyListState = rememberLazyListState()

  LaunchedEffect(isSearching) {
    if (isSearching) {
      focusRequester.requestFocus()
    }
  }

  if (isSearching) {
    BackHandler {
      isSearching = false
      query = TextFieldValue()
    }
  } else if (inSelectMode) {
    BackHandler(onBack = onExitSelectionMode)
  }

  val filteredSongs =
    remember(sortedSongs, query) {
      if (query.text.isEmpty()) sortedSongs
      else
        sortedSongs.filter { song ->
          song.title.contains(query.text, true) ||
            song.artists.any { it.name.contains(query.text, true) }
        }
    }

  LaunchedEffect(filteredSongs) {
    selection.fastForEachReversed { songId ->
      if (filteredSongs.find { it.id == songId } == null) {
        selection.remove(songId)
      }
    }
  }

  val heroUnderBar by rememberStageHeroUnderBar(lazyListState)

  val downloadUtil = LocalDownloadUtil.current
  var downloadState by remember { mutableIntStateOf(Download.STATE_STOPPED) }
  LaunchedEffect(filteredSongs) {
    if (filteredSongs.isEmpty()) {
      downloadState = Download.STATE_STOPPED
      return@LaunchedEffect
    }
    downloadUtil.downloads.collect { downloads ->
      downloadState =
        if (filteredSongs.all { downloads[it.id]?.state == Download.STATE_COMPLETED }) {
          Download.STATE_COMPLETED
        } else if (
          filteredSongs.all {
            downloads[it.id]?.state == Download.STATE_QUEUED ||
              downloads[it.id]?.state == Download.STATE_DOWNLOADING ||
              downloads[it.id]?.state == Download.STATE_COMPLETED
          }
        ) {
          Download.STATE_DOWNLOADING
        } else {
          Download.STATE_STOPPED
        }
    }
  }

  val downloadAllSongs: () -> Unit = {
    val pending =
      filteredSongs.filter { downloadUtil.downloads.value[it.id]?.state != Download.STATE_COMPLETED }
    pending.forEach { song ->
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
    }
  }
  val showPlaylistMenu: () -> Unit = {
    menuState.show {
      CachePlaylistMenu(
        downloadState = downloadState,
        onQueue = { playerConnection.addToQueue(filteredSongs.map { it.toMediaItem() }) },
        onDownload = downloadAllSongs,
        onDismiss = { menuState.dismiss() }
      )
    }
  }

  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    LazyColumn(
      state = lazyListState,
      contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
      if (filteredSongs.isEmpty() && !isSearching) {
        item(key = "empty_placeholder") {
          EmptyPlaceholder(
            icon = R.drawable.music_note,
            text = stringResource(R.string.playlist_is_empty),
            modifier = Modifier.animateItem()
          )
        }
      }

      if (filteredSongs.isEmpty() && isSearching) {
        item(key = "no_results") {
          EmptyPlaceholder(
            icon = R.drawable.search,
            text = stringResource(R.string.no_results_found),
            modifier = Modifier.animateItem()
          )
        }
      } else {
        if (filteredSongs.isNotEmpty() && !isSearching) {
          item(key = "playlist_header") {
            CachePlaylistHeader(
              songs = filteredSongs,
              context = context,
              downloadState = downloadState,
              onDownload = downloadAllSongs,
              modifier = Modifier.animateItem()
            )
          }
        }

        if (filteredSongs.isNotEmpty()) {
          item(key = "sort_header") {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 8.dp)
                  .animateItem(),
            ) {
              SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = onSortTypeChange,
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { sortType ->
                  when (sortType) {
                    SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                    SongSortType.NAME -> R.string.sort_by_name
                    SongSortType.ARTIST -> R.string.sort_by_artist
                    SongSortType.PLAY_TIME -> R.string.sort_by_play_time
                  }
                },
                modifier = Modifier.weight(1f),
                asHeading = true,
              )
            }
          }
        }

        itemsIndexed(filteredSongs, key = { _, song -> song.id }) { index, song ->
          val onCheckedChange: (Boolean) -> Unit = {
            if (it) {
              selection.add(song.id)
            } else {
              selection.remove(song.id)
            }
          }

          SongListItem(
            song = song,
            isActive = song.id == mediaMetadata?.id,
            isPlaying = isPlaying,
            shape = listItemShape(index, filteredSongs.size),
            trailingContent = {
              if (inSelectMode) {
                Checkbox(checked = song.id in selection, onCheckedChange = onCheckedChange)
              } else {
                IconButton(
                  onClick = {
                    menuState.show {
                      SongMenu(
                        originalSong = song,
                        navController = navController,
                        onDismiss = menuState::dismiss,
                        isFromCache = true,
                      )
                    }
                  }
                ) {
                  Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
                }
              }
            },
            modifier =
              Modifier.fillMaxWidth()
                .animateItem()
                .combinedClickable(
                  onClick = {
                    if (inSelectMode) {
                      onCheckedChange(song.id !in selection)
                    } else if (song.id == mediaMetadata?.id) {
                      playerConnection.togglePlayPause()
                    } else {
                      playerConnection.playQueue(
                        ListQueue(
                          title = "Cache Songs",
                          items = filteredSongs.map { it.toMediaItem() },
                          startIndex = index
                        )
                      )
                    }
                  },
                  onLongClick = {
                    if (!inSelectMode) {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      inSelectMode = true
                      onCheckedChange(true)
                    }
                  }
                )
                .animateItem()
          )
        }

        item(key = "bottom_spacer") { Spacer(Modifier.height(50.dp)) }
      }
    }

    DraggableScrollbar(
      modifier =
        Modifier.padding(
            LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues()
          )
          .align(Alignment.CenterEnd),
      scrollState = lazyListState,
      headerItems = 2
    )

    TopAppBar(
      title = {
        when {
          inSelectMode -> {
            Text(
              text = pluralStringResource(R.plurals.n_song, selection.size, selection.size),
              style = MaterialTheme.typography.titleLarge
            )
          }
          isSearching -> {
            TextField(
              value = query,
              onValueChange = { query = it },
              placeholder = {
                Text(
                  text = stringResource(R.string.search),
                  style = MaterialTheme.typography.titleLarge
                )
              },
              singleLine = true,
              textStyle = MaterialTheme.typography.titleLarge,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              colors =
                TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                ),
              modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
          }
          else -> {
            if (!heroUnderBar || filteredSongs.isEmpty()) {
              Text(
                stringResource(R.string.cached_playlist),
                style = MaterialTheme.typography.titleLarge
              )
            }
          }
        }
      },
      navigationIcon = {
        IconButton(
          modifier = Modifier.stageBarButton(),
          onClick = {
            when {
              isSearching -> {
                isSearching = false
                query = TextFieldValue()
                focusManager.clearFocus()
              }
              inSelectMode -> {
                onExitSelectionMode()
              }
              else -> {
                navController.navigateUp()
              }
            }
          },
          onLongClick = {
            if (!isSearching && !inSelectMode) {
              navController.backToMain()
            }
          }
        ) {
          Icon(
            painter =
              painterResource(if (inSelectMode) R.drawable.close else R.drawable.arrow_back),
            contentDescription = null
          )
        }
      },
      actions = {
        if (inSelectMode) {
          Checkbox(
            checked = selection.size == filteredSongs.size && selection.isNotEmpty(),
            onCheckedChange = {
              if (selection.size == filteredSongs.size) {
                selection.clear()
              } else {
                selection.clear()
                selection.addAll(filteredSongs.map { it.id })
              }
            }
          )
          IconButton(
            enabled = selection.isNotEmpty(),
            onClick = {
              menuState.show {
                SelectionSongMenu(
                  songSelection = filteredSongs.filter { it.id in selection },
                  onDismiss = menuState::dismiss,
                  clearAction = onExitSelectionMode
                )
              }
            }
          ) {
            Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
          }
        } else if (!isSearching) {
          IconButton(onClick = { isSearching = true }, modifier = Modifier.stageBarButton()) {
            Icon(
              painter = painterResource(R.drawable.search),
              contentDescription = stringResource(R.string.search)
            )
          }
          if (filteredSongs.isNotEmpty()) {
            IconButton(onClick = showPlaylistMenu, modifier = Modifier.stageBarButton()) {
              Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = stringResource(R.string.more_options)
              )
            }
          }
        }
      },
      colors = stageHeroBarColors(heroUnderBar && !isSearching && !inSelectMode)
    )
  }
}

@Composable
private fun CachePlaylistHeader(
  songs: List<Song>,
  context: android.content.Context,
  downloadState: Int,
  onDownload: () -> Unit,
  modifier: Modifier = Modifier
) {
  val playerConnection = LocalPlayerConnection.current ?: return
  val cacheLength = remember(songs) { songs.fastSumBy { it.song.duration ?: 0 } }

  Column(modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)) {
    StageHero(
      title = stringResource(R.string.cached_playlist),
      subtitle =
        buildString {
          append(pluralStringResource(R.plurals.n_song, songs.size, songs.size))
          if (cacheLength > 0) {
            append(" • ")
            append(makeTimeString(cacheLength * 1000L))
          }
        },
      artwork = { StageHeroBlendedArtwork(songs.map { it.thumbnailUrl }) },
      leadingActions = {
        StageHeroDownloadButton(downloadState = downloadState, onClick = onDownload)
      },
      trailingActions = {
        StageHeroShuffleButton(
          onClick = {
            playerConnection.playQueue(
              ListQueue(
                title = context.getString(R.string.cached_playlist),
                items = songs.shuffled().map { it.toMediaItem() },
              )
            )
          }
        )
        StageHeroPlayButton(
          onClick = {
            playerConnection.playQueue(
              ListQueue(
                title = context.getString(R.string.cached_playlist),
                items = songs.map { it.toMediaItem() },
              )
            )
          }
        )
      }
    )


  }
}
