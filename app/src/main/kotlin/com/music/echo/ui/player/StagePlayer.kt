package echo.music.iad1tya.ui.player

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.CastButton

internal val StagePlayerPadding = 24.dp

/** Height the player bar takes at the bottom. */
internal val StageDockPeekHeight = 66.dp

/** The size of the icons in the action row. */
internal val StageActionIconSize = 23.dp

/** The alpha that gives the board's soft white for secondary text on the player. */
internal const val StageMutedAlpha = 0.9f

private val StageHeaderButtonSize = 44.dp

private val StageActionTargetSize = 29.dp

/**
 * Lays out the artwork and the sections below it, top to bottom. The artwork stays square and takes
 * what the sections leave, up to the full width. Height left over goes evenly between the sections,
 * up to [maxGap] each so they stay together, and the rest goes above the artwork.
 */
@Composable
internal fun StageEvenLayout(
  artwork: @Composable () -> Unit,
  sections: List<@Composable () -> Unit>,
  modifier: Modifier = Modifier,
  horizontalPadding: Dp = StagePlayerPadding,
  minGap: Dp = 8.dp,
  maxGap: Dp = 32.dp,
) {
  Layout(
    content = {
      Box { artwork() }
      sections.forEach { section -> Box { section() } }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val sectionPlaceables =
      measurables.drop(1).map {
        it.measure(Constraints(minWidth = width, maxWidth = width, maxHeight = height))
      }
    val sectionsHeight = sectionPlaceables.sumOf { it.height }
    val gapCount = sectionPlaceables.size
    val artworkSide =
      minOf(
          width - horizontalPadding.roundToPx() * 2,
          height - sectionsHeight - minGap.roundToPx() * (gapCount + 1),
        )
        .coerceAtLeast(0)
    val artworkPlaceable =
      measurables.first().measure(Constraints.fixed(width, artworkSide))
    val freeHeight = (height - sectionsHeight - artworkSide - minGap.roundToPx()).coerceAtLeast(0)
    val gap = (freeHeight / gapCount.coerceAtLeast(1)).coerceAtMost(maxGap.roundToPx())

    layout(width, height) {
      var y = minGap.roundToPx() + freeHeight - gap * gapCount
      artworkPlaceable.place(0, y)
      y += artworkPlaceable.height + gap
      sectionPlaceables.forEach {
        it.place(0, y)
        y += it.height + gap
      }
    }
  }
}

@Composable
private fun StageActionLabel(
  text: String,
  color: Color,
  fontWeight: FontWeight = FontWeight.Bold,
) {
  Text(
    text = text,
    color = color,
    fontSize = 12.sp,
    fontWeight = fontWeight,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    textAlign = TextAlign.Center,
  )
}

/** An icon over its label, for the action row of the player. */
@Composable
internal fun StageAction(
  label: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  active: Boolean = false,
  activeColor: Color = color,
  icon: @Composable () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    modifier =
      modifier
        .heightIn(min = 58.dp)
        .clip(RoundedCornerShape(16.dp))
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .alpha(if (enabled) 1f else 0.35f)
        .padding(horizontal = 6.dp),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.size(StageActionTargetSize),
    ) {
      icon()
      if (active) StageActiveDot(color = color, modifier = Modifier.align(Alignment.BottomCenter))
    }
    StageActionLabel(text = label, color = color)
  }
}

/**
 * The existing [CastButton] with a label under it. CastButton draws nothing when Cast is turned off
 * or unavailable, and in that case the label goes too.
 */
@Composable
internal fun StageCastAction(
  color: Color,
  modifier: Modifier = Modifier,
  labelWeight: FontWeight = FontWeight.Bold,
) {
  Layout(
    content = {
      CastButton(modifier = Modifier.size(StageActionTargetSize), tintColor = color)
      StageActionLabel(text = stringResource(R.string.widget_cast), color = color, fontWeight = labelWeight)
    },
    modifier = modifier,
  ) { measurables, constraints ->
    if (measurables.size < 2) {
      layout(0, 0) {}
    } else {
      val button = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
      val label = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
      val spacing = 2.dp.roundToPx()
      val width = maxOf(button.width, label.width)
      layout(width, button.height + spacing + label.height) {
        button.place((width - button.width) / 2, 0)
        label.place((width - label.width) / 2, button.height + spacing)
      }
    }
  }
}

