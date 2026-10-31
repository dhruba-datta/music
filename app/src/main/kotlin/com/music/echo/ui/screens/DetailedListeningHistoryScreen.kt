/** vivimusic Project (C) 2026 Licensed under GPL-3.0 | See git history for contributors */
package echo.music.iad1tya.ui.screens

import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalAlbumsGrid
import echo.music.iad1tya.ui.component.LocalArtistsGrid
import echo.music.iad1tya.ui.component.LocalSongsGrid
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.StageSectionHeader
import echo.music.iad1tya.ui.component.StageStatCards
import echo.music.iad1tya.ui.component.StageStatCircles
import echo.music.iad1tya.ui.component.StageStatEntry
import echo.music.iad1tya.ui.component.StageStatTile
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import echo.music.iad1tya.ui.utils.resize
import echo.music.iad1tya.utils.joinByBullet
import echo.music.iad1tya.utils.makeTimeString
import echo.music.iad1tya.viewmodels.DetailedListeningHistoryViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedListeningHistoryScreen(
  navController: NavController,
  viewModel: DetailedListeningHistoryViewModel = hiltViewModel()
) {
  val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
  val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsState()
  val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsState()
  val totalPlayTime by viewModel.totalPlayTime.collectAsState()

  val uniqueSongsCount by viewModel.uniqueSongsCount.collectAsState()
  val uniqueArtistsCount by viewModel.uniqueArtistsCount.collectAsState()
  val uniqueAlbumsCount by viewModel.uniqueAlbumsCount.collectAsState()

  val dateStr =
    Instant.ofEpochMilli(viewModel.startTimestamp)
      .atZone(ZoneOffset.UTC)
      .format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))

  Scaffold(
    topBar = {
      StageTopBar(
        title = { StageBarTitle(dateStr) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
          }
        }
)
    }
  ) { paddingValues ->
    LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
      item {
        val totalHours = totalPlayTime / (1000 * 60 * 60)
        val totalMinutes = (totalPlayTime / (1000 * 60)) % 60
        val totalSeconds = (totalPlayTime / 1000) % 60
        val detailedTimeStr =
          if (totalHours > 0) {
            "${totalHours}h ${"%02d".format(totalMinutes)}m"
          } else {
            "${totalMinutes}m ${totalSeconds}s"
          }
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier =
            Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)
              .fillMaxWidth()
              .height(IntrinsicSize.Min),
        ) {
          StageStatTile(
            value = detailedTimeStr,
            label = "Total listening time",
            modifier = Modifier.weight(1f).fillMaxHeight(),
          )
          StageStatTile(
            value = mostPlayedArtists.firstOrNull()?.artist?.name ?: "-",
            label = "Top Artist",
            modifier = Modifier.weight(1f).fillMaxHeight(),
          )
        }
      }

      if (mostPlayedSongs.isNotEmpty()) {
        item {
          StageSectionHeader(title = "Songs")
          StageStatCards(
            entries =
              mostPlayedSongs.map { song ->
                StageStatEntry(
                  key = song.id,
                  title = song.title,
                  subtitle =
                    joinByBullet(
                      "${song.songCountListened} plays",
                      makeTimeString(song.timeListened ?: 0L)
                    ),
                  thumbnailUrl = song.thumbnailUrl,
                )
              },
            artHeight = 72.dp,
          )
        }
      }

      if (mostPlayedArtists.isNotEmpty()) {
        item {
          StageSectionHeader(title = "Artists")
          StageStatCircles(
            entries =
              mostPlayedArtists.map { artist ->
                StageStatEntry(
                  key = artist.id,
                  title = artist.artist.name,
                  subtitle = "${artist.songCount} plays",
                  thumbnailUrl = artist.artist.thumbnailUrl?.resize(544, 544),
                )
              },
            size = 60.dp,
          )
        }
      }

      if (mostPlayedAlbums.isNotEmpty()) {
        item {
          StageSectionHeader(title = "Albums")
          StageStatCards(
            entries =
              mostPlayedAlbums.map { album ->
                StageStatEntry(
                  key = album.id,
                  title = album.album.title,
                  subtitle =
                    joinByBullet(
                      "${album.songCountListened} plays",
                      makeTimeString(album.timeListened ?: 0L)
                    ),
                  thumbnailUrl = album.album.thumbnailUrl?.resize(544, 544),
                )
              },
            artHeight = 72.dp,
          )
        }
      }

      item { Spacer(modifier = Modifier.height(60.dp)) }
    }
  }
}
