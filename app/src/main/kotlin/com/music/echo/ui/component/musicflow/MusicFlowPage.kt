package echo.music.iad1tya.ui.component.musicflow

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.offline.Download
import coil3.compose.AsyncImage
import echo.music.iad1tya.R
import echo.music.iad1tya.lyrics.LyricsUtils.parseLyrics
import echo.music.iad1tya.musicflow.MusicFlowItem
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.component.shimmer.TextPlaceholder
import echo.music.iad1tya.ui.screens.ambient.AmbientCanvasBackground
import echo.music.iad1tya.ui.utils.resize

private val FlowActionSize = 46.dp

data class MusicFlowPageActions(
  val onTogglePlay: () -> Unit,
  val onLike: () -> Unit,
  val onLyrics: () -> Unit,
  val onDownload: () -> Unit,
  val onMore: () -> Unit,
  val onArtist: (String) -> Unit,
)

@Composable
fun MusicFlowPage(
  item: MusicFlowItem,
  isCurrentPlayback: Boolean,
  isPlaying: Boolean,
  isLiked: Boolean,
  downloadState: Int?,
  showCanvas: Boolean,
  lyricsAvailable: Boolean,
  showLyrics: Boolean,
  lyricsText: String?,
  bottomPadding: Dp,
  position: () -> Long,
  duration: () -> Long,
  actions: MusicFlowPageActions,
  modifier: Modifier = Modifier,
) {
  val song = item.song
  val artistName = remember(song) { song.artists.joinToString(", ") { it.name } }
  val primaryArtistId = song.artists.firstOrNull { it.id != null }?.id
  val currentActions by rememberUpdatedState(actions)
  val currentlyLiked by rememberUpdatedState(isLiked)
  val playPauseLabel = stringResource(if (isPlaying) R.string.pause else R.string.play)

  var showHeartBurst by remember { mutableStateOf(false) }
  val heartScale by animateFloatAsState(
    targetValue = if (showHeartBurst) 1.25f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    finishedListener = { if (it > 0f) showHeartBurst = false },
    label = "HeartBurstScale",
  )

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = { currentActions.onTogglePlay() },
            onDoubleTap = {
              if (!currentlyLiked) currentActions.onLike()
              showHeartBurst = true
            },
            onLongPress = { currentActions.onMore() },
          )
        }
  ) {
    // 1. FULL-SCREEN MUSIC ART / VIDEO (Full-bleed Reel Experience)
    // Ambient soft blur underlay for smooth loading transitions
    AsyncImage(
      model = song.thumbnail.resize(240, 240),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.fillMaxSize().then(
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(60.dp) else Modifier
        ),
    )

    // Full-screen high-resolution crisp album art
    AsyncImage(
      model = song.thumbnail.resize(1920, 1920),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    // Full-screen canvas video loop if available
    if (showCanvas && isCurrentPlayback) {
      AmbientCanvasBackground(modifier = Modifier.fillMaxSize())
    }

    // 2. TOP FLOATING LYRIC PREVIEW CAPSULE (Reel Style)
    Box(
      modifier =
        Modifier.align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(top = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      if (showLyrics && lyricsAvailable && !lyricsText.isNullOrBlank()) {
        LyricPreviewCapsule(
          lyricsText = lyricsText,
          position = position,
          onClick = actions.onLyrics,
        )
      }
    }

    // 3. CENTER PLAY/PAUSE INDICATOR (Visible when paused, fades out on play)
    AnimatedVisibility(
      visible = isCurrentPlayback && !isPlaying,
      enter = fadeIn() + scaleIn(initialScale = 0.8f),
      exit = fadeOut() + scaleOut(targetScale = 0.8f),
      modifier = Modifier.align(Alignment.Center),
    ) {
      Icon(
        painter = painterResource(R.drawable.stage_play),
        contentDescription = playPauseLabel,
        tint = Color.White.copy(alpha = 0.65f),
        modifier = Modifier.size(56.dp),
      )
    }

    // 4. CENTER DOUBLE-TAP HEART BURST
    if (showHeartBurst || heartScale > 0.05f) {
      Icon(
        painter = painterResource(R.drawable.favorite),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier =
          Modifier.align(Alignment.Center)
            .size(88.dp)
            .graphicsLayer {
              scaleX = heartScale
              scaleY = heartScale
              alpha = (heartScale / 1.25f).coerceIn(0f, 1f)
            },
      )
    }

    // 5. BOTTOM OVERLAY: title on the left, the song's actions in a rail on the right. Play and skip
    // live in the bottom player, and share and add to playlist in the more menu.
    Row(
      verticalAlignment = Alignment.Bottom,
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              0f to Color.Transparent,
              0.35f to Color.Black.copy(alpha = 0.45f),
              1f to Color.Black.copy(alpha = 0.92f),
            )
          )
          .padding(start = 22.dp, end = 10.dp, top = 56.dp, bottom = bottomPadding + 16.dp),
    ) {
      MetadataSection(
        title = song.title,
        artistName = artistName,
        primaryArtistId = primaryArtistId,
        onArtist = actions.onArtist,
        modifier = Modifier.weight(1f).padding(end = 12.dp, bottom = 10.dp),
      )
      ActionRail(
        isLiked = isLiked,
        lyricsAvailable = lyricsAvailable,
        downloadState = downloadState,
        actions = actions,
      )
    }
  }
}

