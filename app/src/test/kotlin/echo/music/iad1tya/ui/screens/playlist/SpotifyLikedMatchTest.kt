package echo.music.iad1tya.ui.screens.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifyLikedMatchTest {
  @Test
  fun titleDropsServiceExtras() {
    assertEquals("yesterday", matchableTitle("Yesterday - Remastered 2009"))
    assertEquals("yesterday", matchableTitle("Yesterday (Official Audio)"))
    assertEquals("(intro)", matchableTitle("(Intro)"))
  }

  @Test
  fun artistsMatchAcrossJoinedNames() {
    assertTrue(sharesArtist(listOf("Dua Lipa"), listOf("Dua Lipa & DaBaby")))
    assertFalse(sharesArtist(listOf("Dua Lipa"), listOf("Adele")))
    assertFalse(sharesArtist(listOf(""), listOf("Adele")))
  }
}
