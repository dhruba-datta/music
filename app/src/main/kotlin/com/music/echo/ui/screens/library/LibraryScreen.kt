package echo.music.iad1tya.ui.screens.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.LibraryFilter
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.StageSheetGroup
import echo.music.iad1tya.ui.component.StageSheetHandle
import echo.music.iad1tya.ui.component.StageSheetRow
import echo.music.iad1tya.ui.component.StageSheetShape
import echo.music.iad1tya.ui.component.StageSheetTitle
import echo.music.iad1tya.ui.component.TextFieldDialog
import echo.music.iad1tya.utils.onlinePlaylistIdFromUrl
import echo.music.iad1tya.utils.onlinePlaylistRoute

@Composable
fun LibraryScreen(navController: NavController) {
  var filterType by rememberSaveable { mutableStateOf(LibraryFilter.LIBRARY) }
  var showFabMenu by remember { mutableStateOf(false) }
  var showImportMenu by remember { mutableStateOf(false) }
  var showYoutubeImportDialog by remember { mutableStateOf(false) }
  var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
  var showCreatePlaylistOptionsDialog by rememberSaveable { mutableStateOf(false) }
  var showTasteGenerationDialog by rememberSaveable { mutableStateOf(false) }
  var showAiPlaylistDialog by rememberSaveable { mutableStateOf(false) }
  val context = LocalContext.current

  BackHandler(enabled = filterType != LibraryFilter.LIBRARY) { filterType = LibraryFilter.LIBRARY }

  val currentInsets = LocalPlayerAwareWindowInsets.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  // Create button (56) plus the 4dp gap the stacked shuffle button sits above it with.
  val fabHeightPlusSpacing = 60.dp
  val newInsets =
    remember(currentInsets, density, layoutDirection) {
      object : WindowInsets {
        override fun getLeft(density: Density, layoutDirection: LayoutDirection) =
          currentInsets.getLeft(density, layoutDirection)

        override fun getTop(density: Density) = currentInsets.getTop(density)

        override fun getRight(density: Density, layoutDirection: LayoutDirection) =
          currentInsets.getRight(density, layoutDirection)

        override fun getBottom(density: Density) =
          currentInsets.getBottom(density) + with(density) { fabHeightPlusSpacing.roundToPx() }
      }
    }

  CompositionLocalProvider(LocalPlayerAwareWindowInsets provides newInsets) {
    Box(
      modifier = Modifier.fillMaxSize(),
    ) {
      when (filterType) {
        LibraryFilter.LIBRARY -> LibraryMixScreen(navController, { filterType = it })
        LibraryFilter.PLAYLISTS ->
          LibraryPlaylistsScreen(navController, { filterType = LibraryFilter.LIBRARY })
        LibraryFilter.SONGS ->
          LibrarySongsScreen(navController, { filterType = LibraryFilter.LIBRARY })
        LibraryFilter.ALBUMS ->
          LibraryAlbumsScreen(navController, { filterType = LibraryFilter.LIBRARY })
        LibraryFilter.ARTISTS ->
          LibraryArtistsScreen(navController, { filterType = LibraryFilter.LIBRARY })
      }

      val bottomPadding = with(density) { currentInsets.getBottom(density).toDp() }
      Box(modifier = Modifier.fillMaxSize().padding(end = 18.dp, bottom = bottomPadding + 14.dp)) {
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
          androidx.compose.material3.FloatingActionButton(
            onClick = { showFabMenu = true },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
            elevation =
              androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
              )
          ) {
            Icon(painter = painterResource(R.drawable.add), contentDescription = "Add")
          }
        }
      }
    }
  }

  if (showFabMenu) {
    @kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.ModalBottomSheet(
      onDismissRequest = { showFabMenu = false },
      sheetState =
        @kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
      shape = StageSheetShape,
      dragHandle = { StageSheetHandle() },
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      StageSheetTitle(stringResource(R.string.create))
      StageSheetGroup {
        StageSheetRow(
          title = stringResource(R.string.create_playlist),
          onClick = {
            showFabMenu = false
            showCreatePlaylistDialog = true
          },
          leading = {
            Icon(
              painter = painterResource(R.drawable.playlist_add),
              contentDescription = null,
              modifier = Modifier.size(22.dp)
            )
          }
        )
        StageSheetRow(
          title = "Create from Taste",
          onClick = {
            showFabMenu = false
            showTasteGenerationDialog = true
          },
          leading = {
            androidx.compose.foundation.Image(
              painter = painterResource(R.drawable.tast),
              contentDescription = null,
              modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape)
            )
          }
        )
        StageSheetRow(
          title = stringResource(R.string.import_playlist),
          onClick = {
            showFabMenu = false
            showImportMenu = true
          },
          leading = {
            Icon(
              painter = painterResource(R.drawable.download),
              contentDescription = null,
              modifier = Modifier.size(22.dp)
            )
          }
        )
      }
    }
  }

  if (showImportMenu) {
    @kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.ModalBottomSheet(
      onDismissRequest = { showImportMenu = false },
      sheetState =
        @kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
      shape = StageSheetShape,
      dragHandle = { StageSheetHandle() },
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      StageSheetTitle(stringResource(R.string.import_playlist))
      StageSheetGroup {
        StageSheetRow(
          title = stringResource(R.string.import_from_spotify),
          onClick = {
            showImportMenu = false
            navController.navigate("settings/account?highlightKey=Spotify")
          }
        )
        StageSheetRow(
          title = stringResource(R.string.import_from_youtube_music),
          onClick = {
            showImportMenu = false
            showYoutubeImportDialog = true
          }
        )
      }
    }
  }

  if (showYoutubeImportDialog) {
    var url by remember { mutableStateOf(TextFieldValue("")) }
    val invalidUrlMessage = stringResource(R.string.invalid_playlist_url)
    echo.music.iad1tya.ui.component.TextFieldDialog(
      icon = { Icon(painter = painterResource(R.drawable.link), contentDescription = null) },
      title = {
        Column {
          Text(text = stringResource(R.string.import_youtube_music_playlist_title))
          Text(
            text = stringResource(R.string.import_youtube_music_playlist_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      },
      initialTextFieldValue = url,
      autoFocus = true,
      onDismiss = { showYoutubeImportDialog = false },
      onDone = { finalUrl ->
        val route = onlinePlaylistRoute(onlinePlaylistIdFromUrl(finalUrl))
        if (route != null) {
          navController.navigate(route)
        } else {
          android.widget.Toast.makeText(
              context,
              invalidUrlMessage,
              android.widget.Toast.LENGTH_SHORT
            )
            .show()
        }
        showYoutubeImportDialog = false
      }
    )
  }

  if (showCreatePlaylistDialog) {
    echo.music.iad1tya.ui.component.CreatePlaylistDialog(
      onDismiss = { showCreatePlaylistDialog = false },
      initialTextFieldValue = null,
      allowSyncing = true,
      onPlaylistCreated = { playlistId ->
        showCreatePlaylistDialog = false
        navController.navigate("local_playlist/$playlistId")
      }
    )
  }

  if (showCreatePlaylistOptionsDialog) {
    DefaultDialog(
      onDismiss = { showCreatePlaylistOptionsDialog = false },
      title = { Text(stringResource(R.string.create_playlist)) },
      buttons = {
        androidx.compose.material3.Button(
          onClick = { showCreatePlaylistOptionsDialog = false },
          modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
          Text(stringResource(R.string.cancel), fontWeight = FontWeight.ExtraBold)
        }
      },
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      ) {
        CreatePlaylistOption(
          title = stringResource(R.string.create_playlist_normally),
          onClick = {
            showCreatePlaylistOptionsDialog = false
            showCreatePlaylistDialog = true
          }
        )
        CreatePlaylistOption(
          title = stringResource(R.string.create_playlist_with_ai),
          onClick = {
            showCreatePlaylistOptionsDialog = false
            showAiPlaylistDialog = true
          }
        )
      }
    }
  }

  if (showAiPlaylistDialog) {
    echo.music.iad1tya.ui.component.CreateAiPlaylistDialog(
      onDismiss = { showAiPlaylistDialog = false },
      onPlaylistCreated = { playlistId ->
        showAiPlaylistDialog = false
        navController.navigate("local_playlist/$playlistId")
      }
    )
  }

  if (showTasteGenerationDialog) {
    echo.music.iad1tya.ui.component.CreateFromTasteDialog(
      onDismiss = { showTasteGenerationDialog = false },
      onPlaylistCreated = { playlistId ->
        showTasteGenerationDialog = false
        navController.navigate("local_playlist/$playlistId")
      }
    )
  }
}

@Composable
private fun CreatePlaylistOption(title: String, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 54.dp)
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Text(
      text = title,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.weight(1f)
    )
    Icon(
      painter = painterResource(R.drawable.navigate_next),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(18.dp)
    )
  }
}
