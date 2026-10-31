package echo.music.iad1tya.ui.screens.recognition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.ListItemHeight
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.db.entities.RecognitionHistory
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StageSectionHeader
import echo.music.iad1tya.ui.utils.backToMain
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecognitionHistoryScreen(navController: NavController) {
  val database = LocalDatabase.current
  val menuState = LocalMenuState.current
  val coroutineScope = rememberCoroutineScope()

  val historyItems by database.recognitionHistory().collectAsState(initial = emptyList())
  var showClearDialog by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<RecognitionHistory?>(null) }

  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

  val filteredItems =
    remember(historyItems, query) {
      if (query.text.isEmpty()) historyItems
      else
        historyItems.filter { item ->
          item.title.contains(query.text, ignoreCase = true) ||
            item.artist.contains(query.text, ignoreCase = true)
        }
    }

  val groupedItems =
    remember(filteredItems) {
      val today = LocalDate.now()
      filteredItems.groupBy { item ->
        val date = item.recognizedAt.toLocalDate()
        when {
          date == today -> "Today"
          date == today.minusDays(1) -> "Yesterday"
          date >= today.minusDays(7) -> "This Week"
          else -> item.recognizedAt.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
      }
    }

  if (showClearDialog) {
    DefaultDialog(
      onDismiss = { showClearDialog = false },
      icon = { Icon(painter = painterResource(R.drawable.delete), contentDescription = null) },
      title = { Text(stringResource(R.string.clear_recognition_history)) },
      buttons = {
        TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.cancel)) }
        TextButton(
          onClick = {
            coroutineScope.launch(Dispatchers.IO) { database.query { clearRecognitionHistory() } }
            showClearDialog = false
          }
        ) {
          Text(stringResource(R.string.clear))
        }
      }
    ) {
      Text(
        text = stringResource(R.string.clear_recognition_history_confirm),
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

  itemToDelete?.let { item ->
    DefaultDialog(
      onDismiss = { itemToDelete = null },
      icon = { Icon(painter = painterResource(R.drawable.delete), contentDescription = null) },
      title = { Text(stringResource(R.string.delete)) },
      buttons = {
        TextButton(onClick = { itemToDelete = null }) { Text(stringResource(R.string.cancel)) }
        TextButton(
          onClick = {
            coroutineScope.launch(Dispatchers.IO) {
              database.query { deleteRecognitionHistoryById(item.id) }
            }
            itemToDelete = null
          }
        ) {
          Text(stringResource(R.string.delete))
        }
      }
    ) {
      Text(
        text = stringResource(R.string.delete_playlist_confirm, item.title),
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { StageBarTitle(stringResource(R.string.recognition_history)) },
        navigationIcon = {
          IconButton(
            onClick = { navController.navigateUp() },
            onLongClick = { navController.backToMain() }
          ) {
            Icon(painter = painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        actions = {
          if (historyItems.isNotEmpty()) {
            IconButton(onClick = { showClearDialog = true }) {
              Icon(
                painter = painterResource(R.drawable.clear_all),
                contentDescription = stringResource(R.string.clear_recognition_history)
              )
            }
          }
        }
      )
    }
  ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp)
      ) {
        BasicTextField(
          value = query,
          onValueChange = { query = it },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth().height(48.dp),
          decorationBox = { innerTextField ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.padding(start = 16.dp, end = 4.dp)
            ) {
              Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
              Box(modifier = Modifier.weight(1f)) {
                if (query.text.isEmpty()) {
                  Text(
                    text = stringResource(R.string.search_history),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                innerTextField()
              }
              if (query.text.isNotEmpty()) {
                IconButton(onClick = { query = TextFieldValue() }) {
                  Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        )
      }

      when {
        historyItems.isEmpty() -> {

          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                painter = painterResource(R.drawable.history),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No recognition history",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        filteredItems.isEmpty() -> {

          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No results for \"${query.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        else -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
              LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues()
          ) {
            if (query.text.isEmpty()) {

              groupedItems.forEach { (label, groupItems) ->
                stickyHeader(key = "header_$label") {
                  StageSectionHeader(
                    title = label,
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                  )
                }
                items(items = groupItems, key = { it.id }) { item ->
                  RecognitionHistoryItem(
                    item = item,
                    onClick = {
                      val searchQuery = "${item.title} ${item.artist}"
                      navController.navigate(
                        "search/${java.net.URLEncoder.encode(searchQuery, "UTF-8")}"
                      )
                    },
                    onDelete = { itemToDelete = item }
                  )
                }
              }
            } else {

              items(items = filteredItems, key = { it.id }) { item ->
                RecognitionHistoryItem(
                  item = item,
                  onClick = {
                    val searchQuery = "${item.title} ${item.artist}"
                    navController.navigate(
                      "search/${java.net.URLEncoder.encode(searchQuery, "UTF-8")}"
                    )
                  },
                  onDelete = { itemToDelete = item }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecognitionHistoryItem(
  item: RecognitionHistory,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val timeText =
    remember(item.recognizedAt) {
      val date = item.recognizedAt.toLocalDate()
      val today = LocalDate.now()
      val pattern = if (date >= today.minusDays(1)) "HH:mm" else "MMM dd, yyyy HH:mm"
      item.recognizedAt.format(DateTimeFormatter.ofPattern(pattern))
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = ListItemHeight)
        .clickable { onClick() }
        .padding(start = 22.dp, end = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp)
  ) {
    AsyncImage(
      model = item.coverArtUrl,
      contentDescription = null,
      modifier = Modifier.size(ListThumbnailSize).clip(RoundedCornerShape(ThumbnailCornerRadius)),
      contentScale = ContentScale.Crop
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.title,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = "${item.artist} · $timeText",
        fontSize = 13.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    IconButton(onClick = onDelete) {
      Icon(
        painter = painterResource(R.drawable.delete),
        contentDescription = stringResource(R.string.delete_from_history),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
