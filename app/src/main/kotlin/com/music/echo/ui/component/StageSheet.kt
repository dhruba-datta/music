package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

/** The left-aligned 22sp sheet title. */
@Composable
fun StageSheetTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.ExtraBold,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
  )
}

/** A flat 52dp sheet row: a 22dp line icon, a bold label and optional trailing content. */
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
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(enabled = onClick != null) { onClick?.invoke() }
        .heightIn(min = 52.dp)
        .padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(22.dp),
    )
    Text(
      text = text,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    trailing()
  }
}
