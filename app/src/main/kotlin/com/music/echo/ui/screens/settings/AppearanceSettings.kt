package echo.music.iad1tya.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppFont
import echo.music.iad1tya.constants.AppleMusicLyricsBlurKey
import echo.music.iad1tya.constants.CanvasThumbnailAnimationKey
import echo.music.iad1tya.constants.CropAlbumArtKey
import echo.music.iad1tya.constants.DefaultOpenTabKey
import echo.music.iad1tya.constants.DensityScale
import echo.music.iad1tya.constants.DensityScaleKey
import echo.music.iad1tya.constants.DynamicThemeKey
import echo.music.iad1tya.constants.EnableHighRefreshRateKey
import echo.music.iad1tya.constants.EnableLyricsThumbnailPlayPauseKey
import echo.music.iad1tya.constants.GridItemSize
import echo.music.iad1tya.constants.GridItemsSizeKey
import echo.music.iad1tya.constants.HidePlayerThumbnailKey
import echo.music.iad1tya.constants.HideStatusBarOnFullscreenKey
import echo.music.iad1tya.constants.ListenTogetherInTopBarKey
import echo.music.iad1tya.constants.LyricsAnimationStyle
import echo.music.iad1tya.constants.LyricsAnimationStyleKey
import echo.music.iad1tya.constants.LyricsClickKey
import echo.music.iad1tya.constants.LyricsGlowEffectKey
import echo.music.iad1tya.constants.LyricsLineSpacingKey
import echo.music.iad1tya.constants.LyricsScrollKey
import echo.music.iad1tya.constants.LyricsStandardBlurKey
import echo.music.iad1tya.constants.LyricsTextPositionKey
import echo.music.iad1tya.constants.LyricsTextSizeKey
import echo.music.iad1tya.constants.MiniPlayerBackgroundStyleKey
import echo.music.iad1tya.constants.PlayerBackgroundStyle
import echo.music.iad1tya.constants.PlayerBackgroundStyleKey
import echo.music.iad1tya.constants.PlayerButtonsStyle
import echo.music.iad1tya.constants.PlayerButtonsStyleKey
import echo.music.iad1tya.constants.RotatingThumbnailKey
import echo.music.iad1tya.constants.SelectedFontKey
import echo.music.iad1tya.constants.SelectedThemeColorKey
import echo.music.iad1tya.constants.ShowBottomPlaylistKey
import echo.music.iad1tya.constants.ShowCachedPlaylistKey
import echo.music.iad1tya.constants.ShowCommentButtonKey
import echo.music.iad1tya.constants.ShowDownloadedPlaylistKey
import echo.music.iad1tya.constants.ShowExportedPlaylistKey
import echo.music.iad1tya.constants.ShowLikedPlaylistKey
import echo.music.iad1tya.constants.ShowTopPlaylistKey
import echo.music.iad1tya.constants.SliderStyle
import echo.music.iad1tya.constants.SliderStyleKey
import echo.music.iad1tya.constants.SquigglySliderKey
import echo.music.iad1tya.constants.StageLyricsLineSpacingDefault
import echo.music.iad1tya.constants.StageLyricsTextSizeDefault
import echo.music.iad1tya.constants.SwipeLyricsKey
import echo.music.iad1tya.constants.SwipeSensitivityKey
import echo.music.iad1tya.constants.SwipeThumbnailKey
import echo.music.iad1tya.constants.SwipeToRemoveSongKey
import echo.music.iad1tya.constants.SwipeToSongKey
import echo.music.iad1tya.constants.ThumbnailCornerRadiusKey
import echo.music.iad1tya.constants.UseNewPlayerDesignKey
import echo.music.iad1tya.constants.WavyPlayPauseKey
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.EnumDialog
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.component.PlayerSliderTrack
import echo.music.iad1tya.ui.component.SquigglySlider
import echo.music.iad1tya.ui.component.StagePillButton
import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.ui.component.StageTopBar
import echo.music.iad1tya.ui.component.ThumbnailCornerRadiusModal
import echo.music.iad1tya.ui.component.WavySlider
import echo.music.iad1tya.ui.theme.DefaultThemeColor
import echo.music.iad1tya.ui.theme.PlayerSliderColors
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  activity: Activity,
  snackbarHostState: SnackbarHostState,
  highlightKey: String? = null
) {
  val scrollState = androidx.compose.foundation.rememberScrollState()

  var openSection by rememberSaveable { mutableStateOf<String?>(null) }
  // A search result lands on the full page so its row can be scrolled to and highlighted.
  val inSection = { key: String? -> highlightKey != null || openSection == key }
  BackHandler(enabled = openSection != null) { openSection = null }
  LaunchedEffect(openSection) { scrollState.scrollTo(0) }

  val (selectedFontValue) = rememberPreference(SelectedFontKey, defaultValue = AppFont.MANROPE.value)
  val (dynamicTheme, onDynamicThemeChange) =
    rememberPreference(DynamicThemeKey, defaultValue = false)
  val (enableLegacyIcon, onEnableLegacyIconChange) =
    rememberPreference(echo.music.iad1tya.constants.EnableLegacyIconKey, defaultValue = false)

  val (enableHighRefreshRate, onEnableHighRefreshRateChange) =
    rememberPreference(echo.music.iad1tya.constants.EnableHighRefreshRateKey, defaultValue = true)
  val (liveBlurDensity, onLiveBlurDensityChange) =
    rememberPreference(echo.music.iad1tya.constants.LiveBlurDensityKey, defaultValue = 50f)
  val (selectedThemeColorInt) =
    rememberPreference(SelectedThemeColorKey, defaultValue = DefaultThemeColor.toArgb())

  val isUsingCustomColor = selectedThemeColorInt != DefaultThemeColor.toArgb()
  val coroutineScope = rememberCoroutineScope()

  val (useNewPlayerDesign, onUseNewPlayerDesignChange) =
    rememberPreference(UseNewPlayerDesignKey, defaultValue = true)
  val (showCodecOnPlayer, onShowCodecOnPlayerChange) =
    rememberPreference(echo.music.iad1tya.constants.ShowCodecOnPlayerKey, defaultValue = false)
  val (hidePlayerSlider, onHidePlayerSliderChange) =
    rememberPreference(echo.music.iad1tya.constants.HidePlayerSliderKey, defaultValue = false)
  val (hidePlayerThumbnail, onHidePlayerThumbnailChange) =
    rememberPreference(HidePlayerThumbnailKey, defaultValue = false)
  val (cropAlbumArt, onCropAlbumArtChange) =
    rememberPreference(CropAlbumArtKey, defaultValue = false)
  val (playerBackground, onPlayerBackgroundChange) =
    rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.GRADIENT)
  val (miniPlayerBackground, onMiniPlayerBackgroundChange) =
    rememberEnumPreference(
      MiniPlayerBackgroundStyleKey,
      defaultValue = PlayerBackgroundStyle.DEFAULT
    )

  val (defaultOpenTab, onDefaultOpenTabChange) =
    rememberEnumPreference(DefaultOpenTabKey, defaultValue = NavigationTab.HOME)
  val (playerButtonsStyle, onPlayerButtonsStyleChange) =
    rememberEnumPreference(PlayerButtonsStyleKey, defaultValue = PlayerButtonsStyle.DEFAULT)
  val (lyricsPosition, onLyricsPositionChange) =
    rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.LEFT)
  val (lyricsClick, onLyricsClickChange) = rememberPreference(LyricsClickKey, defaultValue = true)
  val (lyricsScroll, onLyricsScrollChange) =
    rememberPreference(LyricsScrollKey, defaultValue = true)
  val (lyricsAnimationStyle, onLyricsAnimationStyleChange) =
    rememberEnumPreference(LyricsAnimationStyleKey, defaultValue = LyricsAnimationStyle.echomusic_1)
  // When nothing is saved, these show what the current player really uses.
  val unsetLyricsTextSize = if (useNewPlayerDesign) StageLyricsTextSizeDefault else 24f
  val unsetLyricsLineSpacing = if (useNewPlayerDesign) StageLyricsLineSpacingDefault else 1.3f
  val (lyricsTextSize, onLyricsTextSizeChange) =
    rememberPreference(LyricsTextSizeKey, defaultValue = unsetLyricsTextSize)
  val (lyricsLineSpacing, onLyricsLineSpacingChange) =
    rememberPreference(LyricsLineSpacingKey, defaultValue = unsetLyricsLineSpacing)
  val (lyricsGlowEffect, onLyricsGlowEffectChange) =
    rememberPreference(LyricsGlowEffectKey, defaultValue = false)
  val (appleMusicLyricsBlur, onAppleMusicLyricsBlurChange) =
    rememberPreference(AppleMusicLyricsBlurKey, defaultValue = true)
  val (lyricsStandardBlur, onLyricsStandardBlurChange) =
    rememberPreference(LyricsStandardBlurKey, defaultValue = false)
  val (swipeLyrics, onSwipeLyricsChange) = rememberPreference(SwipeLyricsKey, defaultValue = false)
  val (enableLyricsThumbnailPlayPause, onEnableLyricsThumbnailPlayPauseChange) =
    rememberPreference(EnableLyricsThumbnailPlayPauseKey, defaultValue = false)
  val (hideStatusBarOnFullscreen, onHideStatusBarOnFullscreenChange) =
    rememberPreference(HideStatusBarOnFullscreenKey, defaultValue = false)

  val (sliderStyle, onSliderStyleChange) =
    rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.SLIM)
  val (squigglySlider, onSquigglySliderChange) =
    rememberPreference(SquigglySliderKey, defaultValue = false)
  val (wavyPlayPause, onWavyPlayPauseChange) =
    rememberPreference(WavyPlayPauseKey, defaultValue = true)
  val (swipeThumbnail, onSwipeThumbnailChange) =
    rememberPreference(SwipeThumbnailKey, defaultValue = true)
  val (swipeSensitivity, onSwipeSensitivityChange) =
    rememberPreference(SwipeSensitivityKey, defaultValue = 0.73f)
  val (canvasThumbnailAnimation, onCanvasThumbnailAnimationChange) =
    rememberPreference(CanvasThumbnailAnimationKey, defaultValue = false)
  val (rotatingThumbnail, onRotatingThumbnailChange) =
    rememberPreference(RotatingThumbnailKey, defaultValue = false)
  val (gridItemSize, onGridItemSizeChange) =
    rememberEnumPreference(GridItemsSizeKey, defaultValue = GridItemSize.SMALL)

  val context = activity as Context
  val sharedPreferences = remember {
    context.getSharedPreferences("echomusic_settings", Context.MODE_PRIVATE)
  }
  val prefDensityScale =
    remember(sharedPreferences) { sharedPreferences.getFloat("density_scale_factor", 1.0f) }
  val (densityScale, setDensityScale) =
    rememberPreference(DensityScaleKey, defaultValue = prefDensityScale)
  var showRestartDialog by rememberSaveable { mutableStateOf(false) }
  var showDensityScaleDialog by rememberSaveable { mutableStateOf(false) }

  val onDensityScaleChange: (Float) -> Unit = { newScale ->
    setDensityScale(newScale)

    sharedPreferences.edit { putFloat("density_scale_factor", newScale) }
    showRestartDialog = true
  }

  // The Listen Together row is hidden here because that button is always in the top bar.
  // The preference itself is kept.
  rememberPreference(ListenTogetherInTopBarKey, defaultValue = true)

  val (swipeToSong, onSwipeToSongChange) = rememberPreference(SwipeToSongKey, defaultValue = true)

  val (swipeToRemoveSong, onSwipeToRemoveSongChange) =
    rememberPreference(SwipeToRemoveSongKey, defaultValue = false)

  val (showLikedPlaylist, onShowLikedPlaylistChange) =
    rememberPreference(ShowLikedPlaylistKey, defaultValue = true)
  val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) =
    rememberPreference(ShowDownloadedPlaylistKey, defaultValue = true)
  val (showExportedPlaylist, onShowExportedPlaylistChange) =
    rememberPreference(ShowExportedPlaylistKey, defaultValue = true)
  val (showTopPlaylist, onShowTopPlaylistChange) =
    rememberPreference(ShowTopPlaylistKey, defaultValue = true)
  val (showBottomPlaylist, onShowBottomPlaylistChange) =
    rememberPreference(ShowBottomPlaylistKey, defaultValue = true)
  val (showCachedPlaylist, onShowCachedPlaylistChange) =
    rememberPreference(ShowCachedPlaylistKey, defaultValue = true)
  val (showCommentButton, onShowCommentButtonChange) =
    rememberPreference(ShowCommentButtonKey, defaultValue = false)

  val availableBackgroundStyles =
    PlayerBackgroundStyle.entries.filter {
      it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

  val availableMiniPlayerBackgroundStyles =
    availableBackgroundStyles.filter {
      it != PlayerBackgroundStyle.APPLE_MUSIC && it != PlayerBackgroundStyle.GRADIENT
    }

  var showSliderOptionDialog by rememberSaveable { mutableStateOf(false) }

  var showPlayerBackgroundDialog by rememberSaveable { mutableStateOf(false) }

  var showMiniPlayerBackgroundDialog by rememberSaveable { mutableStateOf(false) }

  var showPlayerButtonsStyleDialog by rememberSaveable { mutableStateOf(false) }

  var showLyricsPositionDialog by rememberSaveable { mutableStateOf(false) }

  var showLyricsAnimationStyleDialog by rememberSaveable { mutableStateOf(false) }

  var showLyricsTextSizeDialog by rememberSaveable { mutableStateOf(false) }

  var showLyricsLineSpacingDialog by rememberSaveable { mutableStateOf(false) }

  if (showLyricsPositionDialog) {
    EnumDialog(
      onDismiss = { showLyricsPositionDialog = false },
      onSelect = {
        onLyricsPositionChange(it)
        showLyricsPositionDialog = false
      },
      title = stringResource(R.string.lyrics_text_position),
      current = lyricsPosition,
      values = LyricsPosition.values().toList(),
      valueText = {
        when (it) {
          LyricsPosition.LEFT -> stringResource(R.string.left)
          LyricsPosition.CENTER -> stringResource(R.string.center)
          LyricsPosition.RIGHT -> stringResource(R.string.right)
        }
      }
    )
  }

  if (showLyricsAnimationStyleDialog) {
    EnumDialog(
      onDismiss = { showLyricsAnimationStyleDialog = false },
      onSelect = {
        onLyricsAnimationStyleChange(it)
        showLyricsAnimationStyleDialog = false
      },
      title = stringResource(R.string.lyrics_animation_style),
      current = lyricsAnimationStyle,
      values = LyricsAnimationStyle.values().toList(),
      valueText = {
        when (it) {
          LyricsAnimationStyle.NONE -> stringResource(R.string.none)
          LyricsAnimationStyle.FADE -> stringResource(R.string.fade)
          LyricsAnimationStyle.GLOW -> stringResource(R.string.glow)
          LyricsAnimationStyle.SLIDE -> stringResource(R.string.slide)
          LyricsAnimationStyle.KARAOKE -> stringResource(R.string.karaoke)
          LyricsAnimationStyle.APPLE -> stringResource(R.string.apple_music_style)
          LyricsAnimationStyle.APPLE_V2 -> stringResource(R.string.apple_music_style_letter)
          LyricsAnimationStyle.echomusic_1 -> stringResource(R.string.echomusic_1)
          LyricsAnimationStyle.LYRICS_V2 -> stringResource(R.string.lyrics_v2_fluid)
          LyricsAnimationStyle.METRO_LYRICS -> stringResource(R.string.lyrics_animation_metro)
        }
      }
    )
  }

  if (showLyricsTextSizeDialog) {
    var tempTextSize by remember { mutableFloatStateOf(lyricsTextSize) }

    DefaultDialog(
      onDismiss = {
        tempTextSize = lyricsTextSize
        showLyricsTextSizeDialog = false
      },
      buttons = {
        StagePillButton(onClick = {
            onLyricsTextSizeChange(tempTextSize)
            showLyricsTextSizeDialog = false
          }) {
          Text(stringResource(android.R.string.ok))
        }
        StagePillButton(onClick = {
            tempTextSize = lyricsTextSize
            showLyricsTextSizeDialog = false
          }, primary = false) {
          Text(stringResource(android.R.string.cancel))
        }
        StagePillButton(onClick = { tempTextSize = unsetLyricsTextSize }, primary = false) { Text(stringResource(R.string.reset)) }
      }
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = stringResource(R.string.lyrics_text_size),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
          text = "${tempTextSize.roundToInt()} sp",
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Slider(
          value = tempTextSize,
          onValueChange = { tempTextSize = it },
          valueRange = 16f..36f,
          steps = 19,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }

  if (showLyricsLineSpacingDialog) {
    var tempLineSpacing by remember { mutableFloatStateOf(lyricsLineSpacing) }

    DefaultDialog(
      onDismiss = {
        tempLineSpacing = lyricsLineSpacing
        showLyricsLineSpacingDialog = false
      },
      buttons = {
        StagePillButton(onClick = {
            onLyricsLineSpacingChange(tempLineSpacing)
            showLyricsLineSpacingDialog = false
          }) {
          Text(stringResource(android.R.string.ok))
        }
        StagePillButton(onClick = {
            tempLineSpacing = lyricsLineSpacing
            showLyricsLineSpacingDialog = false
          }, primary = false) {
          Text(stringResource(android.R.string.cancel))
        }
        StagePillButton(onClick = { tempLineSpacing = unsetLyricsLineSpacing }, primary = false) { Text(stringResource(R.string.reset)) }
      }
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = stringResource(R.string.lyrics_line_spacing),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
          text = "${String.format("%.1f", tempLineSpacing)}x",
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Slider(
          value = tempLineSpacing,
          onValueChange = { tempLineSpacing = it },
          valueRange = 1.0f..4.0f,
          steps = 59,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }

  if (showPlayerButtonsStyleDialog) {
    EnumDialog(
      onDismiss = { showPlayerButtonsStyleDialog = false },
      onSelect = {
        onPlayerButtonsStyleChange(it)
        showPlayerButtonsStyleDialog = false
      },
      title = stringResource(R.string.player_buttons_style),
      current = playerButtonsStyle,
      values = PlayerButtonsStyle.values().toList(),
      valueText = {
        when (it) {
          PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
          PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
          PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
        }
      }
    )
  }

  if (showPlayerBackgroundDialog) {
    EnumDialog(
      onDismiss = { showPlayerBackgroundDialog = false },
      onSelect = {
        onPlayerBackgroundChange(it)
        showPlayerBackgroundDialog = false
      },
      title = stringResource(R.string.player_background_style),
      current = playerBackground,
      values = availableBackgroundStyles,
      valueText = {
        when (it) {
          PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
          PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
          PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
          PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
          PlayerBackgroundStyle.APPLE_MUSIC -> stringResource(R.string.apple_music)
          PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
          PlayerBackgroundStyle.LIQUID_GLASS ->
            stringResource(R.string.player_background_liquid_glass)
        }
      }
    )
  }

  if (showMiniPlayerBackgroundDialog) {
    EnumDialog(
      onDismiss = { showMiniPlayerBackgroundDialog = false },
      onSelect = {
        onMiniPlayerBackgroundChange(it)
        showMiniPlayerBackgroundDialog = false
      },
      title = stringResource(R.string.miniplayer_background_style),
      current = miniPlayerBackground,
      values = availableMiniPlayerBackgroundStyles,
      valueText = {
        when (it) {
          PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
          PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
          PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
          PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
          PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
          PlayerBackgroundStyle.LIQUID_GLASS ->
            stringResource(R.string.player_background_liquid_glass)
          else -> stringResource(R.string.unknown)
        }
      }
    )
  }

  var showDefaultOpenTabDialog by rememberSaveable { mutableStateOf(false) }

  if (showDefaultOpenTabDialog) {
    EnumDialog(
      onDismiss = { showDefaultOpenTabDialog = false },
      onSelect = {
        onDefaultOpenTabChange(it)
        showDefaultOpenTabDialog = false
      },
      title = stringResource(R.string.default_open_tab),
      current = defaultOpenTab,
      values = NavigationTab.values().toList(),
      valueText = {
        when (it) {
          NavigationTab.HOME -> stringResource(R.string.home)
          NavigationTab.SEARCH -> stringResource(R.string.search)
          NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
        }
      }
    )
  }

  var showGridSizeDialog by rememberSaveable { mutableStateOf(false) }

  if (showGridSizeDialog) {
    EnumDialog(
      onDismiss = { showGridSizeDialog = false },
      onSelect = {
        onGridItemSizeChange(it)
        showGridSizeDialog = false
      },
      title = stringResource(R.string.grid_cell_size),
      current = gridItemSize,
      values = GridItemSize.values().toList(),
      valueText = {
        when (it) {
          GridItemSize.BIG -> stringResource(R.string.big)
          GridItemSize.SMALL -> stringResource(R.string.small)
        }
      }
    )
  }

  if (showRestartDialog) {
    DefaultDialog(
      onDismiss = { showRestartDialog = false },
      buttons = {
        StagePillButton(onClick = {
            showRestartDialog = false
            val intent =
              context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
              }
            context.startActivity(intent)
            Runtime.getRuntime().exit(0)
          }) {
          Text(text = stringResource(R.string.restart))
        }
        StagePillButton(onClick = { showRestartDialog = false }, primary = false) {
          Text(text = stringResource(android.R.string.cancel))
        }
      }
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = stringResource(R.string.restart_required),
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = stringResource(R.string.density_restart_message),
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }

  if (showDensityScaleDialog) {
    DefaultDialog(
      onDismiss = { showDensityScaleDialog = false },
      buttons = {
        StagePillButton(onClick = { showDensityScaleDialog = false }) {
          Text(text = stringResource(android.R.string.cancel))
        }
      }
    ) {
      Column {
        DensityScale.entries.forEach { scale ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clickable {
                  onDensityScaleChange(scale.value)
                  showDensityScaleDialog = false
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = scale.label,
              style = MaterialTheme.typography.bodyLarge,
              color =
                if (densityScale == scale.value) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                }
            )
          }
        }
      }
    }
  }

  if (showSliderOptionDialog) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    DefaultDialog(
      buttons = {
        StagePillButton(onClick = { showSliderOptionDialog = false }) {
          Text(text = stringResource(android.R.string.cancel))
        }
      },
      onDismiss = { showSliderOptionDialog = false }
    ) {
      val sliderPreviewColors =
        PlayerSliderColors.getSliderColors(
          MaterialTheme.colorScheme.primary,
          PlayerBackgroundStyle.DEFAULT,
          isSystemInDarkTheme()
        )

      val barItem: @Composable (Modifier) -> Unit = { modifier ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier =
            modifier
              .height(84.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(
                1.dp,
                if (sliderStyle == SliderStyle.DEFAULT && !squigglySlider)
                  MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
              )
              .clickable {
                onSliderStyleChange(SliderStyle.DEFAULT)
                onSquigglySliderChange(false)
                showSliderOptionDialog = false
              }
              .padding(12.dp)
        ) {
          Slider(
            value = 0.35f,
            valueRange = 0f..1f,
            onValueChange = {},
            colors = sliderPreviewColors,
            enabled = false,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = "Bar",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      val wavyItem: @Composable (Modifier) -> Unit = { modifier ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier =
            modifier
              .height(84.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(
                1.dp,
                if (sliderStyle == SliderStyle.WAVY && !squigglySlider)
                  MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
              )
              .clickable {
                onSliderStyleChange(SliderStyle.WAVY)
                onSquigglySliderChange(false)
                showSliderOptionDialog = false
              }
              .padding(12.dp)
        ) {
          WavySlider(
            value = 0.5f,
            valueRange = 0f..1f,
            onValueChange = {},
            colors = sliderPreviewColors,
            modifier = Modifier.weight(1f),
            isPlaying = true,
            enabled = false
          )
          Text(
            text = stringResource(R.string.wavy),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      val slimItem: @Composable (Modifier) -> Unit = { modifier ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier =
            modifier
              .height(84.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(
                1.dp,
                if (sliderStyle == SliderStyle.SLIM) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
              )
              .clickable {
                onSliderStyleChange(SliderStyle.SLIM)
                onSquigglySliderChange(false)
                showSliderOptionDialog = false
              }
              .padding(12.dp)
        ) {
          Slider(
            value = 0.65f,
            valueRange = 0f..1f,
            onValueChange = {},
            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
            track = { sliderState ->
              PlayerSliderTrack(sliderState = sliderState, colors = sliderPreviewColors)
            },
            colors = sliderPreviewColors,
            enabled = false,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = stringResource(R.string.slim),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      val squigglyItem: @Composable (Modifier) -> Unit = { modifier ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier =
            modifier
              .height(84.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(
                1.dp,
                if (sliderStyle == SliderStyle.WAVY && squigglySlider)
                  MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
              )
              .clickable {
                onSliderStyleChange(SliderStyle.WAVY)
                onSquigglySliderChange(true)
                showSliderOptionDialog = false
              }
              .padding(12.dp)
        ) {
          SquigglySlider(
            value = 0.5f,
            valueRange = 0f..1f,
            onValueChange = {},
            modifier = Modifier.weight(1f),
            enabled = false,
            colors = sliderPreviewColors,
            isPlaying = true
          )
          Text(
            text = stringResource(R.string.squiggly),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      if (isLandscape) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          barItem(Modifier.weight(1f))
          wavyItem(Modifier.weight(1f))
          slimItem(Modifier.weight(1f))
          squigglyItem(Modifier.weight(1f))
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            barItem(Modifier.weight(1f))
            wavyItem(Modifier.weight(1f))
          }
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            slimItem(Modifier.weight(1f))
            squigglyItem(Modifier.weight(1f))
          }
        }
      }
    }
  }

  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
      )
      .verticalScroll(scrollState)
      .padding(horizontal = 18.dp)
  ) {
    Spacer(
      Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    )

    if (inSection(null)) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.theme),
      items =
        buildList {
          add(
            Material3SettingsItem(
              icon = painterResource(echo.music.iad1tya.R.drawable.alphabet_cyrillic),
              title = { Text(stringResource(echo.music.iad1tya.R.string.app_font)) },
              value = {
                val fontLabel =
                  when (AppFont.fromValue(selectedFontValue)) {
                    AppFont.SYSTEM -> stringResource(echo.music.iad1tya.R.string.font_system)
                    AppFont.SANS_FLEX -> stringResource(echo.music.iad1tya.R.string.font_sans_flex)
                    AppFont.MANROPE -> "Manrope"
                    AppFont.OUTFIT -> stringResource(echo.music.iad1tya.R.string.font_outfit)
                    AppFont.PLUS_JAKARTA_SANS ->
                      stringResource(echo.music.iad1tya.R.string.font_plus_jakarta_sans)
                    AppFont.CUSTOM -> stringResource(echo.music.iad1tya.R.string.font_custom)
                  }
                Text(fontLabel)
              },
              onClick = { navController.navigate("settings/appearance/font") }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.theme)),
              icon = painterResource(R.drawable.palette),
              title = { Text(stringResource(R.string.theme)) },
              description = { Text(stringResource(R.string.theme_desc)) },
              onClick = { navController.navigate("settings/appearance/theme") }
            )
          )

          add(
            Material3SettingsItem(
              icon = painterResource(R.drawable.water_drop),
              title = { Text(stringResource(R.string.liquid_glass)) },
              description = { Text(stringResource(R.string.liquid_glass_settings)) },
              onClick = { navController.navigate("settings/appearance/liquidglass") }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.enable_high_refresh_rate)),
              icon = painterResource(R.drawable.speed),
              title = { Text(stringResource(R.string.enable_high_refresh_rate)) },
              description = { Text(stringResource(R.string.enable_high_refresh_rate_desc)) },
              trailingContent = {
                StageSwitch(checked = enableHighRefreshRate, onCheckedChange = onEnableHighRefreshRateChange)
              },
              onClick = { onEnableHighRefreshRateChange(!enableHighRefreshRate) }
            )
          )
        }
    )
    }

    if (highlightKey == null && openSection == null) {
      Material3SettingsGroup(
        title = stringResource(R.string.stage_more),
        items =
          listOf(
          Material3SettingsItem(
            title = { Text(stringResource(R.string.mini_player)) },
            onClick = { openSection = "mini" },
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.player)) },
            description = { Text(stringResource(R.string.stage_appearance_player_desc)) },
            onClick = { openSection = "player" },
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.lyrics)) },
            description = { Text(stringResource(R.string.stage_appearance_lyrics_desc)) },
            onClick = { openSection = "lyrics" },
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.misc)) },
            description = { Text(stringResource(R.string.stage_appearance_misc_desc)) },
            onClick = { openSection = "misc" },
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.auto_playlists)) },
            onClick = { openSection = "auto" },
          )
          ),
      )
    }

    if (inSection("mini")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(id = R.string.mini_player),
      items =
        buildList {
          add(
            Material3SettingsItem(
              isHighlighted =
                (highlightKey == stringResource(R.string.miniplayer_background_style)),
              icon = painterResource(R.drawable.dock_to_top),
              title = { Text(stringResource(R.string.miniplayer_background_style)) },
              value = {
                Text(
                  when (miniPlayerBackground) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                    PlayerBackgroundStyle.LIQUID_GLASS ->
                      stringResource(R.string.player_background_liquid_glass)
                    else -> stringResource(R.string.follow_theme)
                  }
                )
              },
              onClick = { showMiniPlayerBackgroundDialog = true }
            )
          )
        }
    )
    }

    Spacer(modifier = Modifier.height(16.dp))

    val (thumbnailCornerRadius, onThumbnailCornerRadiusChange) =
      rememberPreference(ThumbnailCornerRadiusKey, defaultValue = 3f)

    var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }
    var showThumbnailCornerRadiusDialog by rememberSaveable { mutableStateOf(false) }

    if (inSection("player")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.player),
      items =
        listOfNotNull(
          Material3SettingsItem(
            isHighlighted = (highlightKey == "Apple Music Inspired"),
            icon = painterResource(R.drawable.apple_music_me),
            title = { Text("Apple Music Inspired") },
            trailingContent = {
              StageSwitch(checked = !useNewPlayerDesign, onCheckedChange = { isChecked ->
                  onUseNewPlayerDesignChange(!isChecked)
                  if (isChecked) {
                    onPlayerBackgroundChange(PlayerBackgroundStyle.APPLE_MUSIC)
                  }
                })
            },
            onClick = {
              val newAppleMusicInspired = useNewPlayerDesign
              onUseNewPlayerDesignChange(!newAppleMusicInspired)
              if (newAppleMusicInspired) {
                onPlayerBackgroundChange(PlayerBackgroundStyle.APPLE_MUSIC)
              }
            }
          ),
          if (!useNewPlayerDesign)
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Hide volume slider"),
              icon = painterResource(R.drawable.linear_scale),
              title = { Text("Hide volume slider") },
              description = { Text("Hide the volume slider on the Apple Music player") },
              trailingContent = {
                StageSwitch(checked = hidePlayerSlider, onCheckedChange = onHidePlayerSliderChange)
              },
              onClick = { onHidePlayerSliderChange(!hidePlayerSlider) }
            )
          else null,
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.player_background_style)),
            icon = painterResource(R.drawable.gradient),
            title = { Text(stringResource(R.string.player_background_style)) },
            value = {
              Text(
                when (playerBackground) {
                  PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                  PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                  PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                  PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                  PlayerBackgroundStyle.APPLE_MUSIC -> stringResource(R.string.apple_music)
                  PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                  PlayerBackgroundStyle.LIQUID_GLASS ->
                    stringResource(R.string.player_background_liquid_glass)
                }
              )
            },
            onClick = { showPlayerBackgroundDialog = true }
          ),
          if (playerBackground != PlayerBackgroundStyle.APPLE_MUSIC)
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.hide_player_thumbnail)),
              icon = painterResource(R.drawable.hide_image),
              title = { Text(stringResource(R.string.hide_player_thumbnail)) },
              description = { Text(stringResource(R.string.hide_player_thumbnail_desc)) },
              trailingContent = {
                StageSwitch(checked = hidePlayerThumbnail, onCheckedChange = onHidePlayerThumbnailChange)
              },
              onClick = { onHidePlayerThumbnailChange(!hidePlayerThumbnail) }
            )
          else null,
          if (playerBackground != PlayerBackgroundStyle.APPLE_MUSIC)
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.thumbnail_corner_radius)),
              icon = painterResource(R.drawable.image),
              title = { Text(stringResource(R.string.thumbnail_corner_radius)) },
              description = { Text(stringResource(R.string.thumbnail_corner_radius_desc)) },
              trailingContent = {
                Text(
                  text = "${thumbnailCornerRadius.roundToInt()}dp",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              onClick = { showThumbnailCornerRadiusDialog = true }
            )
          else null,
          if (playerBackground != PlayerBackgroundStyle.APPLE_MUSIC)
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.crop_album_art)),
              icon = painterResource(R.drawable.crop),
              title = { Text(stringResource(R.string.crop_album_art)) },
              description = { Text(stringResource(R.string.crop_album_art_desc)) },
              trailingContent = {
                StageSwitch(checked = cropAlbumArt, onCheckedChange = onCropAlbumArtChange)
              },
              onClick = { onCropAlbumArtChange(!cropAlbumArt) }
            )
          else null,
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.player_buttons_style)),
            icon = painterResource(R.drawable.palette),
            title = { Text(stringResource(R.string.player_buttons_style)) },
            value = {
              Text(
                when (playerButtonsStyle) {
                  PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                  PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                  PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                }
              )
            },
            onClick = { showPlayerButtonsStyleDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.player_slider_style)),
            icon = painterResource(R.drawable.sliders),
            title = { Text(stringResource(R.string.player_slider_style)) },
            value = {
              Text(
                when (sliderStyle) {
                  SliderStyle.DEFAULT -> "Bar"
                  SliderStyle.WAVY ->
                    if (squigglySlider) stringResource(R.string.squiggly)
                    else stringResource(R.string.wavy)
                  SliderStyle.SLIM -> stringResource(R.string.slim)
                }
              )
            },
            onClick = { showSliderOptionDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.wavy_play_pause)),
            icon = painterResource(R.drawable.play),
            title = { Text(stringResource(R.string.wavy_play_pause)) },
            description = { Text(stringResource(R.string.wavy_play_pause_desc)) },
            trailingContent = {
              StageSwitch(checked = wavyPlayPause, onCheckedChange = onWavyPlayPauseChange)
            },
            onClick = { onWavyPlayPauseChange(!wavyPlayPause) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.enable_swipe_thumbnail)),
            icon = painterResource(R.drawable.swipe),
            title = { Text(stringResource(R.string.enable_swipe_thumbnail)) },
            description = { Text(stringResource(R.string.enable_swipe_thumbnail_desc)) },
            trailingContent = {
              StageSwitch(checked = swipeThumbnail, onCheckedChange = onSwipeThumbnailChange)
            },
            onClick = { onSwipeThumbnailChange(!swipeThumbnail) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.echomusic_canvas)),
            icon = painterResource(R.drawable.ic_canvas),
            title = { Text(stringResource(R.string.echomusic_canvas)) },
            description = { Text(stringResource(R.string.echomusic_canvas_desc)) },
            trailingContent = {
              StageSwitch(checked = canvasThumbnailAnimation, onCheckedChange = onCanvasThumbnailAnimationChange)
            },
            onClick = { onCanvasThumbnailAnimationChange(!canvasThumbnailAnimation) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.rotating_thumbnail)),
            icon = painterResource(R.drawable.image),
            title = { Text(stringResource(R.string.rotating_thumbnail)) },
            description = { Text(stringResource(R.string.rotating_thumbnail_desc)) },
            trailingContent = {
              StageSwitch(checked = rotatingThumbnail, onCheckedChange = onRotatingThumbnailChange)
            },
            onClick = { onRotatingThumbnailChange(!rotatingThumbnail) }
          ),
          // Show comment button hidden
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_comment_button)),
            icon = painterResource(R.drawable.chat_msg),
            title = { Text(stringResource(R.string.show_comment_button)) },
            description = { Text(stringResource(R.string.show_comment_button_description)) },
            trailingContent = {
              StageSwitch(checked = showCommentButton, onCheckedChange = onShowCommentButtonChange)
            },
            onClick = { onShowCommentButtonChange(!showCommentButton) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == "Show codec on player"),
            icon = painterResource(R.drawable.info),
            title = { Text("Show codec on player") },
            description = { Text("Display audio codec information below the timeline") },
            trailingContent = {
              StageSwitch(checked = showCodecOnPlayer, onCheckedChange = onShowCodecOnPlayerChange)
            }
          ),
          if (swipeThumbnail)
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.swipe_sensitivity)),
              icon = painterResource(R.drawable.tune),
              title = { Text(stringResource(R.string.swipe_sensitivity)) },
              value = {
                Text(
                  stringResource(
                    R.string.sensitivity_percentage,
                    (swipeSensitivity * 100).roundToInt()
                  )
                )
              },
              onClick = { showSensitivityDialog = true }
            )
          else null
        )
    )
    }

    if (showSensitivityDialog) {
      var tempSensitivity by remember { mutableFloatStateOf(swipeSensitivity) }

      DefaultDialog(
        onDismiss = {
          tempSensitivity = swipeSensitivity
          showSensitivityDialog = false
        },
        buttons = {
        StagePillButton(onClick = {
              onSwipeSensitivityChange(tempSensitivity)
              showSensitivityDialog = false
            }) {
            Text(stringResource(android.R.string.ok))
          }
        StagePillButton(onClick = {
              tempSensitivity = swipeSensitivity
              showSensitivityDialog = false
            }, primary = false) {
            Text(stringResource(android.R.string.cancel))
          }
        StagePillButton(onClick = { tempSensitivity = 0.73f }, primary = false) { Text(stringResource(R.string.reset)) }
      }
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = stringResource(R.string.swipe_sensitivity),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          Text(
            text =
              stringResource(R.string.sensitivity_percentage, (tempSensitivity * 100).roundToInt()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          Slider(
            value = tempSensitivity,
            onValueChange = { tempSensitivity = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    if (showThumbnailCornerRadiusDialog) {
      ThumbnailCornerRadiusModal(
        initialRadius = thumbnailCornerRadius,
        onDismiss = { showThumbnailCornerRadiusDialog = false },
        onRadiusSelected = { radius ->
          onThumbnailCornerRadiusChange(radius)
          showThumbnailCornerRadiusDialog = false
        }
      )
    }

    if (inSection("lyrics")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.lyrics),
      items =
        listOfNotNull(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_text_position)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_text_position)) },
            value = {
              Text(
                when (lyricsPosition) {
                  LyricsPosition.LEFT -> stringResource(R.string.left)
                  LyricsPosition.CENTER -> stringResource(R.string.center)
                  LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
              )
            },
            onClick = { showLyricsPositionDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_animation_style)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_animation_style)) },
            value = {
              Text(
                when (lyricsAnimationStyle) {
                  LyricsAnimationStyle.NONE -> stringResource(R.string.none)
                  LyricsAnimationStyle.FADE -> stringResource(R.string.fade)
                  LyricsAnimationStyle.GLOW -> stringResource(R.string.glow)
                  LyricsAnimationStyle.SLIDE -> stringResource(R.string.slide)
                  LyricsAnimationStyle.KARAOKE -> stringResource(R.string.karaoke)
                  LyricsAnimationStyle.echomusic_1 -> stringResource(R.string.echomusic_1)
                  LyricsAnimationStyle.APPLE -> stringResource(R.string.apple_music_style)
                  LyricsAnimationStyle.APPLE_V2 -> stringResource(R.string.apple_music_style_letter)
                  LyricsAnimationStyle.LYRICS_V2 -> stringResource(R.string.lyrics_v2_fluid)
                  LyricsAnimationStyle.METRO_LYRICS ->
                    stringResource(R.string.lyrics_animation_metro)
                }
              )
            },
            onClick = { showLyricsAnimationStyleDialog = true }
          ),
          if (lyricsAnimationStyle == LyricsAnimationStyle.echomusic_1) {
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.apple_music_lyrics_blur)),
              icon = painterResource(R.drawable.lyrics),
              title = { Text(stringResource(R.string.apple_music_lyrics_blur)) },
              description = { Text(stringResource(R.string.apple_music_lyrics_blur_desc)) },
              trailingContent = {
                StageSwitch(checked = appleMusicLyricsBlur, onCheckedChange = onAppleMusicLyricsBlurChange)
              },
              onClick = { onAppleMusicLyricsBlurChange(!appleMusicLyricsBlur) }
            )
          } else null,
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.standard_lyrics_blur)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.standard_lyrics_blur)) },
            description = { Text(stringResource(R.string.apple_music_lyrics_blur_desc)) },
            trailingContent = {
              StageSwitch(checked = lyricsStandardBlur, onCheckedChange = onLyricsStandardBlurChange)
            },
            onClick = { onLyricsStandardBlurChange(!lyricsStandardBlur) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_text_size)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_text_size)) },
            value = { Text("${lyricsTextSize.roundToInt()} sp") },
            onClick = { showLyricsTextSizeDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_line_spacing)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_line_spacing)) },
            value = { Text("${String.format("%.1f", lyricsLineSpacing)}x") },
            onClick = { showLyricsLineSpacingDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_click_change)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_click_change)) },
            description = { Text(stringResource(R.string.lyrics_click_change_desc)) },
            trailingContent = {
              StageSwitch(checked = lyricsClick, onCheckedChange = onLyricsClickChange)
            },
            onClick = { onLyricsClickChange(!lyricsClick) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_auto_scroll)),
            icon = painterResource(R.drawable.lyrics),
            title = { Text(stringResource(R.string.lyrics_auto_scroll)) },
            description = { Text(stringResource(R.string.lyrics_auto_scroll_desc)) },
            trailingContent = {
              StageSwitch(checked = lyricsScroll, onCheckedChange = onLyricsScrollChange)
            },
            onClick = { onLyricsScrollChange(!lyricsScroll) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_swipe_to_change_song)),
            icon = painterResource(R.drawable.swipe),
            title = { Text(stringResource(R.string.lyrics_swipe_to_change_song)) },
            description = { Text(stringResource(R.string.lyrics_swipe_to_change_song_desc)) },
            trailingContent = {
              StageSwitch(checked = swipeLyrics, onCheckedChange = onSwipeLyricsChange)
            },
            onClick = { onSwipeLyricsChange(!swipeLyrics) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.lyrics_thumbnail_play_pause)),
            icon = painterResource(R.drawable.play),
            title = { Text(stringResource(R.string.lyrics_thumbnail_play_pause)) },
            description = { Text(stringResource(R.string.lyrics_thumbnail_play_pause_desc)) },
            trailingContent = {
              StageSwitch(checked = enableLyricsThumbnailPlayPause, onCheckedChange = onEnableLyricsThumbnailPlayPauseChange)
            },
            onClick = { onEnableLyricsThumbnailPlayPauseChange(!enableLyricsThumbnailPlayPause) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.hide_status_bar_on_fullscreen)),
            icon = painterResource(R.drawable.fullscreen),
            title = { Text(stringResource(R.string.hide_status_bar_on_fullscreen)) },
            description = { Text(stringResource(R.string.hide_status_bar_on_fullscreen_desc)) },
            trailingContent = {
              StageSwitch(checked = hideStatusBarOnFullscreen, onCheckedChange = onHideStatusBarOnFullscreenChange)
            },
            onClick = { onHideStatusBarOnFullscreenChange(!hideStatusBarOnFullscreen) }
          )
        )
    )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (inSection("misc")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.misc),
      items =
        listOfNotNull(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.default_open_tab)),
            icon = painterResource(R.drawable.nav_bar),
            title = { Text(stringResource(R.string.default_open_tab)) },
            value = {
              Text(
                when (defaultOpenTab) {
                  NavigationTab.HOME -> stringResource(R.string.home)
                  NavigationTab.SEARCH -> stringResource(R.string.search)
                  NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                }
              )
            },
            onClick = { showDefaultOpenTabDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.swipe_song_to_add)),
            icon = painterResource(R.drawable.swipe),
            title = { Text(stringResource(R.string.swipe_song_to_add)) },
            description = { Text(stringResource(R.string.swipe_song_to_add_desc)) },
            trailingContent = {
              StageSwitch(checked = swipeToSong, onCheckedChange = onSwipeToSongChange)
            },
            onClick = { onSwipeToSongChange(!swipeToSong) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.haptics)),
            icon = painterResource(R.drawable.vibration),
            title = { Text(stringResource(R.string.haptics)) },
            description = { Text(stringResource(R.string.haptics_desc)) },
            onClick = { navController.navigate("settings/appearance/haptics") }
          ),

          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.swipe_song_to_remove)),
            icon = painterResource(R.drawable.swipe),
            title = { Text(stringResource(R.string.swipe_song_to_remove)) },
            description = { Text(stringResource(R.string.swipe_song_to_remove_desc)) },
            trailingContent = {
              StageSwitch(checked = swipeToRemoveSong, onCheckedChange = onSwipeToRemoveSongChange)
            },
            onClick = { onSwipeToRemoveSongChange(!swipeToRemoveSong) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.grid_cell_size)),
            icon = painterResource(R.drawable.grid_view),
            title = { Text(stringResource(R.string.grid_cell_size)) },
            value = {
              Text(
                when (gridItemSize) {
                  GridItemSize.BIG -> stringResource(R.string.big)
                  GridItemSize.SMALL -> stringResource(R.string.small)
                }
              )
            },
            onClick = { showGridSizeDialog = true }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.display_density)),
            icon = painterResource(R.drawable.grid_view),
            title = { Text(stringResource(R.string.display_density)) },
            value = { Text(DensityScale.fromValue(densityScale).label) },
            onClick = { showDensityScaleDialog = true }
          )
        )
    )
    }

    if (inSection("auto")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.auto_playlists),
      items =
        listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_liked_playlist)),
            icon = painterResource(R.drawable.favorite),
            title = { Text(stringResource(R.string.show_liked_playlist)) },
            description = { Text(stringResource(R.string.show_liked_playlist_desc)) },
            trailingContent = {
              StageSwitch(checked = showLikedPlaylist, onCheckedChange = onShowLikedPlaylistChange)
            },
            onClick = { onShowLikedPlaylistChange(!showLikedPlaylist) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_downloaded_playlist)),
            icon = painterResource(R.drawable.offline),
            title = { Text(stringResource(R.string.show_downloaded_playlist)) },
            description = { Text(stringResource(R.string.show_downloaded_playlist_desc)) },
            trailingContent = {
              StageSwitch(checked = showDownloadedPlaylist, onCheckedChange = onShowDownloadedPlaylistChange)
            },
            onClick = { onShowDownloadedPlaylistChange(!showDownloadedPlaylist) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.action_exported)),
            icon = painterResource(R.drawable.download),
            title = { Text(stringResource(R.string.action_exported)) },
            description = { Text(stringResource(R.string.action_exported_desc)) },
            trailingContent = {
              StageSwitch(checked = showExportedPlaylist, onCheckedChange = onShowExportedPlaylistChange)
            },
            onClick = { onShowExportedPlaylistChange(!showExportedPlaylist) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_top_playlist)),
            icon = painterResource(R.drawable.trending_up),
            title = { Text(stringResource(R.string.show_top_playlist)) },
            description = { Text(stringResource(R.string.show_top_playlist_desc)) },
            trailingContent = {
              StageSwitch(checked = showTopPlaylist, onCheckedChange = onShowTopPlaylistChange)
            },
            onClick = { onShowTopPlaylistChange(!showTopPlaylist) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_bottom_playlist)),
            icon = painterResource(R.drawable.trending_down),
            title = { Text(stringResource(R.string.show_bottom_playlist)) },
            description = { Text(stringResource(R.string.show_bottom_playlist_desc)) },
            trailingContent = {
              StageSwitch(checked = showBottomPlaylist, onCheckedChange = onShowBottomPlaylistChange)
            },
            onClick = { onShowBottomPlaylistChange(!showBottomPlaylist) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.show_cached_playlist)),
            icon = painterResource(R.drawable.cached),
            title = { Text(stringResource(R.string.show_cached_playlist)) },
            description = { Text(stringResource(R.string.show_cached_playlist_desc)) },
            trailingContent = {
              StageSwitch(checked = showCachedPlaylist, onCheckedChange = onShowCachedPlaylistChange)
            },
            onClick = { onShowCachedPlaylistChange(!showCachedPlaylist) }
          )
        )
    )
    }
    Spacer(modifier = Modifier.height(16.dp))

    Spacer(
      Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
      )
    )
  }

  StageTopBar(
    title = { Text(stringResource(R.string.appearance)) },
    navigationIcon = {
      IconButton(onClick = { if (openSection != null) openSection = null else navController.navigateUp() }, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
      }
    }
)
}

enum class DarkMode {
  ON,
  OFF,
  AUTO,
}

enum class NavigationTab {
  HOME,
  SEARCH,
  LIBRARY,
}

enum class LyricsPosition {
  LEFT,
  CENTER,
  RIGHT,
}
