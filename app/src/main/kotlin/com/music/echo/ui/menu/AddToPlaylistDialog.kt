package echo.music.iad1tya.ui.menu

import echo.music.iad1tya.ui.component.StagePillButton
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.music.innertube.YouTube
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AddToPlaylistSortDescendingKey
import echo.music.iad1tya.constants.AddToPlaylistSortTypeKey
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.PlaylistSortType
import echo.music.iad1tya.db.entities.Playlist
import echo.music.iad1tya.ui.component.CreatePlaylistDialog
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.ListDialog
import echo.music.iad1tya.ui.component.ListItem
import echo.music.iad1tya.ui.component.PlaylistListItem
import echo.music.iad1tya.ui.component.SortHeader
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.PlaylistsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistDialog(
  isVisible: Boolean,
  allowSyncing: Boolean = true,
  initialTextFieldValue: String? = null,
  onGetSong: suspend (Playlist) -> List<String>,
  onDismiss: () -> Unit,
  viewModel: PlaylistsViewModel = hiltViewModel()
) {
  val database = LocalDatabase.current
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val (sortType, onSortTypeChange) =
    rememberEnumPreference(AddToPlaylistSortTypeKey, PlaylistSortType.NAME)
  val (sortDescending, onSortDescendingChange) =
    rememberPreference(AddToPlaylistSortDescendingKey, false)
  val playlists by viewModel.allPlaylists.collectAsState()
  val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
  var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }

  var showDuplicateDialog by remember { mutableStateOf(false) }
  var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
  var songIds by remember { mutableStateOf<List<String>?>(null) }
  var duplicates by remember { mutableStateOf(emptyList<String>()) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  val filteredPlaylists =
    remember(playlists, searchQuery) {
      if (searchQuery.isBlank()) {
        playlists
      } else {
        playlists.filter { it.playlist.name.contains(searchQuery, ignoreCase = true) }
      }
    }

  if (isVisible) {
    androidx.compose.material3.ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
      shape = echo.music.iad1tya.ui.component.StageSheetShape,
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      dragHandle = { echo.music.iad1tya.ui.component.StageSheetHandle() },
    ) {
    androidx.compose.foundation.lazy.LazyColumn(
      contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 18.dp),
    ) {
      item {
        echo.music.iad1tya.ui.component.StageSheetTitle(
          title = stringResource(R.string.add_to_playlist)
        )
      }

      if (playlists.isNotEmpty()) {
        item {
          echo.music.iad1tya.ui.component.StageSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = stringResource(R.string.search),
            modifier = Modifier.padding(start = 18.dp, top = 6.dp, end = 18.dp),
          )
        }
      }

      item {
        echo.music.iad1tya.ui.component.StageSheetRow(
          text = stringResource(R.string.create_playlist),
          icon = R.drawable.add,
          onClick = { showCreatePlaylistDialog = true },
          modifier =
            Modifier.padding(start = 18.dp, top = 4.dp, end = 18.dp)
              .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
        )
      }

      if (playlists.isNotEmpty()) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp),
          ) {
            SortHeader(
              sortType = sortType,
              sortDescending = sortDescending,
              onSortTypeChange = onSortTypeChange,
              onSortDescendingChange = onSortDescendingChange,
              sortTypeText = { sortType ->
                when (sortType) {
                  PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                  PlaylistSortType.NAME -> R.string.sort_by_name
                  PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                  PlaylistSortType.LAST_UPDATED -> R.string.sort_by_last_updated
                }
              },
            )
          }
        }
      }

      if (filteredPlaylists.isEmpty() && searchQuery.isNotEmpty()) {
        item {
          Text(
            text = stringResource(R.string.no_results_found),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      itemsIndexed(filteredPlaylists) { index, playlist ->
        PlaylistListItem(
          playlist = playlist,
          modifier =
            Modifier.clickable {
              selectedPlaylist = playlist
              coroutineScope.launch {
                val ids = songIds ?: withContext(Dispatchers.IO) { onGetSong(playlist) }
                songIds = ids
                val dupes = withContext(Dispatchers.IO) { database.playlistDuplicates(playlist.id, ids) }
                duplicates = dupes
                if (dupes.isNotEmpty()) {
                  showDuplicateDialog = true
                } else {
                  onDismiss()
                  withContext(Dispatchers.IO) {
                    database.addSongToPlaylist(playlist, ids)
                    playlist.playlist.browseId?.let { plist ->
                      ids.forEach { YouTube.addToPlaylist(plist, it) }
                    }
                  }
                  Toast.makeText(
                      context,
                      context.getString(R.string.added_to_playlist, playlist.playlist.name),
                      Toast.LENGTH_SHORT
                    )
                    .show()
                }
              }
            }
        )
      }
    }
    }
  }

  if (showCreatePlaylistDialog) {
    CreatePlaylistDialog(
      onDismiss = { showCreatePlaylistDialog = false },
      initialTextFieldValue = initialTextFieldValue,
      allowSyncing = allowSyncing
    )
  }

  if (showDuplicateDialog) {
    DefaultDialog(
      title = { Text(stringResource(R.string.duplicates)) },
      buttons = {
        StagePillButton(onClick = { showDuplicateDialog = false }) {
          Text(stringResource(android.R.string.cancel))
        }
        StagePillButton(onClick = {
            showDuplicateDialog = false
            onDismiss()
            val targetPlaylist = selectedPlaylist ?: return@StagePillButton
            val allIds = songIds.orEmpty()
            coroutineScope.launch(Dispatchers.IO) {
              database.transaction { addSongToPlaylist(targetPlaylist, allIds) }
              targetPlaylist.playlist.browseId?.let { plist ->
                allIds.forEach { YouTube.addToPlaylist(plist, it) }
              }
              withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.added_to_playlist, targetPlaylist.playlist.name),
                    Toast.LENGTH_SHORT
                  )
                  .show()
              }
            }
          }, primary = false) {
          Text(stringResource(R.string.add_anyway))
        }
        StagePillButton(onClick = {
            showDuplicateDialog = false
            onDismiss()
            val targetPlaylist = selectedPlaylist ?: return@StagePillButton
            val nonDuplicates = songIds.orEmpty().filter { !duplicates.contains(it) }
            coroutineScope.launch(Dispatchers.IO) {
              if (nonDuplicates.isNotEmpty()) {
                database.transaction {
                  addSongToPlaylist(targetPlaylist, nonDuplicates)
                }
                targetPlaylist.playlist.browseId?.let { plist ->
                  nonDuplicates.forEach { YouTube.addToPlaylist(plist, it) }
                }
              }
              withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.added_to_playlist, targetPlaylist.playlist.name),
                    Toast.LENGTH_SHORT
                  )
                  .show()
              }
            }
          }, primary = false) {
          Text(stringResource(R.string.skip_duplicates))
        }
        StagePillButton(onClick = {
            showDuplicateDialog = false
            onDismiss()
            val targetPlaylist = selectedPlaylist ?: return@StagePillButton
            val toRemoveSongIds = duplicates.toSet()
            coroutineScope.launch(Dispatchers.IO) {
              val maps = database.playlistSongMaps(targetPlaylist.id, 0)
                .filter { it.songId in toRemoveSongIds }
              database.transaction {
                maps.forEach { map ->
                  move(map.playlistId, map.position, Int.MAX_VALUE)
                  delete(map.copy(position = Int.MAX_VALUE))
                }
              }
              targetPlaylist.playlist.browseId?.let { browseId ->
                maps.forEach { map ->
                  map.setVideoId?.let { setVideoId ->
                    YouTube.removeFromPlaylist(browseId, map.songId, setVideoId)
                  }
                }
              }
              withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(
                      R.string.removed_song_from_playlist,
                      targetPlaylist.playlist.name
                    ),
                    Toast.LENGTH_SHORT
                  )
                  .show()
              }
            }
          }, primary = false) {
          Text(
            text = stringResource(R.string.remove_from_playlist),
            color = MaterialTheme.colorScheme.error
          )
        }
      },
      onDismiss = { showDuplicateDialog = false }
    ) {
      Text(
        text =
          if (duplicates.size == 1) {
            stringResource(R.string.duplicates_description_single)
          } else {
            stringResource(R.string.duplicates_description_multiple, duplicates.size)
          },
        textAlign = TextAlign.Start,
        modifier = Modifier.align(Alignment.Start)
      )
    }
  }
}
