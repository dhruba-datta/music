package echo.music.iad1tya.echomusic.updater

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.StageButton
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface UpdateStep {
  data object Checking : UpdateStep

  data object UpToDate : UpdateStep

  data class Available(val release: SelfUpdater.Release) : UpdateStep

  data class Downloading(val release: SelfUpdater.Release, val received: Long, val total: Long) :
    UpdateStep

  data class NeedsPermission(val apk: File) : UpdateStep

  data object Installing : UpdateStep

  data class Confirm(val intent: Intent) : UpdateStep

  data class Failed(val message: String, val retry: () -> Unit) : UpdateStep
}

private fun megabytes(bytes: Long) = "%.1f".format(bytes / 1_048_576.0)

/**
 * Walks through checking, downloading and installing in one pop-up. It starts from [release] when
 * the startup check already found one, and checks GitHub itself otherwise.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfUpdateDialog(release: SelfUpdater.Release? = null, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var step by remember {
    mutableStateOf(if (release != null) UpdateStep.Available(release) else UpdateStep.Checking)
  }

  fun install(apk: File) {
    if (
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        !context.packageManager.canRequestPackageInstalls()
    ) {
      step = UpdateStep.NeedsPermission(apk)
      return
    }
    step = UpdateStep.Installing
    scope.launch {
      try {
        withContext(Dispatchers.IO) { SelfUpdater.install(context.applicationContext, apk) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        step = UpdateStep.Failed("Android could not start the install (${e.message}).") { install(apk) }
      }
    }
  }

  fun download(target: SelfUpdater.Release) {
    step = UpdateStep.Downloading(target, 0, target.size)
    scope.launch {
      try {
        val apk =
          withContext(Dispatchers.IO) {
            SelfUpdater.download(context, target) { received, total ->
              step = UpdateStep.Downloading(target, received, total)
            }
          }
        install(apk)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        step =
          UpdateStep.Failed("The download stopped (${e.message}). Check your connection and try again.") {
            download(target)
          }
      }
    }
  }

  fun check() {
    step = UpdateStep.Checking
    scope.launch {
      try {
        val latest = withContext(Dispatchers.IO) { SelfUpdater.findUpdate(context) }
        step = if (latest != null) UpdateStep.Available(latest) else UpdateStep.UpToDate
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        step = UpdateStep.Failed("Could not reach GitHub. Check your connection and try again.") { check() }
      }
    }
  }

  LaunchedEffect(Unit) {
    if (release == null) check()
    SelfUpdater.installEvents.collect { event ->
      when (event) {
        null -> Unit
        is SelfUpdater.InstallEvent.NeedsConfirmation -> {
          step = UpdateStep.Confirm(event.intent)
          context.startActivity(event.intent)
          SelfUpdater.installEvents.value = null
        }
        is SelfUpdater.InstallEvent.Failed -> {
          step = UpdateStep.Failed("The install did not finish (${event.message}).") { check() }
          SelfUpdater.installEvents.value = null
        }
      }
    }
  }

  val busy = step is UpdateStep.Downloading || step is UpdateStep.Installing
  fun later(target: SelfUpdater.Release) {
    SelfUpdater.snooze(context, target)
    onDismiss()
  }

  BasicAlertDialog(onDismissRequest = { if (!busy) onDismiss() }) {
    Surface(
      modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = titleOf(step),
          fontSize = 22.sp,
          lineHeight = 28.sp,
          fontWeight = FontWeight.ExtraBold,
        )

        when (val current = step) {
          UpdateStep.Checking ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
              UpdateNote("Looking for a newer version of Music…")
            }
          UpdateStep.UpToDate -> UpdateNote("Music ${BuildConfig.VERSION_NAME} is the latest version.")
          is UpdateStep.Available -> {
            ReleaseSummary(current.release)
            UpdateNote("${megabytes(current.release.size)} MB download. Your data stays as it is.")
          }
          is UpdateStep.Downloading -> {
            val fraction =
              if (current.total > 0) current.received.toFloat() / current.total else 0f
            ReleaseSummary(current.release)
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
              Box(
                modifier =
                  Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
              )
            }
            UpdateNote(
              "${megabytes(current.received)} of ${megabytes(current.total)} MB · " +
                "${(fraction * 100).toInt()}%. Keep Music open until it finishes."
            )
          }
          is UpdateStep.NeedsPermission ->
            UpdateNote("Android needs your permission once: switch on \"Allow from this source\" for Music, then come back and tap Continue.")
          UpdateStep.Installing -> {
            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
              trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            UpdateNote("Music closes while it updates. Open it again if it doesn't come back by itself.")
          }
          is UpdateStep.Confirm ->
            UpdateNote("Android's installer is open: tap Update. Music closes while it updates.")
          is UpdateStep.Failed -> UpdateNote(current.message)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          when (val current = step) {
            UpdateStep.Checking -> StageButton("Cancel", onClick = onDismiss, primary = false)
            UpdateStep.UpToDate -> StageButton("OK", onClick = onDismiss)
            is UpdateStep.Available -> {
              StageButton("Update", onClick = { download(current.release) })
              StageButton("Later", onClick = { later(current.release) }, primary = false)
            }
            is UpdateStep.NeedsPermission -> {
              StageButton("Continue", onClick = { install(current.apk) })
              StageButton(
                text = "Open Settings",
                onClick = {
                  context.startActivity(
                    Intent(
                      Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                      Uri.parse("package:${context.packageName}"),
                    )
                  )
                },
                primary = false,
              )
            }
            is UpdateStep.Confirm ->
              StageButton("Open Installer", onClick = { context.startActivity(current.intent) })
            is UpdateStep.Failed -> {
              StageButton("Try Again", onClick = current.retry)
              StageButton("Close", onClick = onDismiss, primary = false)
            }
            is UpdateStep.Downloading,
            UpdateStep.Installing -> Unit
          }
        }
      }
    }
  }
}

/** The installed version and the notes of the new one. */
@Composable
private fun ReleaseSummary(release: SelfUpdater.Release) {
  Text(
    text = "You have ${BuildConfig.VERSION_NAME}.",
    fontSize = 14.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
  if (release.notes.isNotBlank()) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())
    ) {
      Text(text = "What's New", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      UpdateNote(release.notes)
    }
  }
}

@Composable
private fun UpdateNote(text: String) {
  Text(
    text = text,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

private fun titleOf(step: UpdateStep): String =
  when (step) {
    UpdateStep.Checking -> "Checking for Updates"
    UpdateStep.UpToDate -> "You're Up to Date"
    is UpdateStep.Available -> "Version ${step.release.tag.removePrefix("v")} Is Ready"
    is UpdateStep.Downloading -> "Version ${step.release.tag.removePrefix("v")} Is Ready"
    is UpdateStep.NeedsPermission -> "Allow Installs"
    UpdateStep.Installing -> "Installing Update"
    is UpdateStep.Confirm -> "Tap Update to Finish"
    is UpdateStep.Failed -> "Something Didn't Work"
  }
