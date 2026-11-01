package echo.music.iad1tya.extensions

import echo.music.iad1tya.db.entities.Album
import echo.music.iad1tya.db.entities.Playlist
import echo.music.iad1tya.db.entities.Song

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) asReversed() else this

fun <T> MutableList<T>.move(
  fromIndex: Int,
  toIndex: Int,
): MutableList<T> {
  add(toIndex, removeAt(fromIndex))
  return this
}

fun List<Song>.filterExplicit(enabled: Boolean = true) =
  if (enabled) {
    filter { !it.song.explicit }
  } else {
    this
  }

fun List<Song>.filterVideoSongs(enabled: Boolean = true) =
  if (enabled) {
    filter { !it.song.isVideo }
  } else {
    this
  }

fun List<Album>.filterExplicitAlbums(enabled: Boolean = true) =
  if (enabled) {
    filter { !it.album.explicit }
  } else {
    this
  }

fun List<Playlist>.filterYoutubeShorts(enabled: Boolean = false) =
  if (enabled) {
    filterNot { it.playlist.browseId?.startsWith("SS") == true }
  } else {
    this
  }
