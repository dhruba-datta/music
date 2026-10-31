package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.GridThumbnailCornerRadius

/** One entry on the Library's grid of automatic playlists. */
data class AutoPlaylistTile(val title: String, val icon: Int, val onClick: () -> Unit)

private fun tileColors(icon: Int): List<Color> =
  when (icon) {
    R.drawable.favorite -> listOf(Color(0xFFFF8A5C), Color(0xFFD9486A), Color(0xFF512A72))
    R.drawable.offline -> listOf(Color(0xFF6A4C93), Color(0xFF2A6F97))
    R.drawable.download -> listOf(Color(0xFF2A6F97), Color(0xFF45B26B))
    R.drawable.cached -> listOf(Color(0xFFE04F7F), Color(0xFF6A4C93))
    R.drawable.trending_up -> listOf(Color(0xFFF4B860), Color(0xFFD9486A))
    R.drawable.trending_down -> listOf(Color(0xFF386641), Color(0xFFA7C957))
    else -> listOf(Color(0xFF6A4C93), Color(0xFFD9486A))
  }

/** Artwork card for an automatic playlist: a colour tile with the title under it. */
@Composable
fun AutoPlaylistButton(
  title: String,
  icon: Int,
  onClick: () -> Unit,
  artHeight: Dp,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.clip(RoundedCornerShape(GridThumbnailCornerRadius)).clickable(onClick = onClick)) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.fillMaxWidth()
          .height(artHeight)
          .clip(RoundedCornerShape(GridThumbnailCornerRadius))
          .background(
            Brush.linearGradient(
              colors = tileColors(icon),
              start = Offset.Zero,
              end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            )
          ),
    ) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(26.dp),
      )
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = title,
      fontSize = 15.sp,
      lineHeight = 19.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/** The Library's automatic playlists, laid out as the board's `.cards` grid. */
@Composable
fun AutoPlaylistGrid(
  tiles: List<AutoPlaylistTile>,
  columns: Int,
  artHeight: Dp,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp),
  ) {
    tiles.chunked(columns).forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        row.forEach { tile ->
          AutoPlaylistButton(
            title = tile.title,
            icon = tile.icon,
            onClick = tile.onClick,
            artHeight = artHeight,
            modifier = Modifier.weight(1f),
          )
        }
        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
      }
    }
  }
}
