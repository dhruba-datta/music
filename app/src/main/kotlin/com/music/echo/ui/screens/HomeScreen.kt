package echo.music.iad1tya.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import echo.music.iad1tya.LocalListenTogetherManager
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.db.entities.SpeedDialItem
import echo.music.iad1tya.playback.queues.LocalAlbumRadio
import echo.music.iad1tya.ui.component.AlbumPlayButton
import echo.music.iad1tya.ui.component.ItemThumbnail
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.ui.component.StageSheetRow
import echo.music.iad1tya.ui.component.StageSheetCards
import echo.music.iad1tya.ui.component.StageSheetTitle
import echo.music.iad1tya.utils.joinByBullet
import echo.music.iad1tya.utils.makeTimeString
import echo.music.iad1tya.utils.reportException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.echo.utils.hapticScrollEdge
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.utils.completed
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.ListItemHeight
import echo.music.iad1tya.constants.ListThumbnailSize
import echo.music.iad1tya.constants.RandomizeHomeOrderKey
import echo.music.iad1tya.constants.ShowSpeedDialKey
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.db.entities.Album
import echo.music.iad1tya.db.entities.Artist
import echo.music.iad1tya.db.entities.LocalItem
import echo.music.iad1tya.db.entities.Playlist
import echo.music.iad1tya.db.entities.PlaylistEntity
import echo.music.iad1tya.db.entities.PlaylistSongMap
import echo.music.iad1tya.db.entities.Song
import echo.music.iad1tya.extensions.toMediaItem
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.ListQueue
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.ChipsRow
import echo.music.iad1tya.ui.component.LocalBottomSheetPageState
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.SpeedDialShortcut
import echo.music.iad1tya.ui.component.SPEED_DIAL_COLUMNS
import echo.music.iad1tya.ui.component.SPEED_DIAL_PAGES
import echo.music.iad1tya.ui.component.StageHeroStrip
import echo.music.iad1tya.ui.component.StageSpeedDialPager
import echo.music.iad1tya.ui.component.RandomizeGridItem
import echo.music.iad1tya.ui.component.SongListItem
import echo.music.iad1tya.ui.component.SpeedDialGridItem
import echo.music.iad1tya.ui.component.YouTubeListItem
import echo.music.iad1tya.ui.component.shimmer.GridItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.component.shimmer.TextPlaceholder
import echo.music.iad1tya.ui.menu.AlbumMenu
import echo.music.iad1tya.ui.menu.ArtistMenu
import echo.music.iad1tya.ui.menu.SongMenu
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.ui.utils.SnapLayoutInfoProvider
import echo.music.iad1tya.ui.utils.resize
import echo.music.iad1tya.utils.listItemShape
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.CommunityPlaylistItem
import echo.music.iad1tya.viewmodels.DailyDiscoverItem
import echo.music.iad1tya.viewmodels.HomeViewModel
import kotlin.math.min
import kotlin.random.Random
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Turns text a quarter to the left, so it reads from the bottom of the card to the top. */
private fun Modifier.bottomToTopText(): Modifier = layout { measurable, constraints ->
  val placeable =
    measurable.measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxHeight,
        minHeight = 0,
        maxHeight = constraints.maxWidth,
      )
    )
  layout(placeable.height, placeable.width) {
    placeable.placeWithLayer(
      x = (placeable.height - placeable.width) / 2,
      y = (placeable.width - placeable.height) / 2,
    ) {
      rotationZ = -90f
    }
  }
}

private fun NavController.navigateToPlaylistItem(playlist: PlaylistItem) {
  when (val playlistId = playlist.id.removePrefix("VL")) {
    "LM" -> navigate("auto_playlist/liked")
    "SE" -> navigate("auto_playlist/downloaded")
    else -> navigate("online_playlist/$playlistId")
  }
}

sealed class HomeSection(val id: String, val baseWeight: Int) {
  data object SpeedDial : HomeSection("speed_dial", 100)

  data object AiRecommendations : HomeSection("ai_recommendations", 95)

  data object HeroCarousel : HomeSection("hero_carousel", 90)

  data object DailyDiscover : HomeSection("daily_discover", 80)

  data object KeepListening : HomeSection("keep_listening", 50)

  data object AccountPlaylists : HomeSection("account_playlists", 40)

  data object ForgottenFavorites : HomeSection("forgotten_favorites", 30)


  data object YouTubeMusic : HomeSection("youtube_music", 10)

  data object Spotify : HomeSection("spotify", 9)

  data class SimilarRecommendation(val index: Int) :
    HomeSection("similar_recommendation_$index", 10)

  data class HomePageSection(val index: Int) : HomeSection("home_page_section_$index", 10)

  data object MoodAndGenres : HomeSection("mood_and_genres", 5)
}

