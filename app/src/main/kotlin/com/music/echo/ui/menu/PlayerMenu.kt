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
        androidx.compose.foundation.Image(
          painter = painterResource(R.drawable.songdna),
          contentDescription = null,
          modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.CircleShape)
        )
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
                Text("Movie / Album", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(dnaAlbum, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Column {
                Text("About the Song", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(dnaMeaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column {
                Text("About the Artist", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
      val infiniteTransition = rememberInfiniteTransition()
      val animatedColor by
        infiniteTransition.animateColor(
          initialValue = MaterialTheme.colorScheme.primary,
          targetValue = MaterialTheme.colorScheme.tertiary,
          animationSpec =
            infiniteRepeatable(
              animation = tween(400, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            ),
          label = "dnaColor"
        )
      val isInLibrary = librarySong?.song?.inLibrary != null
      val (queueLockedPref) = rememberPreference(QueueEditLockKey, defaultValue = false)
      val effectiveQueueLocked = isQueueLocked ?: queueLockedPref

      val timeline = playerConnection.player.currentTimeline
      val currentIdx = playerConnection.player.currentMediaItemIndex
      val hasUpcomingSongs =
        !timeline.isEmpty &&
          currentIdx != C.INDEX_UNSET &&
          timeline.getNextWindowIndex(
            currentIdx,
            Player.REPEAT_MODE_OFF,
            playerConnection.player.shuffleModeEnabled
          ) != C.INDEX_UNSET

      val tiles = buildList {
        if (artists.isNotEmpty()) {
          add(
            PlayerMenuTile(
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
            icon = { PlayerMenuTileIcon(R.drawable.playlist_add) },
            label = { PlayerMenuTileLabel(stringResource(R.string.add_to_an_playlist)) },
            onClick = { showChoosePlaylistDialog = true }
          )
        )
        add(
          PlayerMenuTile(
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
            icon = {
              androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.songdna),
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(CircleShape)
              )
            },
            label = { PlayerMenuTileLabel("Song DNA", color = animatedColor) },
            onClick = { showSongDnaDialog = true }
          )
        )
        add(
          PlayerMenuTile(
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
        if (!isListenTogetherGuest && !effectiveQueueLocked && !inSelectMode && hasUpcomingSongs) {
          add(
            PlayerMenuTile(
              icon = { PlayerMenuTileIcon(R.drawable.clear_all) },
              label = { PlayerMenuTileLabel(stringResource(R.string.clear_queue)) },
              onClick = {
                if (onClearQueue != null) {
                  onClearQueue()
                  onDismiss()
                } else {
                  showClearQueueDialog = true
                }
              }
            )
          )
        }
        add(
          when (download?.state) {
            Download.STATE_COMPLETED ->
              PlayerMenuTile(
                icon = { PlayerMenuTileIcon(R.drawable.offline) },
                label = { PlayerMenuTileLabel(stringResource(R.string.remove_download)) },
                onClick = {
                  DownloadService.sendRemoveDownload(
                    context,
                    ExoDownloadService::class.java,
                    mediaMetadata.id,
                    false,
                  )
                }
              )
            Download.STATE_QUEUED,
            Download.STATE_DOWNLOADING ->
              PlayerMenuTile(
                icon = {
                  CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                },
                label = { PlayerMenuTileLabel(stringResource(R.string.downloading)) },
                onClick = {
                  DownloadService.sendRemoveDownload(
                    context,
                    ExoDownloadService::class.java,
                    mediaMetadata.id,
                    false,
                  )
                }
              )
            else ->
              PlayerMenuTile(
                icon = { PlayerMenuTileIcon(R.drawable.download) },
                label = { PlayerMenuTileLabel(stringResource(R.string.action_download)) },
                onClick = {
                  database.transaction { insert(mediaMetadata) }
                  val downloadRequest =
                    DownloadRequest.Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                      .setCustomCacheKey(mediaMetadata.id)
                      .setData(mediaMetadata.title.toByteArray())
                      .build()
                  DownloadService.sendAddDownload(
                    context,
                    ExoDownloadService::class.java,
                    downloadRequest,
                    false,
                  )
                }
              )
          }
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
        add(
          PlayerMenuTile(
            icon = { PlayerMenuTileIcon(R.drawable.notification) },
            label = { PlayerMenuTileLabel("Set as Ringtone") },
            onClick = {
              if (ringtoneViewModel.hasSettingsPermission(context)) {
                ringtoneViewModel.showTrimmer(
                  mediaMetadata.id,
                  mediaMetadata.title,
                  mediaMetadata.artists.joinToString { it.name },
                  mediaMetadata.duration
                )
              } else {
                ringtoneViewModel.requestSettingsPermission(context)
              }
              onDismiss()
            }
          )
        )
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

    item {
      // Draws nothing where Cast is unavailable, so the tile collapses with it.
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .padding(start = PlayerMenuTilePadding, end = PlayerMenuTilePadding, bottom = 18.dp)
            .clip(PlayerMenuTileShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      ) {
        echo.music.iad1tya.ui.component.CastButton(asMenuItem = true)
      }
    }
  }
}

private val PlayerMenuTileShape = RoundedCornerShape(18.dp)
private val PlayerMenuTilePadding = 18.dp

private class PlayerMenuTile(
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
    fontSize = 13.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
}

/** Three tiles per row, each row as tall as its tallest label. */
@Composable
private fun PlayerMenuTileGrid(tiles: List<PlayerMenuTile>) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier =
      Modifier.fillMaxWidth().padding(start = PlayerMenuTilePadding, top = 10.dp, end = PlayerMenuTilePadding, bottom = 8.dp),
  ) {
    tiles.chunked(3).forEach { row ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
      ) {
        row.forEach { tile ->
          Surface(
            onClick = tile.onClick,
            shape = PlayerMenuTileShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).fillMaxHeight(),
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
              modifier =
                Modifier.heightIn(min = 76.dp).padding(horizontal = 6.dp, vertical = 8.dp),
            ) {
              tile.icon()
              tile.label()
            }
          }
        }
        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.listen_together),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.listen_together_not_configured),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Text(stringResource(android.R.string.ok))
          }
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
    ListDialog(
      onDismiss = {
        selectedUserForMenu = null
        selectedUsername = null
      }
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Start
        ) {
          Icon(
            painter = painterResource(R.drawable.group),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
              text = stringResource(R.string.manage_user),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = selectedUsername ?: "",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(12.dp)) }

      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable {
              selectedUserForMenu?.let { listenTogetherManager.kickUser(it, "Removed by host") }
              selectedUserForMenu = null
              selectedUsername = null
            },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(
              painter = painterResource(R.drawable.close),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = stringResource(R.string.kick_user),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
              )
              Text(
                text = stringResource(R.string.kick_user_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }

      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable {
              selectedUserForMenu?.let { userId ->
                selectedUsername?.let { username ->
                  listenTogetherManager.blockUser(username)
                  listenTogetherManager.kickUser(userId, R.string.user_blocked_by_host.toString())
                }
              }
              selectedUserForMenu = null
              selectedUsername = null
            },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(
              painter = painterResource(R.drawable.close),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = stringResource(R.string.permanently_kick_user),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = stringResource(R.string.permanently_kick_user_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }

      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable {
              selectedUserForMenu?.let { listenTogetherManager.transferHost(it) }
              selectedUserForMenu = null
              selectedUsername = null
            },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(
              painter = painterResource(R.drawable.crown),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = stringResource(R.string.transfer_ownership),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = stringResource(R.string.transfer_ownership_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }
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

  ListDialog(onDismiss = onDismiss) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
      ) {
        Icon(
          painter = painterResource(R.drawable.group),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text =
            if (isInRoom) {
              if (isHost) stringResource(R.string.hosting_room)
              else stringResource(R.string.in_room)
            } else {
              stringResource(R.string.listen_together)
            },
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    item {
      Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color =
          when (connectionState) {
            ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ConnectionState.CONNECTING,
            ConnectionState.RECONNECTING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ConnectionState.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
          }
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier =
                Modifier.size(10.dp)
                  .background(
                    color =
                      when (connectionState) {
                        ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                        ConnectionState.CONNECTING,
                        ConnectionState.RECONNECTING -> MaterialTheme.colorScheme.secondary
                        ConnectionState.ERROR -> MaterialTheme.colorScheme.error
                        ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.outline
                      },
                    shape = RoundedCornerShape(50)
                  )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text =
                when (connectionState) {
                  ConnectionState.CONNECTED -> stringResource(R.string.listen_together_connected)
                  ConnectionState.CONNECTING -> stringResource(R.string.listen_together_connecting)
                  ConnectionState.RECONNECTING ->
                    stringResource(R.string.listen_together_reconnecting)
                  ConnectionState.ERROR -> stringResource(R.string.listen_together_error)
                  ConnectionState.DISCONNECTED ->
                    stringResource(R.string.listen_together_disconnected)
                },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color =
                when (connectionState) {
                  ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                  ConnectionState.CONNECTING,
                  ConnectionState.RECONNECTING -> MaterialTheme.colorScheme.secondary
                  ConnectionState.ERROR -> MaterialTheme.colorScheme.error
                  ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
          }

          if (
            connectionState == ConnectionState.CONNECTING ||
              connectionState == ConnectionState.RECONNECTING
          ) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            if (
              connectionState == ConnectionState.DISCONNECTED ||
                connectionState == ConnectionState.ERROR
            ) {
              Button(
                onClick = { listenTogetherManager.connect() },
                modifier = Modifier.weight(1f),
                colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Text(stringResource(R.string.connect), fontWeight = FontWeight.SemiBold)
              }
            } else {
              Button(
                onClick = { listenTogetherManager.disconnect() },
                modifier = Modifier.weight(1f),
                colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Text(stringResource(R.string.disconnect), fontWeight = FontWeight.SemiBold)
              }
              FilledTonalButton(
                onClick = { listenTogetherManager.forceReconnect() },
                modifier = Modifier.weight(1f)
              ) {
                Text("Reconnect", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(12.dp)) }

    if (connectionState == ConnectionState.CONNECTED && !isInRoom) {
      item {
        Text(
          text = stringResource(R.string.listen_together_background_disconnect_note),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 24.dp),
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
      }
    }

    if (isInRoom) {

      roomState?.let { room ->
        item {
          Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = stringResource(R.string.room_code),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text(
                  text = room.roomCode,
                  style = MaterialTheme.typography.headlineLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 6.sp
                )
              }
              if (isHost) {
                Spacer(modifier = Modifier.height(12.dp))
                val inviteLink =
                  remember(room.roomCode) {
                    "https://metroserverx.meowery.eu/listen?code=${room.roomCode}"
                  }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StagePillButton(onClick = {
                      val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE)
                          as android.content.ClipboardManager
                      val clip = android.content.ClipData.newPlainText("Room Code", room.roomCode)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                        .show()
                    }) {
                    Icon(
                      painter = painterResource(R.drawable.content_copy),
                      contentDescription = stringResource(R.string.copy_code),
                      modifier = Modifier.size(18.dp)
                    )
                    
                    Text(stringResource(R.string.copy_code))
                  }
        StagePillButton(onClick = {
                      val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE)
                          as android.content.ClipboardManager
                      val clip =
                        android.content.ClipData.newPlainText("Listen Together Link", inviteLink)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                        .show()
                    }, primary = false) {
                    Icon(
                      painter = painterResource(R.drawable.link),
                      contentDescription = stringResource(R.string.copy_link),
                      modifier = Modifier.size(18.dp)
                    )
                    
                    Text(stringResource(R.string.copy_link))
                  }
      }
              }
            }
          }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        val connectedUsers = room.users.filter { it.isConnected }

        item {
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
              text = stringResource(R.string.connected_users, connectedUsers.size),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              connectedUsers.forEach { user ->
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier =
                    Modifier.width(72.dp)
                      .clickable(
                        enabled = isHost && user.userId != userId,
                        onClick = {
                          selectedUserForMenu = user.userId
                          selectedUsername = user.username
                        }
                      )
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Surface(
                      modifier = Modifier.size(52.dp),
                      shape = RoundedCornerShape(50),
                      color =
                        if (user.isHost) {
                          MaterialTheme.colorScheme.primary
                        } else if (user.userId == userId) {
                          MaterialTheme.colorScheme.secondary
                        } else {
                          MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                          text = user.username.take(1).uppercase(),
                          style = MaterialTheme.typography.titleLarge,
                          fontWeight = FontWeight.Bold,
                          color =
                            if (user.isHost) {
                              MaterialTheme.colorScheme.onPrimary
                            } else if (user.userId == userId) {
                              MaterialTheme.colorScheme.onSecondary
                            } else {
                              MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                      }
                    }

                    if (user.isHost || user.userId == userId) {
                      Surface(
                        modifier =
                          Modifier.align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(18.dp),
                        shape = RoundedCornerShape(50),
                        color =
                          if (user.isHost) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.secondary
                      ) {
                        Box(
                          contentAlignment = Alignment.Center,
                          modifier = Modifier.fillMaxSize()
                        ) {
                          Icon(
                            painter =
                              painterResource(
                                if (user.isHost) R.drawable.crown else R.drawable.person
                              ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                          )
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = user.username,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (user.userId == userId) FontWeight.Bold else FontWeight.Medium,
                    color =
                      if (user.isHost) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                  )

                  if (user.isHost) {
                    Text(
                      text = stringResource(R.string.host_label),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                  } else if (user.userId == userId) {
                    Text(
                      text = stringResource(R.string.you_label),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                  }
                }
              }
            }
          }
        }

        if (isHost && pendingJoinRequests.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = stringResource(R.string.pending_requests),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(pendingJoinRequests) { request ->
            Surface(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondary
                  ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                      Text(
                        text = request.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                      )
                    }
                  }
                  Text(
                    text = request.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(onClick = { listenTogetherManager.approveJoin(request.userId) }) {
                    Icon(
                      painter = painterResource(R.drawable.check),
                      contentDescription = stringResource(R.string.approve),
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  IconButton(
                    onClick = {
                      listenTogetherManager.rejectJoin(request.userId, "Rejected by host")
                    }
                  ) {
                    Icon(
                      painter = painterResource(R.drawable.close),
                      contentDescription = stringResource(R.string.reject),
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
              }
            }
          }
        }

        if (isHost && pendingSuggestions.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = stringResource(R.string.pending_suggestions),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(pendingSuggestions) { suggestion ->
            Surface(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    painter = painterResource(R.drawable.queue_music),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = suggestion.trackInfo.title,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = suggestion.fromUsername,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(
                    onClick = { listenTogetherManager.approveSuggestion(suggestion.suggestionId) }
                  ) {
                    Icon(
                      painter = painterResource(R.drawable.check),
                      contentDescription = stringResource(R.string.approve),
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  IconButton(
                    onClick = {
                      listenTogetherManager.rejectSuggestion(
                        suggestion.suggestionId,
                        "Rejected by host"
                      )
                    }
                  ) {
                    Icon(
                      painter = painterResource(R.drawable.close),
                      contentDescription = stringResource(R.string.reject),
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(20.dp))
          Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StagePillButton(onClick = {
                listenTogetherManager.leaveRoom()
                onDismiss()
              }) {
              Icon(
                painter = painterResource(R.drawable.logout),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              
              Text(stringResource(R.string.leave_room), fontWeight = FontWeight.SemiBold)
            }
        StagePillButton(onClick = onDismiss, primary = false) {
              Text(stringResource(R.string.cancel), fontWeight = FontWeight.Medium)
            }
      }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    } else {

      item {
        Surface(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = stringResource(R.string.listen_together_description),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            OutlinedTextField(
              value = usernameInput,
              onValueChange = { usernameInput = it },
              label = { Text(stringResource(R.string.username)) },
              placeholder = { Text(stringResource(R.string.enter_username)) },
              leadingIcon = {
                Icon(
                  painterResource(R.drawable.person),
                  null,
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              trailingIcon = {
                if (usernameInput.isNotBlank()) {
                  IconButton(onClick = { usernameInput = "" }) {
                    Icon(painterResource(R.drawable.close), null)
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                  focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
              modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text(
              text = stringResource(R.string.join_existing_room),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
              value = roomCodeInput,
              onValueChange = {
                roomCodeInput = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8)
              },
              label = { Text(stringResource(R.string.room_code)) },
              placeholder = { Text("ABCD1234") },
              supportingText = {
                Text(
                  text = "${roomCodeInput.length}/8",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              leadingIcon = {
                Icon(
                  painterResource(R.drawable.token),
                  null,
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                  focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
              modifier = Modifier.fillMaxWidth()
            )

            if (isJoiningRoom) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = waitingForApprovalText,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            joinErrorMessage?.let { msg ->
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(12.dp)
                ) {
                  Icon(
                    painterResource(R.drawable.error),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
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
              modifier = Modifier.weight(1f),
              enabled = (usernameInput.trim().isNotBlank() || savedUsername.isNotBlank()),
              colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(R.string.create_room), fontWeight = FontWeight.SemiBold)
            }

            if (roomCodeInput.length == 8) {
              Button(
                onClick = {
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
                    Toast.makeText(context, R.string.error_username_empty, Toast.LENGTH_SHORT)
                      .show()
                  }
                },
                modifier = Modifier.weight(1f),
                enabled = (usernameInput.trim().isNotBlank() || savedUsername.isNotBlank()),
                colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
              ) {
                Icon(
                  painter = painterResource(R.drawable.login),
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.join_room), fontWeight = FontWeight.SemiBold)
              }
            }
          }

          TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(
              stringResource(R.string.cancel),
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