/** A plain icon button that gets a filled circle while its mode is on. */
@Composable
internal fun StageToggleIcon(
  icon: Int,
  contentDescription: String,
  active: Boolean,
  enabled: Boolean,
  color: Color,
  activeColor: Color,
  activeContentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .size(StageHeaderButtonSize)
        .clip(CircleShape)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .alpha(if (enabled) 1f else 0.35f),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription,
      tint = color,
      modifier = Modifier.size(24.dp),
    )
    if (active) StageActiveDot(color = color, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

/** The small dot under a player control that is switched on. */
@Composable
private fun StageActiveDot(color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier.padding(bottom = 2.dp).size(4.dp).background(color, CircleShape)
  )
}

@Composable
internal fun StageHeaderButton(
  icon: Int,
  contentDescription: String?,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .size(StageHeaderButtonSize)
        .clip(CircleShape)
        .clickable(role = Role.Button, onClick = onClick),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription,
      tint = color,
      modifier = Modifier.size(24.dp),
    )
  }
}

/** The player's one bottom bar. Cast leaves its slot empty where it is unavailable. */
@Composable
internal fun StageDock(
  upNextActive: Boolean,
  lyricsActive: Boolean,
  timerActive: Boolean,
  timerEnabled: Boolean,
  onUpNext: () -> Unit,
  onLyrics: () -> Unit,
  onTimer: () -> Unit,
  onPlaylist: () -> Unit,
  color: Color,
  modifier: Modifier = Modifier,
) {
  // The app's bottom bar on the player: fixed to the edge, and what's on rises in a bubble.
  Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .fillMaxWidth()
        .background(color.copy(alpha = 0.10f))
        .drawBehind {
          drawLine(
            color = color.copy(alpha = 0.18f),
            start = Offset.Zero,
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
          )
        }
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
        )
        .height(StageDockPeekHeight),
  ) {
    StageCastAction(color = color, labelWeight = FontWeight.Medium)
    StageDockAction(
      icon = R.drawable.stage_queue,
      label = "Up next",
      active = upNextActive,
      color = color,
      onClick = onUpNext,
    )
    StageDockAction(
      icon = R.drawable.lyrics,
      label = stringResource(R.string.lyrics),
      active = lyricsActive,
      color = color,
      onClick = onLyrics,
    )
    StageDockAction(
      icon = R.drawable.bedtime,
      label = "Timer",
      active = timerActive,
      enabled = timerEnabled,
      color = color,
      onClick = onTimer,
    )
    StageDockAction(
      icon = R.drawable.playlist_add,
      label = "Playlist",
      color = color,
      onClick = onPlaylist,
    )
  }
}

@Composable
private fun StageDockAction(
  @DrawableRes icon: Int,
  label: String,
  color: Color,
  onClick: () -> Unit,
  active: Boolean = false,
  enabled: Boolean = true,
) {
  val bubbleColor = color
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    modifier =
      Modifier.widthIn(min = 56.dp)
        .fillMaxHeight()
        .clickable(
          enabled = enabled,
          role = Role.Button,
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = false, radius = 32.dp),
          onClick = onClick,
        )
        .alpha(if (enabled) 1f else 0.35f),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(StageActionTargetSize)) {
      if (active) {
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier.offset(y = (-14).dp)
              .requiredSize(46.dp)
              .shadow(8.dp, CircleShape)
              .background(bubbleColor, CircleShape),
        ) {
          Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (bubbleColor.luminance() > 0.5f) Color.Black else Color.White,
            modifier = Modifier.size(StageActionIconSize),
          )
        }
      } else {
        Icon(
          painter = painterResource(icon),
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(StageActionIconSize),
        )
      }
    }
    StageActionLabel(
      text = label,
      color = color,
      fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
    )
  }
}
