package echo.music.iad1tya.generate

import echo.music.iad1tya.db.entities.RecommendationExclusionEntity
import echo.music.iad1tya.db.entities.SongPlayStatsEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecommendationEngineTest {
  private lateinit var fakeStatsDao: FakeSongPlayStatsDao
  private lateinit var fakeExclusionDao: FakeRecommendationExclusionDao
  private lateinit var engine: RecommendationEngine

  @Before
  fun setup() {
    fakeStatsDao = FakeSongPlayStatsDao()
    fakeExclusionDao = FakeRecommendationExclusionDao()
    engine = RecommendationEngine(
      songPlayStatsDao = fakeStatsDao,
      recommendationExclusionDao = fakeExclusionDao,
      database = null,
    ).apply {
      allowNullDatabaseForTesting = true
    }
  }

  @Test
  fun testNullDatabaseFailsWhenNotTestingAllowed() = runTest {
    fakeStatsDao.upsert(
      SongPlayStatsEntity(
        trackKey = "blinding lights|the weeknd",
        title = "Blinding Lights",
        artist = "The Weeknd",
        videoId = "seed_vid",
        totalPlayTimeMs = 300_000L,
        playCount = 10,
        skipCount = 0,
        lastPlayedAtMillis = System.currentTimeMillis(),
      )
    )

    val strictEngine = RecommendationEngine(
      songPlayStatsDao = fakeStatsDao,
      recommendationExclusionDao = fakeExclusionDao,
      database = null,
    )
    strictEngine.allowNullDatabaseForTesting = false

    val result = strictEngine.generate(10)
    assertTrue("Null database must fail when not explicitly allowed", result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Database not available") == true)
  }

  @Test
  fun testEmptyHistoryFailsGracefully() = runTest {
    val result = engine.generate(20)
    assertTrue("Empty history should fail", result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("No listening history") == true)
  }

  @Test
  fun testAllExcludedFailsGracefully() = runTest {
    fakeStatsDao.upsert(
      SongPlayStatsEntity(
        trackKey = "bad song|bad artist",
        title = "Bad Song",
        artist = "Bad Artist",
        videoId = "vid1",
        totalPlayTimeMs = 120_000L,
        playCount = 5,
        skipCount = 0,
        lastPlayedAtMillis = System.currentTimeMillis(),
      )
    )
    fakeExclusionDao.upsert(
      RecommendationExclusionEntity(
        trackKey = "bad song|bad artist",
        excludedAtMillis = System.currentTimeMillis(),
        trackName = "Bad Song",
        artistName = "Bad Artist",
      )
    )

    val result = engine.generate(20)
    assertTrue("All excluded should fail", result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("All candidate tracks have been excluded") == true)
  }

  @Test
  fun testRecommendationSucceedsWithLocalSeeds() = runTest {
    fakeStatsDao.upsert(
      SongPlayStatsEntity(
        trackKey = "blinding lights|the weeknd",
        title = "Blinding Lights",
        artist = "The Weeknd",
        videoId = "seed_vid",
        totalPlayTimeMs = 300_000L,
        playCount = 10,
        skipCount = 0,
        lastPlayedAtMillis = System.currentTimeMillis(),
      )
    )

    val result = engine.generate(10)
    assertTrue("Generation should succeed", result.isSuccess)
    val playlistId = result.getOrNull()
    assertTrue("PlaylistId should start with local_taste_", playlistId?.startsWith("local_taste_") == true)
  }
}
