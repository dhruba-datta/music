package echo.music.iad1tya.ui.component

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.R

@Composable
fun <E> ChipsRow(
  chips: List<Pair<E, String>>,
  currentValue: E,
  onValueUpdate: (E) -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
  startPadding: Dp = 22.dp,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(top = 12.dp)
        .horizontalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
  ) {
    Spacer(Modifier.width(startPadding))

    chips.forEach { (value, label) ->
      val isSelected = currentValue == value

      val cornerRadius by
        animateDpAsState(
          targetValue = 50.dp,
          animationSpec =
            spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMedium
            ),
          label = "corner_radius"
        )

      FilterChip(
        label = {
          Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
        },
        selected = isSelected,
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.surface,
            selectedLeadingIconColor = MaterialTheme.colorScheme.surface
          ),
        onClick = { onValueUpdate(value) },
        shape = RoundedCornerShape(cornerRadius),
        border = null,
        modifier =
          Modifier.height(44.dp).animateContentSize(
            animationSpec =
              spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
              )
          )
      )

      Spacer(Modifier.width(8.dp))
    }
  }
}
