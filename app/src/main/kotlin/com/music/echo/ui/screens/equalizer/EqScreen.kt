package echo.music.iad1tya.ui.screens.equalizer

import echo.music.iad1tya.ui.component.StageAlertDialog
import echo.music.iad1tya.ui.component.StagePillButton
import android.annotation.SuppressLint
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.session.PlaybackState
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.ui.component.StageRadio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.utils.launchSafely
import echo.music.dsp.core.models.SavedEQProfile
import timber.log.Timber

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun EqScreen(
  navController: NavController? = null,
  viewModel: EQViewModel = hiltViewModel(),
  playbackState: PlaybackState? = null,
  onOpenFullEqualizer: (() -> Unit)? = null,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val playerConnection = LocalPlayerConnection.current

  var showError by remember { mutableStateOf<String?>(null) }

  val activityResultLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

  val filePickerLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
      if (uri != null) {
        try {
          val contentResolver = context.contentResolver

          var fileName = "custom_eq.txt"
          contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
              val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
              if (displayNameIndex >= 0) {
                val name = cursor.getString(displayNameIndex)
                if (!name.isNullOrBlank()) {
                  fileName = name
                }
              }
            }
          }

          val inputStream = contentResolver.openInputStream(uri)

          if (inputStream != null) {
            viewModel.importCustomProfile(
              fileName = fileName,
              inputStream = inputStream,
              onSuccess = { Timber.d("Custom EQ profile imported successfully: $fileName") },
              onError = { error ->
                Timber.d("Error: Unable to import Custom EQ profile: $fileName")
                showError = context.getString(R.string.import_error_title) + ": " + error.message
              }
            )
          } else {
            showError = context.getString(R.string.error_file_read)
          }
        } catch (e: Exception) {
          showError = context.getString(R.string.error_file_open, e.message)
        }
      }
    }

  EqScreenContent(
    profiles = state.profiles,
    activeProfileId = state.activeProfileId,
    onProfileSelected = { viewModel.selectProfile(it) },
    onImportCustomEQ = { filePickerLauncher.launchSafely("text/plain", context) },
    onOpenSystemEqualizer = {
      playerConnection?.let { connection ->
        val intent =
          Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, connection.player.audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
          }
        if (intent.resolveActivity(context.packageManager) != null) {
          activityResultLauncher.launch(intent)
        }
      }
    },
    onNavigateToAxion = {
      onOpenFullEqualizer?.invoke() ?: navController?.navigate("settings/equalizer")
    },
    onDeleteProfile = { viewModel.deleteProfile(it) }
  )

  if (showError != null) {
    StageAlertDialog(
      onDismissRequest = { showError = null },
      title = { Text(stringResource(R.string.import_error_title)) },
      text = { Text(showError ?: "") },
      confirmButton = {
        StagePillButton(onClick = { showError = null }) { Text(stringResource(android.R.string.ok)) }
      }
)
  }

  if (state.error != null) {
    StageAlertDialog(
      onDismissRequest = { viewModel.clearError() },
      title = { Text(stringResource(R.string.error_title)) },
      text = { Text(stringResource(R.string.error_eq_apply_failed, state.error ?: "")) },
      confirmButton = {
        StagePillButton(onClick = { viewModel.clearError() }) {
          Text(stringResource(android.R.string.ok))
        }
      }
)
  }
}

/** The Equalizer sheet opened from the player menu: presets here, every band on the full page. */
@Composable
fun StageQuickEqualizer(onOpenFullEqualizer: () -> Unit) {
  EqScreen(onOpenFullEqualizer = onOpenFullEqualizer)
}

@Composable
private fun EqScreenContent(
  profiles: List<SavedEQProfile>,
  activeProfileId: String?,
  onProfileSelected: (String?) -> Unit,
  onImportCustomEQ: () -> Unit,
  onOpenSystemEqualizer: () -> Unit,
  onNavigateToAxion: () -> Unit,
  onDeleteProfile: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = stringResource(R.string.equalizer),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 22.dp, top = 12.dp, end = 150.dp, bottom = 4.dp),
      )
      Row(modifier = Modifier.align(Alignment.TopEnd).padding(top = 1.dp, end = 10.dp)) {
        StageEqAction(R.drawable.tune, stringResource(R.string.echo_equalizer), onNavigateToAxion)
        StageEqAction(R.drawable.add, stringResource(R.string.import_profile), onImportCustomEQ)
        StageEqAction(
          R.drawable.equalizer,
          stringResource(R.string.system_equalizer),
          onOpenSystemEqualizer,
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 6.dp, end = 18.dp),
      contentPadding = PaddingValues(bottom = 18.dp),
    ) {
      item {
        StageEqRow(
          title = stringResource(R.string.eq_disabled),
          selected = activeProfileId == null,
          onSelected = { onProfileSelected(null) },
        )
      }

      val customProfiles = profiles.filter { it.isCustom }

      items(customProfiles) { profile ->
        EQProfileItem(
          profile = profile,
          isSelected = activeProfileId == profile.id,
          onSelected = { onProfileSelected(profile.id) },
          onDelete = { onDeleteProfile(profile.id) },
        )
      }

      if (customProfiles.isEmpty()) {
        item {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
          ) {
            Text(
              text = stringResource(R.string.no_profiles),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StageButton(text = stringResource(R.string.import_profile), onClick = onImportCustomEQ)
            StageButton(
              text = stringResource(R.string.system_equalizer),
              onClick = onOpenSystemEqualizer,
              primary = false,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StageEqAction(@DrawableRes icon: Int, description: String, onClick: () -> Unit) {
  IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
    Icon(painter = painterResource(icon), contentDescription = description)
  }
}

/** One preset row: a radio, its name, and room for a delete button. */
@Composable
private fun StageEqRow(
  title: String,
  selected: Boolean,
  onSelected: () -> Unit,
  trailing: (@Composable () -> Unit)? = null,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      Modifier.fillMaxWidth()
        .selectable(selected = selected, role = Role.RadioButton, onClick = onSelected)
        .heightIn(min = 54.dp)
        .padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    StageRadio(selected = selected)
    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    trailing?.invoke()
  }
}

@Composable
private fun EQProfileItem(
  profile: SavedEQProfile,
  isSelected: Boolean,
  onSelected: () -> Unit,
  onDelete: () -> Unit
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  StageEqRow(
    title = profile.deviceModel,
    selected = isSelected,
    onSelected = onSelected,
    trailing = {
      IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(44.dp)) {
        Icon(
          painter = painterResource(R.drawable.delete),
          contentDescription = stringResource(R.string.delete_profile_desc),
        )
      }
    },
  )

  if (showDeleteDialog) {
    StageAlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text(stringResource(R.string.delete_profile_desc)) },
      text = { Text(stringResource(R.string.delete_profile_confirmation, profile.name)) },
      confirmButton = {
        StageButton(
          text = stringResource(android.R.string.ok),
          onClick = {
            onDelete()
            showDeleteDialog = false
          },
        )
      },
      dismissButton = {
        StageButton(
          text = stringResource(android.R.string.cancel),
          onClick = { showDeleteDialog = false },
          primary = false,
        )
      },
    )
  }
}
