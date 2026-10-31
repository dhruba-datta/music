package echo.music.iad1tya.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gap = 8.dp
private val Edge = 12.dp
private val PreviousWidth = 44.dp
private val NextWidth = 56.dp
private val LastWidth = 44.dp

/** Lets the strip loop forever once a card can never be on screen twice. */
private const val MIN_LOOPING_CARDS = 6

private fun lerp(values: List<Float>, position: Float): Float {
  val index = floor(position).toInt().coerceIn(0, values.size - 2)
  val fraction = position - index
  return values[index] + (values[index + 1] - values[index]) * fraction
}

/**
 * Home's card strip: one card before the active card and two after it.
 *
 * [item] gets the card index and how open the card is, from 0 (a thin peek) to 1 (active).
 */
@Composable
fun StageHeroStrip(
  count: Int,
  modifier: Modifier = Modifier,
  autoAdvanceMillis: Long = 5000,
  item: @Composable BoxScope.(index: Int, expansion: Float) -> Unit,
) {
  val position = remember { Animatable(if (count > 1) 1f else 0f) }
  val scope = rememberCoroutineScope()
  var dragging by remember { mutableStateOf(false) }
  val looping = count >= MIN_LOOPING_CARDS

  fun settled(target: Float): Float =
    if (looping) target else target.coerceIn(0f, (count - 1).coerceAtLeast(0).toFloat())

  LaunchedEffect(count) {
    if (!looping) position.snapTo(settled(position.value))
    while (true) {
      delay(autoAdvanceMillis)
      if (!dragging && count > 1) {
        val next = position.value.roundToInt() + 1f
        position.animateTo(if (!looping && next > count - 1) 0f else next)
      }
    }
  }

  BoxWithConstraints(modifier = modifier.clipToBounds()) {
    val activeWidth =
      (maxWidth - Edge * 2 - PreviousWidth - NextWidth - LastWidth - Gap * 3).coerceAtLeast(120.dp)
    val slotPx = with(LocalDensity.current) { (activeWidth + Gap).toPx() }

    // Card edges at distances -2 to 3 from the active card.
    val previousX = Edge
    val activeX = previousX + PreviousWidth + Gap
    val nextX = activeX + activeWidth + Gap
    val lastX = nextX + NextWidth + Gap
    val xs: List<Dp> =
      listOf(
        previousX - PreviousWidth - Gap,
        previousX,
        activeX,
        nextX,
        lastX,
        lastX + LastWidth + Gap,
      )
    val widths: List<Dp> =
      listOf(PreviousWidth, PreviousWidth, activeWidth, NextWidth, LastWidth, LastWidth)
    val xValues = xs.map { it.value }
    val widthValues = widths.map { it.value }

    Box(
      modifier =
        Modifier.matchParentSize().pointerInput(count) {
          detectHorizontalDragGestures(
            onDragStart = { dragging = true },
            onDragEnd = {
              dragging = false
              scope.launch { position.animateTo(settled(position.value.roundToInt().toFloat())) }
            },
            onDragCancel = {
              dragging = false
              scope.launch { position.animateTo(settled(position.value.roundToInt().toFloat())) }
            },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              scope.launch { position.snapTo(position.value - dragAmount / slotPx) }
            },
          )
        }
    ) {
      for (index in 0 until count) {
        var distance = index - position.value
        if (looping) {
          distance = ((distance % count) + count) % count
          if (distance > count / 2f) distance -= count
        }
        if (distance < -2f || distance > 3f) continue

        val slot = distance + 2f
        val width = lerp(widthValues, slot)
        val expansion =
          ((width - NextWidth.value) / (activeWidth.value - NextWidth.value)).coerceIn(0f, 1f)

        key(index) {
          Box(
            modifier =
              Modifier.offset(x = lerp(xValues, slot).dp).width(width.dp).fillMaxHeight()
          ) {
            item(index, expansion)
          }
        }
      }
    }
  }
}
