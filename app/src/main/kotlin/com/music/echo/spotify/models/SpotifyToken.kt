/*
 * EchoMusic (2026)
 * © Chartreux Westia — github.com/koiverse
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package echo.music.iad1tya.spotify.models

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyInternalToken(
  val accessToken: String,
  val accessTokenExpirationTimestampMs: Long,
  val isAnonymous: Boolean = false,
  val clientId: String = "",
)
