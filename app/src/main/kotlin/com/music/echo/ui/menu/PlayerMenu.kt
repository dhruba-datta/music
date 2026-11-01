package echo.music.iad1tya.ui.menu

import echo.music.iad1tya.ui.screens.equalizer.StageQuickEqualizer
import echo.music.iad1tya.ui.component.StageAlertDialog
import echo.music.iad1tya.ui.component.StagePillButton
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.music.innertube.YouTube
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalDownloadUtil
import echo.music.iad1tya.LocalListenTogetherManager
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.EnableExportAsMp3Key
import echo.music.iad1tya.constants.ExportDirectoryUriKey
import echo.music.iad1tya.constants.ExportedSongIdsKey
import echo.music.iad1tya.constants.ExportingSongIdsKey
import echo.music.iad1tya.constants.ListItemHeight
import echo.music.iad1tya.constants.VarispeedKey

import echo.music.iad1tya.constants.QueueEditLockKey
import echo.music.iad1tya.constants.ShowLyricsOnPlayerKey
import echo.music.iad1tya.listentogether.ConnectionState
import echo.music.iad1tya.listentogether.ListenTogetherEvent
import echo.music.iad1tya.ui.component.stageLabel
import echo.music.iad1tya.models.MediaMetadata
import echo.music.iad1tya.playback.ExoDownloadService
import echo.music.iad1tya.ui.component.BottomSheetState
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.ListDialog
import echo.music.iad1tya.utils.rememberPreference
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlayerMenu(
  mediaMetadata: MediaMetadata?,
  navController: NavController,
  playerBottomSheetState: BottomSheetState,
  isQueueTrigger: Boolean? = false,
  isQueueLocked: Boolean? = null,
  inSelectMode: Boolean = false,
  onShowDetailsDialog: () -> Unit,
  onClearQueue: (() -> Unit)? = null,
  onDismiss: () -> Unit,
) {
  mediaMetadata ?: return
  val context = LocalContext.current
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val menuState = echo.music.iad1tya.ui.component.LocalMenuState.current

  val audioManager = remember {
    context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
  }
  var systemVolume by remember {
    androidx.compose.runtime.mutableFloatStateOf(
      audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat()
    )
  }
  var maxSystemVolume by remember {
    androidx.compose.runtime.mutableFloatStateOf(
      audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).toFloat()
    )
  }

  androidx.compose.runtime.DisposableEffect(context) {
    val volumeChangeReceiver =
      object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: android.content.Intent) {
          if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
            if (streamType == android.media.AudioManager.STREAM_MUSIC) {
              systemVolume =
                audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat()
              maxSystemVolume =
                audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).toFloat()
            }
          }
        }
      }
    context.registerReceiver(
      volumeChangeReceiver,
      android.content.IntentFilter("android.media.VOLUME_CHANGED_ACTION")
    )
    onDispose { context.unregisterReceiver(volumeChangeReceiver) }
  }

  val castHandler =
    remember(playerConnection) {
      try {
        playerConnection.service.castConnectionHandler
      } catch (e: Exception) {
        null
      }
    }
    val varispeedMode by rememberPreference(VarispeedKey, defaultValue = false)
    var showSpeedDialog by rememberSaveable { mutableStateOf(false) }

  val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
  val castVolume by
    castHandler?.castVolume?.collectAsState() ?: remember { mutableFloatStateOf(1f) }
  val castDeviceName by
    castHandler?.castDeviceName?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

  val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)
  val coroutineScope = rememberCoroutineScope()

  val download by
    LocalDownloadUtil.current.getDownload(mediaMetadata.id).collectAsState(initial = null)

  val artists = remember(mediaMetadata.artists) { mediaMetadata.artists.filter { it.id != null } }

  var showChoosePlaylistDialog by rememberSaveable { mutableStateOf(false) }

  val (enableExportAsMp3) = rememberPreference(key = EnableExportAsMp3Key, defaultValue = false)
  val (exportDirectoryUri) = rememberPreference(key = ExportDirectoryUriKey, defaultValue = "")
  val (exportingSongIds) = rememberPreference(key = ExportingSongIdsKey, defaultValue = "")
  val (exportedSongIds) = rememberPreference(key = ExportedSongIdsKey, defaultValue = "")
  val (showLyricsOnPlayer, onShowLyricsOnPlayerChange) =
    rememberPreference(key = ShowLyricsOnPlayerKey, defaultValue = false)

  val (exportProgressString) =
    rememberPreference(key = echo.music.iad1tya.constants.ExportProgressKey, defaultValue = "")
  val exportProgress =
    remember(exportProgressString, mediaMetadata.id) {
      exportProgressString
        .split(',')
        .find { it.startsWith("${mediaMetadata.id}:") }
        ?.split(':')
        ?.getOrNull(1)
        ?.toIntOrNull() ?: 0
    }

  val isExporting =
    remember(exportingSongIds, mediaMetadata.id) {
      exportingSongIds.split(",").contains(mediaMetadata.id)
    }
  val isExported =
    remember(exportedSongIds, mediaMetadata.id) {
      exportedSongIds.split(",").contains(mediaMetadata.id)
    }

  var showReExportDialog by
    androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  if (showReExportDialog) {
    StageAlertDialog(
      onDismissRequest = { showReExportDialog = false },
      title = { androidx.compose.material3.Text("Re-export") },
      text = { androidx.compose.material3.Text("Wanna re-export it again?") },
      confirmButton = {
        StagePillButton(onClick = {
            showReExportDialog = false
            if (exportDirectoryUri.isBlank()) {
              android.widget.Toast.makeText(
                  context,
                  context.getString(R.string.export_directory_not_set),
                  android.widget.Toast.LENGTH_SHORT
                )
                .show()
              onDismiss()
            } else {
              onDismiss()
              echo.music.iad1tya.playback.AudioExportService.start(
                context = context,
                songId = mediaMetadata.id,
                songTitle = mediaMetadata.title,
                songArtist = artists.joinToString(", ") { it.name },
                songAlbum = mediaMetadata.album?.title ?: "",
                artworkUrl = mediaMetadata.thumbnailUrl ?: "",
                targetDirectoryUri = exportDirectoryUri
              )
            }
          }) {
          androidx.compose.material3.Text("Yes")
        }
      },
      dismissButton = {
        StagePillButton(onClick = { showReExportDialog = false }, primary = false) {
          androidx.compose.material3.Text("No")
        }
      }
)
  }

  var showClearQueueDialog by rememberSaveable { mutableStateOf(false) }
  if (showClearQueueDialog) {
    DefaultDialog(
      onDismiss = { showClearQueueDialog = false },
      icon = {
        Icon(
          painter = painterResource(R.drawable.clear_all),
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
      },
      title = { Text(stringResource(R.string.clear_queue)) },
      buttons = {
        StagePillButton(onClick = {
            showClearQueueDialog = false
            onDismiss()
            playerConnection.clearQueue()
          }) {
          Text(stringResource(R.string.clear))
        }
        StagePillButton(onClick = { showClearQueueDialog = false }, primary = false) {
          Text(stringResource(R.string.cancel))
        }
      }
    ) {
      Text(
        text = stringResource(R.string.clear_queue_confirm),
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

  var showListenTogetherDialog by rememberSaveable { mutableStateOf(false) }

  val listenTogetherManager = LocalListenTogetherManager.current
  val ringtoneViewModel = echo.music.iad1tya.LocalRingtoneViewModel.current
  val isListenTogetherGuest by
    listenTogetherManager?.guestPlaybackRestricted?.collectAsState(initial = false)
      ?: remember { mutableStateOf(false) }
  val pendingSuggestions by
    listenTogetherManager?.pendingSuggestions?.collectAsState(initial = emptyList())
      ?: remember { mutableStateOf(emptyList()) }

  AddToPlaylistDialog(
    isVisible = showChoosePlaylistDialog,
    onGetSong = {
      database.transaction { insert(mediaMetadata) }
      listOf(mediaMetadata.id)
    },
    onDismiss = { showChoosePlaylistDialog = false }
  )

  ListenTogetherDialog(
    visible = showListenTogetherDialog,
    mediaMetadata = mediaMetadata,
    onDismiss = { showListenTogetherDialog = false }
  )

  var showSelectArtistDialog by rememberSaveable { mutableStateOf(false) }

  if (showSelectArtistDialog) {
    ListDialog(
      onDismiss = { showSelectArtistDialog = false },
    ) {
      items(artists) { artist ->
        Box(
          contentAlignment = Alignment.CenterStart,
          modifier =
            Modifier.fillParentMaxWidth()
              .height(ListItemHeight)
              .clickable {
                navController.navigate("artist/${artist.id}")
                showSelectArtistDialog = false
                playerBottomSheetState.collapseSoft()
                onDismiss()
              }
              .padding(horizontal = 24.dp),
        ) {
          Text(
            text = artist.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }

  var showSongDnaDialog by rememberSaveable { mutableStateOf(false) }
  var dnaAlbum by rememberSaveable { mutableStateOf("Fetching...") }
  var dnaMeaning by rememberSaveable { mutableStateOf("Fetching info from Wikipedia...") }
  var dnaBio by rememberSaveable { mutableStateOf("Fetching biography...") }
  var dnaFetched by rememberSaveable { mutableStateOf(false) }

  if (showSongDnaDialog) {
    LaunchedEffect(mediaMetadata.id) {
      if (!dnaFetched) {
         kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val artistName = mediaMetadata.artists.firstOrNull()?.name ?: ""
                val songTitle = mediaMetadata.title
                val query = java.net.URLEncoder.encode("$artistName $songTitle", "UTF-8")
                val searchUrl = java.net.URL("https://genius.com/api/search/multi?q=$query")
                val searchJson = searchUrl.readText()
                val searchRoot = org.json.JSONObject(searchJson).getJSONObject("response")
                val sections = searchRoot.getJSONArray("sections")
                var songId = -1
                for (i in 0 until sections.length()) {
                    val sec = sections.getJSONObject(i)
                    if (sec.getString("type") == "top_hit" || sec.getString("type") == "song") {
                        val hits = sec.getJSONArray("hits")
                        if (hits.length() > 0) {
                            songId = hits.getJSONObject(0).getJSONObject("result").getInt("id")
                            break
                        }
                    }
                }
                if (songId == -1) {
                    dnaAlbum = mediaMetadata.album?.title ?: "Unknown"
                    dnaMeaning = "Meaning not found."
                    dnaBio = "Biography not found."
                } else {
                    val songUrl = java.net.URL("https://genius.com/api/songs/$songId?text_format=plain")
                    val songJson = songUrl.readText()
                    val songObj = org.json.JSONObject(songJson).getJSONObject("response").getJSONObject("song")
                    
                    if (songObj.has("album") && !songObj.isNull("album")) {
                        dnaAlbum = songObj.getJSONObject("album").getString("name")
                    } else {
                        dnaAlbum = mediaMetadata.album?.title ?: "Unknown"
                    }
                    
                    dnaMeaning = "Info not found." // Reset so Wikipedia can override it
                    
                    var artistId = -1
                    if (songObj.has("primary_artist") && !songObj.isNull("primary_artist")) {
                        artistId = songObj.getJSONObject("primary_artist").getInt("id")
                    }
                    
                    if (artistId != -1) {
                        val artistUrl = java.net.URL("https://genius.com/api/artists/$artistId?text_format=plain")
                        val artistJson = artistUrl.readText()
                        val artistObj = org.json.JSONObject(artistJson).getJSONObject("response").getJSONObject("artist")
                        if (artistObj.has("description") && !artistObj.isNull("description")) {
                            dnaBio = artistObj.getJSONObject("description").getString("plain")
                            if (dnaBio == "?") dnaBio = "Biography not found."
                        } else {
                            dnaBio = "Biography not found."
                        }
                    } else {
                        dnaBio = "Biography not found."
                    }
                }
                
                // --- WIKIPEDIA FALLBACKS ---
                
                // 1. Wikipedia exclusively for About the Song
                if (true) {
                    dnaMeaning = "Info not found." 
                    try {
                        val wpQuery = java.net.URLEncoder.encode(songTitle + " " + dnaAlbum, "UTF-8")
                        val wpSearchUrl = java.net.URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$wpQuery&utf8=&format=json")
                        val wpSearchRoot = org.json.JSONObject(wpSearchUrl.readText()).getJSONObject("query")
                        val wpSearchList = wpSearchRoot.getJSONArray("search")
                        if (wpSearchList.length() > 0) {
                            val wpTitle = wpSearchList.getJSONObject(0).getString("title")
                            val wpTitleEnc = java.net.URLEncoder.encode(wpTitle, "UTF-8")
                            val wpExtractUrl = java.net.URL("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exsentences=4&exlimit=1&titles=$wpTitleEnc&explaintext=1&format=json")
                            val wpExtractRoot = org.json.JSONObject(wpExtractUrl.readText()).getJSONObject("query").getJSONObject("pages")
                            val firstKey = wpExtractRoot.keys().next()
                            val extract = wpExtractRoot.getJSONObject(firstKey).optString("extract", "")
                            if (extract.isNotBlank() && !extract.contains("may refer to")) {
                                dnaMeaning = extract
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                
                // 2. Wikipedia fallback for Artist Bio
                if (dnaBio == "Biography not found." && artistName.isNotBlank()) {
                    try {
                        val wpQuery = java.net.URLEncoder.encode(artistName, "UTF-8")
                        val wpSearchUrl = java.net.URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$wpQuery&utf8=&format=json")
                        val wpSearchRoot = org.json.JSONObject(wpSearchUrl.readText()).getJSONObject("query")
                        val wpSearchList = wpSearchRoot.getJSONArray("search")
                        if (wpSearchList.length() > 0) {
                            val wpTitle = wpSearchList.getJSONObject(0).getString("title")
                            val wpTitleEnc = java.net.URLEncoder.encode(wpTitle, "UTF-8")
                            val wpExtractUrl = java.net.URL("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exsentences=4&exlimit=1&titles=$wpTitleEnc&explaintext=1&format=json")
                            val wpExtractRoot = org.json.JSONObject(wpExtractUrl.readText()).getJSONObject("query").getJSONObject("pages")
                            val firstKey = wpExtractRoot.keys().next()
                            val extract = wpExtractRoot.getJSONObject(firstKey).optString("extract", "")
                            if (extract.isNotBlank() && !extract.contains("may refer to")) {
                                dnaBio = extract
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dnaAlbum = mediaMetadata.album?.title ?: "Unknown"
                dnaMeaning = "Failed to fetch meaning."
                dnaBio = "Failed to fetch biography."
            }
            dnaFetched = true
         }
      }
    }

    DefaultDialog(
      onDismiss = { showSongDnaDialog = false },
      icon = {
        Icon(painter = painterResource(R.drawable.info), contentDescription = null)
      },
      title = {
        Text("Song DNA", fontWeight = FontWeight.Bold)
      },
      buttons = {
        StagePillButton(onClick = { showSongDnaDialog = false }) { Text(stringResource(android.R.string.ok)) }
      }
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
            Column {
                Text("Movie / Album", fontWeight = FontWeight.Bold)
                Text(dnaAlbum, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Column {
                Text("About the Song", fontWeight = FontWeight.Bold)
                Text(dnaMeaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column {
                Text("About the Artist", fontWeight = FontWeight.Bold)
                Text(dnaBio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
      }
    }
  }

  var showPitchTempoDialog by rememberSaveable { mutableStateOf(false) }

  if (showSpeedDialog) {
    SpeedDialog(
      onDismiss = { showSpeedDialog = false },
    )
  }

  if (showPitchTempoDialog) {
    TempoPitchDialog(
      onDismiss = { showPitchTempoDialog = false },
    )
  }

  if (isQueueTrigger != true) {
    if (isCasting && castDeviceName != null) {
      Column(
        modifier =
          Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 6.dp),
      ) {
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.cast),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = stringResource(R.string.casting_to, castDeviceName ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }

  val configuration = LocalConfiguration.current
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

  LazyColumn(
    contentPadding =
      PaddingValues(
        start = 0.dp,
        top = 0.dp,
        end = 0.dp,
        bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
      ),
  ) {
    item { echo.music.iad1tya.ui.component.StageSheetTitle(title = mediaMetadata.title) }

    item {
      val startingRadioText = stringResource(R.string.starting_radio)
      val isInLibrary = librarySong?.song?.inLibrary != null

      val tiles = buildList {
        if (artists.isNotEmpty()) {
          add(
            PlayerMenuTile(
              order = 13,
              icon = { PlayerMenuTileIcon(R.drawable.artist) },
              label = { PlayerMenuTileLabel(stringResource(R.string.view_artist)) },
              onClick = {
                if (mediaMetadata.artists.size == 1) {
                  navController.navigate("artist/${mediaMetadata.artists[0].id}")
                  playerBottomSheetState.collapseSoft()
                  onDismiss()
                } else {
                  showSelectArtistDialog = true
                }
              }
            )
          )
        }
        if (mediaMetadata.album != null) {
          add(
            PlayerMenuTile(
              order = 14,
              icon = { PlayerMenuTileIcon(R.drawable.album) },
              label = { PlayerMenuTileLabel(stringResource(R.string.view_album)) },
              onClick = {
                navController.navigate("album/${mediaMetadata.album!!.id}")
                playerBottomSheetState.collapseSoft()
                onDismiss()
              }
            )
          )
        }
        if (!isListenTogetherGuest) {
          add(
            PlayerMenuTile(
              order = 10,
              icon = { PlayerMenuTileIcon(R.drawable.radio) },
              label = { PlayerMenuTileLabel(stringResource(R.string.start_an_radio)) },
              onClick = {
                Toast.makeText(context, startingRadioText, Toast.LENGTH_SHORT).show()
                playerConnection.startRadioSeamlessly()
                onDismiss()
              }
            )
          )
        }
        add(
          PlayerMenuTile(
            order = 11,
            icon = {
              PlayerMenuTileIcon(
                if (isInLibrary) R.drawable.library_add_check else R.drawable.library_add
              )
            },
            label = {
              PlayerMenuTileLabel(
                stringResource(
                  if (isInLibrary) R.string.remove_from_library else R.string.add_to_library
                )
              )
            },
            onClick = {
              playerConnection.toggleLibrary()
              onDismiss()
            }
          )
        )
        add(
          PlayerMenuTile(
            order = 30,
            icon = { PlayerMenuTileIcon(R.drawable.info) },
            label = { PlayerMenuTileLabel("Song DNA") },
            onClick = { showSongDnaDialog = true }
          )
        )
        add(
          PlayerMenuTile(
            order = 22,
            icon = { PlayerMenuTileIcon(R.drawable.fullscreen) },
            label = { PlayerMenuTileLabel("Ambient Mode") },
            onClick = {
              navController.navigate("ambient_mode")
              playerBottomSheetState.collapseSoft()
              onDismiss()
            }
          )
        )
        add(
          PlayerMenuTile(
            order = 23,
            icon = { PlayerMenuTileIcon(R.drawable.lyrics) },
            label = {
              PlayerMenuTileLabel(if (showLyricsOnPlayer) "Hide Lyrics" else "Show Lyrics")
            },
            onClick = {
              onShowLyricsOnPlayerChange(!showLyricsOnPlayer)
              onDismiss()
            }
          )
        )
        if (enableExportAsMp3) {
          add(
            when {
              isExported ->
                PlayerMenuTile(
                  icon = { PlayerMenuTileIcon(R.drawable.folder_managed) },
                  label = { PlayerMenuTileLabel(stringResource(R.string.action_exported)) },
                  onClick = { showReExportDialog = true }
                )
              isExporting ->
                PlayerMenuTile(
                  icon = {
                    CircularProgressIndicator(
                      progress = { exportProgress / 100f },
                      modifier = Modifier.size(24.dp),
                      strokeWidth = 2.dp,
                      trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                  },
                  label = {
                    PlayerMenuTileLabel("${stringResource(R.string.exporting)} $exportProgress%")
                  },
                  onClick = {}
                )
              else ->
                PlayerMenuTile(
                  icon = { PlayerMenuTileIcon(R.drawable.file_export) },
                  label = { PlayerMenuTileLabel(stringResource(R.string.action_export)) },
                  onClick = {
                    if (exportDirectoryUri.isBlank()) {
                      Toast.makeText(
                          context,
                          context.getString(R.string.export_directory_not_set),
                          Toast.LENGTH_SHORT
                        )
                        .show()
                      onDismiss()
                    } else {
                      onDismiss()
                      echo.music.iad1tya.playback.AudioExportService.start(
                        context = context,
                        songId = mediaMetadata.id,
                        songTitle = mediaMetadata.title,
                        songArtist = artists.joinToString(", ") { it.name },
                        songAlbum = mediaMetadata.album?.title ?: "",
                        artworkUrl = mediaMetadata.thumbnailUrl ?: "",
                        targetDirectoryUri = exportDirectoryUri
                      )
                    }
                  }
                )
            }
          )
        }
        if (isListenTogetherGuest || pendingSuggestions.isNotEmpty()) {
          add(
            PlayerMenuTile(
              icon = {
                Box {
                  PlayerMenuTileIcon(R.drawable.group)
                  if (pendingSuggestions.isNotEmpty()) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.offset(x = 8.dp, y = (-6).dp).align(Alignment.TopEnd)
                    ) {
                      Text(
                        text = pendingSuggestions.size.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                      )
                    }
                  }
                }
              },
              label = { PlayerMenuTileLabel(stringResource(R.string.listen_together)) },
              onClick = { showListenTogetherDialog = true }
            )
          )
        }
        if (isListenTogetherGuest) {
          add(
            PlayerMenuTile(
              icon = { PlayerMenuTileIcon(R.drawable.replay) },
              label = { PlayerMenuTileLabel(stringResource(R.string.resync)) },
              onClick = {
                listenTogetherManager?.requestSync()
                onDismiss()
              }
            )
          )
        }
        if (isQueueTrigger != true) {
          add(
            PlayerMenuTile(
              order = 20,
              icon = { PlayerMenuTileIcon(R.drawable.equalizer) },
              label = { PlayerMenuTileLabel(stringResource(R.string.equalizer)) },
              onClick = {
                menuState.show {
                  StageQuickEqualizer(
                    onOpenFullEqualizer = {
                      menuState.dismiss()
                      playerBottomSheetState.collapseSoft()
                      navController.navigate("settings/equalizer")
                    }
                  )
                }
              }
            )
          )
        }
        add(
          PlayerMenuTile(
            order = 31,
            icon = { PlayerMenuTileIcon(R.drawable.info) },
            label = { PlayerMenuTileLabel(stringResource(R.string.details)) },
            onClick = {
              onShowDetailsDialog()
              onDismiss()
            }
          )
        )
        if (isQueueTrigger != true) {
          add(
            PlayerMenuTile(
              order = 21,
              icon = { PlayerMenuTileIcon(R.drawable.tune) },
              label = { PlayerMenuTileLabel(stringResource(R.string.advanced)) },
              onClick = {
                if (!varispeedMode) showPitchTempoDialog = true else showSpeedDialog = true
              }
            )
          )
        }
        add(
          PlayerMenuTile(
            order = 12,
            icon = { PlayerMenuTileIcon(R.drawable.share) },
            label = { PlayerMenuTileLabel(stringResource(R.string.share)) },
            onClick = {
              val intent =
                android.content.Intent().apply {
                  action = android.content.Intent.ACTION_SEND
                  type = "text/plain"
                  putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    "https://share.echomusic.fun/watch?v=${mediaMetadata.id}"
                  )
                }
              context.startActivity(android.content.Intent.createChooser(intent, null))
              onDismiss()
            }
          )
        )
      }

      PlayerMenuTileGrid(tiles = tiles)
    }
  }
}

private val PlayerMenuTileShape = RoundedCornerShape(18.dp)
private val PlayerMenuTilePadding = 18.dp

private class PlayerMenuTile(
  /** Rows sort by this; each ten starts a new group. */
  val order: Int = 40,
  val icon: @Composable () -> Unit,
  val label: @Composable () -> Unit,
  val onClick: () -> Unit,
)

@Composable
private fun PlayerMenuTileIcon(@DrawableRes icon: Int) {
  Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
}

@Composable
private fun PlayerMenuTileLabel(
  text: String,
  color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
  Text(
    text = text,
    color = color,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
}

/** Three tiles per row, each row as tall as its tallest label. */
@Composable
private fun PlayerMenuTileGrid(tiles: List<PlayerMenuTile>) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier =
      Modifier.fillMaxWidth().padding(start = PlayerMenuTilePadding, top = 10.dp, end = PlayerMenuTilePadding),
  ) {
    tiles.sortedBy { it.order }.groupBy { it.order / 10 }.values.forEach { group ->
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth().clip(PlayerMenuTileShape),
      ) {
        group.forEach { tile ->
          Surface(
            onClick = tile.onClick,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.heightIn(min = 54.dp).padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
              tile.icon()
              tile.label()
            }
          }
        }
      }
    }
  }
}

@Composable
fun TempoPitchDialog(onDismiss: () -> Unit) {
  val playerConnection = LocalPlayerConnection.current ?: return
  var tempo by remember { mutableFloatStateOf(playerConnection.player.playbackParameters.speed) }
  var transposeValue by remember {
    mutableIntStateOf(round(12 * log2(playerConnection.player.playbackParameters.pitch)).toInt())
  }
  val updatePlaybackParameters = {
    playerConnection.player.playbackParameters =
      PlaybackParameters(tempo, 2f.pow(transposeValue.toFloat() / 12))
  }
  val listenTogetherManager = echo.music.iad1tya.LocalListenTogetherManager.current
  val isInRoom = listenTogetherManager?.isInRoom ?: false

  echo.music.iad1tya.ui.component.DefaultDialog(
    onDismiss = onDismiss,
    title = { Text(stringResource(R.string.tempo_and_pitch)) },
    buttons = {
      echo.music.iad1tya.ui.component.StageButton(
        text = stringResource(android.R.string.ok),
        onClick = onDismiss,
      )
      echo.music.iad1tya.ui.component.StageButton(
        text = stringResource(R.string.reset),
        primary = false,
        onClick = {
          tempo = 1f
          transposeValue = 0
          updatePlaybackParameters()
        },
      )
    },
  ) {
    if (!isInRoom) {
      ValueAdjuster(
        icon = R.drawable.speed,
        label = stringResource(R.string.stage_tempo),
        currentValue = tempo,
        values = (0..35).map { round((0.25f + it * 0.05f) * 100) / 100 },
        onValueUpdate = {
          tempo = it
          updatePlaybackParameters()
        },
        valueText = { "%.2fx".format(it) },
      )
    }
    ValueAdjuster(
      icon = R.drawable.discover_tune,
      label = stringResource(R.string.stage_pitch),
      currentValue = transposeValue,
      values = (-12..12).toList(),
      onValueUpdate = {
        transposeValue = it
        updatePlaybackParameters()
      },
      valueText = { "${if (it > 0) "+" else ""}$it st" },
    )
  }
}

@Composable
fun <T> ValueAdjuster(
  @DrawableRes icon: Int,
  currentValue: T,
  values: List<T>,
  onValueUpdate: (T) -> Unit,
  valueText: (T) -> String,
  modifier: Modifier = Modifier,
  label: String? = null,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth().heightIn(min = 54.dp),
  ) {
    if (label != null) {
      Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f),
      )
    } else {
      Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.weight(1f))
    }

    IconButton(
      enabled = currentValue != values.first(),
      onClick = { onValueUpdate(values[values.indexOf(currentValue) - 1]) },
      modifier = Modifier.size(44.dp),
    ) {
      Icon(painter = painterResource(R.drawable.remove), contentDescription = null)
    }

    Text(
      text = valueText(currentValue),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(min = 48.dp),
    )

    IconButton(
      enabled = currentValue != values.last(),
      onClick = { onValueUpdate(values[values.indexOf(currentValue) + 1]) },
      modifier = Modifier.size(44.dp),
    ) {
      Icon(painter = painterResource(R.drawable.add), contentDescription = null)
    }
  }
}

@Composable
fun ListenTogetherDialog(visible: Boolean, mediaMetadata: MediaMetadata?, onDismiss: () -> Unit) {
  if (!visible) return

  val context = LocalContext.current
  val listenTogetherManager = echo.music.iad1tya.LocalListenTogetherManager.current

  if (listenTogetherManager == null) {
    ListDialog(onDismiss = onDismiss) {
      item {
        Column(
          modifier = Modifier.fillMaxWidth().padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            painter = painterResource(R.drawable.group),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.listen_together),
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.listen_together_not_configured),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(24.dp))
          echo.music.iad1tya.ui.component.StageButton(
            text = stringResource(android.R.string.ok),
            onClick = onDismiss,
          )
        }
      }
    }
    return
  }

  val connectionState by listenTogetherManager.connectionState.collectAsState()
  val roomState by listenTogetherManager.roomState.collectAsState()
  val userId by listenTogetherManager.userId.collectAsState()
  val pendingJoinRequests by listenTogetherManager.pendingJoinRequests.collectAsState()
  val pendingSuggestions by listenTogetherManager.pendingSuggestions.collectAsState()

  var savedUsername by
    rememberPreference(echo.music.iad1tya.constants.ListenTogetherUsernameKey, "")
  var roomCodeInput by rememberSaveable { mutableStateOf("") }
  var usernameInput by rememberSaveable { mutableStateOf(savedUsername) }

  var isCreatingRoom by rememberSaveable { mutableStateOf(false) }
  var isJoiningRoom by rememberSaveable { mutableStateOf(false) }
  var joinErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

  var selectedUserForMenu by rememberSaveable { mutableStateOf<String?>(null) }
  var selectedUsername by rememberSaveable { mutableStateOf<String?>(null) }

  val waitingForApprovalText = stringResource(R.string.waiting_for_approval)
  val invalidRoomCodeText = stringResource(R.string.invalid_room_code)
  val joinRequestDeniedText = stringResource(R.string.join_request_denied)

  if (selectedUserForMenu != null && selectedUsername != null) {
    val closeManageUser = {
      selectedUserForMenu = null
      selectedUsername = null
    }
    ListDialog(
      onDismiss = closeManageUser,
      title = { Text(text = selectedUsername ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
    ) {
      item {
        echo.music.iad1tya.ui.component.StageRoomManageRows(
          onKick = {
            selectedUserForMenu?.let { listenTogetherManager.kickUser(it, "Removed by host") }
            closeManageUser()
          },
          onBlock = {
            selectedUserForMenu?.let { userId ->
              selectedUsername?.let { username ->
                listenTogetherManager.blockUser(username)
                listenTogetherManager.kickUser(userId, R.string.user_blocked_by_host.toString())
              }
            }
            closeManageUser()
          },
          onTransfer = {
            selectedUserForMenu?.let { listenTogetherManager.transferHost(it) }
            closeManageUser()
          },
          modifier = Modifier.padding(horizontal = 8.dp),
        )
      }
    }
    return
  }

  LaunchedEffect(savedUsername) {
    if (usernameInput.isBlank() && savedUsername.isNotBlank()) {
      usernameInput = savedUsername
    }
  }

  LaunchedEffect(listenTogetherManager) {
    listenTogetherManager.events.collect { event ->
      when (event) {
        is ListenTogetherEvent.JoinRejected -> {
          val reason = event.reason
          joinErrorMessage =
            when {
              reason.isNullOrBlank() -> joinRequestDeniedText
              reason.contains("invalid", ignoreCase = true) == true -> invalidRoomCodeText
              else -> "$joinRequestDeniedText: $reason"
            }
          isJoiningRoom = false
          isCreatingRoom = false
        }
        is ListenTogetherEvent.JoinApproved -> {
          isJoiningRoom = false
          joinErrorMessage = null
        }
        is ListenTogetherEvent.RoomCreated -> {
          isCreatingRoom = false
          val clipboard =
            context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
              as android.content.ClipboardManager
          val clip = android.content.ClipData.newPlainText("ListenTogetherRoom", event.roomCode)
          clipboard.setPrimaryClip(clip)
        }
        else -> {}
      }
    }
  }

  val isInRoom = listenTogetherManager.isInRoom
  val isHost = roomState?.hostId == userId

  ListDialog(
    onDismiss = onDismiss,
    title = {
      Text(
        text =
          if (isInRoom) {
            if (isHost) stringResource(R.string.hosting_room) else stringResource(R.string.in_room)
          } else {
            stringResource(R.string.listen_together)
          },
      )
    },
  ) {
    item {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val room = roomState
        if (isInRoom && room != null) {
          echo.music.iad1tya.ui.component.StageRoomHero(
            roomCode = room.roomCode,
            label =
              if (isHost) {
                stringResource(R.string.stage_your_room) + " · " + connectionState.stageLabel()
              } else {
                stringResource(R.string.listen_together_you_are_guest)
              },
          )
          if (isHost) {
            echo.music.iad1tya.ui.component.StageRoomCopyButtons(roomCode = room.roomCode)
          }

          Text(
            text = stringResource(R.string.stage_in_the_room),
            fontSize = 19.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Column {
            room.users
              .filter { it.isConnected }
              .forEach { user ->
                echo.music.iad1tya.ui.component.StageRoomUserRow(
                  user = user,
                  isCurrentUser = user.userId == userId,
                  enabled = isHost && user.userId != userId,
                  onClick = {
                    selectedUserForMenu = user.userId
                    selectedUsername = user.username
                  },
                )
              }
          }

          if (isHost && pendingJoinRequests.isNotEmpty()) {
            Text(
              text = stringResource(R.string.listen_together_join_requests),
              fontSize = 19.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Column {
              pendingJoinRequests.forEach { request ->
                echo.music.iad1tya.ui.component.StageRoomRequestRow(
                  username = request.username,
                  onApprove = { listenTogetherManager.approveJoin(request.userId) },
                  onReject = {
                    listenTogetherManager.rejectJoin(request.userId, "Rejected by host")
                  },
                )
              }
            }
          }

          if (isHost && pendingSuggestions.isNotEmpty()) {
            Text(
              text = stringResource(R.string.pending_suggestions),
              fontSize = 19.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Column {
              pendingSuggestions.forEach { suggestion ->
                echo.music.iad1tya.ui.component.StageRoomSuggestionRow(
                  title = suggestion.trackInfo.title,
                  fromUsername = suggestion.fromUsername,
                  onApprove = { listenTogetherManager.approveSuggestion(suggestion.suggestionId) },
                  onReject = {
                    listenTogetherManager.rejectSuggestion(
                      suggestion.suggestionId,
                      "Rejected by host"
                    )
                  },
                )
              }
            }
          }

          echo.music.iad1tya.ui.component.StageRoomConnection(
            connectionState = connectionState,
            onConnect = { listenTogetherManager.connect() },
            onDisconnect = { listenTogetherManager.disconnect() },
            onReconnect = { listenTogetherManager.forceReconnect() },
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StagePillButton(
              onClick = {
                listenTogetherManager.leaveRoom()
                onDismiss()
              },
              primary = false,
            ) {
              Text(stringResource(R.string.leave_room))
            }
            StagePillButton(onClick = onDismiss, primary = false) {
              Text(stringResource(R.string.cancel))
            }
          }
        } else {
          val hasUsername = usernameInput.trim().isNotBlank() || savedUsername.isNotBlank()
          echo.music.iad1tya.ui.component.StageRoomJoinForm(
            roomCode = roomCodeInput,
            onRoomCodeChange = {
              roomCodeInput = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8)
            },
            username = usernameInput,
            onUsernameChange = { usernameInput = it },
            joinEnabled = hasUsername && roomCodeInput.length == 8,
            createEnabled = hasUsername,
            isJoining = isJoiningRoom,
            errorMessage = joinErrorMessage,
            onJoin = {
              val username = usernameInput.takeIf { it.isNotBlank() } ?: savedUsername
              val finalUsername = username.trim()
              if (finalUsername.isNotBlank()) {
                savedUsername = finalUsername
                Toast.makeText(
                    context,
                    context.getString(R.string.joining_room, roomCodeInput),
                    Toast.LENGTH_SHORT
                  )
                  .show()
                isJoiningRoom = true
                isCreatingRoom = false
                joinErrorMessage = null
                listenTogetherManager.connect()
                listenTogetherManager.joinRoom(roomCodeInput, finalUsername)
              } else {
                Toast.makeText(context, R.string.error_username_empty, Toast.LENGTH_SHORT).show()
              }
            },
            onCreate = {
              val username = usernameInput.takeIf { it.isNotBlank() } ?: savedUsername
              val finalUsername = username.trim()
              if (finalUsername.isNotBlank()) {
                savedUsername = finalUsername
                Toast.makeText(context, R.string.creating_room, Toast.LENGTH_SHORT).show()
                isCreatingRoom = true
                isJoiningRoom = false
                joinErrorMessage = null
                listenTogetherManager.connect()
                listenTogetherManager.createRoom(finalUsername)
              } else {
                Toast.makeText(context, R.string.error_username_empty, Toast.LENGTH_SHORT).show()
              }
            },
          )

          echo.music.iad1tya.ui.component.StageRoomConnection(
            connectionState = connectionState,
            onConnect = { listenTogetherManager.connect() },
            onDisconnect = { listenTogetherManager.disconnect() },
            onReconnect = { listenTogetherManager.forceReconnect() },
          )

          if (connectionState == ConnectionState.CONNECTED) {
            Text(
              text = stringResource(R.string.listen_together_background_disconnect_note),
              fontSize = 13.sp,
              lineHeight = 17.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          StagePillButton(onClick = onDismiss, primary = false) {
            Text(stringResource(R.string.cancel))
          }
        }
      }
    }
  }
}

@Composable
fun SpeedDialog(onDismiss: () -> Unit) {
  val playerConnection = LocalPlayerConnection.current ?: return
  var speed by remember { mutableFloatStateOf(playerConnection.player.playbackParameters.speed) }
  val updatePlaybackParameters = {
    playerConnection.player.playbackParameters = PlaybackParameters(speed, speed)
  }
  val listenTogetherManager = echo.music.iad1tya.LocalListenTogetherManager.current
  val isInRoom = listenTogetherManager?.isInRoom ?: false

  echo.music.iad1tya.ui.component.DefaultDialog(
    onDismiss = onDismiss,
    title = { Text(stringResource(R.string.speed)) },
    buttons = {
      echo.music.iad1tya.ui.component.StageButton(
        text = stringResource(android.R.string.ok),
        onClick = onDismiss,
      )
      echo.music.iad1tya.ui.component.StageButton(
        text = stringResource(R.string.reset),
        primary = false,
        onClick = {
          speed = 1f
          updatePlaybackParameters()
        },
      )
    },
  ) {
    if (!isInRoom) {
      ValueAdjuster(
        icon = R.drawable.speed,
        label = stringResource(R.string.speed),
        currentValue = speed,
        values = (0..35).map { round((0.25f + it * 0.05f) * 100) / 100 },
        onValueUpdate = {
          speed = it
          updatePlaybackParameters()
        },
        valueText = { "%.2fx".format(it) },
      )
    }
  }
}
