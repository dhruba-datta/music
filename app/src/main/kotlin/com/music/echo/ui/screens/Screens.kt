package echo.music.iad1tya.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import echo.music.iad1tya.R

@Immutable
sealed class Screens(
  @StringRes val titleId: Int,
  @DrawableRes val iconIdInactive: Int,
  @DrawableRes val iconIdActive: Int,
  val route: String,
) {
  object Home :
    Screens(
      titleId = R.string.home,
      iconIdInactive = R.drawable.home_outlined,
      iconIdActive = R.drawable.home_filled,
      route = "home"
    )

  object Search :
    Screens(
      titleId = R.string.search,
      iconIdInactive = R.drawable.search,
      iconIdActive = R.drawable.search,
      route = "search_input"
    )

  object Recognize :
    Screens(
      titleId = R.string.recognition,
      iconIdInactive = R.drawable.mic,
      iconIdActive = R.drawable.mic,
      route = "recognition"
    )

  object ListenTogether :
    Screens(
      titleId = R.string.together,
      iconIdInactive = R.drawable.group_outlined,
      iconIdActive = R.drawable.group_filled,
      route = "listen_together"
    )

  object Library :
    Screens(
      titleId = R.string.filter_library,
      iconIdInactive = R.drawable.library_music_outlined,
      iconIdActive = R.drawable.library_music_filled,
      route = "library"
    )

  object Generate :
    Screens(
      titleId = R.string.playlists,
      iconIdInactive = R.drawable.playlist_play,
      iconIdActive = R.drawable.playlist_play,
      route = "generate"
    )

  companion object {
    val MainScreens = listOf(Home, Search, Recognize, Library)
  }
}
