package echo.music.iad1tya.ui.screens.settings

import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.CanvasThumbnailAnimationKey
import echo.music.iad1tya.constants.MusicFlowAnimatedBackgroundKey
import echo.music.iad1tya.constants.MusicFlowAutoNextKey
import echo.music.iad1tya.constants.MusicFlowAutoplayKey
import echo.music.iad1tya.constants.MusicFlowCellularPrefetchKey
import echo.music.iad1tya.constants.MusicFlowEnabledKey
import echo.music.iad1tya.constants.MusicFlowHideRecentKey
import echo.music.iad1tya.constants.MusicFlowLyricsKey
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicFlowSettings(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
) {
  val scrollState = rememberScrollState()
  val (inheritedCanvas) = rememberPreference(CanvasThumbnailAnimationKey, defaultValue = false)

  @Composable
  fun toggle(
    key: Preferences.Key<Boolean>,
    default: Boolean,
    title: Int,
    description: Int? = null,
  ): Material3SettingsItem {
    val (checked, onCheckedChange) = rememberPreference(key, defaultValue = default)
    return Material3SettingsItem(
      title = { Text(stringResource(title)) },
      description = description?.let { { Text(stringResource(it)) } },
      trailingContent = { StageSwitch(checked = checked, onCheckedChange = onCheckedChange) },
      onClick = { onCheckedChange(!checked) })
  }

  val general =
    listOf(
      toggle(MusicFlowEnabledKey, true, R.string.music_flow_enable, R.string.stage_music_flow_enable_description),
      toggle(MusicFlowAutoplayKey, true, R.string.music_flow_autoplay),
      toggle(MusicFlowAutoNextKey, true, R.string.music_flow_auto_next),
      toggle(MusicFlowHideRecentKey, true, R.string.music_flow_hide_recent),
    )
  val visuals =
    listOf(
      toggle(MusicFlowAnimatedBackgroundKey, inheritedCanvas, R.string.music_flow_animated_background),
      toggle(MusicFlowLyricsKey, true, R.string.music_flow_lyrics),
      toggle(MusicFlowCellularPrefetchKey, true, R.string.music_flow_cellular_prefetch),
    )

  Column(
    Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
      .verticalScroll(scrollState)
      .padding(horizontal = 18.dp)
  ) {
    Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))
    Spacer(modifier = Modifier.height(12.dp))

    Material3SettingsGroup(scrollState = scrollState, items = general)

    Material3SettingsGroup(scrollState = scrollState, title = stringResource(R.string.appearance), items = visuals)
    Spacer(modifier = Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
  }

  StageTopBar(
    title = { Text(stringResource(R.string.music_flow)) },
    navigationIcon = {
      IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = stringResource(R.string.back_button_desc))
      }
    }
)
}
