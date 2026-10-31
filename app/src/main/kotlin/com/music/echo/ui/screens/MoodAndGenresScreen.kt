package echo.music.iad1tya.ui.screens

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.NavigationTitle
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.component.shimmer.ListItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.viewmodels.MoodAndGenresViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAndGenresScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  viewModel: MoodAndGenresViewModel = hiltViewModel(),
) {
  val localConfiguration = LocalConfiguration.current
  val itemsPerRow = if (localConfiguration.orientation == ORIENTATION_LANDSCAPE) 3 else 2

  val moodAndGenresList by viewModel.moodAndGenres.collectAsState()

  LazyColumn(
    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
  ) {
    if (moodAndGenresList == null) {
      item(key = "mood_and_genres_shimmer") {
        ShimmerHost(modifier = Modifier.animateItem()) { repeat(8) { ListItemPlaceHolder() } }
      }
    }

    moodAndGenresList?.forEachIndexed { index, moodAndGenres ->
      item(key = "mood_and_genres_section_$index") {
        Column(
          modifier = Modifier.animateItem(),
        ) {
          NavigationTitle(
            title = moodAndGenres.title,
          )
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 18.dp),
          ) {
            moodAndGenres.items.chunked(itemsPerRow).forEach { row ->
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach {
                  MoodAndGenresButton(
                    title = it.title,
                    height = if (index == 0) MoodButtonHeight else MoodAndGenresButtonHeight,
                    onClick = {
                      navController.navigate(
                        "youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}"
                      )
                    },
                    modifier = Modifier.weight(1f),
                  )
                }

                repeat(itemsPerRow - row.size) { Spacer(Modifier.weight(1f)) }
              }
            }
          }
        }
      }
    }
  }

  TopAppBar(
    title = { StageBarTitle(stringResource(R.string.mood_and_genres)) },
    navigationIcon = {
      IconButton(
        onClick = navController::navigateUp,
        onLongClick = navController::backToMain,
      ) {
        Icon(
          painterResource(R.drawable.arrow_back),
          contentDescription = null,
        )
      }
    },
  )
}

/** The 145 degree gradients of the Stage mood tiles; a title always gets the same one. */
private val MoodTileGradients =
  listOf(
    listOf(0f to Color(0xFFFF8A5C), 0.55f to Color(0xFFD9486A), 1f to Color(0xFF512A72)),
    listOf(0f to Color(0xFF6A4C93), 1f to Color(0xFF2A6F97)),
    listOf(0f to Color(0xFF2A6F97), 1f to Color(0xFF45B26B)),
    listOf(0f to Color(0xFFE04F7F), 1f to Color(0xFF6A4C93)),
    listOf(0f to Color(0xFFF4B860), 1f to Color(0xFFD9486A)),
    listOf(0f to Color(0xFF386641), 1f to Color(0xFFA7C957)),
  )

private fun moodTileBrush(title: String): Brush {
  val stops = MoodTileGradients[title.hashCode().mod(MoodTileGradients.size)]
  return object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
      // CSS linear-gradient(145deg): the gradient line runs corner to corner along that angle.
      val angle = Math.toRadians(145.0)
      val dirX = sin(angle).toFloat()
      val dirY = -cos(angle).toFloat()
      val half = (abs(size.width * dirX) + abs(size.height * dirY)) / 2f
      val center = size.center
      return LinearGradientShader(
        from = Offset(center.x - dirX * half, center.y - dirY * half),
        to = Offset(center.x + dirX * half, center.y + dirY * half),
        colors = stops.map { it.second },
        colorStops = stops.map { it.first },
      )
    }
  }
}

@Composable
fun MoodAndGenresButton(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = MoodAndGenresButtonHeight,
) {
  val brush = remember(title) { moodTileBrush(title) }
  Box(
    contentAlignment = Alignment.BottomStart,
    modifier =
      modifier
        .height(height)
        .clip(RoundedCornerShape(18.dp))
        .background(brush)
        .clickable(onClick = onClick)
        .padding(14.dp),
  ) {
    Text(
      text = title,
      fontSize = 17.sp,
      lineHeight = 21.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

val MoodAndGenresButtonHeight = 72.dp
private val MoodButtonHeight = 80.dp
