package echo.music.iad1tya.ui.screens.playlist

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.models.WatchEndpoint
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.spotify.SpotifyMapper
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.ui.component.ListItem
import echo.music.iad1tya.ui.component.PlayingIndicator
import echo.music.iad1tya.ui.component.StageHero
import echo.music.iad1tya.ui.component.StageHeroArtwork
import echo.music.iad1tya.ui.component.StageHeroIconButton
import echo.music.iad1tya.ui.component.StageHeroPlayButton
import echo.music.iad1tya.ui.component.StageHeroShuffleButton
import echo.music.iad1tya.ui.component.rememberStageHeroUnderBar
import echo.music.iad1tya.utils.joinByBullet
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.makeTimeString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyPlaylistDetailScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  playlistId: String,
  playlistTitle: String,
  playlistCover: String,
  viewModel: SpotifyPlaylistDetailViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()
  val playerConnection = LocalPlayerConnection.current
  val mediaMetadata by playerConnection?.mediaMetadata?.collectAsState()
    ?: remember { androidx.compose.runtime.mutableStateOf(null) }
  val isPlaying by playerConnection?.isEffectivelyPlaying?.collectAsState()
    ?: remember { androidx.compose.runtime.mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val lazyListState = rememberLazyListState()
  val context = LocalContext.current

  val effectiveTitle = uiState.title.ifBlank { playlistTitle }
  val effectiveCover = uiState.coverUrl ?: playlistCover

  val isPlaylistCurrent = uiState.tracks.any {
    it.name.equals(mediaMetadata?.title, ignoreCase = true)
  }
  val isPlaylistPlaying = isPlaying && isPlaylistCurrent

  val transparentAppBar by rememberStageHeroUnderBar(lazyListState)

  Box(modifier = Modifier.fillMaxSize()) {
    if (uiState.isLoading && uiState.tracks.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(36.dp),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    } else {
      LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues(),
      ) {
        // Playlist Header Hero Section
        item(key = "header") {
          SpotifyPlaylistHeader(
            title = effectiveTitle,
            coverUrl = effectiveCover,
            tracks = uiState.tracks,
            totalCount = if (uiState.totalCount > 0) uiState.totalCount else uiState.tracks.size,
            isPlaylistPlaying = isPlaylistPlaying,
            onPlayClick = {
              if (isPlaylistCurrent) {
                playerConnection?.togglePlayPause()
              } else {
                uiState.tracks.firstOrNull()?.let { firstTrack ->
                  viewModel.playTrack(
                    track = firstTrack,
                    onSuccess = { songItem ->
                      playerConnection?.playQueue(
                        YouTubeQueue(
                          endpoint = WatchEndpoint(videoId = songItem.id),
                          preloadItem = songItem.toMediaMetadata(),
                        )
                      )
                    },
                    onError = {
                      coroutineScope.launch {
                        snackbarHostState.showSnackbar("Unable to match stream on YouTube/Addon")
                      }
                    }
                  )
                }
              }
            },
            onShuffle = {
              val shuffled = uiState.tracks.shuffled()
              shuffled.firstOrNull()?.let { firstTrack ->
                viewModel.playTrack(
                  track = firstTrack,
                  onSuccess = { songItem ->
                    playerConnection?.playQueue(
                      YouTubeQueue(
                        endpoint = WatchEndpoint(videoId = songItem.id),
                        preloadItem = songItem.toMediaMetadata(),
                      )
                    )
                  },
                  onError = {
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Unable to match stream on YouTube/Addon")
                    }
                  }
                )
              }
            },
            onShare = {
              val shareUrl = if (playlistId == "liked") {
                "https://open.spotify.com/collection/tracks"
              } else {
                "https://open.spotify.com/playlist/$playlistId"
              }
              val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$effectiveTitle: $shareUrl")
              }
              context.startActivity(Intent.createChooser(intent, null))
            }
          )
        }

        // Track List Items (styled as grouped Nothing OS cards)
        itemsIndexed(
          items = uiState.tracks,
          key = { index, track -> "${track.id}_$index" }
        ) { index, track ->
          val isCurrentSong = mediaMetadata?.title.equals(track.name, ignoreCase = true)
          val isResolving = uiState.resolvingTrackId == track.id
          val coverUrl = SpotifyMapper.getTrackThumbnail(track)
          val durationText = if (track.durationMs > 0) makeTimeString(track.durationMs.toLong()) else null

          ListItem(
            title = track.name,
            subtitle = {
              val artistsText = track.artists.joinToString(", ") { it.name }
              val sub = joinByBullet(artistsText, durationText)
              if (sub.isNotBlank()) {
                Text(
                  text = sub,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            },
            thumbnailContent = {
              Box(
                modifier = Modifier.size(ListThumbnailSize),
                contentAlignment = Alignment.Center,
              ) {
                AsyncImage(
                  model = coverUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                )

                if (isResolving) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .clip(RoundedCornerShape(ThumbnailCornerRadius))
                      .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp,
                      color = Color.White,
                    )
                  }
                } else if (isCurrentSong) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .clip(RoundedCornerShape(ThumbnailCornerRadius))
                      .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isPlaying) {
                      PlayingIndicator(
                        color = Color.White,
                        modifier = Modifier.height(20.dp),
                      )
                    } else {
                      Icon(
                        painter = painterResource(R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                      )
                    }
                  }
                }
              }
            },
            trailingContent = {
              IconButton(
                onClick = {
                  val trackUrl = "https://open.spotify.com/track/${track.id}"
                  val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${track.name}: $trackUrl")
                  }
                  context.startActivity(Intent.createChooser(intent, null))
                }
              ) {
                Icon(
                  painter = painterResource(R.drawable.more_vert),
                  contentDescription = "More",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
              }
            },
            shape = listItemShape(index, uiState.tracks.size),
            isActive = isCurrentSong,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                if (isCurrentSong) {
                  playerConnection?.togglePlayPause()
                } else {
                  viewModel.playTrack(
                    track = track,
                    onSuccess = { songItem ->
                      playerConnection?.playQueue(
                        YouTubeQueue(
                          endpoint = WatchEndpoint(videoId = songItem.id),
                          preloadItem = songItem.toMediaMetadata(),
                        )
                      )
                    },
                    onError = {
                      coroutineScope.launch {
                        snackbarHostState.showSnackbar("Could not match \"${track.name}\" to a playable stream")
                      }
                    }
                  )
                }
              },
          )
        }
      }
    }

    // Transparent TopAppBar overlaid directly inside Box over the LazyColumn
    TopAppBar(
      title = {
        AnimatedVisibility(
          visible = !transparentAppBar,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Text(
            text = effectiveTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      },
      navigationIcon = {
        IconButton(
          onClick = { navController.navigateUp() },
          modifier = Modifier.background(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            CircleShape
          )
        ) {
          Icon(
            painter = painterResource(R.drawable.arrow_back),
            contentDescription = "Back",
          )
        }
      },
      actions = {
        IconButton(
          onClick = { viewModel.refresh() },
          modifier = Modifier.background(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            CircleShape
          )
        ) {
          Icon(
            painter = painterResource(R.drawable.cached),
            contentDescription = "Refresh",
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (transparentAppBar) Color.Transparent else MaterialTheme.colorScheme.surface
      ),
      scrollBehavior = scrollBehavior,
    )

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
private fun SpotifyPlaylistHeader(
  title: String,
  coverUrl: String?,
  tracks: List<SpotifyTrack>,
  totalCount: Int,
  isPlaylistPlaying: Boolean,
  onPlayClick: () -> Unit,
  onShuffle: () -> Unit,
  onShare: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val totalDurationMs = remember(tracks) { tracks.sumOf { it.durationMs.toLong() } }

  StageHero(
    title = title,
    modifier = modifier,
    subtitle =
      buildString {
        append("Spotify Playlist • ")
        append(pluralStringResource(R.plurals.n_song, totalCount, totalCount))
        if (totalDurationMs > 0) {
          append(" • ")
          val totalSec = totalDurationMs / 1000
          val hours = totalSec / 3600
          val minutes = (totalSec % 3600) / 60
          if (hours > 0) {
            append("${hours}h ${minutes}m")
          } else {
            append("${minutes}m")
          }
        }
      },
    artwork = { StageHeroArtwork(coverUrl) },
    leadingActions = {
      StageHeroIconButton(
        icon = R.drawable.share,
        contentDescription = stringResource(R.string.share),
        onClick = onShare,
      )
    },
    trailingActions = {
      StageHeroShuffleButton(onClick = onShuffle)
      StageHeroPlayButton(onClick = onPlayClick, isPlaying = isPlaylistPlaying)
    },
  )
}
