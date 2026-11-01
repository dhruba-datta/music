package echo.music.iad1tya.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.component.StageChip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import echo.music.iad1tya.constants.BlockedArtistsKey
import echo.music.iad1tya.utils.dataStore
import echo.music.iad1tya.utils.rememberPreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedArtistsScreen(navController: NavController) {
  val context = LocalContext.current
  val blockedArtists by
    rememberPreference(key = BlockedArtistsKey, defaultValue = emptySet<String>())
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      StageTopBar(
        title = { Text("Blocked Artists") },
        navigationIcon = {
          IconButton(onClick = navController::navigateUp) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = stringResource(R.string.cd_back))
          }
        }
)
    }
  ) { padding ->
    if (blockedArtists.isEmpty()) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(padding)) {
        Text(
          text = "No blocked artists",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        Material3SettingsGroup(
          items =
            blockedArtists.toList().map { entry ->
              val name =
                if (entry.contains("||")) entry.substringAfter("||") else "Unknown Artist"
              Material3SettingsItem(
                title = { Text(name) },
                trailingContent = {
                  StageChip(
                    onClick = {
                      coroutineScope.launch {
                        context.dataStore.edit { prefs ->
                          val current = prefs[BlockedArtistsKey] ?: emptySet()
                          prefs[BlockedArtistsKey] = current - entry
                        }
                      }
                    }
                  ) {
                    Text("Unblock")
                  }
                },
              )
            }
        )
      }
    }
  }
}
