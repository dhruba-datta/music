package echo.music.iad1tya.ui.screens.playlist

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachReversed
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalDownloadUtil
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.HideExplicitKey
import echo.music.iad1tya.db.entities.Playlist
import echo.music.iad1tya.db.entities.PlaylistEntity
import echo.music.iad1tya.db.entities.PlaylistSongMap
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.ExoDownloadService
import echo.music.iad1tya.playback.queues.YouTubePlaylistQueue
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.HeartBurstIcon
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.StageHero
import echo.music.iad1tya.ui.component.StageHeroAction
import echo.music.iad1tya.ui.component.StageHeroArtwork
import echo.music.iad1tya.ui.component.StageHeroDownloadButton
import echo.music.iad1tya.ui.component.StageHeroIconButton
import echo.music.iad1tya.ui.component.StageHeroPlayButton
import echo.music.iad1tya.ui.component.StageHeroShuffleButton
import echo.music.iad1tya.ui.component.YouTubeGridItem
import echo.music.iad1tya.ui.component.YouTubeListItem
import echo.music.iad1tya.ui.component.rememberStageHeroUnderBar
import echo.music.iad1tya.ui.component.stageBarButton
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSelectionSongMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.ui.utils.resize
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.OnlinePlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun OnlinePlaylistScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  viewModel: OnlinePlaylistViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val database = LocalDatabase.current
  val haptic = LocalHapticFeedback.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val coroutineScope = rememberCoroutineScope()

  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val playlist by viewModel.playlist.collectAsState()
  val songs by viewModel.playlistSongs.collectAsState()
  val dbPlaylist by viewModel.dbPlaylist.collectAsState()
  val relatedItems by viewModel.relatedItems.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val isLoadingMore by viewModel.isLoadingMore.collectAsState()
  val error by viewModel.error.collectAsState()

  val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

  val lazyListState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }

  var isSearching by rememberSaveable { mutableStateOf(false) }
  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
  val downloadUtil = LocalDownloadUtil.current
  val context = LocalContext.current

  var downloadState by remember { mutableIntStateOf(Download.STATE_STOPPED) }

  LaunchedEffect(songs) {
    if (songs.isNullOrEmpty()) return@LaunchedEffect
    downloadUtil.downloads.collect { downloads ->
      downloadState =
        if (songs.all { downloads[it.id]?.state == Download.STATE_COMPLETED }) {
          Download.STATE_COMPLETED
        } else if (
          songs.all {
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

  val filteredSongs =
    remember(songs, query) {
      if (query.text.isEmpty()) songs.mapIndexed { i, s -> i to s }
      else
        songs
          .mapIndexed { i, s -> i to s }
          .filter {
            it.second.title.contains(query.text, true) ||
              it.second.artists.fastAny { a -> a.name.contains(query.text, true) }
          }
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

  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(isSearching) { if (isSearching) focusRequester.requestFocus() }

  LaunchedEffect(filteredSongs) {
    selection.fastForEachReversed { songId ->
      if (filteredSongs.find { it.second.id == songId } == null) {
        selection.remove(songId)
      }
    }
  }

  val transparentAppBar by rememberStageHeroUnderBar(lazyListState)

  if (isSearching) {
    BackHandler {
      isSearching = false
      query = TextFieldValue()
    }
  } else if (inSelectMode) {
    BackHandler(onBack = onExitSelectionMode)
  }

  Box(Modifier.fillMaxSize()) {
    LazyColumn(
      state = lazyListState,
      contentPadding =
        LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues(),
    ) {
      if (playlist == null || songs.isEmpty()) {
        if (isLoading) {
          item(key = "loading_placeholder") {
            Box(
              modifier = Modifier.fillMaxWidth().padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              ContainedLoadingIndicator()
            }
          }
        }
      } else {
        playlist?.let { playlist ->
          if (!isSearching) {
            item(key = "playlist_header") {
              OnlinePlaylistHeader(
                playlist = playlist,
                songs = songs,
                dbPlaylist = dbPlaylist,
                downloadState = downloadState,
                coroutineScope = coroutineScope,
                continuation = viewModel.continuation
              )
            }
          }

          itemsIndexed(filteredSongs) { index, (_, songItem) ->
            val onCheckedChange: (Boolean) -> Unit = {
              if (it) {
                selection.add(songItem.id)
              } else {
                selection.remove(songItem.id)
              }
            }

            YouTubeListItem(
              item = songItem,
              isActive = mediaMetadata?.id == songItem.id,
              isPlaying = isPlaying,
              isSelected = inSelectMode && songItem.id in selection,
              shape = listItemShape(index, filteredSongs.size),
              modifier =
                Modifier.combinedClickable(
                    enabled = !hideExplicit || !songItem.explicit,
                    onClick = {
                      if (inSelectMode) {
                        onCheckedChange(songItem.id !in selection)
                      } else if (songItem.id == mediaMetadata?.id) {
                        playerConnection.togglePlayPause()
                      } else {
                        playerConnection.playQueue(
                          YouTubePlaylistQueue(
                            playlistId = playlist.id,
                            playlistTitle = playlist.title,
                            initialSongs = filteredSongs.map { it.second },
                            initialContinuation = viewModel.continuation,
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
                  .animateItem(),
              trailingContent = {
                if (inSelectMode) {
                  Checkbox(checked = songItem.id in selection, onCheckedChange = onCheckedChange)
                } else {
                  IconButton(
                    onClick = {
                      menuState.show {
                        YouTubeSongMenu(songItem, navController, menuState::dismiss)
                      }
                    }
                  ) {
                    Icon(painterResource(R.drawable.more_vert), null)
                  }
                }
              }
            )
          }

          if (isLoadingMore) {
            item(key = "loading_more") {
              Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                ContainedLoadingIndicator()
              }
            }
          }

          if (relatedItems.isNotEmpty() && !isSearching) {
            item(key = "related_title") {
              NavigationTitle(
                title = stringResource(R.string.you_might_also_like),
                modifier = Modifier.animateItem()
              )
            }

            item(key = "related_items") {
              LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().animateItem()
              ) {
                items(relatedItems) { item ->
                  YouTubeGridItem(
                    item = item,
                    modifier =
                      Modifier.width(160.dp)
                        .combinedClickable(
                          onClick = {
                            when (item) {
                              is PlaylistItem ->
                                navController.navigate("online_playlist/${item.id}")
                              is AlbumItem -> navController.navigate("album/${item.browseId}")
                              is ArtistItem -> navController.navigate("artist/${item.id}")
                              is SongItem ->
                                playerConnection.playQueue(
                                  YouTubeQueue(WatchEndpoint(videoId = item.id))
                                )
                            }
                          },
                          onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                              when (item) {
                                is PlaylistItem ->
                                  YouTubePlaylistMenu(
                                    playlist = item,
                                    coroutineScope = coroutineScope,
                                    onDismiss = menuState::dismiss
                                  )
                                is SongItem ->
                                  YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                  )
                                is AlbumItem ->
                                  YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                  )
                                is ArtistItem ->
                                  YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
                              }
                            }
                          }
                        )
                  )
                }
              }
            }
          }

          item(key = "bottom_spacer") { Spacer(Modifier.height(50.dp)) }
        }
      }
    }

    TopAppBar(
      title = {
        if (inSelectMode) {
          Text(
            text = pluralStringResource(R.plurals.n_song, selection.size, selection.size),
            style = MaterialTheme.typography.titleLarge
          )
        } else if (isSearching) {
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
        } else {
          AnimatedContent(
            targetState = !transparentAppBar,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "TopAppBarTitle"
          ) { show ->
            if (show) {
              Text(playlist?.title ?: "")
            }
          }
        }
      },
      navigationIcon = {
        IconButton(
          modifier =
            Modifier.background(
              androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
              CircleShape
            ),
          onClick = {
            if (isSearching) {
              isSearching = false
              query = TextFieldValue()
            } else if (inSelectMode) {
              onExitSelectionMode()
            } else {
              navController.navigateUp()
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
                selection.addAll(filteredSongs.map { it.second.id })
              }
            }
          )
          IconButton(
            enabled = selection.isNotEmpty(),
            onClick = {
              menuState.show {
                YouTubeSelectionSongMenu(
                  songSelection =
                    filteredSongs.filter { it.second.id in selection }.map { it.second },
                  onDismiss = menuState::dismiss,
                  clearAction = onExitSelectionMode
                )
              }
            }
          ) {
            Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
          }
        } else if (!isSearching) {
          IconButton(
            onClick = { isSearching = true },
            modifier =
              Modifier.background(
                androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                CircleShape
              )
          ) {
            Icon(painter = painterResource(R.drawable.search), contentDescription = null)
          }
          val currentPlaylist = playlist
          if (currentPlaylist != null && songs.isNotEmpty()) {
            IconButton(
              onClick = {
                menuState.show {
                  YouTubePlaylistMenu(
                    playlist = currentPlaylist,
                    songs = songs,
                    coroutineScope = coroutineScope,
                    onDismiss = menuState::dismiss,
                  )
                }
              },
              modifier = Modifier.stageBarButton()
            ) {
              Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = stringResource(R.string.more_options)
              )
            }
          }
        }
      },
      colors =
        if (transparentAppBar && !isSearching && !inSelectMode) {
          androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
          )
        } else {
          androidx.compose.material3.TopAppBarDefaults.topAppBarColors()
        }
    )

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun OnlinePlaylistHeader(
  playlist: PlaylistItem,
  songs: List<SongItem>,
  dbPlaylist: Playlist?,
  downloadState: Int,
  coroutineScope: CoroutineScope,
  continuation: String?,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val database = LocalDatabase.current
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val hasExplicitContent = remember(songs) { songs.any { it.explicit } }
  val totalDuration = songs.sumOf { it.duration ?: 0 }

  StageHero(
    title = playlist.title,
    modifier = modifier,
    subtitle =
      buildString {
        playlist.author?.name?.let {
          append(it)
          append(" • ")
        }
        append(pluralStringResource(R.plurals.n_song, songs.size, songs.size))
        if (totalDuration > 0) {
          append(" • ")
          val hours = totalDuration / 3600
          val minutes = (totalDuration % 3600) / 60
          if (hours > 0) {
            append("${hours}h ${minutes}m")
          } else {
            append("${minutes}m")
          }
        }
      },
    artwork = { StageHeroArtwork(playlist.thumbnail?.resize(1200, 1200)) },
    subtitleContent = {
      if (hasExplicitContent) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            painter = painterResource(R.drawable.explicit),
            contentDescription = "Explicit",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = stringResource(R.string.explicit),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    leadingActions = {
      StageHeroDownloadButton(
        downloadState = downloadState,
        onClick = {
          when (downloadState) {
            Download.STATE_COMPLETED,
            Download.STATE_DOWNLOADING -> {
              songs.forEach { song ->
                DownloadService.sendRemoveDownload(
                  context,
                  ExoDownloadService::class.java,
                  song.id,
                  false,
                )
              }
            }
            else -> {
              songs.forEach { song ->
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
          }
        }
      )
      StageHeroAction(
        onClick = {
          coroutineScope.launch(Dispatchers.IO) {
            if (dbPlaylist != null) {
              database.withTransaction {
                val currentPlaylist = dbPlaylist.playlist
                update(currentPlaylist, playlist)
                update(currentPlaylist.toggleLike())
              }
            } else {
              database.withTransaction {
                val playlistEntity =
                  PlaylistEntity(
                      name = playlist.title,
                      browseId = playlist.id,
                      thumbnailUrl = playlist.thumbnail,
                      isEditable = playlist.isEditable,
                      remoteSongCount =
                        playlist.songCountText?.let {
                          Regex("""\d+""").find(it)?.value?.toIntOrNull()
                        },
                      playEndpointParams = playlist.playEndpoint?.params,
                      shuffleEndpointParams = playlist.shuffleEndpoint?.params,
                      radioEndpointParams = playlist.radioEndpoint?.params
                    )
                    .toggleLike()
                insert(playlistEntity)
                songs
                  .map { it.toMediaMetadata() }
                  .onEach { insert(it) }
                  .mapIndexed { index, song ->
                    PlaylistSongMap(
                      songId = song.id,
                      playlistId = playlistEntity.id,
                      position = index,
                      setVideoId = song.setVideoId
                    )
                  }
                  .forEach { insert(it) }
              }
            }
          }
        }
      ) {
        HeartBurstIcon(isLiked = dbPlaylist?.playlist?.bookmarkedAt != null, iconSize = 22.dp)
      }
      StageHeroIconButton(
        icon = R.drawable.share,
        contentDescription = stringResource(R.string.share),
        onClick = {
          val intent =
            Intent().apply {
              action = Intent.ACTION_SEND
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, playlist.shareLink)
            }
          context.startActivity(Intent.createChooser(intent, null))
        }
      )
    },
    trailingActions = {
      StageHeroShuffleButton(
        onClick = {
          if (songs.isNotEmpty()) {
            playerConnection.playQueue(
              YouTubePlaylistQueue(
                playlistId = playlist.id,
                playlistTitle = playlist.title,
                initialSongs = songs.shuffled(),
                initialContinuation = continuation
              )
            )
          }
        }
      )
      StageHeroPlayButton(
        isPlaying = isPlaying && mediaMetadata?.album?.id == playlist.id,
        onClick = {
          if (songs.isNotEmpty()) {
            playerConnection.playQueue(
              YouTubePlaylistQueue(
                playlistId = playlist.id,
                playlistTitle = playlist.title,
                initialSongs = songs,
                initialContinuation = continuation
              )
            )
          }
        }
      )
    }
  )
}
