package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AudioQuality
import echo.music.iad1tya.constants.AudioQualityKey
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.UseLoginForBrowse
import echo.music.iad1tya.constants.YtmSyncKey
import echo.music.iad1tya.ui.component.StageSheetHandle
import echo.music.iad1tya.ui.component.StageSheetRow
import echo.music.iad1tya.ui.component.StageSheetShape
import echo.music.iad1tya.ui.component.StageSheetTitle
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialoge(
  onDismissRequest: () -> Unit,
  onNavigate: (String) -> Unit,
  homeViewModel: HomeViewModel
) {
  val (audioQuality) = rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.OPUS)
  val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
  val isLoggedIn =
    remember(innerTubeCookie) {
      innerTubeCookie.isNotEmpty() && "SAPISID" in parseCookieString(innerTubeCookie)
    }

  val accountName by homeViewModel.accountName.collectAsState()
  val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

  val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)
  val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, true)

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    shape = StageSheetShape,
    dragHandle = { StageSheetHandle() },
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    StageSheetTitle(if (isLoggedIn) accountName else "Anonymous")

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 12.dp)
    ) {
      StageSheetRow(
        text = stringResource(R.string.account),
        icon = R.drawable.stage_account,
        onClick = {
          onDismissRequest()
          if (isLoggedIn) onNavigate("settings/account") else onNavigate("login")
        },
        trailing = {
          if (isLoggedIn && !accountImageUrl.isNullOrBlank()) {
            AsyncImage(
              model = accountImageUrl,
              contentDescription = "Profile Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.size(30.dp).clip(CircleShape)
            )
          }
        }
      )
      StageSheetRow(
        text = stringResource(R.string.ai_lyrics_translation),
        icon = R.drawable.stage_ai,
        onClick = {
          onDismissRequest()
          onNavigate("settings/ai")
        }
      )
      StageSheetRow(
        text = stringResource(R.string.settings),
        icon = R.drawable.stage_settings,
        onClick = {
          onDismissRequest()
          onNavigate("settings")
        }
      )
      StageSheetRow(
        text = "Extensions",
        icon = R.drawable.extension,
        onClick = {
          onDismissRequest()
          onNavigate("settings/extensions")
        }
      )

      if (isLoggedIn) {
        StageSheetRow(
          text = "Use Account for Browsing",
          icon = R.drawable.add_circle,
          onClick = {
            val newVal = !useLoginForBrowse
            com.music.innertube.YouTube.useLoginForBrowse = newVal
            onUseLoginForBrowseChange(newVal)
          },
          trailing = {
            Switch(
              checked = useLoginForBrowse,
              onCheckedChange = {
                com.music.innertube.YouTube.useLoginForBrowse = it
                onUseLoginForBrowseChange(it)
              },
              colors = stageSwitchColors()
            )
          }
        )
        StageSheetRow(
          text = "YouTube Music Sync",
          icon = R.drawable.cached,
          onClick = { onYtmSyncChange(!ytmSync) },
          trailing = {
            Switch(
              checked = ytmSync,
              onCheckedChange = onYtmSyncChange,
              colors = stageSwitchColors()
            )
          }
        )
      }

      StageSheetRow(
        text = "About",
        icon = R.drawable.info,
        onClick = {
          onDismissRequest()
          onNavigate("settings/about")
        },
        trailing = {
          Text(
            BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      )
    }
  }
}

@Composable
private fun stageSwitchColors() =
  SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.surface,
    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
  )
