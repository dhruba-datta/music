package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
