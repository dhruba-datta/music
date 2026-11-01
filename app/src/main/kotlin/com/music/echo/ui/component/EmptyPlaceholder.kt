package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyPlaceholder(
  @DrawableRes icon: Int,
  text: String,
  modifier: Modifier = Modifier,
) {
  // Lists give their items no height to fill, so reserve one from the screen to center in.
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier.fillMaxWidth().stageEmptyHeight().padding(12.dp),
  ) {
    Image(
      painter = painterResource(icon),
      contentDescription = null,
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
      modifier = Modifier.size(64.dp),
    )

    Spacer(Modifier.height(12.dp))

    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
    )
  }
}

/** The height an empty page centers its message in, leaving room for the bars around it. */
@Composable
fun Modifier.stageEmptyHeight(): Modifier =
  heightIn(min = LocalConfiguration.current.screenHeightDp.dp * 0.6f)
