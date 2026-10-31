@file:OptIn(ExperimentalMaterial3Api::class)

package echo.music.iad1tya.ui.screens.generate

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.ui.component.StageRowValue
import echo.music.iad1tya.ui.component.StageTopBar
import echo.music.iad1tya.viewmodels.GenerateUiState
import echo.music.iad1tya.viewmodels.GenerateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GenerateScreen(
  navController: NavController,
  viewModel: GenerateViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val stats by viewModel.statsCount.collectAsStateWithLifecycle()
  val exclusions by viewModel.exclusionCount.collectAsStateWithLifecycle()
  val database = LocalDatabase.current
  val coroutineScope = rememberCoroutineScope()

  val background = MaterialTheme.colorScheme.background

  Scaffold(
    topBar = {
      StageTopBar(
        title = { StageBarTitle("Local Taste Engine") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              painter = painterResource(R.drawable.arrow_back),
              contentDescription = "Back",
            )
          }
        },
        containerColor = background,
      )
    },
    containerColor = background,
    contentWindowInsets = LocalPlayerAwareWindowInsets.current,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(bottom = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (val state = uiState) {
        is GenerateUiState.Idle -> {
          TasteState(
            title = "Smart Taste Mix",
            message = "A personal mix from your listening. On-device, offline, no account.",
            discColor = TasteDiscColor,
            topPadding = 34.dp,
            discSize = 120.dp,
          ) {
            Icon(
              painter = painterResource(R.drawable.stage_sparkle),
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(50.dp),
            )
          }
          Box(modifier = Modifier.padding(horizontal = 18.dp)) {
            Material3SettingsGroup(
              items = listOf(
                Material3SettingsItem(
                  title = { Text("Track Profile") },
                  description = { Text(if (stats.isNotEmpty()) "Active" else "Building") },
                  showChevron = false,
                ),
                Material3SettingsItem(
                  title = { Text("Exclusions") },
                  value = { StageRowValue("${exclusions.size} hidden") },
                  showChevron = false,
                ),
                Material3SettingsItem(
                  title = { Text("How it works") },
                  description = { Text("Uses listening time, completion and skips") },
                  showChevron = false,
                ),
              ),
            )
          }
          StageButton(
            text = "Generate Taste Mix",
            onClick = { viewModel.generate() },
            modifier = Modifier.padding(18.dp),
          )
        }

        is GenerateUiState.Generating -> {
          TasteState(title = "Smart Taste Mix", message = state.message) {
            CircularProgressIndicator(
              modifier = Modifier.size(44.dp),
              color = MaterialTheme.colorScheme.onSurface,
              strokeWidth = 3.dp,
            )
          }
          TasteActions {
            StageButton(
              text = "Cancel",
              onClick = { viewModel.cancelGeneration() },
              primary = false,
            )
          }
        }

        is GenerateUiState.Success -> {
          TasteState(
            title = "Taste Mix Ready!",
            message = "Your playlist has been saved to your library.",
          ) {
            TasteDiscIcon(R.drawable.check)
          }
          TasteActions {
            StageButton(
              text = "Open Playlist",
              onClick = { navController.navigate("local_playlist/${state.playlistId}") },
            )
            StageButton(
              text = "Generate Another",
              onClick = { viewModel.generate() },
              primary = false,
            )
          }
        }

        is GenerateUiState.EmptyHistory -> {
          TasteState(
            title = "Not Enough History",
            message = "Play a few songs first.",
          ) {
            TasteDiscIcon(R.drawable.music_note)
          }
          TasteActions {
            StageButton(
              text = "Explore Music",
              onClick = { navController.navigate("home") },
            )
          }
        }

        is GenerateUiState.AllExcluded -> {
          TasteState(
            title = "All Candidates Excluded",
            message = "All potential seed tracks are currently in your exclusion list.",
          ) {
            TasteDiscIcon(R.drawable.close)
          }
          TasteActions {
            StageButton(
              text = "Reset Exclusions & Retry",
              onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                  database.recommendationExclusionDao.clear()
                  viewModel.generate()
                }
              },
            )
          }
        }

        is GenerateUiState.Error -> {
          TasteState(title = "Generation Error", message = state.message) {
            TasteDiscIcon(R.drawable.close)
          }
          TasteActions {
            StageButton(text = "Retry", onClick = { viewModel.generate() })
          }
        }
      }
    }
  }
}

private val TasteDiscColor = Color(0xFF8B63D9)

/** The centred page head of the Stage state screens: a disc, a 23sp title and a short message. */
@Composable
private fun TasteState(
  title: String,
  message: String,
  discColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
  topPadding: Dp = 26.dp,
  discSize: Dp = 84.dp,
  disc: @Composable () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp, top = topPadding),
  ) {
    Box(
      modifier = Modifier
        .padding(bottom = 10.dp)
        .size(discSize)
        .background(discColor, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      disc()
    }
    Text(
      text = title,
      fontSize = 23.sp,
      lineHeight = 29.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )
    Text(
      text = message,
      fontSize = 15.sp,
      lineHeight = 21.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(bottom = 10.dp),
    )
  }
}

@Composable
private fun TasteDiscIcon(@DrawableRes icon: Int) {
  Icon(
    painter = painterResource(icon),
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.size(38.dp),
  )
}

/** The stacked pills under a state page. */
@Composable
private fun TasteActions(content: @Composable () -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
  ) {
    content()
  }
}
