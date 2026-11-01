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
import androidx.compose.runtime.mutableIntStateOf
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
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gap = 8.dp
private val Edge = 12.dp
private val PreviousWidth = 36.dp
private val NextWidth = 76.dp

/** Lets the strip loop forever once a card can never be on screen twice. */
private const val MIN_LOOPING_CARDS = 6

private fun lerp(values: List<Float>, position: Float): Float {
  val index = floor(position).toInt().coerceIn(0, values.size - 2)
  val fraction = position - index
  return values[index] + (values[index + 1] - values[index]) * fraction
}

/**
 * Home's card strip: a thin card before the wide active card and one card after it.
 *
 * [item] gets the card index, how open the card is, from 0 (a peek) to 1 (active), and how visible
 * the small title of the next card should be. Its last argument runs a tap's action, sliding the
 * card into the active spot first if it is only peeking.
 */
@Composable
fun StageHeroStrip(
  count: Int,
  modifier: Modifier = Modifier,
  autoAdvanceMillis: Long = 3500,
  item:
    @Composable
    BoxScope.(
      index: Int,
      expansion: Float,
      nextTitleAlpha: Float,
      activate: (action: () -> Unit) -> Unit,
    ) -> Unit,
) {
  val position = remember { Animatable(if (count > 1) 1f else 0f) }
  val scope = rememberCoroutineScope()
  var dragging by remember { mutableStateOf(false) }
  var activations by remember { mutableIntStateOf(0) }
  val looping = count >= MIN_LOOPING_CARDS

  fun settled(target: Float): Float =
    if (looping) target else target.coerceIn(0f, (count - 1).coerceAtLeast(0).toFloat())

  fun distanceTo(index: Int): Float {
    val distance = index - position.value
    if (!looping) return distance
    val wrapped = ((distance % count) + count) % count
    return if (wrapped > count / 2f) wrapped - count else wrapped
  }

  LaunchedEffect(count) {
    if (!looping) position.snapTo(settled(position.value))
  }

  // Restarted by each activation, so the card just tapped gets its full time in the active spot.
  LaunchedEffect(count, activations) {
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
      (maxWidth - Edge * 2 - PreviousWidth - NextWidth - Gap * 2).coerceAtLeast(120.dp)
    val slotPx = with(LocalDensity.current) { (activeWidth + Gap).toPx() }

    // Card edges at distances -2 to 2 from the active card.
    val previousX = Edge
    val activeX = previousX + PreviousWidth + Gap
    val nextX = activeX + activeWidth + Gap
    val xs: List<Dp> =
      listOf(
        previousX - PreviousWidth - Gap,
        previousX,
        activeX,
        nextX,
        nextX + NextWidth + Gap,
      )
    val widths: List<Dp> =
      listOf(PreviousWidth, PreviousWidth, activeWidth, NextWidth, NextWidth)
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
        val distance = distanceTo(index)
        if (distance < -2f || distance > 2f) continue

        val slot = distance + 2f
        val width = lerp(widthValues, slot)
        val expansion =
          ((width - NextWidth.value) / (activeWidth.value - NextWidth.value)).coerceIn(0f, 1f)

        key(index) {
          Box(
            modifier =
              Modifier.offset(x = lerp(xValues, slot).dp).width(width.dp).fillMaxHeight()
          ) {
            item(index, expansion, (1f - expansion) * distance.coerceIn(0f, 1f)) { action ->
              val offset = distanceTo(index)
              if (abs(offset) < 0.5f) {
                action()
              } else {
                activations++
                scope.launch {
                  position.animateTo((position.value + offset).roundToInt().toFloat())
                  action()
                }
              }
            }
          }
        }
      }
    }
  }
}
