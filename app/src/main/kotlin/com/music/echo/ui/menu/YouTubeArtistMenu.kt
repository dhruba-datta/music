package echo.music.iad1tya.ui.menu

import echo.music.iad1tya.ui.component.StageSheetTitle
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.music.innertube.models.ArtistItem
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalListenTogetherManager
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.db.entities.ArtistEntity
import echo.music.iad1tya.db.entities.SpeedDialItem
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.Material3MenuItemData
import echo.music.iad1tya.ui.component.MenuActionGrid
import echo.music.iad1tya.ui.component.MenuTileLabel
import echo.music.iad1tya.ui.component.YouTubeListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeArtistMenu(
  artist: ArtistItem,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val libraryArtist by database.artist(artist.id).collectAsState(initial = null)
  val listenTogetherManager = LocalListenTogetherManager.current
  val isGuest = listenTogetherManager?.isGuestPlaybackRestricted == true
  val isPinned by database.speedDialDao.isPinned(artist.id).collectAsState(initial = false)
  val coroutineScope = rememberCoroutineScope()

  StageSheetTitle(title = artist.title)

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
    item {
      MenuActionGrid(
        items =
          buildList {
            if (!isGuest) {
              artist.radioEndpoint?.let { watchEndpoint ->
                add(
                  Material3MenuItemData(
                    icon = {
                      Icon(
                        painter = painterResource(R.drawable.radio),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                      )
                    },
                    title = { MenuTileLabel(text = stringResource(R.string.start_an_radio)) },
                    onClick = {
                      playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                      onDismiss()
                    }
                  )
                )
              }
            }

            add(
              Material3MenuItemData(
                icon = {
                  Icon(
                    painter = painterResource(if (isPinned) R.drawable.remove else R.drawable.add),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                  )
                },
                title = { MenuTileLabel(text = if (isPinned) "Unpin" else "Pin") },
                onClick = {
                  coroutineScope.launch(Dispatchers.IO) {
                    if (isPinned) {
                      database.speedDialDao.delete(artist.id)
                    } else {
                      database.speedDialDao.insert(SpeedDialItem.fromYTItem(artist))
                    }
                  }
                  onDismiss()
                }
              )
            )

            add(
              Material3MenuItemData(
                icon = {
                  Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                  )
                },
                title = { MenuTileLabel(text = stringResource(R.string.share)) },
                onClick = {
                  val intent =
                    Intent().apply {
                      action = Intent.ACTION_SEND
                      type = "text/plain"
                      putExtra(Intent.EXTRA_TEXT, artist.shareLink)
                    }
                  context.startActivity(Intent.createChooser(intent, null))
                  onDismiss()
                }
              )
            )
            addAll(
              listOf(
                Material3MenuItemData(
                  title = {
                    MenuTileLabel(
                      text =
                        if (libraryArtist?.artist?.bookmarkedAt != null)
                          stringResource(R.string.subscribed)
                        else stringResource(R.string.subscribe)
                    )
                  },
                  icon = {
                    Icon(
                      painter =
                        painterResource(
                          if (libraryArtist?.artist?.bookmarkedAt != null) {
                            R.drawable.subscribed
                          } else {
                            R.drawable.subscribe
                          }
                        ),
                      contentDescription = null,
                    )
                  },
                  onClick = {
                    database.query {
                      val libraryArtist = libraryArtist
                      if (libraryArtist != null) {
                        update(libraryArtist.artist.toggleLike())
                      } else {
                        insert(
                          ArtistEntity(
                              id = artist.id,
                              name = artist.title,
                              channelId = artist.channelId,
                              thumbnailUrl = artist.thumbnail,
                            )
                            .toggleLike()
                        )
                      }
                    }
                  }
                )
              )
            )
          }
      )
    }
  }
}