@Composable
private fun LyricPreviewCapsule(
  lyricsText: String?,
  position: () -> Long,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val parsedLines = remember(lyricsText) {
    if (lyricsText?.startsWith("[") == true) {
      parseLyrics(lyricsText).filter { it.text.isNotBlank() }
    } else {
      emptyList()
    }
  }

  val currentPosition = position()
  val activeLine = remember(currentPosition, parsedLines, lyricsText) {
    if (parsedLines.isNotEmpty()) {
      val idx = parsedLines.indexOfLast { it.time <= currentPosition }
      parsedLines.getOrNull(if (idx >= 0) idx else 0)?.text.orEmpty()
    } else {
      lyricsText?.lines()?.firstOrNull { it.isNotBlank() }.orEmpty()
    }
  }

  AnimatedVisibility(
    visible = activeLine.isNotBlank(),
    enter = fadeIn() + slideInVertically { -it / 2 },
    exit = fadeOut() + slideOutVertically { -it / 2 },
    modifier = modifier,
  ) {
    Box(
      modifier =
        Modifier.padding(horizontal = 28.dp)
          .clip(RoundedCornerShape(22.dp))
          .background(Color.Black.copy(alpha = 0.50f))
          .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(22.dp))
          .clickable(role = Role.Button, onClickLabel = stringResource(R.string.lyrics), onClick = onClick)
          .padding(horizontal = 16.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          painter = painterResource(R.drawable.stage_lyrics),
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.9f),
          modifier = Modifier.size(16.dp),
        )
        AnimatedContent(
          targetState = activeLine,
          transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 })
              .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
              .using(SizeTransform(clip = false))
          },
          label = "LyricPreviewText",
        ) { line ->
          Text(
            text = line,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun MetadataSection(
  title: String,
  artistName: String,
  primaryArtistId: String?,
  onArtist: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(
      text = title,
      fontSize = 26.sp,
      lineHeight = 31.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White,
      maxLines = 1,
      modifier = Modifier.fillMaxWidth().basicMarquee(initialDelayMillis = 1500, velocity = 30.dp),
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = artistName,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Medium,
      color = Color.White.copy(alpha = 0.75f),
      maxLines = 1,
      modifier =
        Modifier.fillMaxWidth()
          .then(
            if (primaryArtistId != null) {
              Modifier.clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.view_artist),
              ) {
                onArtist(primaryArtistId)
              }
            } else {
              Modifier
            }
          )
          .basicMarquee(initialDelayMillis = 1500, velocity = 30.dp),
    )
  }
}

@Composable
private fun ActionRail(
  isLiked: Boolean,
  lyricsAvailable: Boolean,
  downloadState: Int?,
  actions: MusicFlowPageActions,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier = modifier,
  ) {
    FlowActionButton(
      icon = if (isLiked) R.drawable.favorite else R.drawable.stage_heart,
      label = stringResource(if (isLiked) R.string.music_flow_unlike else R.string.action_like),
      tint = if (isLiked) MaterialTheme.colorScheme.error else Color.White,
      onClick = actions.onLike,
    )
    FlowActionButton(
      icon = R.drawable.stage_lyrics,
      label = stringResource(R.string.lyrics),
      enabled = lyricsAvailable,
      onClick = actions.onLyrics,
    )
    FlowDownloadButton(downloadState = downloadState, onClick = actions.onDownload)
    FlowActionButton(
      icon = R.drawable.stage_more_row,
      label = stringResource(R.string.more_options),
      onClick = actions.onMore,
    )
  }
}

@Composable
private fun FlowActionTarget(
  label: String,
  enabled: Boolean = true,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier.size(FlowActionSize)
        .clip(CircleShape)
        .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
        .semantics { contentDescription = label },
  ) {
    content()
  }
}

@Composable
private fun FlowActionButton(
  icon: Int,
  label: String,
  tint: Color = Color.White,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  FlowActionTarget(label = label, enabled = enabled, onClick = onClick) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = if (enabled) tint else tint.copy(alpha = 0.35f),
      modifier = Modifier.size(26.dp),
    )
  }
}

@Composable
private fun FlowDownloadButton(downloadState: Int?, onClick: () -> Unit) {
  when (downloadState) {
    Download.STATE_COMPLETED ->
      FlowActionButton(
        icon = R.drawable.stage_downloaded,
        label = stringResource(R.string.remove_download),
        onClick = onClick,
      )
    Download.STATE_QUEUED,
    Download.STATE_DOWNLOADING ->
      FlowActionTarget(label = stringResource(R.string.downloading), onClick = onClick) {
        CircularProgressIndicator(
          modifier = Modifier.size(22.dp),
          strokeWidth = 2.dp,
          color = Color.White,
        )
      }
    else ->
      FlowActionButton(
        icon = R.drawable.stage_download,
        label = stringResource(R.string.action_download),
        onClick = onClick,
      )
  }
}

@Composable
fun MusicFlowPagePlaceholder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize().background(Color.Black),
    contentAlignment = Alignment.BottomCenter,
  ) {
    ShimmerHost(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = 24.dp),
      ) {
        TextPlaceholder(
          height = 24.dp,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(0.65f),
        )
        Spacer(Modifier.height(8.dp))
        TextPlaceholder(
          height = 16.dp,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(0.40f),
        )
      }
    }
  }
}
