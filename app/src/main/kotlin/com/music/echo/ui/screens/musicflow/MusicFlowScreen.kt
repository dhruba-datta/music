package echo.music.iad1tya.ui.screens.musicflow

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.SongItem
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalDownloadUtil
import echo.music.iad1tya.LocalExpandPlayer
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalSetTabMenu
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.LocalSyncUtils
import echo.music.iad1tya.R
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.db.entities.LyricsEntity
import echo.music.iad1tya.db.entities.Song
import echo.music.iad1tya.db.entities.SongEntity
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.models.toSongEntity
import echo.music.iad1tya.playback.ExoDownloadService
import echo.music.iad1tya.playback.PlayerConnection
import echo.music.iad1tya.musicflow.MusicFlowFeedError
import echo.music.iad1tya.musicflow.MusicFlowItem
import echo.music.iad1tya.musicflow.MusicFlowPlaybackStatus
import echo.music.iad1tya.musicflow.MusicFlowUiState
import echo.music.iad1tya.musicflow.TakeoverBlock
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.musicflow.MusicFlowPage
import echo.music.iad1tya.ui.component.musicflow.MusicFlowPageActions
import echo.music.iad1tya.ui.component.musicflow.MusicFlowPagePlaceholder
import echo.music.iad1tya.ui.component.musicflow.MusicFlowTopMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.ui.player.InlineLyricsView
import echo.music.iad1tya.utils.SyncUtils
import echo.music.iad1tya.viewmodels.MusicFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun MusicFlowScreen(
  navController: NavController,
  viewModel: MusicFlowViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val database = LocalDatabase.current
  val syncUtils = LocalSyncUtils.current
  val downloadUtil = LocalDownloadUtil.current
  val menuState = LocalMenuState.current
  val expandPlayer = LocalExpandPlayer.current
  val scope = rememberCoroutineScope()

  DisposableEffect(playerConnection) {
    viewModel.attach(playerConnection)
    onDispose { viewModel.detach() }
  }

  val state by viewModel.uiState.collectAsState()
  val preferences by viewModel.preferences.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
  val currentMediaId = mediaMetadata?.id

  val reduceMotion = remember { context.isReducedMotion() }
  val showCanvas = preferences.animatedBackground && !preferences.dataSaver && !reduceMotion
  val allowArtPrefetch = remember(preferences) {
    !preferences.dataSaver && (preferences.cellularPrefetch || !context.isActiveNetworkMetered())
  }

  var progress by remember(currentMediaId) { mutableFloatStateOf(0f) }
  var positionMs by remember(currentMediaId) { mutableLongStateOf(0L) }
  var durationMs by remember(currentMediaId) { mutableLongStateOf(0L) }

  LaunchedEffect(playerConnection, currentMediaId) {
    while (isActive) {
      val (position, duration) = playerConnection.positionAndDuration()
      positionMs = position
      durationMs = duration
      progress = if (duration > 0) position.toFloat() / duration else 0f
      delay(POSITION_POLL_MS)
    }
  }

  val lyricsReady = currentLyrics.isUsableFor(currentMediaId)

  var lyricsExpanded by rememberSaveable { mutableStateOf(false) }
  BackHandler(enabled = lyricsExpanded) { lyricsExpanded = false }

  val items = state.items
  // Always one page past the feed, so a swipe off the last song moves even before more have loaded.
  val pageCount = if (items.isEmpty()) 0 else items.size + 1
  val pagerState = rememberPagerState(initialPage = state.selectedIndex) { pageCount }
  val flingBehavior =
    PagerDefaults.flingBehavior(state = pagerState, snapPositionalThreshold = SWIPE_THRESHOLD)
  val swipeDistancePx = with(LocalDensity.current) { HORIZONTAL_SWIPE_DISTANCE.toPx() }

  // Where the pager is headed on its own, to follow the player. Pages it stops on along the way, when
  // the song changes again mid-scroll, are not choices and must not start playing.
  var followTarget by remember { mutableStateOf<Int?>(null) }

  LaunchedEffect(pagerState) {
    pagerState.interactionSource.interactions.collect {
      if (it is DragInteraction.Start) followTarget = null
    }
  }
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
      .distinctUntilChanged()
      .collect { page ->
        val target = followTarget
        if (target == null || target == page) {
          followTarget = null
          viewModel.onPageSettled(page)
        }
      }
  }
  var shownItemCount by remember { mutableIntStateOf(items.size) }
  LaunchedEffect(state.selectedIndex, items.size) {
    val settled = pagerState.settledPage
    val wasOnTrailingPage = shownItemCount > 0 && settled >= shownItemCount
    shownItemCount = items.size
    if (items.isEmpty()) return@LaunchedEffect
    // A swipe in progress wins; a page move already following the player is simply replaced.
    if (followTarget == null) snapshotFlow { pagerState.isScrollInProgress }.first { !it }
    if (wasOnTrailingPage && settled in items.indices) {
      viewModel.onPageSettled(settled)
      return@LaunchedEffect
    }
    val target = state.selectedIndex
    if (target < pageCount && pagerState.settledPage != target) {
      followTarget = target
      if (reduceMotion) pagerState.scrollToPage(target) else pagerState.animateScrollToPage(target)
    }
  }

  val selectedItem = items.getOrNull(state.selectedIndex)
  val previousLabel = stringResource(R.string.music_flow_previous)
  val nextLabel = stringResource(R.string.music_flow_next)

  val openMenu by rememberUpdatedState<() -> Unit> {
    menuState.show {
      MusicFlowTopMenu(
        state = state,
        selectedItem = selectedItem,
        onPrevious = viewModel::previous,
        onNext = viewModel::next,
        onOpenPlayer = expandPlayer,
        onRestoreQueue = {
          val restored = viewModel.restorePreviousQueue()
          Toast.makeText(
              context,
              if (restored) R.string.music_flow_queue_restored else R.string.music_flow_queue_restore_unavailable,
              Toast.LENGTH_SHORT,
            )
            .show()
        },
        onNotInterested = { item, persist ->
          viewModel.notInterested(item, persist)
          Toast.makeText(context, R.string.music_flow_feedback_saved, Toast.LENGTH_SHORT).show()
        },
        onLessLikeThis = { item ->
          viewModel.lessLikeThis(item)
          Toast.makeText(context, R.string.music_flow_feedback_saved, Toast.LENGTH_SHORT).show()
        },
        onSettings = { navController.navigate("settings/music_flow") },
        onDismiss = menuState::dismiss,
      )
    }
  }
  val setTabMenu = LocalSetTabMenu.current
  DisposableEffect(setTabMenu) {
    setTabMenu { openMenu() }
    onDispose { setTabMenu(null) }
  }

  // A tab like the others: the page sits between the shared top bar and the bottom bars.
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Vertical))
        .background(Color.Black)
  ) {
    when {
      items.isEmpty() && state.error != null && !state.isInitialLoading ->
        FeedMessage(
          message = stringResource(state.error!!.messageRes()),
          action = stringResource(R.string.retry),
          onAction = viewModel::retryFeed,
        )
      items.isEmpty() -> MusicFlowPagePlaceholder()
      else ->
        VerticalPager(
          state = pagerState,
          beyondViewportPageCount = if (allowArtPrefetch) 1 else 0,
          userScrollEnabled = !lyricsExpanded,
          flingBehavior = flingBehavior,
          key = { page -> items.getOrNull(page)?.songId ?: "music_flow_trailing_$page" },
          modifier =
            Modifier.fillMaxSize()
              .pointerInput(viewModel, swipeDistancePx) {
                var dragged = 0f
                detectHorizontalDragGestures(
                  onDragStart = { dragged = 0f },
                  onDragEnd = {
                    if (dragged <= -swipeDistancePx) viewModel.next()
                    else if (dragged >= swipeDistancePx) viewModel.previous()
                  },
                  onHorizontalDrag = { change, amount ->
                    change.consume()
                    dragged += amount
                  },
                )
              }
              .semantics {
                customActions =
                  listOf(
                    CustomAccessibilityAction(previousLabel) { viewModel.previous(); true },
                    CustomAccessibilityAction(nextLabel) { viewModel.next(); true },
                  )
              },
        ) { page ->
          val item = items.getOrNull(page)
          when {
            item != null -> {
              val librarySong by database.song(item.songId).collectAsState(initial = null)
              val download by downloadUtil.getDownload(item.songId).collectAsState(initial = null)
              val isCurrent = item.songId == currentMediaId
              val currentLyricsText = if (isCurrent) currentLyrics?.lyrics else null
              val songDuration = remember(item.song.duration) { (item.song.duration ?: 0) * 1000L }

              MusicFlowPage(
                item = item,
                isCurrentPlayback = isCurrent,
                isPlaying = isPlaying,
                isLiked = librarySong?.song?.liked == true,
                downloadState = download?.state,
                showCanvas = showCanvas && page == state.selectedIndex,
                lyricsAvailable = isCurrent && lyricsReady,
                showLyrics = isCurrent && preferences.lyrics,
                lyricsText = currentLyricsText,
                progress = { progress },
                position = { if (isCurrent) positionMs else 0L },
                duration = { if (isCurrent && durationMs > 0) durationMs else songDuration },
                actions =
                  MusicFlowPageActions(
                    onTogglePlay = viewModel::togglePlayPause,
                    onLike = { toggleLike(database, syncUtils, item.song, librarySong) },
                    onLyrics = { lyricsExpanded = true },
                    onDownload = { toggleDownload(context, database, item.song, download?.state) },
                    onMore = {
                      menuState.show {
                        YouTubeSongMenu(
                          song = item.song,
                          navController = navController,
                          onDismiss = menuState::dismiss,
                          showSongHeader = false,
                        )
                      }
                    },
                    onArtist = { navController.navigate("artist/$it") },
                  ),
              )
            }
            state.isLoadingMore || (state.hasMore && state.error == null) -> MusicFlowPagePlaceholder()
            state.error != null ->
              FeedMessage(
                message = stringResource(state.error!!.messageRes()),
                action = stringResource(R.string.retry),
                onAction = viewModel::loadMoreIfNeeded,
              )
            else ->
              FeedMessage(
                message = stringResource(R.string.music_flow_end),
                action = stringResource(R.string.music_flow_start_over),
                onAction = viewModel::retryFeed,
              )
          }
        }
    }

    PlaybackStatus(
      state = state,
      onStatusAction = viewModel::togglePlayPause,
      onRetryPlayback = viewModel::retryPlayback,
      onSkip = viewModel::next,
      hasPlaybackError = playerConnection.error.collectAsState().value != null && selectedItem?.songId == currentMediaId,
    )

    AnimatedVisibility(visible = lyricsExpanded, enter = fadeIn(), exit = fadeOut()) {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
        InlineLyricsView(
          mediaMetadata = mediaMetadata,
          showLyrics = true,
          modifier = Modifier.fillMaxSize().padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
          positionProvider = { playerConnection.player.currentPosition },
        )
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(16.dp)
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.12f))
              .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
              .clickable(role = Role.Button, onClickLabel = stringResource(R.string.close)) { lyricsExpanded = false },
        ) {
          Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = stringResource(R.string.close),
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

/** The offline note and playback problems, over the top of the page. */
@Composable
private fun PlaybackStatus(
  state: MusicFlowUiState,
  onStatusAction: () -> Unit,
  onRetryPlayback: () -> Unit,
  onSkip: () -> Unit,
  hasPlaybackError: Boolean,
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      if (state.isOfflineFeed) {
        Text(
          text = stringResource(R.string.music_flow_offline_banner),
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.65f),
        )
      }

      val status = statusMessage(state.playbackStatus, hasPlaybackError)
      AnimatedVisibility(visible = status != null, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier =
            Modifier.clip(RoundedCornerShape(24.dp))
              .background(Color.White.copy(alpha = 0.16f))
              .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
              .clickable(
                enabled = state.playbackStatus == MusicFlowPlaybackStatus.ExternalOverride && !hasPlaybackError,
                onClick = onStatusAction,
              )
              .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
          Text(text = status.orEmpty(), style = MaterialTheme.typography.labelMedium, color = Color.White)
          if (hasPlaybackError) {
            TextButton(onClick = onRetryPlayback) { Text(stringResource(R.string.retry), color = Color.White) }
            TextButton(onClick = onSkip) { Text(stringResource(R.string.music_flow_skip), color = Color.White) }
          }
        }
      }
    }
  }
}

