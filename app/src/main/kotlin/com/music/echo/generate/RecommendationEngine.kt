package echo.music.iad1tya.generate

import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.db.daos.RecommendationExclusionDao
import echo.music.iad1tya.db.daos.SongPlayStatsDao
import echo.music.iad1tya.db.entities.SongPlayStatsEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
open class RecommendationEngine @Inject constructor(
  private val songPlayStatsDao: SongPlayStatsDao,
  private val recommendationExclusionDao: RecommendationExclusionDao,
  private val database: MusicDatabase? = null,
) : LocalTasteEngine(songPlayStatsDao, recommendationExclusionDao, database) {
  var allowNullDatabaseForTesting: Boolean = false

  override suspend fun generate(count: Int): Result<String> = withContext(Dispatchers.IO) {
    try {
      val exclusions = recommendationExclusionDao.getAll().map { it.trackKey }.toSet()
      val timeOfDay = currentTimeOfDay()

      val now = System.currentTimeMillis()
      val thirtyDaysAgo = now - 30L * 86_400_000L

      val recentStats = songPlayStatsDao.mostPlayedSince(thirtyDaysAgo, limit = 100)
        .ifEmpty { songPlayStatsDao.recentlyPlayed(limit = 20) }

      if (recentStats.isEmpty()) {
        return@withContext Result.failure(IllegalStateException("No listening history available yet"))
      }

      val likedSongs = database?.likedSongsByRowIdAsc()?.firstOrNull().orEmpty()
      val likedKeys = likedSongs.map { song ->
        trackKeyOf(song.song.title, song.artists.joinToString(", ") { it.name })
      }.toSet()

      val eligibleSeeds = recentStats
        .filter { it.trackKey !in exclusions && applyCandidateFilter(it) }
        .sortedByDescending { scoreStats(it, likedKeys, timeOfDay) }

      if (eligibleSeeds.isEmpty()) {
        return@withContext Result.failure(IllegalStateException("All candidate tracks have been excluded"))
      }

      // 1. Local YouTube related tracks expansion from the top listening seeds
      val seedVideoIds = eligibleSeeds.mapNotNull { it.videoId }.distinct().take(5)
      val localCandidates = mutableListOf<SongItem>()

      for (videoId in seedVideoIds) {
        val nextResult = runCatching {
          YouTube.next(WatchEndpoint(videoId = videoId)).getOrNull()
        }.getOrNull()

        nextResult?.items?.filterIsInstance<SongItem>()?.let { localCandidates.addAll(it) }

        nextResult?.relatedEndpoint?.let { endpoint ->
          runCatching {
            YouTube.related(endpoint).getOrNull()?.songs
          }.getOrNull()?.let { localCandidates.addAll(it) }
        }
      }

      // 2. Merge candidates and calculate scores from local listening history
      val seenKeys = mutableSetOf<String>()
      seenKeys.addAll(exclusions)

      // Cap combined candidates before scoring (e.g. count * 3)
      val maxCandidatesToScore = (count * 3).coerceAtLeast(30)
      val combinedCandidates = localCandidates
        .distinctBy { it.id }
        .filter { songItem ->
          val artistName = songItem.artists.joinToString(", ") { it.name }.trim()
          val key = trackKeyOf(songItem.title, artistName)
          if (key in seenKeys) false else {
            seenKeys.add(key)
            true
          }
        }
        .take(maxCandidatesToScore)

      // Batch Room stats lookups instead of calling find for each candidate
      val candidateKeys = combinedCandidates.map { songItem ->
        val artistName = songItem.artists.joinToString(", ") { it.name }.trim()
        trackKeyOf(songItem.title, artistName)
      }
      val statsByKey: Map<String, SongPlayStatsEntity> = songPlayStatsDao.findByKeys(candidateKeys)
        .associateBy { it.trackKey }

      data class CandidateWithScore(val song: SongItem, val score: Float)
      val scoredCandidates = mutableListOf<CandidateWithScore>()

      for (songItem in combinedCandidates) {
        val artistName = songItem.artists.joinToString(", ") { it.name }.trim()
        val key = trackKeyOf(songItem.title, artistName)

        val localStats = statsByKey[key]
        val score = if (localStats != null) {
          scoreStats(localStats, likedKeys, timeOfDay)
        } else {
          // Unplayed discovery candidate base score
          5.0f
        }

        scoredCandidates.add(CandidateWithScore(songItem, score))
      }

      val sortedSongs = scoredCandidates
        .sortedByDescending { it.score }
        .map { it.song }

      val finalSongs = if (sortedSongs.size >= 5) {
        sortedSongs.take(count)
      } else {
        // Fallback to local items if needed
        val localFallbackItems = eligibleSeeds
          .filter { it.videoId != null }
          .take(count)
          .map { stats ->
            SongItem(
              id = stats.videoId!!,
              title = stats.title,
              artists = listOf(com.music.innertube.models.Artist(name = stats.artist, id = null)),
              album = null,
              duration = null,
              thumbnail = stats.artworkUrl.orEmpty(),
            )
          }
        (sortedSongs + localFallbackItems).distinctBy { it.id }.take(count)
      }

      if (finalSongs.isEmpty()) {
        return@withContext super.generate(count)
      }

      val playlistId = "local_taste_${System.currentTimeMillis()}"
      val playlistName = "Taste Mix (${timeOfDay.replaceFirstChar { it.uppercase() }})"

      if (database == null && !allowNullDatabaseForTesting) {
        return@withContext Result.failure(IllegalStateException("Database not available to persist playlist"))
      }

      persistPlaylist(playlistId, playlistName, finalSongs)

      Result.success(playlistId)
    } catch (e: Exception) {
      if (e is kotlinx.coroutines.CancellationException) throw e
      Timber.e(e, "RecommendationEngine generation failed, attempting local fallback")
      super.generate(count)
    }
  }
}
