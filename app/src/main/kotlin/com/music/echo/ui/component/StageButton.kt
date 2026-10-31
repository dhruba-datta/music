package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The full-width pill used in Stage dialogs: white when [primary], dark grey otherwise. */
@Composable
fun StageButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  primary: Boolean = true,
  @DrawableRes icon: Int? = null,
) {
  Button(
    onClick = onClick,
    modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
    shape = RoundedCornerShape(25.dp),
    colors =
      if (primary) {
        ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        )
      } else {
        ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          contentColor = MaterialTheme.colorScheme.onSurface,
        )
      },
  ) {
    icon?.let {
      Icon(
        painter = painterResource(it),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(text = text, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
  }
}

/**
 * The small 44dp chip used for a quick action inside a row. [colors] and [shape] are accepted so it
 * can replace a stock button in place; the chip always keeps its own look.
 */
@Composable
fun StageChip(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape? = null,
  colors: ButtonColors? = null,
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.heightIn(min = 44.dp),
    shape = RoundedCornerShape(22.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
  ) {
    ProvideTextStyle(TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) { content() }
  }
}