@Composable
private fun statusMessage(status: MusicFlowPlaybackStatus, hasPlaybackError: Boolean): String? =
  when {
    hasPlaybackError -> stringResource(R.string.music_flow_playback_error)
    status is MusicFlowPlaybackStatus.Blocked ->
      stringResource(
        when (status.reason) {
          TakeoverBlock.CASTING -> R.string.music_flow_blocked_casting
          TakeoverBlock.LISTEN_TOGETHER_GUEST -> R.string.music_flow_blocked_listen_together
        }
      )
    status == MusicFlowPlaybackStatus.ExternalOverride -> stringResource(R.string.music_flow_external)
    status == MusicFlowPlaybackStatus.Switching -> stringResource(R.string.music_flow_buffering)
    else -> null
  }

@Composable
private fun FeedMessage(message: String, action: String, onAction: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize().padding(32.dp),
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(20.dp))
    Button(
      onClick = onAction,
      shape = RoundedCornerShape(24.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
      Text(action)
    }
  }
}

private fun MusicFlowFeedError.messageRes() =
  when (this) {
    MusicFlowFeedError.OFFLINE -> R.string.music_flow_offline
    MusicFlowFeedError.EMPTY -> R.string.music_flow_empty
    MusicFlowFeedError.FAILED -> R.string.music_flow_failed
  }

