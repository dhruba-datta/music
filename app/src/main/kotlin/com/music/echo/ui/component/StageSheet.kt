package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Top corners of the Stage bottom sheets. */
val StageSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** The 44x5 grab handle at the top of a Stage sheet. */
@Composable
fun StageSheetHandle() {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .padding(top = 10.dp)
        .size(width = 44.dp, height = 5.dp)
        .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
  )
}

/** The stacked cards of a sheet, spaced and rounded like a Settings group. */
@Composable
fun StageSheetCards(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 12.dp)
        .clip(RoundedCornerShape(18.dp)),
    content = content,
  )
}

/** A 64dp sheet card row: a 24dp line icon, a label and optional trailing content. */
@Composable
fun StageSheetRow(
  text: String,
  @DrawableRes icon: Int,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  trailing: @Composable RowScope.() -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
        .clickable(enabled = onClick != null) { onClick?.invoke() }
        .heightIn(min = 64.dp)
        .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(24.dp),
    )
    Text(
      text = text,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    trailing()
  }
}
