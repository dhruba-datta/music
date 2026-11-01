package echo.music.iad1tya.constants

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val CONTENT_TYPE_HEADER = 0
const val CONTENT_TYPE_LIST = 1
const val CONTENT_TYPE_SONG = 2
const val CONTENT_TYPE_ARTIST = 3
const val CONTENT_TYPE_ALBUM = 4
const val CONTENT_TYPE_PLAYLIST = 5

val FloatingToolbarHeight = 66.dp
val FloatingToolbarBottomPadding = 22.dp
val NavigationBarHeight = FloatingToolbarHeight
val MiniPlayerHeight = 60.dp
val MiniPlayerBottomSpacing = 6.dp
val QueuePeekHeight = 64.dp
val AppBarHeight = 64.dp

val ListItemHeight = 64.dp
val SuggestionItemHeight = 56.dp
val ListThumbnailSize = 52.dp
val SmallGridThumbnailHeight = 104.dp
val GridThumbnailHeight = 128.dp
val ThumbnailCornerRadius = 6.dp
val GridThumbnailCornerRadius = 18.dp

val PlayerHorizontalPadding = 32.dp

val NavigationBarAnimationSpec =
  spring<Dp>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
