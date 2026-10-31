package echo.music.iad1tya.ui.screens.search.suggestions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import echo.music.iad1tya.LocalDatabase
import kotlinx.coroutines.flow.map
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.GridThumbnailCornerRadius
import echo.music.iad1tya.constants.SuggestionRegionKey
import echo.music.iad1tya.constants.ThumbnailCornerRadius
import echo.music.iad1tya.ui.component.StageSectionHeader
import androidx.compose.ui.res.stringResource
import echo.music.iad1tya.utils.rememberPreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuggestionsTabContent(
  navController: NavController,
  onSearch: (String) -> Unit,
  viewModel: SuggestionsViewModel = hiltViewModel(),
  contentPadding: PaddingValues = PaddingValues(0.dp)
) {
  val suggestionTracks by viewModel.suggestionTracks.collectAsState()
  val suggestionArtists by viewModel.suggestionArtists.collectAsState()
  val suggestionAlbums by viewModel.suggestionAlbums.collectAsState()
  val suggestionVideos by viewModel.suggestionVideos.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val database = LocalDatabase.current
  val recentSearches by
    remember { database.searchHistory().map { it.distinctBy { search -> search.query }.take(10) } }
      .collectAsState(initial = emptyList())
  val isManualLoading by viewModel.isManualLoading.collectAsState()
  val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
  val playerConnection = echo.music.iad1tya.LocalPlayerConnection.current
  val context = LocalContext.current
  val (regionCode, _) = rememberPreference(key = SuggestionRegionKey, defaultValue = "system")

  androidx.compose.runtime.LaunchedEffect(regionCode) { viewModel.refresh(regionCode) }

  val pullToRefreshState = rememberPullToRefreshState()
  val scope = rememberCoroutineScope()

  PullToRefreshBox(
    isRefreshing = isManualLoading,
    onRefresh = { viewModel.refresh(regionCode, force = true) },
    state = pullToRefreshState,
    indicator = {
      PullToRefreshDefaults.LoadingIndicator(
        state = pullToRefreshState,
        isRefreshing = isManualLoading,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    },
    modifier = Modifier.fillMaxSize()
  ) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
      if (recentSearches.isNotEmpty()) {
        item(key = "recent_searches") {
          StageSectionHeader(title = stringResource(R.string.stage_recent_searches))
          LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(items = recentSearches, key = { it.query }) { search ->
              Text(
                text = search.query,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                  Modifier.widthIn(max = 180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable { onSearch(search.query) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
              )
            }
          }
        }
      }

      item(key = "moods_and_genres") {
        StageSectionHeader(
          title = stringResource(R.string.mood_and_genres),
          onMore = { navController.navigate("mood_and_genres") },
        )
      }

      if (
        isLoading &&
          !isManualLoading &&
          suggestionTracks == null &&
          suggestionArtists == null &&
          suggestionAlbums == null &&
          suggestionVideos == null
      ) {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularWavyProgressIndicator()
          }
        }
      }

      suggestionTracks?.let { tracks ->
        item {
          TrendingAppleMusicSection(
            tracks = suggestionTracks!!,
            countryCode = regionCode,
            onTrackClick = { track ->
              android.widget.Toast.makeText(
                  context,
                  "Loading ${track.title}...",
                  android.widget.Toast.LENGTH_SHORT
                )
                .show()
              viewModel.playTrack(track, playerConnection)
            },
            onMoreClick = {
              val code =
                if (regionCode == "system") java.util.Locale.getDefault().country.lowercase()
                else regionCode.lowercase()
              uriHandler.openUri("https://music.apple.com/$code/charts")
            }
          )
        }
      }

      suggestionArtists?.let { artists ->
        item {
          TopArtistsSection(
            artists = artists,
            onArtistClick = { artist ->
              android.widget.Toast.makeText(
                  context,
                  "Loading ${artist.name}...",
                  android.widget.Toast.LENGTH_SHORT
                )
                .show()
              viewModel.navigateToArtist(artist, navController)
            }
          )
        }
      }

      if (
        suggestionTracks == null &&
          suggestionArtists == null &&
          suggestionAlbums == null &&
          suggestionVideos == null &&
          !isLoading
      ) {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No suggestions available at the moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              OutlinedButton(onClick = { viewModel.refresh(regionCode, force = true) }) {
                Text("Refresh")
              }
            }
          }
        }
      }

      if (
        suggestionTracks != null ||
          suggestionArtists != null ||
          suggestionAlbums != null ||
          suggestionVideos != null
      ) {
        item {
          Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Data from Apple Music",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
              text = "echo-music",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

private val TrendingArtSize = 52.dp
private val TrendingArtistSize = 64.dp

@Composable
private fun TrendingTitle(text: String) {
  Text(
    text = text,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrendingAppleMusicSection(
  tracks: List<SuggestionTrack>,
  countryCode: String,
  onTrackClick: (SuggestionTrack) -> Unit,
  onMoreClick: () -> Unit
) {
  if (tracks.isEmpty()) return
  val displayTracks = tracks.take(29)
  val totalItems = displayTracks.size + 1
  val pagerState = rememberPagerState(pageCount = { (totalItems + 4) / 5 })
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = Modifier.fillMaxWidth()) {
    StageSectionHeader(title = stringResource(R.string.stage_trending_songs), onMore = onMoreClick)
    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      modifier =
        Modifier.fillMaxWidth().animateContentSize(tween(300, easing = FastOutSlowInEasing))
    ) { page ->
      Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
        val startIdx = page * 5
        val endIdx = minOf(startIdx + 5, totalItems)
        for (i in startIdx until endIdx) {
          if (i == 29) {
            Row(
              modifier =
                Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onMoreClick() },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
              Box(modifier = Modifier.size(TrendingArtSize), contentAlignment = Alignment.Center) {
                Icon(
                  painterResource(R.drawable.globe_search),
                  null,
                  tint = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(24.dp)
                )
              }
              TrendingTitle("View more on Apple Music")
            }
          } else if (i < displayTracks.size) {
            val track = displayTracks[i]
            Row(
              modifier =
                Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onTrackClick(track) },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
              if (track.thumbnailUrl != null) {
                SubcomposeAsyncImage(
                  model = track.thumbnailUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      LoadingIndicator()
                    }
                  },
                  modifier =
                    Modifier.clip(RoundedCornerShape(ThumbnailCornerRadius)).size(TrendingArtSize)
                )
              }
              Column(Modifier.weight(1f)) {
                TrendingTitle(track.title)
                val playCount =
                  remember(track.rank) {
                    val base = 2_500_000 / (track.rank + 2)
                    if (base >= 1_000_000) String.format("%.1fM plays", base / 1_000_000f)
                    else String.format("%dk plays", base / 1_000)
                  }
                Text(
                  "${track.artist} · $playCount",
                  fontSize = 13.sp,
                  lineHeight = 17.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(
        onClick = {
          coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        },
        enabled = pagerState.currentPage > 0
      ) {
        Icon(painterResource(R.drawable.chevron_leftpx), "Previous")
      }
      IconButton(
        onClick = {
          coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        },
        enabled = pagerState.currentPage < pagerState.pageCount - 1
      ) {
        Icon(painterResource(R.drawable.chevron_right_px), "Next")
      }
    }
  }
}

@Composable
fun TopArtistsSection(artists: List<SuggestionArtist>, onArtistClick: (SuggestionArtist) -> Unit) {
  if (artists.isEmpty()) return
  Column(modifier = Modifier.fillMaxWidth()) {
    StageSectionHeader(title = "Trending Artists")
    LazyRow(
      contentPadding = PaddingValues(horizontal = 22.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(artists) { artist ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.width(TrendingArtistSize).clickable { onArtistClick(artist) }
        ) {
          SubcomposeAsyncImage(
            model = artist.thumbnailUrl,
            contentDescription = artist.name,
            contentScale = ContentScale.Crop,
            loading = {
              Box(
                Modifier.fillMaxSize()
                  .shimmer()
                  .background(MaterialTheme.colorScheme.surfaceVariant)
              )
            },
            modifier =
              Modifier.size(TrendingArtistSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
          )
          Spacer(Modifier.height(6.dp))
          Text(
            artist.name,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}
