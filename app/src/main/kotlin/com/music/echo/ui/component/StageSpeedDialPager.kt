package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val SPEED_DIAL_COLUMNS = 4
const val SPEED_DIAL_PAGES = 3

/** Enough pages either side of the start that a listener never reaches an end. */
private const val LOOP_REPEATS = 1000

/** Home's speed dial: pages of four tiles with dots under them, looping back to the first page. */
@Composable
fun StageSpeedDialPager(
  pages: List<List<@Composable (Modifier) -> Unit>>,
  modifier: Modifier = Modifier,
) {
  val pageCount = pages.size
  if (pageCount == 0) return
  val loops = pageCount > 1

  key(pageCount) {
    val pagerState =
      rememberPagerState(initialPage = if (loops) pageCount * LOOP_REPEATS / 2 else 0) {
        if (loops) pageCount * LOOP_REPEATS else 1
      }

    Column(modifier = modifier) {
      HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 18.dp),
        pageSpacing = 36.dp,
        modifier = Modifier.fillMaxWidth(),
      ) { page ->
        val tiles = pages[page % pageCount]
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          tiles.forEach { tile ->
            Box(modifier = Modifier.weight(1f).aspectRatio(1f)) { tile(Modifier.fillMaxSize()) }
          }
          repeat(SPEED_DIAL_COLUMNS - tiles.size) { Spacer(Modifier.weight(1f)) }
        }
      }

      if (loops) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp, androidx.compose.ui.Alignment.CenterHorizontally),
          modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
          repeat(pageCount) { index ->
            Box(
              modifier =
                Modifier.size(7.dp)
                  .background(
                    if (pagerState.currentPage % pageCount == index) {
                      MaterialTheme.colorScheme.onSurface
                    } else {
                      MaterialTheme.colorScheme.outline
                    },
                    CircleShape,
                  )
            )
          }
        }
      }
    }
  }
}
