package echo.music.iad1tya.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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

private val StageHeaderButtonSize = 44.dp

/**
 * Lays out the artwork and the sections below it, top to bottom. The artwork stays square and takes
 * what the sections leave, up to the full width, and any height left over is shared equally between
 * the gaps. This keeps the player free of empty bands on tall phones and on short ones.
 */
@Composable
internal fun StageEvenLayout(
  artwork: @Composable () -> Unit,
  sections: List<@Composable () -> Unit>,
  modifier: Modifier = Modifier,
  horizontalPadding: Dp = StagePlayerPadding,
  minGap: Dp = 8.dp,
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
    val gapCount = sectionPlaceables.size + 1
    val artworkSide =
      minOf(
          width - horizontalPadding.roundToPx() * 2,
          height - sectionsHeight - minGap.roundToPx() * gapCount,
        )
        .coerceAtLeast(0)
    val artworkPlaceable =
      measurables.first().measure(Constraints.fixed(width, artworkSide))
    val gap = ((height - sectionsHeight - artworkSide) / gapCount).coerceAtLeast(0)

    layout(width, height) {
      var y = gap
      artworkPlaceable.place(0, y)
      y += artworkPlaceable.height + gap
      sectionPlaceables.forEach {
        it.place(0, y)
        y += it.height + gap
      }
    }
  }
}

/**
 * Splits the width equally between the actions. An action that draws nothing, such as Cast where
 * Cast is unavailable, gives its slot to the others.
 */
@Composable
internal fun StageActionRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Layout(content = content, modifier = modifier.fillMaxWidth()) { measurables, constraints ->
    val width = constraints.maxWidth
    val childConstraints = Constraints(maxWidth = width / measurables.size.coerceAtLeast(1))
    val placeables = measurables.map { it.measure(childConstraints) }.filter { it.width > 0 }
    val height = placeables.maxOfOrNull { it.height } ?: 0
    val slot = if (placeables.isEmpty()) 0 else width / placeables.size

    layout(width, height) {
      placeables.forEachIndexed { index, placeable ->
        placeable.place(slot * index + (slot - placeable.width) / 2, 0)
      }
    }
  }
}

@Composable
private fun StageActionLabel(text: String, color: Color) {
  Text(
    text = text,
    color = color,
    fontSize = 12.sp,
    fontWeight = FontWeight.Bold,
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
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier =
      modifier
        .clip(RoundedCornerShape(16.dp))
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .alpha(if (enabled) 1f else 0.35f)
        .padding(horizontal = 6.dp),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.size(40.dp)
          .then(if (active) Modifier.background(activeColor, CircleShape) else Modifier),
    ) {
      icon()
    }
    StageActionLabel(text = label, color = color)
  }
}

/**
 * The existing [CastButton] with a label under it. CastButton draws nothing when Cast is turned off
 * or unavailable, and in that case the label goes too.
 */
@Composable
internal fun StageCastAction(color: Color, modifier: Modifier = Modifier) {
  Layout(
    content = {
      CastButton(modifier = Modifier.size(40.dp), tintColor = color)
      StageActionLabel(text = stringResource(R.string.widget_cast), color = color)
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
        .then(if (active) Modifier.background(activeColor) else Modifier)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .alpha(if (enabled) 1f else 0.35f),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription,
      tint = if (active) activeContentColor else color,
      modifier = Modifier.size(24.dp),
    )
  }
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

/** The two labelled tabs at the bottom of the player. */
@Composable
internal fun StageDock(
  upNextActive: Boolean,
  lyricsActive: Boolean,
  onUpNext: () -> Unit,
  onLyrics: () -> Unit,
  color: Color,
  activeColor: Color,
  activeContentColor: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.12f))
        .padding(4.dp),
  ) {
    StageDockTab(
      text = "Up next",
      active = upNextActive,
      onClick = onUpNext,
      color = color,
      activeColor = activeColor,
      activeContentColor = activeContentColor,
      modifier = Modifier.weight(1f),
    )
    StageDockTab(
      text = stringResource(R.string.lyrics),
      active = lyricsActive,
      onClick = onLyrics,
      color = color,
      activeColor = activeColor,
      activeContentColor = activeContentColor,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun StageDockTab(
  text: String,
  active: Boolean,
  onClick: () -> Unit,
  color: Color,
  activeColor: Color,
  activeContentColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .fillMaxHeight()
        .clip(CircleShape)
        .then(if (active) Modifier.background(activeColor) else Modifier)
        .clickable(role = Role.Tab, onClick = onClick)
        .semantics { selected = active },
  ) {
    Text(
      text = text,
      color = if (active) activeContentColor else color,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
