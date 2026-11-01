package echo.music.iad1tya.ui.screens.library

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.spotify.SpotifyMapper
import echo.music.iad1tya.ui.component.ListItem
import echo.music.iad1tya.ui.component.IconButton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import echo.music.iad1tya.spotify.models.SpotifyHomeFeedItem
import echo.music.iad1tya.ui.component.StageSourceCard
import echo.music.iad1tya.ui.component.StageSourcePinnedCard
import echo.music.iad1tya.ui.component.stageSourceSection
import echo.music.iad1tya.ui.component.StageTopBar
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.joinByBullet
import echo.music.iad1tya.utils.listItemShape

private const val SPOTIFY_MINE = "spotify_mine"
private const val SPOTIFY_SAVED = "spotify_saved"
private const val SPOTIFY_ARTISTS = "spotify_artists_all"
private const val SPOTIFY_LIKED_SONGS_COVER = "https://misc.scdn.co/liked-songs/liked-songs-640.png"

/** The Spotify tab on its own page, opened from the Home card. */
@Composable
fun SpotifyLibraryPage(navController: NavController) {
  LibrarySpotifyScreen(navController, filterContent = {})

  StageTopBar(
    title = { Text(stringResource(R.string.stage_spotify)) },
    navigationIcon = {
      IconButton(
        onClick = navController::navigateUp,
        onLongClick = navController::backToMain,
      ) {
        Icon(
          painterResource(R.drawable.arrow_back),
          contentDescription = null,
        )
      }
    }
  )
}

