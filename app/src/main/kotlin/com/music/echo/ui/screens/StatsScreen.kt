package echo.music.iad1tya.ui.screens

import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.ChipsRow
import echo.music.iad1tya.ui.component.ItemThumbnail
import echo.music.iad1tya.ui.component.StageSectionHeader
import echo.music.iad1tya.ui.component.StageStatCards
import echo.music.iad1tya.ui.component.StageStatCircles
import echo.music.iad1tya.ui.component.StageStatEntry
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.constants.StatPeriod
import echo.music.iad1tya.extensions.toMediaItem
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.ListQueue
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.ChoiceChipsRow
import echo.music.iad1tya.ui.component.HideOnScrollFAB
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.LocalAlbumsGrid
import echo.music.iad1tya.ui.component.LocalArtistsGrid
import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.LocalSongsGrid
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.menu.AlbumMenu
import echo.music.iad1tya.ui.menu.ArtistMenu
import echo.music.iad1tya.ui.menu.SongMenu
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.joinByBullet
import echo.music.iad1tya.utils.makeTimeString
import echo.music.iad1tya.viewmodels.StatsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
  navController: NavController,
  viewModel: StatsViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
  val context = LocalContext.current

  val indexChips by viewModel.indexChips.collectAsState()
  val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
  val mostPlayedSongsStats by viewModel.mostPlayedSongsStats.collectAsState()
  val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsState()
  val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsState()
  val firstEvent by viewModel.firstEvent.collectAsState()
  val currentDate = LocalDateTime.now()

  val totalPlayTime by viewModel.totalPlayTime.collectAsState()
  val allTimePlayTime by viewModel.allTimePlayTime.collectAsState()
  val uniqueSongsCount by viewModel.uniqueSongsCount.collectAsState()
  val uniqueArtistsCount by viewModel.uniqueArtistsCount.collectAsState()
  val uniqueAlbumsCount by viewModel.uniqueAlbumsCount.collectAsState()

  var showHistorySheet by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val selectedOption by viewModel.selectedOption.collectAsState()

  val firstEventTimestamp = firstEvent?.event?.timestamp

  val weeklyDates =
    if (currentDate != null && firstEventTimestamp != null) {
      val minDate = firstEventTimestamp.minusWeeks(1)
      generateSequence(currentDate) { it.minusWeeks(1) }
        .takeWhile { it.isAfter(minDate) }
        .mapIndexed { index, date ->
          val endDate = date.plusWeeks(1).minusDays(1).coerceAtMost(currentDate)
          val formatter = DateTimeFormatter.ofPattern("dd MMM")

          val startDateFormatted = formatter.format(date)
          val endDateFormatted = formatter.format(endDate)

          val startMonth = date.month
          val endMonth = endDate.month
          val startYear = date.year
          val endYear = endDate.year

          val text =
            when {
              startYear != currentDate.year ->
                "$startDateFormatted, $startYear - $endDateFormatted, $endYear"
              startMonth != endMonth -> "$startDateFormatted - $endDateFormatted"
              else -> "${date.dayOfMonth} - $endDateFormatted"
            }
          Pair(index, text)
        }
        .toList()
    } else {
      emptyList()
    }

  val monthlyDates =
    if (currentDate != null && firstEventTimestamp != null) {
      val minDate = firstEventTimestamp.withDayOfMonth(1)
      generateSequence(currentDate.plusMonths(1).withDayOfMonth(1).minusDays(1)) {
          it.minusMonths(1)
        }
        .takeWhile {
          it.isAfter(minDate)
        }
        .mapIndexed { index, date ->
          val formatter = DateTimeFormatter.ofPattern("MMM")
          val formattedDate = formatter.format(date)
          val text =
            if (date.year != currentDate.year) {
              "$formattedDate ${date.year}"
            } else {
              formattedDate
            }
          Pair(index, text)
        }
        .toList()
    } else {
      emptyList()
    }

  val yearlyDates =
    if (currentDate != null && firstEventTimestamp != null) {
      generateSequence(
          currentDate.plusYears(1).withDayOfYear(1).minusDays(1),
        ) {
          it.minusYears(1)
        }
        .takeWhile {
          it.isAfter(firstEventTimestamp)
        }
        .mapIndexed { index, date -> Pair(index, "${date.year}") }
        .toList()
    } else {
      emptyList()
    }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      state = lazyListState,
      contentPadding =
        LocalPlayerAwareWindowInsets.current
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
          .asPaddingValues(),
      modifier =
        Modifier.windowInsetsPadding(
          LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
        )
    ) {
      item(key = "choice_chips") {
        ChipsRow(
          chips =
            listOf(
              OptionStats.CONTINUOUS to stringResource(id = R.string.continuous),
              OptionStats.WEEKS to stringResource(R.string.weeks),
              OptionStats.MONTHS to stringResource(R.string.months),
              OptionStats.YEARS to stringResource(R.string.years),
            ),
          currentValue = selectedOption,
          onValueUpdate = {
            viewModel.selectedOption.value = it
            viewModel.indexChips.value = 0
          },
        )
        ChipsRow(
          chips =
            when (selectedOption) {
              OptionStats.WEEKS -> weeklyDates
              OptionStats.MONTHS -> monthlyDates
              OptionStats.YEARS -> yearlyDates
              OptionStats.CONTINUOUS -> {
                listOf(
                  StatPeriod.WEEK_1.ordinal to pluralStringResource(R.plurals.n_week, 1, 1),
                  StatPeriod.MONTH_1.ordinal to pluralStringResource(R.plurals.n_month, 1, 1),
                  StatPeriod.MONTH_3.ordinal to pluralStringResource(R.plurals.n_month, 3, 3),
                  StatPeriod.MONTH_6.ordinal to pluralStringResource(R.plurals.n_month, 6, 6),
                  StatPeriod.YEAR_1.ordinal to pluralStringResource(R.plurals.n_year, 1, 1),
                  StatPeriod.ALL.ordinal to stringResource(R.string.filter_all),
                )
              }
            },
          currentValue = indexChips,
          onValueUpdate = { viewModel.indexChips.value = it },
        )
      }

      item(key = "mostPlayedSongs") {
        StageSectionHeader(
          title = stringResource(id = R.string.songs),
          modifier = Modifier.animateItem(),
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().animateItem()) {
          val rowWidth = maxWidth - 44.dp
          LazyHorizontalGrid(
            rows = GridCells.Fixed(mostPlayedSongsStats.size.coerceIn(1, 3)),
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(64.dp * mostPlayedSongsStats.size.coerceIn(1, 3)),
          ) {
            itemsIndexed(
              items = mostPlayedSongsStats,
              key = { _, song -> song.id },
            ) { index, song ->
              val playCount = song.songCountListened ?: 0
              val matchedSong =
                mostPlayedSongs.find { it.id == song.id } ?: mostPlayedSongs.getOrNull(index)
              val isActive = song.id == mediaMetadata?.id
              val showMenu: () -> Unit = {
                if (matchedSong != null) {
                  menuState.show {
                    SongMenu(
                      originalSong = matchedSong,
                      navController = navController,
                      onDismiss = menuState::dismiss,
                    )
                  }
                }
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                modifier =
                  Modifier.width(rowWidth)
                    .height(64.dp)
                    .combinedClickable(
                      onClick = {
                        if (isActive) {
                          playerConnection.togglePlayPause()
                        } else {
                          playerConnection.playQueue(
                            YouTubeQueue(
                              endpoint = WatchEndpoint(song.id),
                              preloadItem = matchedSong?.toMediaMetadata(),
                            ),
                          )
                        }
                      },
                      onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu()
                      },
                    ),
              ) {
                ItemThumbnail(
                  thumbnailUrl = song.thumbnailUrl,
                  isActive = isActive,
                  isPlaying = isPlaying,
                  shape = RoundedCornerShape(12.dp),
                  forceCrop = true,
                  modifier = Modifier.size(52.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${index + 1}. ${song.title}",
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text =
                      joinByBullet(
                        pluralStringResource(R.plurals.n_time, playCount, playCount),
                        makeTimeString(song.timeListened),
                      ),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                IconButton(onClick = showMenu) {
                  Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
                }
              }
            }
          }
        }
      }

      item(key = "mostPlayedArtists") {
        StageSectionHeader(
          title = stringResource(id = R.string.artists),
          modifier = Modifier.animateItem(),
        )

        StageStatCircles(
          entries =
            mostPlayedArtists.map { artist ->
              StageStatEntry(
                key = artist.id,
                title = artist.artist.name,
                subtitle = makeTimeString(artist.timeListened?.toLong()),
                thumbnailUrl = artist.artist.thumbnailUrl,
                onClick = { navController.navigate("artist/${artist.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    ArtistMenu(
                      originalArtist = artist,
                      coroutineScope = coroutineScope,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              )
            },
          size = 64.dp,
          modifier = Modifier.animateItem(),
        )
      }

      item(key = "mostPlayedAlbums") {
        StageSectionHeader(
          title = stringResource(id = R.string.albums),
          modifier = Modifier.animateItem(),
        )

        if (mostPlayedAlbums.isNotEmpty()) {
          val albumEntries =
            mostPlayedAlbums.map { album ->
              val playCount = album.songCountListened ?: 0
              StageStatEntry(
                key = album.id,
                title = album.album.title,
                subtitle =
                  joinByBullet(
                    pluralStringResource(R.plurals.n_time, playCount, playCount),
                    makeTimeString(album.timeListened),
                  ),
                thumbnailUrl = album.album.thumbnailUrl,
                onClick = { navController.navigate("album/${album.id}") },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    AlbumMenu(
                      originalAlbum = album,
                      navController = navController,
                      onDismiss = menuState::dismiss,
                    )
                  }
                },
              )
            }
          StageStatCards(entries = albumEntries, artHeight = 52.dp, modifier = Modifier.animateItem())
        }
      }

      item(key = "fab_spacer") { Spacer(Modifier.height(160.dp)) }
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(
              WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )
          )
          .padding(end = 18.dp, bottom = 24.dp),
    ) {
      FloatingActionButton(
        onClick = { showHistorySheet = true },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
      ) {
        Icon(
          painter = painterResource(R.drawable.history),
          contentDescription = stringResource(R.string.history),
        )
      }
      if (mostPlayedSongs.isNotEmpty()) {
        FloatingActionButton(
          onClick = {
            playerConnection.playQueue(
              ListQueue(
                title = context.getString(R.string.most_played_songs),
                items = mostPlayedSongs.map { it.toMediaMetadata().toMediaItem() }.shuffled()
              )
            )
          },
          shape = RoundedCornerShape(18.dp),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        ) {
          Icon(
            painter = painterResource(R.drawable.shuffle),
            contentDescription = stringResource(R.string.shuffle),
          )
        }
      }
    }

    StageTopBar(
      title = { StageBarTitle(stringResource(R.string.stats)) },
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
      },
    )

    if (showHistorySheet) {
      val currentPeriodLabel =
        when (selectedOption) {
          OptionStats.WEEKS -> weeklyDates.getOrNull(indexChips)?.second.orEmpty()
          OptionStats.MONTHS -> monthlyDates.getOrNull(indexChips)?.second.orEmpty()
          OptionStats.YEARS -> yearlyDates.getOrNull(indexChips)?.second.orEmpty()
          OptionStats.CONTINUOUS -> {
            when (indexChips) {
              StatPeriod.WEEK_1.ordinal -> pluralStringResource(R.plurals.n_week, 1, 1)
              StatPeriod.MONTH_1.ordinal -> pluralStringResource(R.plurals.n_month, 1, 1)
              StatPeriod.MONTH_3.ordinal -> pluralStringResource(R.plurals.n_month, 3, 3)
              StatPeriod.MONTH_6.ordinal -> pluralStringResource(R.plurals.n_month, 6, 6)
              StatPeriod.YEAR_1.ordinal -> pluralStringResource(R.plurals.n_year, 1, 1)
              StatPeriod.ALL.ordinal -> stringResource(R.string.filter_all)
              else -> ""
            }
          }
        }

      ActivityHistoryBottomSheet(
        onDismiss = { showHistorySheet = false },
        totalPlayTimeMs = totalPlayTime,
        allTimePlayTimeMs = allTimePlayTime,
        uniqueSongs = uniqueSongsCount,
        uniqueArtists = uniqueArtistsCount,
        uniqueAlbums = uniqueAlbumsCount,
        periodLabel = currentPeriodLabel
      )
    }
  }
}
