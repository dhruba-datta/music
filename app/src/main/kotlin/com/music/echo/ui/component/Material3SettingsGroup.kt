package echo.music.iad1tya.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.utils.scrollToOnHighlight

/**
 * The Stage settings group: an optional 19sp heading above one 18dp card of rows.
 *
 * [tight] is the denser variant of the main Settings list: 48dp rows with 30dp tiles.
 */
@Composable
fun Material3SettingsGroup(
  title: String? = null,
  compact: Boolean = false,
  tight: Boolean = false,
  scrollState: ScrollState? = null,
  items: List<Material3SettingsItem>
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    title?.let { StageSettingsHeading(text = it, compact = compact) }

    Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      items.forEach { item ->
        Box(
          modifier =
            if (item.isHighlighted) {
              Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            } else {
              Modifier
            }
        ) {
          Material3SettingsItemRow(
            item = item,
            compact = compact,
            tight = tight,
            scrollState = scrollState,
          )
        }
      }
    }
  }
}

/** A section heading above a settings group: 19sp ExtraBold, aligned to the page's 22dp edge. */
@Composable
fun StageSettingsHeading(text: String, modifier: Modifier = Modifier, compact: Boolean = false) {
  Text(
    text = text,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.ExtraBold,
    color = MaterialTheme.colorScheme.onSurface,
    modifier =
      modifier.padding(
        top = if (compact) 4.dp else 18.dp,
        bottom = if (compact) 4.dp else 8.dp,
        start = 4.dp,
      ),
  )
}

@Composable
private fun Material3SettingsItemRow(
  item: Material3SettingsItem,
  compact: Boolean = false,
  tight: Boolean = false,
  scrollState: ScrollState? = null
) {
  val tileSize =
    when {
      tight -> 30.dp
      compact -> 34.dp
      else -> 36.dp
    }
  val tileShape = item.iconShape ?: RoundedCornerShape(if (tight) 10.dp else 12.dp)
  val iconSize = 24.dp
  // Sub-page rows are plain text on the boards; a tile only appears where it was asked for.
  val showPlainIcon =
    compact || tight || item.iconBackground != null || !item.tintIcon || item.showBadge

  val iconTint =
    when {
      !item.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
      else -> MaterialTheme.colorScheme.onSurface
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(
          enabled = item.enabled && item.onClick != null,
          onClick = { item.onClick?.invoke() }
        )
        .then(
          if (scrollState != null) Modifier.scrollToOnHighlight(scrollState, item.isHighlighted)
          else Modifier
        )
        .heightIn(min = if (tight) 48.dp else 54.dp)
        .padding(horizontal = 14.dp, vertical = if (tight) 3.dp else 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (item.customIcon != null) {
      Box(
        modifier = Modifier.size(tileSize).clip(tileShape),
        contentAlignment = Alignment.Center
      ) {
        item.customIcon.invoke()
      }
      Spacer(modifier = Modifier.width(13.dp))
    } else if (item.icon != null && showPlainIcon) {
      val icon = item.icon
      Box(
        modifier =
          Modifier.size(tileSize)
            .clip(tileShape)
            .then(
              Modifier
            ),
        contentAlignment = Alignment.Center
      ) {
        val iconContent: @Composable () -> Unit = {
          if (item.tintIcon) {
            Icon(
              painter = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(iconSize)
            )
          } else {
            Image(
              painter = icon,
              contentDescription = null,
              modifier = Modifier.size(tileSize),
              contentScale = ContentScale.Crop
            )
          }
        }
        if (item.showBadge) {
          BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.error) }) {
            iconContent()
          }
        } else {
          iconContent()
        }
      }

      Spacer(modifier = Modifier.width(13.dp))
    }

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
      ProvideTextStyle(
        TextStyle(
          fontSize = 16.sp,
          lineHeight = 20.sp,
          fontWeight = FontWeight.Bold,
          color =
            if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            else MaterialTheme.colorScheme.onSurface
        )
      ) {
        item.title()
      }

      item.description?.let { desc ->
        ProvideTextStyle(
          TextStyle(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color =
              if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              else MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          desc()
        }
      }
    }

    when {
      item.trailingContent != null -> {
        Spacer(modifier = Modifier.width(13.dp))
        item.trailingContent.invoke()
      }
      item.value != null -> {
        Spacer(modifier = Modifier.width(13.dp))
        ProvideTextStyle(
          TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          item.value.invoke()
        }
      }
      item.onClick != null && item.showChevron -> {
        Spacer(modifier = Modifier.width(13.dp))
        StageRowChevron()
      }
    }
  }
}

data class Material3SettingsItem(
  val icon: Painter? = null,
  val customIcon: (@Composable () -> Unit)? = null,
  val title: @Composable () -> Unit,
  val description: (@Composable () -> Unit)? = null,
  /** The current choice, shown at the end of the row in place of the arrow. */
  val value: (@Composable () -> Unit)? = null,
  val trailingContent: (@Composable () -> Unit)? = null,
  val showBadge: Boolean = false,
  val isHighlighted: Boolean = false,
  val tintIcon: Boolean = true,
  val iconShape: Shape? = null,
  val iconBackground: Color? = null,
  val enabled: Boolean = true,
  val showChevron: Boolean = true,
  val onClick: (() -> Unit)? = null
)
