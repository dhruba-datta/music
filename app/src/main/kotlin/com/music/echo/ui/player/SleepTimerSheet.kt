package echo.music.iad1tya.ui.player

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.playback.SleepTimer
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.ui.component.StageSheetHandle
import echo.music.iad1tya.ui.component.StageSheetShape
import echo.music.iad1tya.utils.makeTimeString
import java.util.Date
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val TimerPresets = listOf(listOf(15, 30, 45), listOf(60, 90, 120))
private const val DefaultTimerMinutes = 30f
private const val MinTimerMinutes = 5f
private const val MaxTimerMinutes = 120f
private const val TimerStepMinutes = 5
private const val AddedMinutes = 10
private const val MillisPerMinute = 60_000.0

/** Sets the sleep timer or, while one is running, shows the time left. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(onDismiss: () -> Unit) {
  val sleepTimer = LocalPlayerConnection.current?.service?.sleepTimer ?: return

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = StageSheetShape,
    dragHandle = { StageSheetHandle() },
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 18.dp),
    ) {
      Text(
        text = stringResource(R.string.sleep_timer),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      if (sleepTimer.isActive) {
        RunningTimer(sleepTimer = sleepTimer, onDismiss = onDismiss)
      } else {
        TimerSetup(sleepTimer = sleepTimer, onDismiss = onDismiss)
      }
    }
  }
}

@Composable
private fun TimerSetup(sleepTimer: SleepTimer, onDismiss: () -> Unit) {
  var minutes by remember { mutableFloatStateOf(DefaultTimerMinutes) }

  TimerValue(text = minutes.roundToInt().toString())
  Text(
    text = stringResource(R.string.stage_timer_minutes),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )

  Spacer(Modifier.height(16.dp))
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    TimerPresets.forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        row.forEach { preset ->
          TimerPreset(
            minutes = preset,
            selected = minutes.roundToInt() == preset,
            onClick = { minutes = preset.toFloat() },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }

  Spacer(Modifier.height(8.dp))
  Slider(
    value = minutes,
    onValueChange = { minutes = it },
    valueRange = MinTimerMinutes..MaxTimerMinutes,
    steps = ((MaxTimerMinutes - MinTimerMinutes) / TimerStepMinutes).toInt() - 1,
  )

  Spacer(Modifier.height(8.dp))
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    StageButton(
      text = stringResource(R.string.end_of_song),
      primary = false,
      onClick = {
        sleepTimer.start(-1)
        onDismiss()
      },
      modifier = Modifier.weight(1f),
    )
    StageButton(
      text = stringResource(R.string.stage_timer_start),
      onClick = {
        sleepTimer.start(minutes.roundToInt())
        onDismiss()
      },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun TimerPreset(
  minutes: Int,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val hours = minutes / 60f
  val label =
    when {
      minutes < 60 -> stringResource(R.string.stage_timer_minutes_short, minutes)
      minutes % 60 == 0 -> stringResource(R.string.stage_timer_hours_short, hours.toInt().toString())
      else -> stringResource(R.string.stage_timer_hours_short, hours.toString())
    }

  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .height(44.dp)
        .clip(CircleShape)
        .background(if (selected) colors.onSurface else colors.onSurface.copy(alpha = 0.1f))
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = if (selected) colors.surface else colors.onSurface,
    )
  }
}

@Composable
private fun RunningTimer(sleepTimer: SleepTimer, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val triggerTime = sleepTimer.triggerTime
  val untilSongEnds = sleepTimer.pauseWhenSongEnd
  var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

  LaunchedEffect(triggerTime) {
    while (true) {
      now = System.currentTimeMillis()
      delay(1000)
    }
  }
  val timeLeft = (triggerTime - now).coerceAtLeast(0)

  Spacer(Modifier.height(28.dp))
  if (untilSongEnds) {
    TimerValue(text = stringResource(R.string.end_of_song))
  } else {
    TimerValue(text = makeTimeString(timeLeft))
    Text(
      text =
        stringResource(
          R.string.stage_timer_stops_at,
          DateFormat.getTimeFormat(context).format(Date(triggerTime)),
        ),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }

  Spacer(Modifier.height(32.dp))
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    if (!untilSongEnds) {
      StageButton(
        text = stringResource(R.string.stage_timer_add_minutes, AddedMinutes),
        primary = false,
        onClick = { sleepTimer.start(ceil(timeLeft / MillisPerMinute).toInt() + AddedMinutes) },
        modifier = Modifier.weight(1f),
      )
    }
    StageButton(
      text = stringResource(R.string.stage_timer_turn_off),
      onClick = {
        sleepTimer.clear()
        onDismiss()
      },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun TimerValue(text: String) {
  Text(
    text = text,
    style =
      LocalTextStyle.current.copy(
        fontSize = 44.sp,
        lineHeight = 50.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum",
      ),
    textAlign = TextAlign.Center,
    modifier = Modifier.padding(top = 12.dp),
  )
}
