package echo.music.iad1tya.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PreferenceGroupTitle(
  title: String,
  modifier: Modifier = Modifier,
) {
  StageSettingsHeading(text = title, modifier = modifier)
}