private fun LyricsEntity?.isUsableFor(mediaId: String?): Boolean =
  this != null && mediaId != null && id == mediaId && lyrics.isNotBlank() && lyrics != LyricsEntity.LYRICS_NOT_FOUND

private fun toggleLike(database: MusicDatabase, syncUtils: SyncUtils, song: SongItem, librarySong: Song?) {
  database.transaction {
    val updated: SongEntity =
      if (librarySong == null) {
        insert(song.toMediaMetadata(), SongEntity::toggleLike)
        song.toMediaMetadata().toSongEntity().let(SongEntity::toggleLike)
      } else {
        librarySong.song.toggleLike().also(::update)
      }
    syncUtils.likeSong(updated)
  }
}

private fun toggleDownload(context: Context, database: MusicDatabase, song: SongItem, state: Int?) {
  when (state) {
    Download.STATE_COMPLETED,
    Download.STATE_QUEUED,
    Download.STATE_DOWNLOADING ->
      DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, song.id, false)
    else -> {
      database.transaction { insert(song.toMediaMetadata()) }
      val request =
        DownloadRequest.Builder(song.id, song.id.toUri())
          .setCustomCacheKey(song.id)
          .setData(song.title.toByteArray())
          .build()
      DownloadService.sendAddDownload(context, ExoDownloadService::class.java, request, false)
    }
  }
}

private fun PlayerConnection.positionAndDuration(): Pair<Long, Long> {
  val castHandler = service.castConnectionHandler
  return if (castHandler?.isCasting?.value == true) {
    castHandler.castPosition.value to castHandler.castDuration.value
  } else {
    player.currentPosition to player.duration
  }
}

private fun Context.isReducedMotion(): Boolean =
  Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

private fun Context.isActiveNetworkMetered(): Boolean =
  (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.isActiveNetworkMetered ?: true

private const val POSITION_POLL_MS = 50L
private const val SWIPE_THRESHOLD = 0.15f
private val HORIZONTAL_SWIPE_DISTANCE = 56.dp
