package echo.music.iad1tya.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StagePillButton
import echo.music.iad1tya.ui.utils.backToMain
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistoryScreen(navController: NavController) {
  val database = LocalDatabase.current
  val history by
    remember { database.searchHistory() }.collectAsState(initial = emptyList())

  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    DefaultDialog(
      onDismiss = { showClearDialog = false },
      content = {
        Text(
          text = stringResource(R.string.clear_search_history_confirm),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(horizontal = 18.dp),
        )
      },
      buttons = {
        StagePillButton(onClick = {
            showClearDialog = false
            database.query { clearSearchHistory() }
          }) {
          Text(text = stringResource(android.R.string.ok))
        }
        StagePillButton(onClick = { showClearDialog = false }, primary = false) {
          Text(text = stringResource(android.R.string.cancel))
        }
      },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { StageBarTitle(stringResource(R.string.search_history)) },
        navigationIcon = {
          IconButton(
            onClick = { navController.navigateUp() },
            onLongClick = { navController.backToMain() }
          ) {
            Icon(painter = painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        actions = {
          if (history.isNotEmpty()) {
            IconButton(onClick = { showClearDialog = true }) {
              Icon(
                painter = painterResource(R.drawable.clear_all),
                contentDescription = stringResource(R.string.clear_search_history)
              )
            }
          }
        }
      )
    }
  ) { paddingValues ->
    if (history.isEmpty()) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(paddingValues)
      ) {
        Text(
          text = stringResource(R.string.stage_no_searches),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
        contentPadding =
          LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues()
      ) {
        items(items = history, key = { it.id }) { search ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier =
              Modifier.fillMaxWidth()
                .clickable {
                  navController.navigate("search/${URLEncoder.encode(search.query, "UTF-8")}")
                }
                .padding(start = 18.dp, end = 8.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.stage_history),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = search.query,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { database.query { delete(search) } }) {
              Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = stringResource(R.string.remove_from_history)
              )
            }
          }
        }
      }
    }
  }
}
