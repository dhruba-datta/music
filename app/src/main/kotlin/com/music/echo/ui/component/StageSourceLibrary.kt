package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val SourceCardWidth = 140.dp
private val SourceCardShape = RoundedCornerShape(18.dp)

/** The wide card at the top of a service page, such as Liked songs. */
@Composable
fun StageSourcePinnedCard(
  title: String,
  subtitle: String?,
  @DrawableRes iconRes: Int,
  colors: List<Color>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .height(84.dp)
        .clip(SourceCardShape)
        .background(Brush.linearGradient(colors))
        .combinedClickable(onClick = onClick)
        .padding(horizontal = 18.dp),
  ) {
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(28.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 13.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/** One album, playlist or artist in a sideways row. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StageSourceCard(
  title: String,
  subtitle: String?,
  imageUrl: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  circular: Boolean = false,
  onLongClick: (() -> Unit)? = null,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier =
      modifier
        .width(SourceCardWidth)
        .clip(SourceCardShape)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick),
  ) {
    AsyncImage(
      model = imageUrl,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.padding(bottom = 6.dp)
          .size(SourceCardWidth)
          .clip(if (circular) CircleShape else SourceCardShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
    Text(
      text = title,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    if (!subtitle.isNullOrBlank()) {
      Text(
        text = subtitle,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** A titled sideways row with a See all link, hidden while there is nothing to show. */
fun <T> LazyListScope.stageSourceSection(
  key: String,
  title: String,
  items: List<T>,
  itemKey: (T) -> Any,
  seeAllLabel: String,
  onSeeAll: () -> Unit,
  card: @Composable (T) -> Unit,
) {
  if (items.isEmpty()) return

  item(key = "${key}_title") {
    NavigationTitle(title = title, onClick = onSeeAll, endText = seeAllLabel)
  }
  item(key = "${key}_list") {
    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(items = items.take(12), key = itemKey) { card(it) }
    }
  }
}
