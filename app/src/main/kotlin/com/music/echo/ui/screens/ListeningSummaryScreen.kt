package com.music.echo.ui.screens

import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.echo.ui.components.ExpressiveBarChart
import com.music.echo.viewmodels.DayUsageData
import com.music.echo.viewmodels.ListeningSummaryViewModel
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StageSectionHeader
import echo.music.iad1tya.ui.component.StageValueRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningSummaryScreen(
  navController: NavController,
  viewModel: ListeningSummaryViewModel = hiltViewModel()
) {
  val weekTotalMs by viewModel.weekTotalMs.collectAsState()
  val weekDailyData by viewModel.weekDailyData.collectAsState()
  val vibeSummary by viewModel.vibeSummary.collectAsState()
  val weekOffset by viewModel.weekOffset.collectAsState()

  fun formatMs(ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
  }

  val weekLabel =
    remember(weekOffset) {
      val monday = LocalDate.now().minusWeeks(weekOffset.toLong()).with(java.time.DayOfWeek.MONDAY)
      val format = java.time.format.DateTimeFormatter.ofPattern("d MMM")
      "${monday.format(format)} to ${monday.plusDays(6).format(format)}"
    }

  var showDatePicker by remember { mutableStateOf(false) }

  // Hoist the state outside the conditional so switching to "year picker" mode doesn't destroy the
  // state
  val datePickerState = rememberDatePickerState()

  if (showDatePicker) {
    LaunchedEffect(Unit) {
      val initial =
        LocalDate.now()
          .minusWeeks(weekOffset.toLong())
          .atStartOfDay()
          .toInstant(ZoneOffset.UTC)
          .toEpochMilli()
      datePickerState.selectedDateMillis = initial
    }

    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { viewModel.setWeekFromEpoch(it) }
            showDatePicker = false
          }
        ) {
          Text("OK")
        }
      },
      dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
    ) {
      DatePicker(state = datePickerState, showModeToggle = false)
    }
  }

  Scaffold(
    topBar = {
      StageTopBar(
        title = { StageBarTitle("Listening Summary") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painter = painterResource(id = R.drawable.arrow_back), contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showDatePicker = true }) {
            Icon(
              painter = painterResource(id = R.drawable.ic_calendar_month),
              contentDescription = "Select Date",
            )
          }
        },
      )
    }
  ) { paddingValues ->
    val days =
      weekDailyData.ifEmpty {
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
          .map { DayUsageData(it, 0L, 0L, 0L, 0L, 0L) }
      }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 6.dp, end = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(onClick = { viewModel.goToPreviousWeek() }) {
            Icon(
              painter = painterResource(id = R.drawable.ic_nav_chevron_left),
              contentDescription = "Previous week",
              modifier = Modifier.size(24.dp),
            )
          }
          Text(text = weekLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          IconButton(onClick = { viewModel.goToNextWeek() }) {
            Icon(
              painter = painterResource(id = R.drawable.ic_nav_chevron_right),
              contentDescription = "Next week",
              modifier = Modifier.size(24.dp),
              tint =
                if (viewModel.isCurrentWeek()) {
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
            )
          }
        }
      }

      item {
        if (vibeSummary.dominantVibePlayTime > 0 || vibeSummary.dominantVibe == "\u2728 Diverse") {
          Column(
            verticalArrangement = Arrangement.Bottom,
            modifier =
              Modifier.padding(start = 18.dp, top = 4.dp, end = 18.dp)
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .background(
                  Brush.linearGradient(
                    0f to Color(0xFFFF8A5C),
                    0.55f to Color(0xFFD9486A),
                    1f to Color(0xFF512A72),
                  ),
                  RoundedCornerShape(28.dp),
                )
                .padding(22.dp),
          ) {
            Text(
              text = "Your Music Vibe This Week",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            Text(
              text = vibeSummary.dominantVibe,
              fontSize = 32.sp,
              lineHeight = 35.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              modifier = Modifier.padding(vertical = 4.dp),
            )
            Text(
              text = "Total listened ${formatMs(weekTotalMs)}",
              fontSize = 15.sp,
              color = Color.White,
            )
          }
        }
      }

      item {
        val longest = (days.maxOfOrNull { it.totalMs } ?: 0L).coerceAtLeast(1L)
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier =
            Modifier.padding(start = 22.dp, top = 12.dp, end = 22.dp).fillMaxWidth().height(120.dp),
        ) {
          days.forEach { day ->
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
              modifier =
                Modifier.weight(1f).fillMaxHeight().clickable {
                  if (day.timestamp > 0L) {
                    navController.navigate("detailed_listening_history/${day.timestamp}")
                  }
                },
            ) {
              val fraction = (day.totalMs.toFloat() / longest).coerceIn(0f, 1f)
              Box(
                modifier =
                  Modifier.fillMaxWidth()
                    .height(96.dp * fraction + 2.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
              )
              Text(
                text = day.dayName.take(1),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      item { StageSectionHeader(title = "Listening breakdown") }

      item {
        val expandedStates = remember(days.size) { days.map { mutableStateOf(false) } }

        Column(
          modifier =
            Modifier.padding(horizontal = 18.dp)
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
          days.forEachIndexed { index, dayData ->
            val isExpanded = expandedStates[index].value
            StageValueRow(
              title = dayData.dayName,
              value = formatMs(dayData.totalMs),
              modifier =
                Modifier.clickable { expandedStates[index].value = !expandedStates[index].value },
              trailing = {
                Icon(
                  painter =
                    painterResource(
                      id = if (isExpanded) R.drawable.expand_less else R.drawable.expand_more
                    ),
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              },
            )

            AnimatedVisibility(
              visible = isExpanded,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut(),
            ) {
              Column(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, bottom = 6.dp)) {
                listOf(
                    "Songs" to formatMs(dayData.songsMs),
                    "Artists" to formatMs(dayData.artistsMs),
                    "Albums" to formatMs(dayData.albumsMs),
                  )
                  .forEach { (label, value) -> StageValueRow(title = label, value = value) }
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(60.dp)) }
    }
  }
}
