package echo.music.iad1tya.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.constants.FloatingToolbarBottomPadding
import echo.music.iad1tya.constants.NavigationBarHeight
import echo.music.iad1tya.ui.screens.Screens
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val BubbleRadius = 25.dp

/**
 * Bottom bar fixed to the screen edge, with a dip under the current tab, a round bubble rising out
 * of the dip with the tab's icon, and the tab's name in bold underneath. Switching tabs sinks the
 * bubble, slides the dip across like a wave, and pops the bubble up at the new tab.
 */
@Composable
fun FloatingNavigationToolbar(
  items: List<Screens>,
  pureBlack: Boolean,
  bottomInset: Dp,
  modifier: Modifier = Modifier,
  onSearchLongClick: (() -> Unit)? = null,
  isSelected: (Screens) -> Boolean,
  onItemClick: (Screens, Boolean) -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val light = colors.surface.luminance() > 0.5f
  val barColor =
    when {
      pureBlack -> Color.Black
      light -> colors.surfaceContainerLowest
      else -> colors.surfaceContainerHigh
    }
  val edgeColor = colors.outlineVariant.copy(alpha = 0.6f)
  val shadowColor = Color.Black.copy(alpha = if (light) 0.07f else 0.3f)

  val activeSlot = items.indexOfFirst(isSelected).coerceAtLeast(0)
  var fromSlot by remember { mutableIntStateOf(activeSlot) }
  var toSlot by remember { mutableIntStateOf(activeSlot) }
  val motion = remember { Animatable(1f) }

  LaunchedEffect(activeSlot) {
    if (activeSlot == toSlot) return@LaunchedEffect
    // Start from wherever the dip is right now, so quick taps chain smoothly.
    fromSlot = toSlot
    toSlot = activeSlot
    motion.snapTo(0f)
    motion.animateTo(1f, tween(durationMillis = 620, easing = LinearEasing))
  }

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val density = LocalDensity.current
    val slotWidth = maxWidth / items.size
    val v = motion.value
    val move = EaseInOutCubic.transform(((v - 0.12f) / 0.6f).coerceIn(0f, 1f))
    val fromX = slotWidth * (fromSlot + 0.5f)
    val toX = slotWidth * (toSlot + 0.5f)
    val x = fromX + (toX - fromX) * move
    // Bubble: sinks over the first 25%, pops back up over the last 40%.
    val lift =
      when {
        v < 0.25f -> 1 - EaseIn.transform(v / 0.25f)
        v < 0.6f -> 0f
        else -> EaseOutBack.transform((v - 0.6f) / 0.4f)
      }
    // The dip flattens a little while it travels, like a wave.
    val depth = 1 - 0.35f * sin(PI.toFloat() * move)
    val bubbleSlot = if (v < 0.5f) fromSlot else toSlot
    val rise = FloatingToolbarBottomPadding
    val bubbleCenterY = rise + 4.dp + 30.dp * (1 - lift)

    Canvas(modifier = Modifier.fillMaxSize()) {
      val top = with(density) { rise.toPx() }
      translate(top = top) {
        drawNotchBar(
          size = Size(size.width, size.height - top),
          color = barColor,
          x = with(density) { x.toPx() },
          radius = with(density) { BubbleRadius.toPx() },
          depth = depth,
          edge = edgeColor,
          shadow = shadowColor,
        )
      }
    }

    Row(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .padding(bottom = bottomInset)
          .fillMaxWidth()
          .height(NavigationBarHeight)
    ) {
      items.forEachIndexed { index, screen ->
        val inBubble = index == bubbleSlot
        NotchBarSlot(
          screen = screen,
          selected = inBubble,
          // The icon lifts out of the selected slot into the bubble.
          iconAlpha = if (inBubble) (1 - lift).coerceIn(0f, 1f) else 1f,
          onClick = { onItemClick(screen, isSelected(screen)) },
          onLongClick = if (screen == Screens.Search) onSearchLongClick else null,
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
    }

    val bubbleColor = colors.primary
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.offset(x = x - BubbleRadius, y = bubbleCenterY - BubbleRadius)
          .size(BubbleRadius * 2)
          .graphicsLayer {
            alpha = lift.coerceIn(0f, 1f)
            val scale = 0.6f + 0.4f * lift.coerceIn(0f, 1.2f)
            scaleX = scale
            scaleY = scale
          }
          .shadow(
            elevation = 8.dp,
            shape = CircleShape,
            ambientColor = bubbleColor.copy(alpha = 0.28f),
            spotColor = bubbleColor.copy(alpha = 0.28f),
          )
          .background(bubbleColor, CircleShape)
    ) {
      Icon(
        painter = painterResource(items[bubbleSlot].iconIdActive),
        contentDescription = null,
        tint = colors.onPrimary,
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotchBarSlot(
  screen: Screens,
  selected: Boolean,
  iconAlpha: Float,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val haptics = LocalHapticFeedback.current
  val colors = MaterialTheme.colorScheme
  val title = stringResource(screen.titleId)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
    modifier =
      modifier
        .semantics { this.selected = selected }
        .combinedClickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = false, radius = 32.dp),
          role = Role.Tab,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
          },
          onLongClick = onLongClick,
        )
        .padding(top = 12.dp, bottom = 10.dp),
  ) {
    Icon(
      painter = painterResource(screen.iconIdInactive),
      contentDescription = null,
      tint = colors.onSurfaceVariant,
      modifier = Modifier.size(23.dp).graphicsLayer { alpha = iconAlpha },
    )
    Text(
      text = title,
      color = if (selected) colors.onSurface else colors.onSurfaceVariant,
      fontSize = 11.5.sp,
      fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
      maxLines = 1,
    )
  }
}

/** The top edge: flat, with a smooth dip centred on [x]. */
private fun notchTop(width: Float, x: Float, radius: Float, depth: Float): Path {
  val dip = (radius + radius * 0.28f) * depth
  val half = radius * 2.15f
  return Path().apply {
    moveTo(min(0f, x - half), 0f)
    lineTo(x - half, 0f)
    cubicTo(x - half * 0.5f, 0f, x - radius * 1.1f, dip, x, dip)
    cubicTo(x + radius * 1.1f, dip, x + half * 0.5f, 0f, x + half, 0f)
    lineTo(max(width, x + half), 0f)
  }
}

/** The bar, a hairline along its edge, and a soft shadow so the dip reads against the page. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNotchBar(
  size: Size,
  color: Color,
  x: Float,
  radius: Float,
  depth: Float,
  edge: Color,
  shadow: Color,
) {
  val top = notchTop(size.width, x, radius, depth)
  val body =
    Path().apply {
      addPath(top)
      lineTo(max(size.width, x + radius * 2.15f), size.height)
      lineTo(min(0f, x - radius * 2.15f), size.height)
      close()
    }
  clipRect(left = 0f, top = -30.dp.toPx(), right = size.width, bottom = size.height) {
    drawIntoCanvas { canvas ->
      val paint =
        android.graphics.Paint().apply {
          isAntiAlias = true
          this.color = shadow.toArgb()
          style = android.graphics.Paint.Style.STROKE
          strokeWidth = 6.dp.toPx()
          maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
      val shifted = Path().apply { addPath(top, Offset(0f, -1.dp.toPx())) }
      canvas.nativeCanvas.drawPath(shifted.asAndroidPath(), paint)
    }
    drawPath(body, color)
    drawPath(top, edge, style = Stroke(width = 1.dp.toPx()))
  }
}
