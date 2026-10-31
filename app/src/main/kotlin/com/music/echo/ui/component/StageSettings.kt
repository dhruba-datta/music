package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R

/** Icon tile colours of the Stage settings boards. */
object StageTile {
  val Rose = Color(0xFF8F2D56)
  val Violet = Color(0xFF6A4C93)
  val Blue = Color(0xFF2A6F97)
  val Green = Color(0xFF386641)
  val Slate = Color(0xFF3F3F4A)
  val Discord = Color(0xFF5865F2)
  val LastFm = Color(0xFFB91C1C)
  val Spotify = Color(0xFF1F6B3A)
}

/**
 * The page bar of every Stage sub-page: a 44dp back button beside a 24sp ExtraBold title, 14dp
 * below the status bar, with room for the page's own actions on the right.
 */
@Composable
fun StageTopBar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: (@Composable () -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  containerColor: Color = MaterialTheme.colorScheme.surface,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .background(containerColor)
        .windowInsetsPadding(windowInsets)
        .padding(start = 12.dp, top = 14.dp, end = 12.dp),
  ) {
    CompositionLocalProvider(
      LocalMinimumInteractiveComponentSize provides 44.dp,
      LocalContentColor provides MaterialTheme.colorScheme.onSurface,
    ) {
      if (navigationIcon != null) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
          navigationIcon()
        }
      }
      Box(modifier = Modifier.weight(1f).heightIn(min = 44.dp).padding(start = if (navigationIcon == null) 10.dp else 0.dp), contentAlignment = Alignment.CenterStart) {
        ProvideTextStyle(
          MaterialTheme.typography.titleLarge.copy(
            letterSpacing = (-0.24).sp,
            lineHeight = 27.6.sp,
          ),
        ) {
          title()
        }
      }
      Row(modifier = Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        actions()
      }
    }
  }
}

/** The Stage toggle: 54 by 32 with a 24dp thumb, white track and dark thumb while on. */
@Composable
fun StageSwitch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val colors = MaterialTheme.colorScheme
  val isDark = colors.surface.luminance() < 0.5f
  val track by
    animateColorAsState(
      when {
        checked -> colors.primary
        isDark -> Color(0xFF3A3A44)
        else -> colors.surfaceContainerHighest
      },
      label = "stageSwitchTrack",
    )
  val thumb by
    animateColorAsState(
      if (checked) colors.onPrimary else colors.onSurfaceVariant,
      label = "stageSwitchThumb",
    )
  val thumbOffset by animateDpAsState(if (checked) 22.dp else 0.dp, label = "stageSwitchOffset")

  Box(
    contentAlignment = Alignment.CenterStart,
    modifier =
      modifier
        .size(width = 54.dp, height = 32.dp)
        .alpha(if (enabled) 1f else 0.38f)
        .clip(RoundedCornerShape(16.dp))
        .background(track)
        .then(
          if (onCheckedChange != null) {
            Modifier.toggleable(
              value = checked,
              enabled = enabled,
              role = Role.Switch,
              onValueChange = onCheckedChange,
            )
          } else {
            Modifier
          }
        )
        .padding(horizontal = 4.dp),
  ) {
    Box(modifier = Modifier.offset(x = thumbOffset).size(24.dp).background(thumb, CircleShape))
  }
}

/** The Stage slider: an 8dp rounded track filled white, with no visible thumb. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  onValueChangeFinished: (() -> Unit)? = null,
) {
  val fill = MaterialTheme.colorScheme.primary
  val rest = MaterialTheme.colorScheme.surfaceContainerHighest
  Slider(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    valueRange = valueRange,
    steps = steps,
    onValueChangeFinished = onValueChangeFinished,
    modifier = modifier.height(24.dp),
    thumb = { Box(Modifier.size(8.dp)) },
    track = { state ->
      val span = state.valueRange.endInclusive - state.valueRange.start
      val fraction =
        if (span > 0f) ((state.value - state.valueRange.start) / span).coerceIn(0f, 1f) else 0f
      Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(rest)) {
        Box(modifier = Modifier.fillMaxWidth(fraction).height(8.dp).background(fill, RoundedCornerShape(4.dp)))
      }
    },
    colors = SliderDefaults.colors(),
  )
}

/** A labelled slider row: title on the left, current value on the right, the track below. */
@Composable
fun StageSliderRow(
  title: String,
  valueText: String,
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  enabled: Boolean = true,
  onValueChangeFinished: (() -> Unit)? = null,
) {
  Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 7.dp)) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      Text(
        text = valueText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    StageSlider(
      value = value,
      onValueChange = onValueChange,
      valueRange = valueRange,
      steps = steps,
      enabled = enabled,
      onValueChangeFinished = onValueChangeFinished,
      modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
    )
  }
}

/** The 22dp radio of the Stage sheets: a thin ring, or a thick white ring when selected. */
@Composable
fun StageRadio(selected: Boolean, modifier: Modifier = Modifier) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier =
      modifier
        .size(22.dp)
        .border(
          width = if (selected) 7.dp else 2.dp,
          color = if (selected) colors.primary else colors.onSurfaceVariant,
          shape = CircleShape,
        )
  )
}

/** A value shown at the end of a row, such as the current choice of a setting. */
@Composable
fun StageRowValue(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
  )
}

/** Trailing arrow of a row that opens another page. */
@Composable
fun StageRowChevron() {
  Icon(
    painter = painterResource(R.drawable.navigate_next),
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.size(18.dp),
  )
}

/** The 48dp search pill of the settings pages. */
@Composable
fun StageSearchField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  @DrawableRes leadingIcon: Int = R.drawable.search,
) {
  val colors = MaterialTheme.colorScheme
  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    textStyle = TextStyle(fontSize = 15.sp, color = colors.onSurface),
    cursorBrush = SolidColor(colors.onSurface),
    modifier = modifier.fillMaxWidth(),
    decorationBox = { field ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(colors.surfaceContainerHighest, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
      ) {
        Icon(
          painter = painterResource(leadingIcon),
          contentDescription = null,
          tint = colors.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
        Box(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          if (value.isEmpty()) {
            Text(text = placeholder, fontSize = 15.sp, color = colors.onSurfaceVariant, maxLines = 1)
          }
          field()
        }
        if (value.isNotEmpty()) {
          Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier =
              Modifier.size(32.dp)
                .clip(CircleShape)
                .clickable { onValueChange("") }
                .padding(6.dp),
          )
        }
      }
    },
  )
}

/**
 * A 50dp dialog pill that takes its label as content, so it can stand in for a text button: white
 * when [primary], dark grey otherwise.
 */
@Composable
fun StagePillButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  primary: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
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
    ProvideTextStyle(TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)) { content() }
  }
}
