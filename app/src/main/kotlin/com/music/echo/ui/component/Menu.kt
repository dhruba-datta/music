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
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The Stage sheet list: one 18dp group of 64dp card rows, 18dp from the sheet's edges, spaced like a
 * Settings group.
 */
@Composable
fun Material3MenuGroup(items: List<Material3MenuItemData>, modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 18.dp, top = 4.dp, end = 18.dp)
        .clip(RoundedCornerShape(18.dp))
        .animateContentSize()
  ) {
    items.forEach { item ->
      val background =
        item.cardColors?.containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
      val contentColor = item.cardColors?.contentColor ?: MaterialTheme.colorScheme.onSurface
      CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
          modifier =
            Modifier.fillMaxWidth().background(background, RoundedCornerShape(4.dp)).let { mod ->
              val onClick = item.onClick
              if (onClick != null) mod.clickable(onClick = onClick) else mod
            }
        ) {
          if (item.customComposable != null) {
            item.customComposable.invoke()
          } else {
            Material3MenuItemRow(item = item)
          }
        }
      }
    }
  }
}

@Composable
private fun Material3MenuItemRow(item: Material3MenuItemData) {
  Row(
    modifier =
      Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 18.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item.icon?.let { icon ->
      Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
    }

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
      ProvideTextStyle(
        TextStyle(
          fontSize = 15.sp,
          lineHeight = 20.sp,
          fontWeight = FontWeight.SemiBold,
          color = LocalContentColor.current,
        )
      ) {
        item.title?.invoke()
      }

      item.description?.let { desc ->
        ProvideTextStyle(
          TextStyle(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        ) {
          desc()
        }
      }
    }
    item.trailingContent?.let { trailing -> trailing() }
  }
}

/** The Stage menu list: icon and label rows in rounded groups, as in the player's menu. */
@Composable
fun MenuActionGrid(items: List<Material3MenuItemData>, modifier: Modifier = Modifier) {
  // Groups of four, never leaving a single row alone at the end.
  val groupSize = if (items.size % 4 == 1) 3 else 4
  Column(
    modifier = modifier.fillMaxWidth().padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items.chunked(groupSize).forEach { group ->
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)),
      ) {
        group.forEach { item -> MenuActionRow(item = item) }
      }
    }
  }
}

@Composable
private fun MenuActionRow(item: Material3MenuItemData) {
  val onClick = item.onClick
  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            item.cardColors?.containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
          )
          .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod }
          .heightIn(min = 54.dp)
          .padding(horizontal = 18.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item.icon?.let { icon ->
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) { icon() }
      }
      Box(modifier = Modifier.weight(1f)) { item.title?.invoke() }
      item.trailingContent?.invoke()
    }
  }
}

/**
 * The title of a Stage sheet: 22sp ExtraBold, with room for one action in the sheet's top corner.
 */
@Composable
fun StageSheetTitle(
  title: String,
  modifier: Modifier = Modifier,
  trailing: (@Composable () -> Unit)? = null,
) {
  Box(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier =
        Modifier.fillMaxWidth()
          .padding(
            start = 22.dp,
            top = 12.dp,
            end = if (trailing != null) 60.dp else 22.dp,
            bottom = 4.dp,
          ),
    )
    if (trailing != null) {
      Box(
        modifier = Modifier.align(Alignment.TopEnd).padding(top = 1.dp, end = 12.dp).size(44.dp),
        contentAlignment = Alignment.Center,
      ) {
        trailing()
      }
    }
  }
}

/** A row label for a [MenuActionGrid]. */
@Composable
fun MenuTileLabel(text: String, color: Color = Color.Unspecified) {
  Text(
    text = text,
    color = color,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold,
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