@Composable
fun LibrarySpotifyScreen(
  navController: NavController,
  filterContent: @Composable () -> Unit,
  viewModel: LibrarySpotifyViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()
  val lifecycleOwner = LocalLifecycleOwner.current
  var seeAll by rememberSaveable { mutableStateOf<String?>(null) }
  val seeAllLabel = stringResource(R.string.stage_see_all)
  val minePlaylistsTitle = stringResource(R.string.stage_your_playlists)
  val savedPlaylistsTitle = stringResource(R.string.stage_saved_playlists)
  val artistsTitle = stringResource(R.string.filter_artists)
  val (minePlaylists, savedPlaylists) =
    remember(uiState.playlists, uiState.accountName) {
      uiState.playlists.partition { it.owner?.displayName == uiState.accountName }
    }

  BackHandler(enabled = seeAll != null) { seeAll = null }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
      item(key = "filter") {
        filterContent()
      }

      if (uiState.isLoading && uiState.playlists.isEmpty()) {
        item(key = "loading") {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(36.dp),
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      } else if (!uiState.isAuthenticated) {
        item(key = "connect_card") {
          SpotifyConnectBanner(
            onConnect = {
              navController.navigate("settings/account?highlightKey=Spotify")
            },
            onRefresh = {
              viewModel.refresh()
            }
          )
        }
      } else {
        if (seeAll == null) {
          item(key = "liked_songs") {
            StageSourcePinnedCard(
              title = stringResource(R.string.spotify_liked_songs),
              subtitle =
                uiState.likedSongsCount?.let {
                  pluralStringResource(R.plurals.n_song, it, it)
                },
              iconRes = R.drawable.favorite,
              colors = listOf(Color(0xFF450AF5), Color(0xFF8E8EE5)),
              onClick = {
                val titleEnc = Uri.encode("Liked Songs")
                val coverEnc = Uri.encode(SPOTIFY_LIKED_SONGS_COVER)
                navController.navigate("spotify_playlist/liked?title=$titleEnc&cover=$coverEnc")
              },
              modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 8.dp),
            )
          }

          listOf(
              SPOTIFY_MINE to (minePlaylistsTitle to minePlaylists),
              SPOTIFY_SAVED to (savedPlaylistsTitle to savedPlaylists),
            )
            .forEach { (key, section) ->
              stageSourceSection(
                key = key,
                title = section.first,
                items = section.second,
                itemKey = { it.id },
                seeAllLabel = seeAllLabel,
                onSeeAll = { seeAll = key },
              ) { playlist ->
                val coverUrl = SpotifyMapper.getPlaylistThumbnail(playlist)
                StageSourceCard(
                  title = playlist.name,
                  subtitle = playlist.tracks?.total?.let { "$it songs" },
                  imageUrl = coverUrl,
                  onClick = {
                    val titleEnc = Uri.encode(playlist.name)
                    val coverEnc = Uri.encode(coverUrl.orEmpty())
                    navController.navigate(
                      "spotify_playlist/${playlist.id}?title=$titleEnc&cover=$coverEnc"
                    )
                  },
                )
              }
            }

          stageSourceSection(
            key = "spotify_artists",
            title = artistsTitle,
            items = uiState.artists,
            itemKey = { it.id },
            seeAllLabel = seeAllLabel,
            onSeeAll = { seeAll = SPOTIFY_ARTISTS },
          ) { artist ->
            StageSourceCard(
              title = artist.name,
              subtitle = null,
              imageUrl = artist.images.firstOrNull()?.url,
              circular = true,
              onClick = {
                navController.navigate("search/${Uri.encode(artist.name)}")
              },
            )
          }

          uiState.feedSections.forEachIndexed { index, section ->
            stageSourceSection(
              key = "spotify_feed_$index",
              title = section.title.orEmpty(),
              items = section.items.distinctBy { it.uri },
              itemKey = { it.uri },
            ) { item ->
              when (item) {
                is SpotifyHomeFeedItem.Playlist ->
                  StageSourceCard(
                    title = item.name,
                    subtitle = item.description ?: item.ownerName,
                    imageUrl = item.imageUrl,
                    onClick = {
                      val titleEnc = Uri.encode(item.name)
                      val coverEnc = Uri.encode(item.imageUrl.orEmpty())
                      navController.navigate(
                        "spotify_playlist/${item.id}?title=$titleEnc&cover=$coverEnc"
                      )
                    },
                  )
                is SpotifyHomeFeedItem.Album -> {
                  val artistNames = item.artists.joinToString { it.name }
                  StageSourceCard(
                    title = item.name,
                    subtitle = artistNames,
                    imageUrl = item.imageUrl,
                    onClick = {
                      navController.navigate("search/${Uri.encode("${item.name} $artistNames")}")
                    },
                  )
                }
                is SpotifyHomeFeedItem.Artist ->
                  StageSourceCard(
                    title = item.name,
                    subtitle = null,
                    imageUrl = item.imageUrl,
                    circular = true,
                    onClick = { navController.navigate("search/${Uri.encode(item.name)}") },
                  )
              }
            }
          }
        } else if (seeAll == SPOTIFY_ARTISTS) {
          items(items = uiState.artists, key = { it.id }) { artist ->
            ListItem(
              title = artist.name,
              subtitle = {},
              thumbnailContent = {
                AsyncImage(
                  model = artist.images.firstOrNull()?.url,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                )
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("search/${Uri.encode(artist.name)}") },
            )
          }
        } else {
          itemsIndexed(
            items = if (seeAll == SPOTIFY_MINE) minePlaylists else savedPlaylists,
            key = { _, pl -> pl.id }
          ) { index, playlist ->
            val coverUrl = SpotifyMapper.getPlaylistThumbnail(playlist)
            val owner = playlist.owner?.displayName?.takeIf { it.isNotBlank() }
            val countText = playlist.tracks?.total?.let { "$it songs" }
            val totalPlaylists = if (seeAll == SPOTIFY_MINE) minePlaylists.size else savedPlaylists.size

            ListItem(
              title = playlist.name,
              subtitle = {
                val sub = joinByBullet(owner, countText)
                if (sub.isNotBlank()) {
                  Text(
                    text = sub,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              },
              thumbnailContent = {
                AsyncImage(
                  model = coverUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                )
              },
              shape = listItemShape(index, totalPlaylists),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val titleEnc = Uri.encode(playlist.name)
                  val coverEnc = Uri.encode(coverUrl.orEmpty())
                  navController.navigate("spotify_playlist/${playlist.id}?title=$titleEnc&cover=$coverEnc")
                },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SpotifyConnectBanner(
  onConnect: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp, top = 26.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .size(84.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_spotify),
        contentDescription = null,
        tint = Color(0xFF1DB954),
        modifier = Modifier.size(38.dp),
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.spotify_live_library),
      fontSize = 23.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = stringResource(R.string.spotify_live_desc),
      fontSize = 15.sp,
      lineHeight = 21.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = onConnect,
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
      ),
      modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(
        text = stringResource(R.string.spotify_connect_account),
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
      onClick = onRefresh,
    ) {
      Icon(
        painter = painterResource(R.drawable.cached),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Already connected? Tap to refresh",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