private val StageCardShape = RoundedCornerShape(18.dp)
private val StageCardArtHeight = 92.dp
private val StageCardTextHeight = 49.dp
private val StageRowPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyDiscoverCard(
  dailyDiscover: echo.music.iad1tya.viewmodels.DailyDiscoverItem,
  onClick: () -> Unit,
  navController: NavController,
  modifier: Modifier = Modifier
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current

  val song = dailyDiscover.recommendation as? SongItem
  val messages =
    listOf(
      R.string.daily_discover_sounds_like,
      R.string.daily_discover_because_you_listen_to,
      R.string.daily_discover_similar_to,
      R.string.daily_discover_based_on,
      R.string.daily_discover_for_fans_of
    )
  val messageRes =
    remember(dailyDiscover.seed.id) {
      messages[kotlin.math.abs(dailyDiscover.seed.id.hashCode()) % messages.size]
    }
  val seedName =
    dailyDiscover.seed.artists.joinToString(", ") { it.name }.ifEmpty { dailyDiscover.seed.title }

  Column(
    modifier =
      modifier.combinedClickable(
        onClick = onClick,
        onLongClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          if (song != null) {
            menuState.show {
              YouTubeSongMenu(
                song = song,
                navController = navController,
                onDismiss = { menuState.dismiss() }
              )
            }
          }
        }
      )
  ) {
    AsyncImage(
      model =
        ImageRequest.Builder(LocalContext.current)
          .data(dailyDiscover.recommendation.thumbnail?.resize(1200, 1200))
          .crossfade(true)
          .build(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxWidth().height(92.dp).clip(StageCardShape)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(messageRes, seedName),
      fontSize = 15.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = dailyDiscover.recommendation.title,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/** The 18dp art tile with a 15sp title and 13sp line under it. */
@Composable
private fun StageCard(
  title: String,
  subtitle: String?,
  modifier: Modifier = Modifier,
  artHeight: Dp = StageCardArtHeight,
  circle: Boolean = false,
  art: @Composable BoxScope.() -> Unit,
) {
  Column(modifier = modifier) {
    Box(
      modifier = Modifier.fillMaxWidth().height(artHeight),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier =
          if (circle) Modifier.size(artHeight).clip(CircleShape)
          else Modifier.fillMaxSize().clip(StageCardShape),
        contentAlignment = Alignment.Center,
        content = art
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = title,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = subtitle.orEmpty(),
      fontSize = 13.sp,
      lineHeight = 18.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/** The grey 22dp dots at the end of a song row. */
@Composable
private fun StageRowMoreButton(onClick: () -> Unit) {
  IconButton(onClick = onClick, modifier = Modifier.offset(x = 3.dp)) {
    Icon(
      painter = painterResource(R.drawable.stage_more_row),
      contentDescription = stringResource(R.string.more_label),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(22.dp)
    )
  }
}

private suspend fun speedDialQueueItems(item: YTItem, database: MusicDatabase): List<MediaItem> =
  when (item) {
    is SongItem -> listOf(item.toMediaMetadata().toMediaItem())
    is AlbumItem -> {
      var album = database.albumWithSongs(item.id).first()
      if (album?.songs.isNullOrEmpty()) {
        YouTube.album(item.id)
          .onSuccess { albumPage -> database.transaction { insert(albumPage) } }
          .onFailure { reportException(it) }
        album = database.albumWithSongs(item.id).first()
      }
      album?.songs.orEmpty().map { it.toMediaItem() }
    }
    is PlaylistItem ->
      YouTube.playlist(item.id).completed().getOrNull()?.songs.orEmpty().map {
        it.toMediaMetadata().toMediaItem()
      }
    is ArtistItem -> emptyList()
  }

/** The speed dial long-press sheet; More options opens the item's full menu. */
@Composable
private fun SpeedDialMenu(
  item: YTItem,
  scope: CoroutineScope,
  onMoreOptions: () -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val listenTogetherManager = LocalListenTogetherManager.current
  val isGuest = listenTogetherManager?.isGuestPlaybackRestricted == true
  val isPinned by database.speedDialDao.isPinned(item.id).collectAsState(initial = false)

  StageSheetTitle(item.title)
  StageSheetCards {
    if (!isGuest && item !is ArtistItem) {
      StageSheetRow(
        text = stringResource(R.string.play_next),
        icon = R.drawable.stage_round_play,
        onClick = {
          scope.launch {
            val items = withContext(Dispatchers.IO) { speedDialQueueItems(item, database) }
            playerConnection.playNext(items)
          }
          onDismiss()
        }
      )
      StageSheetRow(
        text = stringResource(R.string.add_to_queue),
        icon = R.drawable.stage_queue,
        onClick = {
          scope.launch {
            val items = withContext(Dispatchers.IO) { speedDialQueueItems(item, database) }
            playerConnection.addToQueue(items)
          }
          onDismiss()
        }
      )
    }
    StageSheetRow(
      text =
        stringResource(if (isPinned) R.string.unpin_from_speed_dial else R.string.pin_to_speed_dial),
      icon = R.drawable.stage_pin,
      onClick = {
        scope.launch(Dispatchers.IO) {
          if (isPinned) {
            database.speedDialDao.delete(item.id)
          } else {
            database.speedDialDao.insert(SpeedDialItem.fromYTItem(item))
          }
        }
        onDismiss()
      }
    )
    StageSheetRow(
      text = stringResource(R.string.share),
      icon = R.drawable.stage_share,
      onClick = {
        val intent =
          Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, item.shareLink)
          }
        context.startActivity(Intent.createChooser(intent, null))
        onDismiss()
      }
    )
    StageSheetRow(
      text = stringResource(R.string.more_options),
      icon = R.drawable.stage_more_row,
      onClick = onMoreOptions
    )
  }
}

/** A Home section for one music service: suggestions, or a way to connect it. */
private data class HomeMovingCard(
  val title: String,
  val subtitle: String?,
  val imageUrl: String?,
  val onClick: () -> Unit,
)

@Composable
private fun HomeMovingCards(
  title: String,
  cards: List<HomeMovingCard>,
  modifier: Modifier = Modifier,
) {
  if (cards.isEmpty()) return

  Column(modifier = modifier) {
    NavigationTitle(title = title)
    StageHeroStrip(
      count = cards.size,
      modifier = Modifier.padding(top = 4.dp).fillMaxWidth().height(200.dp),
    ) { index, expansion, nextTitleAlpha ->
      val card = cards.getOrNull(index) ?: return@StageHeroStrip
      Box(
        modifier =
          Modifier.fillMaxSize()
            .clip(RoundedCornerShape(if (expansion > 0.5f) 28.dp else 18.dp))
            .clickable(onClick = card.onClick)
      ) {
        AsyncImage(
          model = card.imageUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors =
                    listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
              )
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).alpha(expansion)) {
          Text(
            text = card.title,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          card.subtitle?.let {
            Text(
              text = it,
              fontSize = 14.sp,
              lineHeight = 18.sp,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        Text(
          text = card.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier =
            Modifier.align(Alignment.BottomCenter)
              .padding(vertical = 14.dp)
              .alpha(nextTitleAlpha)
              .bottomToTopText(),
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeSourceSection(
  title: String,
  iconRes: Int,
  iconTint: Color,
  songs: List<SongItem>,
  connected: Boolean,
  connectText: String,
  onOpen: () -> Unit,
  onShowMenu: (SongItem) -> Unit,
  onPlay: (SongItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (connected && songs.isEmpty()) return

  Column(modifier = modifier) {
    NavigationTitle(
      title = title,
      thumbnail = {
        Icon(
          painter = painterResource(iconRes),
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(24.dp),
        )
      },
      onClick = onOpen,
    )
    if (connected) {
      songs.forEach { song ->
        YouTubeListItem(
          item = song,
          color = Color.Transparent,
          isSwipeable = false,
          trailingContent = { StageRowMoreButton(onClick = { onShowMenu(song) }) },
          modifier =
            Modifier.combinedClickable(onClick = { onPlay(song) }, onLongClick = { onShowMenu(song) }),
        )
      }
    } else {
      StageButton(
        text = connectText,
        onClick = onOpen,
        modifier = Modifier.padding(horizontal = 18.dp),
      )
    }
  }
}

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
  navController: NavController,
  snackbarHostState: SnackbarHostState,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val bottomSheetPageState = LocalBottomSheetPageState.current
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current

  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val quickPicks by viewModel.quickPicks.collectAsState()
  val heroSongs by viewModel.heroSongs.collectAsState()
  val aiRecommendedPlaylist by viewModel.aiRecommendedPlaylist.collectAsState()
  val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
  val keepListening by viewModel.keepListening.collectAsState()
  val similarRecommendations by viewModel.similarRecommendations.collectAsState()
  val accountPlaylists by viewModel.accountPlaylists.collectAsState()
  val homePage by viewModel.homePage.collectAsState()
  val explorePage by viewModel.explorePage.collectAsState()
  val dailyDiscover by viewModel.dailyDiscover.collectAsState()
  val communityPlaylists by viewModel.communityPlaylists.collectAsState()

  val allLocalItems by viewModel.allLocalItems.collectAsState()
  val allYtItems by viewModel.allYtItems.collectAsState()
  val speedDialItems by viewModel.speedDialItems.collectAsState()
  val selectedChip by viewModel.selectedChip.collectAsState()
  val spotifyRecommendations by viewModel.spotifyRecommendations.collectAsState()
  val spotifyAlbums by viewModel.spotifyAlbums.collectAsState()

  val isLoading: Boolean by viewModel.isLoading.collectAsState()
  val isMoodAndGenresLoading = isLoading && explorePage?.moodAndGenres == null
  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val isRandomizing by viewModel.isRandomizing.collectAsState()
  val pullRefreshState = rememberPullToRefreshState()

  val quickPicksLazyGridState = rememberLazyGridState()
  val forgottenFavoritesLazyGridState = rememberLazyGridState()

  val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
  val (randomizeHomeOrder) = rememberPreference(RandomizeHomeOrderKey, true)
  val (showSpeedDial) = rememberPreference(ShowSpeedDialKey, true)

  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
  val spotifyStatus by
    produceState<Boolean?>(initialValue = null, isRefreshing) {
      value = viewModel.isSpotifyConnected()
    }
  val spotifyConnected = spotifyStatus == true

  val scope = rememberCoroutineScope()

  fun showHomeSongMenu(song: SongItem) {
    menuState.show {
      YouTubeSongMenu(song = song, navController = navController, onDismiss = menuState::dismiss)
    }
  }

  fun playHomeSong(song: SongItem) {
    playerConnection?.playQueue(
      YouTubeQueue(song.endpoint ?: WatchEndpoint(videoId = song.id), song.toMediaMetadata())
    )
  }

  var randomizeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

  val lazylistState = rememberLazyListState()
  val cardWidth =
    ((LocalConfiguration.current.screenWidthDp.dp - 60.dp) / 3).coerceAtMost(160.dp)
  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  var randomSeed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

  LaunchedEffect(isRefreshing) {
    if (isRefreshing) {
      randomSeed = System.currentTimeMillis()
    }
  }

  val foundInSettings = stringResource(R.string.found_in_settings_content)

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      lazylistState.animateScrollToItem(0)
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  NetworkReload(onReload = viewModel::refresh)

  if (selectedChip != null) {
    BackHandler { viewModel.toggleChip(selectedChip) }
  }

  val playAlbum: (String) -> Unit = { albumId ->
    scope.launch(Dispatchers.IO) {
      var albumWithSongs = database.albumWithSongs(albumId).first()
      if (albumWithSongs?.songs.isNullOrEmpty()) {
        YouTube.album(albumId)
          .onSuccess { albumPage ->
            database.transaction { insert(albumPage) }
            albumWithSongs = database.albumWithSongs(albumId).first()
          }
          .onFailure { reportException(it) }
      }
      albumWithSongs?.let {
        withContext(Dispatchers.Main) { playerConnection.playQueue(LocalAlbumRadio(it)) }
      }
    }
  }

  val localGridItem: @Composable (LocalItem) -> Unit = {
    when (it) {
      is Song ->
        StageCard(
          title = it.song.title,
          subtitle =
            joinByBullet(
              it.artists.joinToString { artist -> artist.name },
              makeTimeString(it.song.duration * 1000L)
            ),
          modifier =
            Modifier.width(cardWidth)
              .combinedClickable(
                onClick = {
                  if (it.id == mediaMetadata?.id) {
                    playerConnection.togglePlayPause()
                  } else {
                    playerConnection.playQueue(
                      YouTubeQueue.radio(it.toMediaMetadata()),
                    )
                  }
                },
                onLongClick = {
                  haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress,
                  )
                  menuState.show {
                    SongMenu(
                      originalSong = it,
                      navController = navController,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              ),
        ) {
          ItemThumbnail(
            thumbnailUrl = it.thumbnailUrl,
            isActive = it.id == mediaMetadata?.id,
            isPlaying = isPlaying,
            shape = StageCardShape,
            forceCrop = true,
          )
        }
      is Album -> {
        val isActive = it.id == mediaMetadata?.album?.id
        StageCard(
          title = it.album.title,
          subtitle =
            joinByBullet(
              it.artists.joinToString { artist -> artist.name },
              it.album.year?.toString()
            ),
          modifier =
            Modifier.width(cardWidth)
              .combinedClickable(
                onClick = { navController.navigate("album/${it.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    AlbumMenu(
                      originalAlbum = it,
                      navController = navController,
                      onDismiss = menuState::dismiss
                    )
                  }
                }
              ),
        ) {
          ItemThumbnail(
            thumbnailUrl = it.album.thumbnailUrl,
            isActive = isActive,
            isPlaying = isPlaying,
            shape = StageCardShape,
            forceCrop = true,
          )
          AlbumPlayButton(visible = !isActive, onClick = { playAlbum(it.id) })
        }
      }
      is Artist ->
        StageCard(
          title = it.artist.name,
          subtitle = pluralStringResource(R.plurals.n_song, it.songCount, it.songCount),
          circle = true,
          modifier =
            Modifier.width(cardWidth)
              .combinedClickable(
                onClick = { navController.navigate("artist/${it.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress,
                  )
                  menuState.show {
                    ArtistMenu(
                      originalArtist = it,
                      coroutineScope = scope,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              ),
        ) {
          ItemThumbnail(
            thumbnailUrl = it.artist.thumbnailUrl,
            isActive = false,
            isPlaying = false,
            shape = CircleShape,
            forceCrop = true,
          )
        }
      is Playlist -> {}
    }
  }

  val showYtItemMenu: (YTItem) -> Unit = { item ->
    menuState.show {
      when (item) {
        is SongItem ->
          YouTubeSongMenu(
            song = item,
            navController = navController,
            onDismiss = menuState::dismiss
          )
        is AlbumItem ->
          YouTubeAlbumMenu(
            albumItem = item,
            navController = navController,
            onDismiss = menuState::dismiss
          )
        is ArtistItem -> YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
        is PlaylistItem ->
          YouTubePlaylistMenu(
            playlist = item,
            coroutineScope = scope,
            onDismiss = menuState::dismiss
          )
      }
    }
  }

  val ytGridItem: @Composable (YTItem, Dp) -> Unit = { item, artHeight ->
    val isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id)
    StageCard(
      title = item.title,
      subtitle =
        when (item) {
          is SongItem ->
            joinByBullet(
              item.artists.joinToString { it.name },
              makeTimeString(item.duration?.times(1000L))
            )
          is AlbumItem ->
            joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
          is ArtistItem -> null
          is PlaylistItem -> joinByBullet(item.author?.name, item.songCountText)
        },
      artHeight = artHeight,
      circle = item is ArtistItem,
      modifier =
        Modifier.width(cardWidth)
          .combinedClickable(
            onClick = {
              when (item) {
                is SongItem ->
                  playerConnection.playQueue(
                    YouTubeQueue(
                      item.endpoint ?: WatchEndpoint(videoId = item.id),
                      item.toMediaMetadata()
                    )
                  )
                is AlbumItem -> navController.navigate("album/${item.id}")
                is ArtistItem -> navController.navigate("artist/${item.id}")
                is PlaylistItem -> navController.navigateToPlaylistItem(item)
              }
            },
            onLongClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              showYtItemMenu(item)
            }
          ),
    ) {
      ItemThumbnail(
        thumbnailUrl = item.thumbnail,
        isActive = isActive,
        isPlaying = isPlaying,
        shape = if (item is ArtistItem) CircleShape else StageCardShape,
        forceCrop = true,
      )
      if (item is AlbumItem) {
        AlbumPlayButton(visible = !isActive, onClick = { playAlbum(item.id) })
      }
    }
  }

  val homeSections =
    remember(
      randomizeHomeOrder,
      randomSeed,
      speedDialItems,
      heroSongs,
      dailyDiscover,
      keepListening,
      accountPlaylists,
      forgottenFavorites,
      communityPlaylists,
      similarRecommendations,
      spotifyRecommendations,
      spotifyStatus,
      homePage?.sections,
      explorePage?.moodAndGenres,
      aiRecommendedPlaylist
    ) {
      val list = mutableListOf<HomeSection>()

      if (showSpeedDial) list.add(HomeSection.SpeedDial)
      if (heroSongs.isNotEmpty()) list.add(HomeSection.HeroCarousel)
      list.add(HomeSection.YouTubeMusic)
      list.add(HomeSection.Spotify)
      if (dailyDiscover?.isNotEmpty() == true) list.add(HomeSection.DailyDiscover)

      if (similarRecommendations?.isNotEmpty() == true) {
        list.add(HomeSection.SimilarRecommendation(0))
      }

      if (randomizeHomeOrder) {
        list.sortedByDescending { section ->
          val sectionRandom = Random(randomSeed + section.id.hashCode())

          val base =
            when (section) {
              HomeSection.HeroCarousel -> 20000
              HomeSection.SpeedDial -> 19000
              HomeSection.YouTubeMusic -> 18000
              HomeSection.Spotify -> 17900
              is HomeSection.SimilarRecommendation -> if (section.index == 0) 17000 else 100
              is HomeSection.HomePageSection -> 9000 - (section.index * 10)
              HomeSection.DailyDiscover -> 500
              HomeSection.KeepListening,
              HomeSection.AccountPlaylists,
              HomeSection.ForgottenFavorites -> 300
              else -> 100
            }

          val modifier =
            when (section) {
              HomeSection.SpeedDial,
              HomeSection.HeroCarousel,
              HomeSection.YouTubeMusic,
              HomeSection.Spotify -> 0
              HomeSection.DailyDiscover -> sectionRandom.nextInt(-200, 400)
              HomeSection.KeepListening,
              HomeSection.AccountPlaylists,
              HomeSection.ForgottenFavorites -> sectionRandom.nextInt(-100, 400)
              else -> sectionRandom.nextInt(-50, 50)
            }
          base + modifier
        }
      } else {
        val defaultOrder =
          mapOf(
            HomeSection.HeroCarousel to 1100,
            HomeSection.SpeedDial to 1000,
            HomeSection.YouTubeMusic to 990,
            HomeSection.Spotify to 985,
            HomeSection.DailyDiscover to 970,
            HomeSection.KeepListening to 960,
            HomeSection.AccountPlaylists to 950,
            HomeSection.ForgottenFavorites to 940,
            HomeSection.MoodAndGenres to 10
          )

        list.sortedByDescending { section ->
          when (section) {
            is HomeSection.HomePageSection -> 900 - section.index
            is HomeSection.SimilarRecommendation ->
              if (section.index == 0) 975 else 30 - section.index
            else -> defaultOrder[section] ?: 0
          }
        }
      }
    }

  // The cards load after the speed dial, so keep the page on them when they arrive.
  LaunchedEffect(heroSongs.isNotEmpty()) {
    if (heroSongs.isNotEmpty() && lazylistState.firstVisibleItemIndex <= 2) {
      lazylistState.scrollToItem(0)
    }
  }

  LaunchedEffect(quickPicks) { quickPicksLazyGridState.scrollToItem(0) }

  LaunchedEffect(forgottenFavorites) { forgottenFavoritesLazyGridState.scrollToItem(0) }

  PullToRefreshBox(
    state = pullRefreshState,
    isRefreshing = isRefreshing,
    onRefresh = viewModel::refresh,
    indicator = {
      PullToRefreshDefaults.LoadingIndicator(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        modifier =
          Modifier.align(Alignment.TopCenter)
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
      )
    }
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
      val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
      val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
      val quickPicksSnapLayoutInfoProvider =
        remember(quickPicksLazyGridState) {
          SnapLayoutInfoProvider(
            lazyGridState = quickPicksLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
              (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
            }
          )
        }
      val forgottenFavoritesSnapLayoutInfoProvider =
        remember(forgottenFavoritesLazyGridState) {
          SnapLayoutInfoProvider(
            lazyGridState = forgottenFavoritesLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
              (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
            }
          )
        }

      LazyColumn(
        state = lazylistState,
        modifier = Modifier.hapticScrollEdge(lazylistState),
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
      ) {
        homeSections.forEach { section ->
          when (section) {
            HomeSection.SpeedDial -> {
              speedDialItems
                .let { items ->
                  item(key = "speed_dial_title") {
                    NavigationTitle(
                      title = stringResource(R.string.speed_dial),
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "speed_dial_list") {
                    val tiles = buildList<@Composable (Modifier) -> Unit> {
                      add { tileModifier ->
                        RandomizeGridItem(
                            isLoading = isRandomizing,
                            modifier = tileModifier,
                            colors = listOf(Color(0xFF8E6BD8), Color(0xFF3B2A6B)),
                            onClick = {
                              if (isRandomizing) {
                                randomizeJob?.cancel()
                              } else {
                                randomizeJob =
                                  scope.launch {
                                    val randomItem = viewModel.getRandomItem()
                                    if (randomItem != null) {
                                      when (randomItem) {
                                        is SongItem ->
                                          playerConnection.playQueue(
                                            YouTubeQueue(
                                              randomItem.endpoint
                                                ?: WatchEndpoint(videoId = randomItem.id),
                                              randomItem.toMediaMetadata()
                                            )
                                          )
                                        is AlbumItem ->
                                          navController.navigate("album/${randomItem.id}")
                                        is ArtistItem ->
                                          navController.navigate("artist/${randomItem.id}")
                                        is PlaylistItem ->
                                          navController.navigateToPlaylistItem(randomItem)
                                      }
                                    }
                                  }
                              }
                            }
                          )
                      }
                      add { tileModifier ->
                        SpeedDialShortcut(
                          iconRes = R.drawable.download,
                          contentDescription = stringResource(R.string.downloaded_songs),
                          colors = listOf(Color(0xFF2F9BE0), Color(0xFF123F73)),
                          onClick = { navController.navigate("auto_playlist/downloaded") },
                          modifier = tileModifier,
                        )
                      }
                      add { tileModifier ->
                        SpeedDialShortcut(
                          iconRes = R.drawable.favorite,
                          contentDescription = stringResource(R.string.liked),
                          colors = listOf(Color(0xFFE04F7F), Color(0xFF7A1F45)),
                          onClick = { navController.navigate("auto_playlist/liked") },
                          modifier = tileModifier,
                        )
                      }
                      add { tileModifier ->
                        SpeedDialShortcut(
                          iconRes = R.drawable.trending_up,
                          contentDescription = stringResource(R.string.my_top) + " 50",
                          colors = listOf(Color(0xFFF5A623), Color(0xFF8A4B0F)),
                          onClick = { navController.navigate("top_playlist/50") },
                          modifier = tileModifier,
                        )
                      }
                      add { tileModifier ->
                        SpeedDialShortcut(
                          iconRes = R.drawable.stage_youtube_music,
                          contentDescription = stringResource(R.string.stage_youtube_music),
                          colors = listOf(Color(0xFFFF4E45), Color(0xFF7A0F1E)),
                          onClick = { navController.navigate(if (isLoggedIn) "account" else "login") },
                          modifier = tileModifier,
                        )
                      }
                      add { tileModifier ->
                        SpeedDialShortcut(
                          iconRes = R.drawable.ic_spotify,
                          contentDescription = stringResource(R.string.stage_spotify),
                          colors = listOf(Color(0xFF1DB954), Color(0xFF0B3B20)),
                          onClick = { navController.navigate("spotify_library") },
                          modifier = tileModifier,
                        )
                      }
                      items.forEach { item ->
                        add { tileModifier ->
                        val isPinned by
                          database.speedDialDao
                            .isPinned(item.id)
                            .collectAsState(initial = false)

                        SpeedDialGridItem(
                          item = item,
                          isPinned = isPinned,
                          isActive =
                            item.id in
                              listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                          isPlaying = isPlaying,
                          modifier =
                            tileModifier
                              .combinedClickable(
                                onClick = {
                                  when (item) {
                                    is SongItem ->
                                      playerConnection.playQueue(
                                        YouTubeQueue(
                                          item.endpoint
                                            ?: WatchEndpoint(videoId = item.id),
                                          item.toMediaMetadata()
                                        )
                                      )
                                    is AlbumItem ->
                                      navController.navigate("album/${item.id}")
                                    is ArtistItem ->
                                      navController.navigate("artist/${item.id}")
                                    is PlaylistItem ->
                                      navController.navigateToPlaylistItem(item)
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                  )
                                  menuState.show {
                                    SpeedDialMenu(
                                      item = item,
                                      scope = scope,
                                      onMoreOptions = { showYtItemMenu(item) },
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                }
                              )
                        )
                      }
                        }
                      }

                    StageSpeedDialPager(
                      pages =
                        tiles.chunked(SPEED_DIAL_COLUMNS).take(SPEED_DIAL_PAGES),
                      modifier = Modifier.fillMaxWidth().animateItem(),
                    )
                  }
                }
            }
            HomeSection.AiRecommendations -> {
              aiRecommendedPlaylist?.let { pair ->
                val (playlist, songs) = pair
                item(key = "ai_recommendation_title") {
                  val lastUpdatedStr =
                    playlist.playlist.lastUpdateTime?.let {
                      "Last updated: " +
                        it.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, h:mm a"))
                    }
                  NavigationTitle(
                    title = playlist.title,
                    label = lastUpdatedStr,
                    onClick = { navController.navigate("local_playlist/${playlist.id}") },
                    modifier = Modifier.animateItem()
                  )
                }
                item(key = "ai_recommendation_list") {
                  LazyRow(
                    contentPadding = StageRowPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.animateItem()
                  ) {
                    items(items = songs.distinctBy { it.id }, key = { it.id }) { songObj ->
                      localGridItem(songObj)
                    }
                  }
                }
              }
            }
            HomeSection.YouTubeMusic -> {
              item(key = "youtube_music") {
                val songs =
                  remember(similarRecommendations, homePage) {
                    (similarRecommendations.orEmpty().flatMap { it.items } +
                        homePage?.sections.orEmpty().flatMap { it.items })
                      .filterIsInstance<SongItem>()
                      .distinctBy { it.id }
                      .take(5)
                  }
                HomeSourceSection(
                  title = stringResource(R.string.stage_youtube_music),
                  iconRes = R.drawable.stage_youtube_music,
                  iconTint = Color(0xFFFF4E45),
                  songs = songs,
                  connected = isLoggedIn,
                  connectText = "Sign in to Google",
                  onOpen = { navController.navigate(if (isLoggedIn) "account" else "login") },
                  onShowMenu = ::showHomeSongMenu,
                  onPlay = ::playHomeSong,
                  modifier = Modifier.animateItem(),
                )
              }
              item(key = "youtube_trending_playlists") {
                val playlists =
                  remember(communityPlaylists, homePage) {
                    (communityPlaylists.orEmpty().map { it.playlist } +
                        homePage?.sections.orEmpty().flatMap { it.items }.filterIsInstance<PlaylistItem>())
                      .distinctBy { it.id }
                      .take(8)
                  }
                HomeMovingCards(
                  title = stringResource(R.string.stage_trending_playlists),
                  cards =
                    playlists.map { playlist ->
                      HomeMovingCard(
                        title = playlist.title,
                        subtitle = playlist.author?.name,
                        imageUrl = playlist.thumbnail,
                        onClick = { navController.navigateToPlaylistItem(playlist) },
                      )
                    },
                  modifier = Modifier.animateItem(),
                )
              }
            }
            HomeSection.Spotify -> {
              item(key = "spotify") {
                val songs =
                  remember(spotifyRecommendations) {
                    spotifyRecommendations
                      .orEmpty()
                      .flatMap { it.items }
                      .filterIsInstance<SongItem>()
                      .distinctBy { it.id }
                      .take(5)
                  }
                if (spotifyStatus != null) {
                  HomeSourceSection(
                    title = stringResource(R.string.stage_spotify),
                    iconRes = R.drawable.ic_spotify,
                    iconTint = Color(0xFF1DB954),
                    songs = songs,
                    connected = spotifyConnected,
                    connectText = "Connect Spotify",
                    onOpen = {
                      navController.navigate(
                        if (spotifyConnected) "spotify_library" else "settings/spotify_import"
                      )
                    },
                    onShowMenu = ::showHomeSongMenu,
                    onPlay = ::playHomeSong,
                    modifier = Modifier.animateItem(),
                  )
                }
              }
              item(key = "spotify_popular_albums") {
                HomeMovingCards(
                  title = stringResource(R.string.stage_popular_albums),
                  cards =
                    spotifyAlbums.map { album ->
                      val artistNames = album.artists.joinToString { it.name }
                      HomeMovingCard(
                        title = album.name,
                        subtitle = artistNames,
                        imageUrl = album.imageUrl,
                        onClick = {
                          navController.navigate(
                            "search/${URLEncoder.encode("${album.name} $artistNames", "UTF-8")}"
                          )
                        },
                      )
                    },
                  modifier = Modifier.animateItem(),
                )
              }
            }
            HomeSection.HeroCarousel -> {
              heroSongs
                .takeIf { it.isNotEmpty() }
                ?.let { songs ->
                  item(key = "hero_carousel") {
                    StageHeroStrip(
                      count = songs.size,
                      modifier =
                        Modifier.padding(top = 16.dp)
                          .fillMaxWidth()
                          .height(236.dp)
                          .animateItem()
                    ) { index, expansion, nextTitleAlpha ->
                      val originalSong = songs.getOrNull(index) ?: return@StageHeroStrip
                      val localSong by
                        database.song(originalSong.id).collectAsState(initial = null)
                      val song = localSong?.toMediaMetadata() ?: originalSong.toMediaMetadata()
                      val isActive = song.id == mediaMetadata?.id

                      Box(
                        modifier =
                          Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(if (expansion > 0.5f) 28.dp else 18.dp))
                            .focusable()
                            .combinedClickable(
                              onClick = {
                                if (isActive) {
                                  playerConnection.togglePlayPause()
                                } else if (localSong?.song?.isLocal == true) {
                                  playerConnection.playQueue(
                                    ListQueue(title = song.title, items = listOf(song.toMediaItem()))
                                  )
                                } else {
                                  playerConnection.playQueue(
                                    YouTubeQueue.radio(song)
                                  )
                                }
                              },
                              onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                  localSong?.let { savedSong ->
                                    SongMenu(
                                      originalSong = savedSong,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  } ?: YouTubeSongMenu(
                                    song = originalSong,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                  )
                                }
                              }
                            )
                      ) {
                        AsyncImage(
                          model =
                            ImageRequest.Builder(LocalContext.current)
                              .data(localSong?.thumbnailUrl ?: song.thumbnailUrl)
                              .crossfade(true)
                              .build(),
                          contentDescription = null,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )

                        Box(
                          modifier =
                            Modifier.fillMaxSize()
                              .background(
                                Brush.verticalGradient(
                                  colors =
                                    listOf(
                                      Color.Transparent,
                                      Color.Transparent,
                                      Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                              )
                        )

                        if (isActive && isPlaying) {
                          Box(
                            modifier =
                              Modifier.align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.onSurface, CircleShape),
                            contentAlignment = Alignment.Center
                          ) {
                            Icon(
                              painter = painterResource(R.drawable.volume_up),
                              contentDescription = null,
                              tint = MaterialTheme.colorScheme.onPrimary,
                              modifier = Modifier.size(18.dp)
                            )
                          }
                        }

                        Column(
                          modifier =
                            Modifier.align(Alignment.BottomStart)
                              .padding(16.dp)
                              .alpha(expansion)
                        ) {
                          Text(
                            text = song.title,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                          Text(
                            text = song.artists.joinToString { it.name },
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }

                        Text(
                          text = song.title,
                          fontSize = 15.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier =
                            Modifier.align(Alignment.BottomCenter)
                              .padding(vertical = 14.dp)
                              .alpha(nextTitleAlpha)
                              .bottomToTopText()
                        )
                      }
                    }
                  }
                }
            }
            HomeSection.DailyDiscover -> {
              dailyDiscover
                ?.takeIf { it.isNotEmpty() }
                ?.let { discoverList ->
                  item(key = "daily_discover_title") {
                    val title = stringResource(R.string.your_daily_discover)
                    NavigationTitle(
                      title = title,
                      onPlayAllClick = {
                        val queueItems =
                          discoverList.mapNotNull {
                            (it.recommendation as? SongItem)?.toMediaMetadata()
                          }

                        if (queueItems.isNotEmpty()) {
                          playerConnection.playQueue(
                            ListQueue(title = title, items = queueItems.map { it.toMediaItem() })
                          )
                        }
                      }
                    )
                  }
                  item(key = "daily_discover_content") {
                    LazyRow(
                      contentPadding = StageRowPadding,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      modifier = Modifier.animateItem()
                    ) {
                      items(discoverList) { item ->
                        DailyDiscoverCard(
                          dailyDiscover = item,
                          onClick = {
                            val song = item.recommendation as? SongItem
                            val mediaMetadata = song?.toMediaMetadata()
                            if (mediaMetadata != null) {
                              playerConnection.playQueue(
                                YouTubeQueue(
                                  song.endpoint ?: WatchEndpoint(videoId = song.id),
                                  mediaMetadata
                                )
                              )
                            }
                          },
                          navController = navController,
                          modifier = Modifier.width((maxWidth - 48.dp) / 2)
                        )
                      }
                    }
                  }
                }
            }
            HomeSection.KeepListening -> {
              keepListening
                ?.takeIf { it.isNotEmpty() }
                ?.let { keepListening ->
                  item(key = "keep_listening_title") {
                    NavigationTitle(
                      title = stringResource(R.string.keep_listening),
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "keep_listening_list") {
                    val rows = if (keepListening.size > 6) 2 else 1
                    LazyHorizontalGrid(
                      state = rememberLazyGridState(),
                      rows = GridCells.Fixed(rows),
                      contentPadding = StageRowPadding,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                      modifier =
                        Modifier.fillMaxWidth()
                          .height(
                            (StageCardArtHeight + StageCardTextHeight) * rows +
                              12.dp * rows
                          )
                          .animateItem()
                    ) {
                      items(keepListening.distinctBy { it.id }, key = { it.id }) {
                        localGridItem(it)
                      }
                    }
                  }
                }
            }
            HomeSection.AccountPlaylists -> {
              accountPlaylists
                ?.takeIf { it.isNotEmpty() }
                ?.let { accountPlaylists ->
                  item(key = "account_playlists_title") {
                    NavigationTitle(
                      title = stringResource(R.string.your_youtube_playlists),
                      onClick = { navController.navigate("account") },
                      endText = "See all",
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "account_playlists_list") {
                    LazyRow(
                      contentPadding = StageRowPadding,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      modifier = Modifier.animateItem()
                    ) {
                      items(
                        items = accountPlaylists.distinctBy { it.id },
                        key = { it.id },
                      ) { item ->
                        ytGridItem(item, StageCardArtHeight)
                      }
                    }
                  }
                }
            }
            HomeSection.ForgottenFavorites -> {
              forgottenFavorites
                ?.takeIf { it.isNotEmpty() }
                ?.let { forgottenFavorites ->
                  item(key = "forgotten_favorites_title") {
                    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
                    NavigationTitle(
                      title = forgottenFavoritesTitle,
                      modifier = Modifier.animateItem(),
                      onPlayAllClick = {
                        playerConnection.playQueue(
                          ListQueue(
                            title = forgottenFavoritesTitle,
                            items = forgottenFavorites.distinctBy { it.id }.map { it.toMediaItem() }
                          )
                        )
                      }
                    )
                  }

                  item(key = "forgotten_favorites_list") {
                    val rows = min(4, forgottenFavorites.size)
                    LazyHorizontalGrid(
                      state = forgottenFavoritesLazyGridState,
                      rows = GridCells.Fixed(rows),
                      contentPadding =
                        WindowInsets.systemBars
                          .only(WindowInsetsSides.Horizontal)
                          .asPaddingValues(),
                      flingBehavior =
                        rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider),
                      modifier = Modifier.fillMaxWidth().height(ListItemHeight * rows).animateItem()
                    ) {
                      itemsIndexed(
                        items = forgottenFavorites.distinctBy { it.id },
                        key = { _, it -> it.id }
                      ) { index, originalSong ->
                        val song by
                          database.song(originalSong.id).collectAsState(initial = originalSong)

                        SongListItem(
                          song = song!!,
                          showInLibraryIcon = true,
                          isActive = song!!.id == mediaMetadata?.id,
                          isPlaying = isPlaying,
                          isSwipeable = false,
                          shape = listItemShape(index = index % rows, count = rows),
                          trailingContent = {
                            StageRowMoreButton(
                              onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                  SongMenu(
                                    originalSong = song!!,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                  )
                                }
                              }
                            )
                          },
                          modifier =
                            Modifier.width(horizontalLazyGridItemWidth)
                              .combinedClickable(
                                onClick = {
                                  if (song!!.id == mediaMetadata?.id) {
                                    playerConnection.togglePlayPause()
                                  } else {
                                    playerConnection.playQueue(
                                      YouTubeQueue.radio(song!!.toMediaMetadata())
                                    )
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  menuState.show {
                                    SongMenu(
                                      originalSong = song!!,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                }
                              )
                        )
                      }
                    }
                  }
                }
            }
            is HomeSection.SimilarRecommendation -> {
              val recommendation = similarRecommendations?.getOrNull(section.index)
              recommendation?.let {
                item(key = "similar_to_title_${section.index}") {
                  NavigationTitle(
                    title =
                      stringResource(R.string.similar_to) + " " + recommendation.title.title,
                    onClick = {
                      val recTitle = recommendation.title
                      when (recTitle) {
                        is Song -> navController.navigate("album/${recTitle.album!!.id}")
                        is Album -> navController.navigate("album/${recTitle.id}")
                        is Artist -> navController.navigate("artist/${recTitle.id}")
                        is Playlist -> {}
                      }
                    },
                    modifier = Modifier.animateItem()
                  )
                }

                item(key = "similar_to_list_${section.index}") {
                  LazyRow(
                    contentPadding = StageRowPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.animateItem()
                  ) {
                    items(recommendation.items.distinctBy { it.id }, key = { it.id }) { item ->
                      ytGridItem(item, 66.dp)
                    }
                  }
                }
              }
            }
            is HomeSection.HomePageSection -> {
              val sectionData = homePage?.sections?.getOrNull(section.index)
              sectionData?.let {
                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
                val hasPlayableSongs = sectionSongs.isNotEmpty()

                val isSongsOnlySection =
                  sectionData.items.isNotEmpty() && sectionData.items.all { it is SongItem }

                item(key = "home_section_title_${section.index}") {
                  NavigationTitle(
                    title = sectionData.title,
                    label = sectionData.label,
                    thumbnail =
                      sectionData.thumbnail?.let { thumbnailUrl ->
                        {
                          val shape = RoundedCornerShape(ThumbnailCornerRadius)
                          AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.size(ListThumbnailSize).clip(shape)
                          )
                        }
                      },
                    onClick =
                      sectionData.endpoint?.let { endpoint ->
                        {
                          when {
                            endpoint.browseId == "FEmusic_moods_and_genres" ->
                              navController.navigate("mood_and_genres")
                            endpoint.params != null ->
                              navController.navigate(
                                "youtube_browse/${endpoint.browseId}?params=${endpoint.params}"
                              )
                            else -> navController.navigate("browse/${endpoint.browseId}")
                          }
                        }
                      },
                    onPlayAllClick =
                      if (hasPlayableSongs) {
                        {
                          playerConnection.playQueue(
                            ListQueue(
                              title = sectionData.title,
                              items = sectionSongs.map { it.toMediaMetadata().toMediaItem() }
                            )
                          )
                        }
                      } else null,
                    modifier = Modifier.animateItem()
                  )
                }

                if (isSongsOnlySection) {

                  item(key = "home_section_list_${section.index}") {
                    LazyHorizontalGrid(
                      state = rememberLazyGridState(),
                      rows = GridCells.Fixed(4),
                      contentPadding =
                        WindowInsets.systemBars
                          .only(WindowInsetsSides.Horizontal)
                          .asPaddingValues(),
                      modifier = Modifier.fillMaxWidth().height(ListItemHeight * 4).animateItem()
                    ) {
                      itemsIndexed(
                        items = sectionSongs.distinctBy { it.id },
                        key = { _, it -> it.id }
                      ) { index, song ->
                        YouTubeListItem(
                          item = song,
                          isActive = song.id == mediaMetadata?.id,
                          isPlaying = isPlaying,
                          isSwipeable = false,
                          shape = listItemShape(index = index % 4, count = 4),
                          trailingContent = {
                            StageRowMoreButton(
                              onClick = {
                                menuState.show {
                                  YouTubeSongMenu(
                                    song = song,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                  )
                                }
                              }
                            )
                          },
                          modifier =
                            Modifier.width(horizontalLazyGridItemWidth)
                              .combinedClickable(
                                onClick = {
                                  if (song.id == mediaMetadata?.id) {
                                    playerConnection.togglePlayPause()
                                  } else {
                                    playerConnection.playQueue(
                                      YouTubeQueue.radio(song.toMediaMetadata())
                                    )
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  menuState.show {
                                    YouTubeSongMenu(
                                      song = song,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                }
                              )
                        )
                      }
                    }
                  }
                } else {

                  item(key = "home_section_list_${section.index}") {
                    LazyRow(
                      contentPadding = StageRowPadding,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      modifier = Modifier.animateItem()
                    ) {
                      items(sectionData.items.distinctBy { it.id }, key = { it.id }) { item ->
                        ytGridItem(item, StageCardArtHeight)
                      }
                    }
                  }
                }
              }
            }
            HomeSection.MoodAndGenres -> {
              explorePage?.moodAndGenres?.let { moodAndGenres ->
                item(key = "mood_and_genres_title") {
                  NavigationTitle(
                    title = stringResource(R.string.mood_and_genres),
                    onClick = { navController.navigate("mood_and_genres") },
                    modifier = Modifier.animateItem()
                  )
                }
                item(key = "mood_and_genres_list") {
                  LazyHorizontalGrid(
                    rows = GridCells.Fixed(4),
                    contentPadding = PaddingValues(6.dp),
                    modifier =
                      Modifier.height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp).animateItem()
                  ) {
                    items(moodAndGenres.distinctBy { it.title }, key = { it.title }) {
                      MoodAndGenresButton(
                        title = it.title,
                        onClick = {
                          navController.navigate(
                            "youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}"
                          )
                        },
                        modifier = Modifier.padding(6.dp).width(180.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        if (isLoading) {
          item(key = "loading_shimmer") {
            ShimmerHost(modifier = Modifier.animateItem()) {
              // 1. Quick Picks Skeleton
              Row(
                modifier =
                  Modifier.horizontalScroll(rememberScrollState())
                    .padding(
                      WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues()
                    )
              ) {
                repeat(3) {
                  Spacer(
                    modifier =
                      Modifier.padding(start = 18.dp, top = 16.dp)
                        .width(304.dp)
                        .height(236.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                  )
                }
              }

              // 2. Speed Dial Skeleton
              TextPlaceholder(
                height = 36.dp,
                modifier = Modifier.padding(12.dp).width(200.dp),
              )
              Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                repeat(2) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    repeat(4) {
                      Spacer(
                        modifier =
                          Modifier.size(74.dp)
                            .clip(StageCardShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                      )
                    }
                  }
                }
              }

              // 3. Generic Row Skeleton
              TextPlaceholder(
                height = 36.dp,
                modifier = Modifier.padding(12.dp).width(250.dp),
              )
              Row(
                modifier =
                  Modifier.horizontalScroll(rememberScrollState())
                    .padding(
                      WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues()
                    )
              ) {
                repeat(4) { GridItemPlaceHolder() }
              }
            }
          }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(30.dp)) }
      }
    }
  }
}
