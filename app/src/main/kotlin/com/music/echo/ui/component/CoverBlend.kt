package echo.music.iad1tya.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/** How much of the blend each of two covers takes. They overlap by the part over one half. */
private const val CoverSpan = 0.56f

/** Where a cover finishes fading in, as a fraction of its own length: the end of the overlap. */
private const val CoverFadeEnd = (2 * CoverSpan - 1) / CoverSpan

/** The most covers one blend can show. */
const val MaxBlendedCovers = 4

/** A list's first covers, without repeats, ready for [CoverBlend]. */
fun blendedCovers(thumbnails: List<String?>): List<String> =
  thumbnails.filterNotNull().filter { it.isNotBlank() }.distinct().take(MaxBlendedCovers)

/**
 * Up to [MaxBlendedCovers] covers side by side, softened only along the seams between them. One
 * cover fills the space, two sit side by side, three put two on the right, and four form a square.
 */
@Composable
fun CoverBlend(covers: List<String>, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    when (covers.size) {
      0 -> Unit
      1 -> Cover(covers[0])
      2 -> BlendedPair(horizontal = true, { Cover(covers[0]) }, { Cover(covers[1]) })
      3 ->
        BlendedPair(
          horizontal = true,
          { Cover(covers[0]) },
          { BlendedPair(horizontal = false, { Cover(covers[1]) }, { Cover(covers[2]) }) },
        )
      else ->
        BlendedPair(
          horizontal = false,
          { BlendedPair(horizontal = true, { Cover(covers[0]) }, { Cover(covers[1]) }) },
          { BlendedPair(horizontal = true, { Cover(covers[2]) }, { Cover(covers[3]) }) },
        )
    }
  }
}

@Composable
private fun Cover(url: String) {
  AsyncImage(
    model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(false).build(),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun BlendedPair(
  horizontal: Boolean,
  first: @Composable () -> Unit,
  second: @Composable () -> Unit,
) {
  val span =
    if (horizontal) Modifier.fillMaxWidth(CoverSpan).fillMaxHeight()
    else Modifier.fillMaxWidth().fillMaxHeight(CoverSpan)

  Box(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.align(Alignment.TopStart).then(span)) { first() }
    Box(modifier = Modifier.align(Alignment.BottomEnd).then(span).fadeInFromStart(horizontal)) {
      second()
    }
  }
}

private fun Modifier.fadeInFromStart(horizontal: Boolean): Modifier =
  graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
      drawContent()
      val end =
        if (horizontal) Offset(size.width * CoverFadeEnd, 0f)
        else Offset(0f, size.height * CoverFadeEnd)
      drawRect(
        brush = Brush.linearGradient(listOf(Color.Transparent, Color.Black), Offset.Zero, end),
        blendMode = BlendMode.DstIn,
      )
    }
