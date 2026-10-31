@file:OptIn(
  ExperimentalMaterial3ExpressiveApi::class,
  ExperimentalLayoutApi::class,
  ExperimentalMaterial3Api::class
)

package echo.music.iad1tya.ui.screens.settings

import echo.music.iad1tya.ui.component.StageSettingsHeading
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.DarkModeKey
import echo.music.iad1tya.constants.DynamicThemeKey
import echo.music.iad1tya.constants.PureBlackKey
import echo.music.iad1tya.constants.PureBlackMiniPlayerKey
import echo.music.iad1tya.constants.SelectedThemeColorKey
import echo.music.iad1tya.ui.theme.DefaultThemeColor
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeScreen(navController: NavController, highlightKey: String? = null) {
  val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, DarkMode.ON)
  val (pureBlack, onPureBlackChangeRaw) = rememberPreference(PureBlackKey, defaultValue = false)
  val (_, onPureBlackMiniPlayerChange) =
    rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)

  val onPureBlackChange: (Boolean) -> Unit = { enabled ->
    onPureBlackChangeRaw(enabled)
    onPureBlackMiniPlayerChange(enabled)
  }
  val (selectedThemeColorInt, onSelectedThemeColorChange) =
    rememberPreference(SelectedThemeColorKey, DefaultThemeColor.toArgb())
  val (_, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = false)

  val selectedThemeColor = Color(selectedThemeColorInt)

  val handleColorSelection: (Color) -> Unit = { color ->
    onSelectedThemeColorChange(color.toArgb())
    onDynamicThemeChange(color == DefaultThemeColor)
  }

  Scaffold(
    topBar = {
      StageTopBar(
        title = { Text(stringResource(R.string.theme_colors)) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              painterResource(R.drawable.arrow_back),
              contentDescription = stringResource(R.string.cd_back)
            )
          }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
)
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        StageSettingsHeading(text = "Theme mode")

        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(horizontal = 4.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ThemeModeCard(
              modifier = Modifier.weight(1f),
              title = stringResource(R.string.dark_theme_follow_system),
              background = Color(0xFF2A2A32),
              contentColor = Color.White,
              isSelected = darkMode == DarkMode.AUTO,
              onClick = {
                onDarkModeChange(DarkMode.AUTO)
                onPureBlackChange(false)
              }
            )
            ThemeModeCard(
              modifier = Modifier.weight(1f),
              title = "Light",
              background = Color(0xFFF4F4F6),
              contentColor = Color(0xFF111111),
              isSelected = darkMode == DarkMode.OFF,
              onClick = {
                onDarkModeChange(DarkMode.OFF)
                onPureBlackChange(false)
              }
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ThemeModeCard(
              modifier = Modifier.weight(1f),
              title = "Dark",
              background = Color(0xFF1A1A20),
              contentColor = Color.White,
              isSelected = darkMode == DarkMode.ON && !pureBlack,
              onClick = {
                onDarkModeChange(DarkMode.ON)
                onPureBlackChange(false)
              }
            )
            ThemeModeCard(
              modifier = Modifier.weight(1f),
              title = "AMOLED",
              background = Color.Black,
              contentColor = Color.White,
              isSelected = pureBlack,
              onClick = {
                onDarkModeChange(DarkMode.ON)
                onPureBlackChange(true)
              }
            )
          }
        }
      }

      item {
        StageSettingsHeading(text = "Colors")

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(32.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
            ),
          elevation = CardDefaults.cardElevation(0.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            val isDynamic = selectedThemeColor == DefaultThemeColor
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    if (!isDynamic) {
                      handleColorSelection(DefaultThemeColor)
                    } else {
                      handleColorSelection(Color(0xFF1E88E5))
                    }
                  }
                  .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Box(
                  modifier =
                    Modifier.size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Text(
                  text = stringResource(R.string.palette_dynamic),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              StageSwitch(checked = isDynamic, onCheckedChange = { checked ->
                  if (checked) {
                    handleColorSelection(DefaultThemeColor)
                  } else {
                    handleColorSelection(Color(0xFF1E88E5))
                  }
                })
            }

            AnimatedVisibility(visible = !isDynamic) {
              HsvColorPicker(
                initialColor =
                  if (selectedThemeColor == DefaultThemeColor) Color(0xFF1E88E5)
                  else selectedThemeColor,
                onColorCommit = { handleColorSelection(it) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ThemeModeCard(
  modifier: Modifier = Modifier,
  title: String,
  background: Color,
  contentColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(18.dp)
  Box(
    modifier =
      modifier
        .height(72.dp)
        .then(
          if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
          else Modifier
        )
        .clip(shape)
        .background(background)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(text = title, color = contentColor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
  }
}


@Composable
fun HsvColorPicker(
  modifier: Modifier = Modifier,
  initialColor: Color,
  onColorCommit: (Color) -> Unit
) {
  var hsv by remember {
    val h = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), h)
    mutableStateOf(h)
  }

  LaunchedEffect(initialColor) {
    val currentC = android.graphics.Color.HSVToColor(hsv)
    if (currentC != initialColor.toArgb()) {
      val h = FloatArray(3)
      android.graphics.Color.colorToHSV(initialColor.toArgb(), h)
      hsv = h
    }
  }

  var hue by remember { mutableFloatStateOf(hsv[0]) }
  var saturation by remember { mutableFloatStateOf(hsv[1]) }
  var value by remember { mutableFloatStateOf(hsv[2]) }

  LaunchedEffect(hsv) {
    hue = hsv[0]
    saturation = hsv[1]
    value = hsv[2]
  }

  fun getLocalColor(): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))

  fun commitColor() {
    hsv = floatArrayOf(hue, saturation, value)
    onColorCommit(getLocalColor())
  }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(getLocalColor())
    )

    CustomColorSlider(
      value = hue,
      onValueChange = { hue = it },
      onValueChangeFinished = { commitColor() },
      valueRange = 0f..360f,
      label = "Hue",
      brush =
        Brush.horizontalGradient(
          colors =
            listOf(
              Color.Red,
              Color.Yellow,
              Color.Green,
              Color.Cyan,
              Color.Blue,
              Color.Magenta,
              Color.Red
            )
        )
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomColorSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  onValueChangeFinished: () -> Unit,
  valueRange: ClosedFloatingPointRange<Float>,
  label: String,
  brush: Brush
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    Box(
      modifier =
        Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(16.dp)).background(brush),
      contentAlignment = Alignment.CenterStart
    ) {
      Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        colors =
          SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
          ),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
