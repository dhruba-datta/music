package echo.music.iad1tya.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Material3MenuGroup(items: List<Material3MenuItemData>, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    items.forEachIndexed { index, item ->
      val shape =
        when {
          items.size == 1 -> RoundedCornerShape(18.dp)
          index == 0 ->
            RoundedCornerShape(
              topStart = 18.dp,
              topEnd = 18.dp,
              bottomStart = 6.dp,
              bottomEnd = 6.dp
            )
          index == items.size - 1 ->
            RoundedCornerShape(
              topStart = 6.dp,
              topEnd = 6.dp,
              bottomStart = 18.dp,
              bottomEnd = 18.dp
            )
          else -> RoundedCornerShape(6.dp)
        }

      val content =
        @Composable {
          if (item.customComposable != null) {
            item.customComposable.invoke()
          } else {
            Material3MenuItemRow(item = item)
          }
        }

      Card(
        modifier =
          Modifier.fillMaxWidth().animateContentSize().let { mod ->
            if (item.onClick != null) mod.clickable(onClick = item.onClick) else mod
          },
        shape = shape,
        colors =
          item.cardColors
            ?: CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        content()
      }
    }
  }
}

@Composable
private fun Material3MenuItemRow(item: Material3MenuItemData) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    item.icon?.let { icon ->
      icon()
      Spacer(modifier = Modifier.width(16.dp))
    }

    Column(modifier = Modifier.weight(1f)) {
      ProvideTextStyle(MaterialTheme.typography.titleMedium) { item.title?.invoke() }

      item.description?.let { desc ->
        Spacer(modifier = Modifier.height(2.dp))
        ProvideTextStyle(
          MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          desc()
        }
      }
    }
    item.trailingContent?.let { trailing ->
      Spacer(modifier = Modifier.width(8.dp))
      trailing()
    }
  }
}

/** The Stage quick grid: every action is a rounded tile with its icon over its label. */
@Composable
fun MenuActionGrid(
  items: List<Material3MenuItemData>,
  modifier: Modifier = Modifier,
  columns: Int = 3,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items.chunked(columns).forEach { rowItems ->
      Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        rowItems.forEach { item ->
          MenuActionTile(item = item, modifier = Modifier.weight(1f).fillMaxHeight())
        }
        repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
      }
    }
  }
}

@Composable
private fun MenuActionTile(item: Material3MenuItemData, modifier: Modifier = Modifier) {
  val shape = RoundedCornerShape(18.dp)
  val onClick = item.onClick

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
    Column(
      modifier =
        modifier
          .clip(shape)
          .background(
            item.cardColors?.containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest,
            shape,
          )
          .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod }
          .heightIn(min = 76.dp)
          .padding(horizontal = 6.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
      item.icon?.let { icon ->
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
      }
      item.title?.invoke()
    }
  }
}

/** A tile label, kept to the two lines a [MenuActionGrid] tile has room for. */
@Composable
fun MenuTileLabel(text: String, color: Color = Color.Unspecified) {
  Text(
    text = text,
    color = color,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
}

data class Material3MenuItemData(
  val icon: (@Composable () -> Unit)? = null,
  val title: (@Composable () -> Unit)? = null,
  val description: (@Composable () -> Unit)? = null,
  val onClick: (() -> Unit)? = null,
  val cardColors: CardColors? = null,
  val trailingContent: (@Composable () -> Unit)? = null,
  val customComposable: (@Composable () -> Unit)? = null
)
