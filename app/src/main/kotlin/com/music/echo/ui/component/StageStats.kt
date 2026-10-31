package echo.music.iad1tya.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** An item on a Stage stats page: its picture and two lines of text. */
data class StageStatEntry(
  val key: String,
  val title: String,
  val subtitle: String,
  val thumbnailUrl: String?,
  val onClick: () -> Unit = {},
  val onLongClick: () -> Unit = {},
)

/** A row that shows three cards at a time, each a wide rounded picture over its two lines. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StageStatCards(entries: List<StageStatEntry>, artHeight: Dp, modifier: Modifier = Modifier) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val cardWidth = (maxWidth - 36.dp - 24.dp) / 3
    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      itemsIndexed(entries, key = { _, entry -> entry.key }) { _, entry ->
        Column(
          verticalArrangement = Arrangement.spacedBy(3.dp),
          modifier =
            Modifier.width(cardWidth)
              .combinedClickable(onClick = entry.onClick, onLongClick = entry.onLongClick),
        ) {
          AsyncImage(
            model = entry.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.fillMaxWidth()
                .height(artHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = entry.title,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = entry.subtitle,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

/** A row of round artist pictures. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StageStatCircles(entries: List<StageStatEntry>, size: Dp, modifier: Modifier = Modifier) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 22.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    itemsIndexed(entries, key = { _, entry -> entry.key }) { _, entry ->
      AsyncImage(
        model = entry.thumbnailUrl,
        contentDescription = entry.title,
        contentScale = ContentScale.Crop,
        modifier =
          Modifier.size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(onClick = entry.onClick, onLongClick = entry.onLongClick),
      )
    }
  }
}

/** One of the two figures at the top of a detailed history page. */
@Composable
fun StageStatTile(value: String, label: String, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp))
        .padding(13.dp)
  ) {
    Text(
      text = value,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = label,
      fontSize = 13.sp,
      lineHeight = 17.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/** A plain settings-style line with a value at its end. */
@Composable
fun StageValueRow(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
  trailing: @Composable () -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier = modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    Text(
      text = title,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    StageRowValue(text = value)
    trailing()
  }
}
