package echo.music.iad1tya.playback

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.music.echo.spotify.SpotifyPlaybackResolver
import com.music.innertube.models.SongItem
import dagger.hilt.android.qualifiers.ApplicationContext
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.spotify.models.SpotifyTrack
import echo.music.iad1tya.utils.reportException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Finds a Spotify playlist's songs on YouTube and queues each one as soon as it is found, so a long
 * playlist starts downloading at once and carries on after its page is closed.
 */
@Singleton
class SpotifyDownloadQueue
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val database: MusicDatabase,
  private val resolver: SpotifyPlaybackResolver,
  private val downloadUtil: DownloadUtil,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val prefs = context.getSharedPreferences("spotify_downloads", Context.MODE_PRIVATE)
  private val statePrefs = context.getSharedPreferences("spotify_download_state", Context.MODE_PRIVATE)
  private val jobs = ConcurrentHashMap<String, Job>()

  private val _preparing = MutableStateFlow<Set<String>>(emptySet())
  /** Ids of the playlists still being matched on YouTube. */
  val preparing: StateFlow<Set<String>> = _preparing.asStateFlow()

  private val _songIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
  /** The YouTube song ids queued so far, by Spotify playlist id. */
  val songIds: StateFlow<Map<String, Set<String>>> = _songIds.asStateFlow()

  private val _stopped =
    MutableStateFlow(statePrefs.getStringSet(STOPPED_KEY, emptySet()).orEmpty().toSet())
  /** Ids of the playlists whose download was cancelled before every song was queued. */
  val stopped: StateFlow<Set<String>> = _stopped.asStateFlow()

  fun load(playlistId: String) {
    val saved = prefs.getStringSet(playlistId, emptySet()).orEmpty()
    _songIds.update { if (playlistId in it) it else it + (playlistId to saved) }
  }

  fun download(
    playlistId: String,
    tracks: List<SpotifyTrack>,
    onFinished: (queued: Int, missing: Int) -> Unit,
  ) {
    if (playlistId in _preparing.value) return
    _preparing.update { it + playlistId }
    setStopped(playlistId, false)
    jobs[playlistId] =
      scope.launch {
        val missing = AtomicInteger()
        try {
          // Small batches rather than one big one: the resolver's three slots stay free between
          // them, so tapping a song to play doesn't wait behind the whole playlist.
          for (batch in tracks.chunked(RESOLVE_BATCH)) {
            batch
              .map { track ->
                launch {
                  val song = resolver.resolveTrack(track)
                  if (song == null) missing.incrementAndGet() else queue(playlistId, song)
                }
              }
              .joinAll()
          }
        } finally {
          jobs.remove(playlistId)
          _preparing.update { it - playlistId }
        }
        val queued = _songIds.value[playlistId].orEmpty().size
        withContext(Dispatchers.Main) { onFinished(queued, missing.get()) }
      }
  }

  private suspend fun queue(playlistId: String, song: SongItem) {
    try {
      database.withTransaction { insert(song.toMediaMetadata()) }
      val alreadyDownloaded = downloadUtil.downloads.value[song.id]?.state == Download.STATE_COMPLETED
      if (!alreadyDownloaded) {
        val request =
          DownloadRequest.Builder(song.id, song.id.toUri())
            .setCustomCacheKey(song.id)
            .setData(song.title.toByteArray())
            .build()
        try {
          DownloadService.sendAddDownload(context, ExoDownloadService::class.java, request, false)
        } catch (e: IllegalStateException) {
          // Android refuses to start a service once the app is in the background.
          withContext(Dispatchers.Main) { downloadUtil.downloadManager.addDownload(request) }
        }
      }
      val ids =
        synchronized(this) {
          val started = startedIds(playlistId)
          statePrefs
            .edit()
            .putStringSet(playlistId, if (alreadyDownloaded) started else started + song.id)
            .apply()
          val updated = _songIds.value[playlistId].orEmpty() + song.id
          _songIds.update { it + (playlistId to updated) }
          updated
        }
      prefs.edit().putStringSet(playlistId, ids).apply()
    } catch (e: Exception) {
      if (e is kotlinx.coroutines.CancellationException) throw e
      reportException(e)
    }
  }

  /** Stops the songs still in progress. Finished ones stay, and a later download resumes the rest. */
  fun cancel(playlistId: String) {
    setStopped(playlistId, true)
    drop(playlistId) { downloadUtil.downloads.value[it]?.state == Download.STATE_COMPLETED }
  }

  fun remove(playlistId: String) {
    setStopped(playlistId, false)
    drop(playlistId) { false }
  }

  private fun drop(playlistId: String, keep: (String) -> Boolean) {
    val job = jobs[playlistId]
    scope.launch {
      job?.cancelAndJoin()
      val kept = _songIds.value[playlistId].orEmpty().filter(keep).toSet()
      val started = startedIds(playlistId)
      // A song stays if another list holds it, or if it was already downloaded before this list.
      val otherLists =
        prefs.all.filterKeys { it != playlistId }.values.flatMap { (it as? Set<*>).orEmpty() }.toSet()
      (started - kept).filterNot { it in otherLists }.forEach { id ->
        DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, id, false)
      }
      synchronized(this@SpotifyDownloadQueue) {
        _songIds.update { it + (playlistId to kept) }
        prefs.edit().putStringSet(playlistId, kept).apply()
        statePrefs.edit().putStringSet(playlistId, started intersect kept).apply()
      }
    }
  }

  /** The songs this list itself started downloading. Older lists didn't record it, so all count. */
  private fun startedIds(playlistId: String): Set<String> =
    statePrefs.getStringSet(playlistId, null)?.toSet()
      ?: prefs.getStringSet(playlistId, emptySet()).orEmpty().toSet()

  private fun setStopped(playlistId: String, stopped: Boolean) {
    _stopped.update { if (stopped) it + playlistId else it - playlistId }
    statePrefs.edit().putStringSet(STOPPED_KEY, _stopped.value).apply()
  }
}

private const val RESOLVE_BATCH = 3
private const val STOPPED_KEY = "stopped:"
