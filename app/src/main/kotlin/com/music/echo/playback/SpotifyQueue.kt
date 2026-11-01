package echo.music.iad1tya.playback

import androidx.media3.common.MediaItem
import com.music.echo.spotify.SpotifyPlaybackResolver
import com.music.innertube.models.SongItem
import echo.music.iad1tya.extensions.toMediaItem
import echo.music.iad1tya.models.MediaMetadata
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.Queue
import echo.music.iad1tya.spotify.models.SpotifyTrack
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Plays a Spotify list in its own order, starting from [firstSong]. Each track has to be matched on
 * YouTube, so they are matched a few at a time as playback gets close to them.
 *
 * [tracks] is read on every page because a long list may still be loading when playback starts.
 */
class SpotifyQueue(
  private val title: String,
  private val tracks: () -> List<SpotifyTrack>,
  private var nextIndex: Int,
  private val firstSong: SongItem,
  private val resolver: SpotifyPlaybackResolver,
) : Queue {
  override val preloadItem: MediaMetadata = firstSong.toMediaMetadata()

  override suspend fun getInitialStatus(): Queue.Status =
    Queue.Status(
      title = title,
      items = listOf(firstSong.toMediaItem()) + nextPage(),
      mediaItemIndex = 0,
    )

  override fun hasNextPage(): Boolean = nextIndex < tracks().size

  override suspend fun nextPage(): List<MediaItem> =
    withContext(IO) {
      var items = emptyList<MediaItem>()
      while (items.isEmpty() && hasNextPage()) {
        val page = tracks().drop(nextIndex).take(PAGE_SIZE)
        items =
          coroutineScope { page.map { async { resolver.resolveTrack(it) } }.awaitAll() }
            .filterNotNull()
            .map { it.toMediaItem() }
        nextIndex += page.size
      }
      items
    }
}

private const val PAGE_SIZE = 6
