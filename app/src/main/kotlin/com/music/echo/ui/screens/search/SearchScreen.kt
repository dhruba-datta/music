package echo.music.iad1tya.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.utils.YouTubeUrlParser
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalIsPlayerExpanded
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.PauseSearchHistoryKey
import echo.music.iad1tya.db.entities.SearchHistory
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.screens.search.suggestions.SuggestionsTabContent
import echo.music.iad1tya.utils.rememberPreference
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, pureBlack: Boolean) {
  val database = LocalDatabase.current
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val isPlayerExpanded = LocalIsPlayerExpanded.current
  val playerConnection = LocalPlayerConnection.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
  val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)

  var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
  var searchActive by rememberSaveable { mutableStateOf(false) }
  var showSearchContent by remember { mutableStateOf(false) }

  LaunchedEffect(searchActive) {
    if (searchActive) {

      kotlinx.coroutines.delay(100)
      showSearchContent = true
    } else {
      showSearchContent = false
    }
  }

  val onSearch: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        focusManager.clearFocus()
        when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
          is YouTubeUrlParser.ParsedUrl.Video -> {
            playerConnection?.playQueue(
              YouTubeQueue(
                WatchEndpoint(videoId = parsedUrl.id),
              ),
            )
          }
          is YouTubeUrlParser.ParsedUrl.Artist -> {
            navController.navigate("artist/${parsedUrl.id}")
          }
          null -> {
            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
          }
        }

        if (!pauseSearchHistory) {
          coroutineScope.launch(Dispatchers.IO) {
            database.query { insert(SearchHistory(query = searchQuery)) }
          }
        }
      }
    }
  }

  val onSearchFromSuggestion: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        focusManager.clearFocus()
        when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
          is YouTubeUrlParser.ParsedUrl.Video -> {
            playerConnection?.playQueue(
              YouTubeQueue(
                WatchEndpoint(videoId = parsedUrl.id),
              ),
            )
          }
          is YouTubeUrlParser.ParsedUrl.Artist -> {
            navController.navigate("artist/${parsedUrl.id}")
          }
          null -> {
            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
          }
        }

        if (!pauseSearchHistory) {
          coroutineScope.launch(Dispatchers.IO) {
            database.query { insert(SearchHistory(query = searchQuery)) }
          }
        }
      }
    }
  }

  Scaffold(
    topBar = {
      Column(
        modifier =
          Modifier.background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 12.dp, top = 8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(24.dp),
            color =
              if (pureBlack) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              else MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.weight(1f)
          ) {
            BasicTextField(
              value = query,
              onValueChange = { query = it },
              modifier =
                Modifier.fillMaxWidth()
                  .height(48.dp)
                  .focusRequester(focusRequester)
                  .onFocusChanged { if (it.isFocused) searchActive = true },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions =
                KeyboardActions(
                  onSearch = {
                    onSearch(query.text)
                    searchActive = false
                  }
                ),
              textStyle = TextStyle(fontFamily = LocalTextStyle.current.fontFamily, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              decorationBox = { innerTextField ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                  IconButton(
                    onClick = {
                      if (searchActive) {
                        searchActive = false
                        query = TextFieldValue("")
                      } else {
                        searchActive = true
                      }
                    }
                  ) {
                    Icon(
                      painter =
                        painterResource(
                          if (searchActive) R.drawable.arrow_back else R.drawable.search
                        ),
                      contentDescription =
                        if (searchActive) stringResource(R.string.dismiss) else null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    if (query.text.isEmpty()) {
                      Text(
                        text = stringResource(R.string.stage_search_music),
                        style =
                          TextStyle(fontFamily = LocalTextStyle.current.fontFamily, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                          )
                      )
                    }
                    innerTextField()
                  }
                  if (query.text.isNotEmpty()) {
                    IconButton(onClick = { query = TextFieldValue("") }) {
                      Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            )
          }

        }
      }
    },
    containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background
  ) { paddingValues ->
    val bottomPadding =
      LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).fillMaxSize()) {
      if (searchActive && showSearchContent) {
        OnlineSearchScreen(
              query = query.text,
              onQueryChange = { query = it },
              navController = navController,
              onSearch = {
                onSearchFromSuggestion(it)
                searchActive = false
              },
              onDismiss = { searchActive = false },
              pureBlack = pureBlack
            )
      } else if (!searchActive) {
        val tabPadding = PaddingValues(bottom = bottomPadding)
        SuggestionsTabContent(
          navController = navController,
          onSearch = onSearchFromSuggestion,
          contentPadding = tabPadding,
        )
      }
    }
  }

  DisposableEffect(lifecycleOwner, isPlayerExpanded) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {

          if (isPlayerExpanded) {
            keyboardController?.hide()
            focusManager.clearFocus()
          }
        }
        Lifecycle.Event.ON_PAUSE -> {

          focusManager.clearFocus()
          keyboardController?.hide()
        }
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)

    if (isPlayerExpanded) {
      keyboardController?.hide()
      focusManager.clearFocus()
    }

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
