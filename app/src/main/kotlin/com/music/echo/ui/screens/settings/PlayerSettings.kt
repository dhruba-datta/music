package echo.music.iad1tya.ui.screens.settings

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import echo.music.iad1tya.ui.component.StagePillButton
import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AudioNormalizationKey
import echo.music.iad1tya.constants.VarispeedKey
import echo.music.iad1tya.constants.AudioOffload
import echo.music.iad1tya.constants.AudioQuality
import echo.music.iad1tya.constants.AudioQualityKey
import echo.music.iad1tya.constants.AutoDownloadOnLikeKey
import echo.music.iad1tya.constants.AutoLoadMoreKey
import echo.music.iad1tya.constants.AutoSkipNextOnErrorKey
import echo.music.iad1tya.constants.AutomixCrossfadeKey
import echo.music.iad1tya.constants.AutomixDebugOverlayKey
import echo.music.iad1tya.constants.CrossfadeDurationKey
import echo.music.iad1tya.constants.CrossfadeEnabledKey
import echo.music.iad1tya.constants.CrossfadeGaplessKey
import echo.music.iad1tya.constants.DisableLoadMoreWhenRepeatAllKey
import echo.music.iad1tya.constants.DownloadOnWifiOnlyKey
import echo.music.iad1tya.constants.DownloadWithMetadataKey
import echo.music.iad1tya.constants.EnableBitPerfectUsbDacKey
import echo.music.iad1tya.constants.EnableExportAsMp3Key
import echo.music.iad1tya.constants.EnableGoogleCastKey
import echo.music.iad1tya.constants.UsbDacVolumeModeKey
import echo.music.iad1tya.constants.HistoryDuration
import echo.music.iad1tya.constants.KeepScreenOn
import echo.music.iad1tya.constants.PauseOnMute
import echo.music.iad1tya.constants.PersistentQueueKey
import echo.music.iad1tya.constants.PreloadLyricsEnabledKey
import echo.music.iad1tya.constants.PreloadNextSongEnabledKey
import echo.music.iad1tya.constants.PreloadNextSongLimitKey
import echo.music.iad1tya.constants.PreventDuplicateTracksInQueueKey
import echo.music.iad1tya.constants.RememberShuffleAndRepeatKey
import echo.music.iad1tya.constants.ResumeOnBluetoothConnectKey
import echo.music.iad1tya.constants.AutoplayOnAutomotiveConnectKey
import echo.music.iad1tya.constants.SeekExtraSeconds
import echo.music.iad1tya.constants.ShufflePlaylistFirstKey
import echo.music.iad1tya.constants.SimilarContent
import echo.music.iad1tya.constants.SkipSilenceInstantKey
import echo.music.iad1tya.constants.SkipSilenceKey
import echo.music.iad1tya.constants.StopMusicOnTaskClearKey
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.EnumDialog
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  val scrollState = androidx.compose.foundation.rememberScrollState()

  var openSection by rememberSaveable { mutableStateOf<String?>(null) }
  // A search result lands on the full page so its row can be scrolled to and highlighted.
  val inSection = { key: String? -> highlightKey != null || openSection == key }
  BackHandler(enabled = openSection != null) { openSection = null }
  LaunchedEffect(openSection) { scrollState.scrollTo(0) }

  val (audioQuality, onAudioQualityChange) =
    rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.OPUS)
  val (audioQualityWifi, onAudioQualityWifiChange) =
    rememberEnumPreference(
      echo.music.iad1tya.constants.AudioQualityWifiKey,
      defaultValue = AudioQuality.LOSSLESS
    )
  val (audioQualityCellular, onAudioQualityCellularChange) =
    rememberEnumPreference(
      echo.music.iad1tya.constants.AudioQualityCellularKey,
      defaultValue = AudioQuality.MEDIUM
    )

  val (crossfadeEnabled, onCrossfadeEnabledChange) =
    rememberPreference(CrossfadeEnabledKey, defaultValue = false)
  val (crossfadeDuration, onCrossfadeDurationChange) =
    rememberPreference(CrossfadeDurationKey, defaultValue = 5f)
  val (automixCrossfade, onAutomixCrossfadeChange) =
    rememberPreference(AutomixCrossfadeKey, defaultValue = false)
  val (automixDebugOverlay, onAutomixDebugOverlayChange) =
    rememberPreference(AutomixDebugOverlayKey, defaultValue = false)
  val (crossfadeGapless, onCrossfadeGaplessChange) =
    rememberPreference(CrossfadeGaplessKey, defaultValue = true)
  val (persistentQueue, onPersistentQueueChange) =
    rememberPreference(PersistentQueueKey, defaultValue = true)
  val (skipSilence, onSkipSilenceChange) = rememberPreference(SkipSilenceKey, defaultValue = false)
  val (skipSilenceInstant, onSkipSilenceInstantChange) =
    rememberPreference(SkipSilenceInstantKey, defaultValue = false)
  val (varispeed, onVarispeedChange) = rememberPreference(VarispeedKey, defaultValue = false)
  val (audioNormalization, onAudioNormalizationChange) =
    rememberPreference(AudioNormalizationKey, defaultValue = true)
  val (audioLoudnessPreset, onAudioLoudnessPresetChange) =
    rememberEnumPreference(
      echo.music.iad1tya.constants.AudioLoudnessPresetKey,
      defaultValue = echo.music.iad1tya.constants.AudioLoudnessPreset.NORMAL
    )
  var showLoudnessPresetDialog by remember { mutableStateOf(false) }

  val (spatialAudio, onSpatialAudioChange) =
    rememberPreference(echo.music.iad1tya.constants.SpatialAudioKey, defaultValue = false)

  val (audioOffload, onAudioOffloadChange) =
    rememberPreference(key = AudioOffload, defaultValue = false)

  val (enableBitPerfectUsbDac, onEnableBitPerfectUsbDacChange) =
    rememberPreference(key = EnableBitPerfectUsbDacKey, defaultValue = false)


  val (preloadNextSongEnabled, onPreloadNextSongEnabledChange) =
    rememberPreference(key = PreloadNextSongEnabledKey, defaultValue = true)

  val (preloadNextSongLimit, onPreloadNextSongLimitChange) =
    rememberPreference(key = PreloadNextSongLimitKey, defaultValue = 10)

  val (preloadLyricsEnabled, onPreloadLyricsEnabledChange) =
    rememberPreference(key = PreloadLyricsEnabledKey, defaultValue = true)

  val (dataSaverEnabled, onDataSaverEnabledChange) =
    rememberPreference(key = echo.music.iad1tya.constants.DataSaverEnabledKey, defaultValue = false)

  val (enableExportAsMp3, onEnableExportAsMp3Change) =
    rememberPreference(key = EnableExportAsMp3Key, defaultValue = false)

  val context = androidx.compose.ui.platform.LocalContext.current

  val (enableGoogleCast, onEnableGoogleCastChange) =
    rememberPreference(key = EnableGoogleCastKey, defaultValue = true)

  val (showSeekButtons, onShowSeekButtonsChange) =
    rememberPreference(echo.music.iad1tya.constants.ShowSeekButtonsKey, defaultValue = false)

  val (seekExtraSeconds, onSeekExtraSeconds) =
    rememberPreference(SeekExtraSeconds, defaultValue = false)

  val (autoLoadMore, onAutoLoadMoreChange) =
    rememberPreference(AutoLoadMoreKey, defaultValue = true)
  val (disableLoadMoreWhenRepeatAll, onDisableLoadMoreWhenRepeatAllChange) =
    rememberPreference(DisableLoadMoreWhenRepeatAllKey, defaultValue = false)
  val (autoDownloadOnLike, onAutoDownloadOnLikeChange) =
    rememberPreference(AutoDownloadOnLikeKey, defaultValue = false)
  val (downloadOnWifiOnly, onDownloadOnWifiOnlyChange) =
    rememberPreference(DownloadOnWifiOnlyKey, defaultValue = false)
  val (downloadWithMetadata, onDownloadWithMetadataChange) =
    rememberPreference(DownloadWithMetadataKey, defaultValue = true)
  val (similarContentEnabled, similarContentEnabledChange) =
    rememberPreference(key = SimilarContent, defaultValue = true)
  val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) =
    rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)
  val (rememberShuffleAndRepeat, onRememberShuffleAndRepeatChange) =
    rememberPreference(RememberShuffleAndRepeatKey, defaultValue = true)
  val (shufflePlaylistFirst, onShufflePlaylistFirstChange) =
    rememberPreference(ShufflePlaylistFirstKey, defaultValue = false)
  val (preventDuplicateTracksInQueue, onPreventDuplicateTracksInQueueChange) =
    rememberPreference(PreventDuplicateTracksInQueueKey, defaultValue = false)
  val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) =
    rememberPreference(StopMusicOnTaskClearKey, defaultValue = true)
  val (pauseOnMute, onPauseOnMuteChange) = rememberPreference(PauseOnMute, defaultValue = false)
  val (resumeOnBluetoothConnect, onResumeOnBluetoothConnectChange) =
    rememberPreference(ResumeOnBluetoothConnectKey, defaultValue = false)
  val (autoplayOnAutomotiveConnect, onAutoplayOnAutomotiveConnectChange) =
    rememberPreference(AutoplayOnAutomotiveConnectKey, defaultValue = false)
  val (keepScreenOn, onKeepScreenOnChange) = rememberPreference(KeepScreenOn, defaultValue = false)
  val (historyDuration, onHistoryDurationChange) =
    rememberPreference(HistoryDuration, defaultValue = 1f)

  var showAudioQualityWifiDialog by remember { mutableStateOf(false) }
  var showAudioQualityCellularDialog by remember { mutableStateOf(false) }
  var showDownloadQualityDialog by remember { mutableStateOf(false) }
  var showPlaybackEngineDialog by remember { mutableStateOf(false) }

  val (playbackEngine, onPlaybackEngineChange) =
    rememberEnumPreference(
      echo.music.iad1tya.constants.PlaybackEngineKey,
      defaultValue = echo.music.iad1tya.constants.PlaybackEngine.AUTO
    )

  val (downloadQuality, onDownloadQualityChange) =
    rememberEnumPreference(
      echo.music.iad1tya.constants.DownloadQualityKey,
      defaultValue = echo.music.iad1tya.constants.DownloadQuality.YOUTUBE
    )

  if (showAudioQualityWifiDialog) {
    EnumDialog(
      onDismiss = { showAudioQualityWifiDialog = false },
      onSelect = {
        onAudioQualityWifiChange(it)
        showAudioQualityWifiDialog = false
      },
      title = stringResource(R.string.audio_quality_wifi),
      current = audioQualityWifi,
      values = listOf(
        AudioQuality.LOSSLESS,
        AudioQuality.HIGH,
        AudioQuality.MEDIUM,
        AudioQuality.LOW,
      ),
      valueText = { it.label },
      valueDescription = { "${it.detail} (${it.hourly})" }
    )
  }

  if (showAudioQualityCellularDialog) {
    EnumDialog(
      onDismiss = { showAudioQualityCellularDialog = false },
      onSelect = {
        onAudioQualityCellularChange(it)
        showAudioQualityCellularDialog = false
      },
      title = stringResource(R.string.audio_quality_cellular),
      current = audioQualityCellular,
      values = listOf(
        AudioQuality.MEDIUM,
        AudioQuality.LOW,
        AudioQuality.HIGH,
        AudioQuality.LOSSLESS,
      ),
      valueText = { it.label },
      valueDescription = { "${it.detail} (${it.hourly})" }
    )
  }

  if (showDownloadQualityDialog) {
    EnumDialog(
      onDismiss = { showDownloadQualityDialog = false },
      onSelect = {
        onDownloadQualityChange(it)
        showDownloadQualityDialog = false
      },
      title = stringResource(R.string.download_quality_title),
      current = downloadQuality,
      values = listOf(echo.music.iad1tya.constants.DownloadQuality.YOUTUBE),
      valueText = {
        when (it) {
          echo.music.iad1tya.constants.DownloadQuality.YOUTUBE -> "YouTube Music (AAC/Default)"
          else -> ""
        }
      }
    )
  }

  if (showLoudnessPresetDialog) {
    EnumDialog(
      onDismiss = { showLoudnessPresetDialog = false },
      onSelect = {
        onAudioLoudnessPresetChange(it)
        showLoudnessPresetDialog = false
      },
      title = stringResource(R.string.audio_loudness_preset),
      current = audioLoudnessPreset,
      values =
        listOf(
          echo.music.iad1tya.constants.AudioLoudnessPreset.QUIET,
          echo.music.iad1tya.constants.AudioLoudnessPreset.NORMAL,
          echo.music.iad1tya.constants.AudioLoudnessPreset.LOUD,
          echo.music.iad1tya.constants.AudioLoudnessPreset.AGGRESSIVE
        ),
      valueText = {
        when (it) {
          echo.music.iad1tya.constants.AudioLoudnessPreset.QUIET ->
            stringResource(R.string.loudness_preset_quiet)
          echo.music.iad1tya.constants.AudioLoudnessPreset.NORMAL ->
            stringResource(R.string.loudness_preset_normal)
          echo.music.iad1tya.constants.AudioLoudnessPreset.LOUD ->
            stringResource(R.string.loudness_preset_loud)
          echo.music.iad1tya.constants.AudioLoudnessPreset.AGGRESSIVE ->
            stringResource(R.string.loudness_preset_aggressive)
        }
      }
    )
  }

  if (showPlaybackEngineDialog) {
    EnumDialog(
      onDismiss = { showPlaybackEngineDialog = false },
      onSelect = {
        onPlaybackEngineChange(it)
        echo.music.iad1tya.utils.YTPlayerUtils.playbackEngine = it
        showPlaybackEngineDialog = false
      },
      title = "Playback Engine",
      current = playbackEngine,
      values =
        listOf(
          echo.music.iad1tya.constants.PlaybackEngine.POTOKEN,
          echo.music.iad1tya.constants.PlaybackEngine.BRAVEPIPE,
          echo.music.iad1tya.constants.PlaybackEngine.AUTO
        ),
      valueText = {
        when (it) {
          echo.music.iad1tya.constants.PlaybackEngine.POTOKEN -> "eXtended InnerTube"
          echo.music.iad1tya.constants.PlaybackEngine.BRAVEPIPE -> "BravePipe (NewPipe)"
          echo.music.iad1tya.constants.PlaybackEngine.AUTO -> "Auto (Try Both)"
        }
      },
      valueDescription = {
        when (it) {
          echo.music.iad1tya.constants.PlaybackEngine.POTOKEN ->
            "Uses WebView PoToken + CipherDeobfuscator. Most reliable and future-proof."
          echo.music.iad1tya.constants.PlaybackEngine.BRAVEPIPE ->
            "Uses NewPipe extractor for stream resolution. Lightweight but may break with YouTube updates."
          echo.music.iad1tya.constants.PlaybackEngine.AUTO ->
            "Tries PoToken first, falls back to BravePipe if it fails."
        }
      }
    )
  }

  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
      )
      .verticalScroll(scrollState)
      .padding(horizontal = 18.dp)
  ) {
    var showCrossfadeBetaDialog by remember { mutableStateOf(false) }

    if (showCrossfadeBetaDialog) {
      DefaultDialog(
        onDismiss = { showCrossfadeBetaDialog = false },
        title = { Text(stringResource(R.string.crossfade_beta_title)) },
        buttons = {
        StagePillButton(onClick = {
              showCrossfadeBetaDialog = false
              onCrossfadeEnabledChange(true)
            }) {
            Text(stringResource(R.string.enable))
          }
        StagePillButton(onClick = { showCrossfadeBetaDialog = false }, primary = false) {
            Text(stringResource(R.string.cancel))
          }
      }
      ) {
        Text(stringResource(R.string.crossfade_beta_message))
      }
    }

    Spacer(
      Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    )


    if (inSection("data")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = "Data Saver",
      items =
        buildList {
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Data Saver Mode (Beta)"),
              icon = painterResource(R.drawable.offline),
              title = { Text("Data Saver Mode (Beta)") },
              description = {
                Text(
                  "Disable lyrics, videos, preloading, background syncs, and force Opus audio to save data."
                )
              },
              trailingContent = {
                StageSwitch(checked = dataSaverEnabled, onCheckedChange = onDataSaverEnabledChange)
              },
              onClick = { onDataSaverEnabledChange(!dataSaverEnabled) }
            )
          )
        }
    )
    }


    if (inSection(null)) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.player),
      items =
        buildList {
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_quality_wifi)),
              icon = painterResource(R.drawable.wifi_proxy),
              title = { Text(stringResource(R.string.audio_quality_wifi)) },
              description = {
                Text("${audioQualityWifi.label} · ${audioQualityWifi.detail}")
              },
              onClick = { showAudioQualityWifiDialog = true }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_quality_cellular)),
              icon = painterResource(R.drawable.network_node),
              title = { Text(stringResource(R.string.audio_quality_cellular)) },
              description = {
                Text("${audioQualityCellular.label} · ${audioQualityCellular.detail} (${audioQualityCellular.hourly})")
              },
              onClick = { showAudioQualityCellularDialog = true }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.download_quality_title)),
              icon = painterResource(R.drawable.download),
              title = { Text(stringResource(R.string.download_quality_title)) },
              value = {
                Text(
                  when (downloadQuality) {
                    echo.music.iad1tya.constants.DownloadQuality.YOUTUBE ->
                      "YouTube Music (AAC/Default)"
                    else -> "YouTube Music (AAC/Default)"
                  }
                )
              },
              onClick = { showDownloadQualityDialog = true }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.crossfade)),
              icon = painterResource(R.drawable.linear_scale),
              title = { Text(stringResource(R.string.crossfade)) },
              description = { Text(stringResource(R.string.crossfade_desc)) },
              showBadge = true,
              trailingContent = {
                StageSwitch(checked = crossfadeEnabled, onCheckedChange = {
                    if (!crossfadeEnabled) {
                      showCrossfadeBetaDialog = true
                    } else {
                      onCrossfadeEnabledChange(false)
                      onAutomixCrossfadeChange(false)
                    }
                  })
              },
              onClick = {
                if (!crossfadeEnabled) {
                  showCrossfadeBetaDialog = true
                } else {
                  onCrossfadeEnabledChange(false)
                  onAutomixCrossfadeChange(false)
                }
              }
            )
          )
          if (crossfadeEnabled) {
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.crossfade_duration)),
                icon = painterResource(R.drawable.timer),
                title = { Text(stringResource(R.string.crossfade_duration)) },
                description = {
                  Column {
                    Text(
                      pluralStringResource(
                        R.plurals.seconds,
                        crossfadeDuration.toInt(),
                        crossfadeDuration.toInt()
                      )
                    )
                    Slider(
                      value = crossfadeDuration,
                      onValueChange = onCrossfadeDurationChange,
                      valueRange = 1f..15f,
                      steps = 14
                    )
                  }
                }
              )
            )
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.crossfade_gapless)),
                icon = painterResource(R.drawable.album),
                title = { Text(stringResource(R.string.crossfade_gapless)) },
                description = { Text(stringResource(R.string.crossfade_gapless_desc)) },
                trailingContent = {
                  StageSwitch(checked = crossfadeGapless, onCheckedChange = onCrossfadeGaplessChange)
                },
                onClick = { onCrossfadeGaplessChange(!crossfadeGapless) }
              )
            )
          }
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.skip_silence)),
              icon = painterResource(R.drawable.fast_forward),
              title = { Text(stringResource(R.string.skip_silence)) },
              description = { Text(stringResource(R.string.skip_silence_desc)) },
              trailingContent = {
                StageSwitch(checked = skipSilence, onCheckedChange = onSkipSilenceChange)
              },
              onClick = { onSkipSilenceChange(!skipSilence) }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_normalization)),
              icon = painterResource(R.drawable.volume_up),
              title = { Text(stringResource(R.string.audio_normalization)) },
              description = { Text(stringResource(R.string.audio_normalization_desc)) },
              trailingContent = {
                StageSwitch(checked = audioNormalization, onCheckedChange = onAudioNormalizationChange)
              },
              onClick = { onAudioNormalizationChange(!audioNormalization) }
            )
          )
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.echo_equalizer)),
              icon = painterResource(R.drawable.echoequlizer),
              title = { Text(stringResource(R.string.echo_equalizer)) },
              description = { Text(stringResource(R.string.echo_equalizer_desc)) },
              onClick = { navController.navigate("settings/equalizer") }
            )
          )
        }
    )
    }


    if (inSection(null)) {
      Material3SettingsGroup(
        scrollState = scrollState,
        title = stringResource(R.string.stage_more),
        items =
          buildList {
            if (highlightKey == null && openSection == null) {
              add(Material3SettingsItem(
            title = { Text("Data Saver") },
            onClick = { openSection = "data" },
          ))
              add(Material3SettingsItem(
            title = { Text(stringResource(R.string.queue)) },
            description = { Text(stringResource(R.string.stage_player_queue_desc)) },
            onClick = { openSection = "queue" },
          ))
              add(Material3SettingsItem(
            title = { Text(stringResource(R.string.misc)) },
            description = { Text(stringResource(R.string.stage_player_misc_desc)) },
            onClick = { openSection = "misc" },
          ))
            }
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.show_seek_buttons)),
              icon = painterResource(R.drawable.fast_forward),
              title = { Text(stringResource(R.string.show_seek_buttons)) },
              description = { Text(stringResource(R.string.show_seek_buttons_desc)) },
              trailingContent = {
                StageSwitch(checked = showSeekButtons, onCheckedChange = onShowSeekButtonsChange)
              },
              onClick = { onShowSeekButtonsChange(!showSeekButtons) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Download with metadata"),
              icon = painterResource(R.drawable.download),
              title = { Text("Download with metadata") },
              description = { Text("Downloads lyrics when downloading a song") },
              trailingContent = {
                StageSwitch(checked = downloadWithMetadata, onCheckedChange = onDownloadWithMetadataChange)
              },
              onClick = { onDownloadWithMetadataChange(!downloadWithMetadata) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.download_on_wifi_only)),
              icon = painterResource(R.drawable.download),
              title = { Text(stringResource(R.string.download_on_wifi_only)) },
              description = { Text(stringResource(R.string.download_on_wifi_only_desc)) },
              trailingContent = {
                StageSwitch(checked = downloadOnWifiOnly, onCheckedChange = onDownloadOnWifiOnlyChange)
              },
              onClick = { onDownloadOnWifiOnlyChange(!downloadOnWifiOnly) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = false,
              icon = painterResource(R.drawable.tune),
              title = { Text("Playback Engine") },
              value = {
                Text(
                  when (playbackEngine) {
                    echo.music.iad1tya.constants.PlaybackEngine.POTOKEN -> "eXtended InnerTube"
                    echo.music.iad1tya.constants.PlaybackEngine.BRAVEPIPE -> "BravePipe (NewPipe)"
                    echo.music.iad1tya.constants.PlaybackEngine.AUTO -> "Auto (Try Both)"
                  }
                )
              },
              onClick = { showPlaybackEngineDialog = true }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = highlightKey == stringResource(R.string.automix),
              icon = painterResource(R.drawable.graphic_eq),
              title = { Text(stringResource(R.string.automix)) },
              description = { Text(stringResource(R.string.automix_desc)) },
              trailingContent = {
                StageSwitch(checked = automixCrossfade, onCheckedChange = {
                    onAutomixCrossfadeChange(it)
                    if (it) {
                      onCrossfadeEnabledChange(true)
                    } else {
                      onCrossfadeEnabledChange(false)
                    }
                  })
              },
              onClick = {
                val newValue = !automixCrossfade
                onAutomixCrossfadeChange(newValue)
                if (newValue) {
                  onCrossfadeEnabledChange(true)
                } else {
                  onCrossfadeEnabledChange(false)
                }
              }
            )
          )
            if (automixCrossfade) {
            add(
              Material3SettingsItem(
                isHighlighted = highlightKey == stringResource(R.string.automix_debug),
                icon = painterResource(R.drawable.bug_report),
                title = { Text(stringResource(R.string.automix_debug)) },
                description = { Text(stringResource(R.string.automix_debug_desc)) },
                trailingContent = {
                  StageSwitch(checked = automixDebugOverlay, onCheckedChange = onAutomixDebugOverlayChange)
                },
                onClick = { onAutomixDebugOverlayChange(!automixDebugOverlay) }
              )
            )
          }
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Bit-Perfect USB-DAC"),
              icon = painterResource(R.drawable.volume_up),
              title = { Text("Bit-Perfect USB-DAC Output") },
              description = { Text("Direct UAC1/UAC2 driver bypassing Android OS mixer") },
              trailingContent = {
                StageSwitch(checked = enableBitPerfectUsbDac, onCheckedChange = onEnableBitPerfectUsbDacChange)
              },
              onClick = { onEnableBitPerfectUsbDacChange(!enableBitPerfectUsbDac) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.history_duration)),
              icon = painterResource(R.drawable.history),
              title = { Text(stringResource(R.string.history_duration)) },
              description = {
                Slider(
                  value = historyDuration,
                  onValueChange = { onHistoryDurationChange(it.roundToInt().toFloat()) },
                  valueRange = 1f..100f,
                  steps = 9
                )
              },
              trailingContent = { Text(text = historyDuration.roundToInt().toString()) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.skip_silence_instant)),
              icon = painterResource(R.drawable.skip_next),
              title = { Text(stringResource(R.string.skip_silence_instant)) },
              description = { Text(stringResource(R.string.skip_silence_instant_desc)) },
              trailingContent = {
                StageSwitch(checked = skipSilenceInstant, onCheckedChange = { onSkipSilenceInstantChange(it) }, enabled = skipSilence)
              },
              onClick = { if (skipSilence) onSkipSilenceInstantChange(!skipSilenceInstant) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.varispeed)),
              icon = painterResource(R.drawable.speed),
              title = { Text(stringResource(R.string.varispeed)) },
              description = { Text(stringResource(R.string.varispeed_description)) },
              trailingContent = {
                StageSwitch(checked = varispeed, onCheckedChange = onVarispeedChange)
              },
              onClick = { onVarispeedChange(!varispeed) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_loudness_preset)),
              icon = painterResource(R.drawable.volume_up),
              title = { Text(stringResource(R.string.audio_loudness_preset)) },
              value = {
                Text(
                  when (audioLoudnessPreset) {
                    echo.music.iad1tya.constants.AudioLoudnessPreset.QUIET ->
                      stringResource(R.string.loudness_preset_quiet)
                    echo.music.iad1tya.constants.AudioLoudnessPreset.NORMAL ->
                      stringResource(R.string.loudness_preset_normal)
                    echo.music.iad1tya.constants.AudioLoudnessPreset.LOUD ->
                      stringResource(R.string.loudness_preset_loud)
                    echo.music.iad1tya.constants.AudioLoudnessPreset.AGGRESSIVE ->
                      stringResource(R.string.loudness_preset_aggressive)
                  }
                )
              },
              onClick = { showLoudnessPresetDialog = true }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.spatial_audio)),
              icon = painterResource(R.drawable.graphic_eq),
              title = { Text(stringResource(R.string.spatial_audio)) },
              description = { Text(stringResource(R.string.spatial_audio_desc)) },
              trailingContent = {
                StageSwitch(checked = spatialAudio, onCheckedChange = onSpatialAudioChange)
              },
              onClick = { onSpatialAudioChange(!spatialAudio) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_offload)),
              icon = painterResource(R.drawable.graphic_eq),
              title = { Text(stringResource(R.string.audio_offload)) },
              description = {
                Text(
                  if (crossfadeEnabled) stringResource(R.string.audio_offload_disabled_by_crossfade)
                  else stringResource(R.string.audio_offload_description)
                )
              },
              trailingContent = {
                StageSwitch(checked = if (crossfadeEnabled) false else audioOffload, onCheckedChange = onAudioOffloadChange, enabled = !crossfadeEnabled)
              },
              onClick = { if (!crossfadeEnabled) onAudioOffloadChange(!audioOffload) }
            )
          )
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Preload Next Song"),
              icon = painterResource(R.drawable.skip_next),
              title = { Text("Preload Next Song") },
              description = { Text("Cache the next song for gapless playback") },
              trailingContent = {
                StageSwitch(checked = preloadNextSongEnabled, onCheckedChange = onPreloadNextSongEnabledChange)
              },
              onClick = { onPreloadNextSongEnabledChange(!preloadNextSongEnabled) }
            )
          )
            if (preloadNextSongEnabled) {
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == "Preload Limit"),
                icon = painterResource(R.drawable.library_music),
                title = { Text("Preload Limit") },
                description = {
                  Slider(
                    value = preloadNextSongLimit.toFloat(),
                    onValueChange = { onPreloadNextSongLimitChange(it.roundToInt()) },
                    valueRange = 1f..10f,
                    steps = 9
                  )
                },
                trailingContent = { Text(text = preloadNextSongLimit.toString()) }
              )
            )

            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == "Preload Lyrics"),
                icon = painterResource(R.drawable.queue_music),
                title = { Text("Preload Lyrics") },
                description = { Text("Also cache lyrics for the preloaded songs") },
                trailingContent = {
                  StageSwitch(checked = preloadLyricsEnabled, onCheckedChange = onPreloadLyricsEnabledChange)
                },
                onClick = { onPreloadLyricsEnabledChange(!preloadLyricsEnabled) }
              )
            )
          }
            if (BuildConfig.CAST_AVAILABLE) {
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.google_cast)),
                icon = painterResource(R.drawable.cast),
                title = { Text(stringResource(R.string.google_cast)) },
                description = { Text(stringResource(R.string.google_cast_description)) },
                trailingContent = {
                  StageSwitch(checked = enableGoogleCast, onCheckedChange = onEnableGoogleCastChange)
                },
                onClick = { onEnableGoogleCastChange(!enableGoogleCast) }
              )
            )
          }
            add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.seek_seconds_addup)),
              icon = painterResource(R.drawable.arrow_forward),
              title = { Text(stringResource(R.string.seek_seconds_addup)) },
              description = { Text(stringResource(R.string.seek_seconds_addup_description)) },
              trailingContent = {
                StageSwitch(checked = seekExtraSeconds, onCheckedChange = onSeekExtraSeconds)
              },
              onClick = { onSeekExtraSeconds(!seekExtraSeconds) }
            )
          )
          }
      )
    }

    if (inSection("queue")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.queue),
      items =
        listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.persistent_queue)),
            icon = painterResource(R.drawable.queue_music),
            title = { Text(stringResource(R.string.persistent_queue)) },
            description = { Text(stringResource(R.string.persistent_queue_desc)) },
            trailingContent = {
              StageSwitch(checked = persistentQueue, onCheckedChange = onPersistentQueueChange)
            },
            onClick = { onPersistentQueueChange(!persistentQueue) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_load_more)),
            icon = painterResource(R.drawable.playlist_add),
            title = { Text(stringResource(R.string.auto_load_more)) },
            description = { Text(stringResource(R.string.auto_load_more_desc)) },
            trailingContent = {
              StageSwitch(checked = autoLoadMore, onCheckedChange = onAutoLoadMoreChange)
            },
            onClick = { onAutoLoadMoreChange(!autoLoadMore) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_download_on_like)),
            icon = painterResource(R.drawable.download),
            title = { Text(stringResource(R.string.auto_download_on_like)) },
            description = { Text(stringResource(R.string.auto_download_on_like_desc)) },
            trailingContent = {
              StageSwitch(checked = autoDownloadOnLike, onCheckedChange = onAutoDownloadOnLikeChange)
            },
            onClick = { onAutoDownloadOnLikeChange(!autoDownloadOnLike) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.enable_similar_content)),
            icon = painterResource(R.drawable.similar),
            title = { Text(stringResource(R.string.enable_similar_content)) },
            description = { Text(stringResource(R.string.similar_content_desc)) },
            trailingContent = {
              StageSwitch(checked = similarContentEnabled, onCheckedChange = similarContentEnabledChange)
            },
            onClick = { similarContentEnabledChange(!similarContentEnabled) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.remember_shuffle_and_repeat)),
            icon = painterResource(R.drawable.shuffle),
            title = { Text(stringResource(R.string.remember_shuffle_and_repeat)) },
            description = { Text(stringResource(R.string.remember_shuffle_and_repeat_desc)) },
            trailingContent = {
              StageSwitch(checked = rememberShuffleAndRepeat, onCheckedChange = onRememberShuffleAndRepeatChange)
            },
            onClick = { onRememberShuffleAndRepeatChange(!rememberShuffleAndRepeat) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.shuffle_playlist_first)),
            icon = painterResource(R.drawable.shuffle),
            title = { Text(stringResource(R.string.shuffle_playlist_first)) },
            description = { Text(stringResource(R.string.shuffle_playlist_first_desc)) },
            trailingContent = {
              StageSwitch(checked = shufflePlaylistFirst, onCheckedChange = onShufflePlaylistFirstChange)
            },
            onClick = { onShufflePlaylistFirstChange(!shufflePlaylistFirst) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.prevent_duplicate_tracks_in_queue)),
            icon = painterResource(R.drawable.queue_music),
            title = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue)) },
            description = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue_desc)) },
            trailingContent = {
              StageSwitch(checked = preventDuplicateTracksInQueue, onCheckedChange = onPreventDuplicateTracksInQueueChange)
            },
            onClick = { onPreventDuplicateTracksInQueueChange(!preventDuplicateTracksInQueue) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_skip_next_on_error)),
            icon = painterResource(R.drawable.skip_next),
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = { Text(stringResource(R.string.auto_skip_next_on_error_desc)) },
            trailingContent = {
              StageSwitch(checked = autoSkipNextOnError, onCheckedChange = onAutoSkipNextOnErrorChange)
            },
            onClick = { onAutoSkipNextOnErrorChange(!autoSkipNextOnError) }
          )
        )
    )
    }


    if (inSection("misc")) {
    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.misc),
      items =
        listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.stop_music_on_task_clear)),
            icon = painterResource(R.drawable.clear_all),
            title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
            description = { Text(stringResource(R.string.stop_music_on_task_clear_desc)) },
            trailingContent = {
              StageSwitch(checked = stopMusicOnTaskClear, onCheckedChange = onStopMusicOnTaskClearChange)
            },
            onClick = { onStopMusicOnTaskClearChange(!stopMusicOnTaskClear) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.pause_music_when_media_is_muted)),
            icon = painterResource(R.drawable.volume_off_pause),
            title = { Text(stringResource(R.string.pause_music_when_media_is_muted)) },
            description = { Text(stringResource(R.string.pause_music_when_media_is_muted_desc)) },
            trailingContent = {
              StageSwitch(checked = pauseOnMute, onCheckedChange = onPauseOnMuteChange)
            },
            onClick = { onPauseOnMuteChange(!pauseOnMute) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.resume_on_bluetooth_connect)),
            icon = painterResource(R.drawable.bluetooth),
            title = { Text(stringResource(R.string.resume_on_bluetooth_connect)) },
            description = { Text(stringResource(R.string.resume_on_bluetooth_connect_desc)) },
            trailingContent = {
              StageSwitch(checked = resumeOnBluetoothConnect, onCheckedChange = onResumeOnBluetoothConnectChange)
            },
            onClick = { onResumeOnBluetoothConnectChange(!resumeOnBluetoothConnect) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.autoplay_on_automotive_connect)),
            icon = painterResource(R.drawable.auto_play),
            title = { Text(stringResource(R.string.autoplay_on_automotive_connect)) },
            description = { Text(stringResource(R.string.autoplay_on_automotive_connect_desc)) },
            trailingContent = {
              StageSwitch(checked = autoplayOnAutomotiveConnect, onCheckedChange = onAutoplayOnAutomotiveConnectChange)
            },
            onClick = { onAutoplayOnAutomotiveConnectChange(!autoplayOnAutomotiveConnect) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.keep_screen_on_when_player_is_expanded)),
            icon = painterResource(R.drawable.screenshot),
            title = { Text(stringResource(R.string.keep_screen_on_when_player_is_expanded)) },
            description = {
              Text(stringResource(R.string.keep_screen_on_when_player_is_expanded_desc))
            },
            trailingContent = {
              StageSwitch(checked = keepScreenOn, onCheckedChange = onKeepScreenOnChange)
            },
            onClick = { onKeepScreenOnChange(!keepScreenOn) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.export_desc)),
            icon = painterResource(R.drawable.file_export),
            title = { Text(stringResource(R.string.export_desc)) },
            description = { Text("Show 'Export as MP3' in menus") },
            trailingContent = {
              StageSwitch(checked = enableExportAsMp3, onCheckedChange = onEnableExportAsMp3Change)
            },
            onClick = { onEnableExportAsMp3Change(!enableExportAsMp3) }
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
    title = { Text(stringResource(R.string.player_and_audio)) },
    navigationIcon = {
      IconButton(onClick = { if (openSection != null) openSection = null else navController.navigateUp() }, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
      }
    }
)
}
