package echo.music.iad1tya.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RandomizeGridItem(isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {

  val loadingAlpha by
    animateFloatAsState(
      targetValue = if (isLoading) 1f else 0f,
      animationSpec = tween(durationMillis = 400),
      label = "loadingAlpha"
    )

  Box(
    modifier =
      modifier
        .aspectRatio(1f)
        .clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      painter = painterResource(R.drawable.stage_shuffle),
      contentDescription = stringResource(R.string.shuffle),
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(28.dp).alpha(1f - loadingAlpha),
    )

    Box(modifier = Modifier.alpha(loadingAlpha)) {
      LoadingIndicator(
        modifier = Modifier.size(48.dp),
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

/** The fixed speed dial tile that opens downloaded songs. */
@Composable
fun DownloadsGridItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .aspectRatio(1f)
        .clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      painter = painterResource(R.drawable.download),
      contentDescription = stringResource(R.string.downloaded_songs),
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(28.dp),
    )
  }
}

/** A Home card that opens one music service. */
@Composable
fun MusicSourceCard(
  title: String,
  @DrawableRes iconRes: Int,
  colors: List<Color>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.SpaceBetween,
    modifier =
      modifier
        .height(92.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(Brush.linearGradient(colors))
        .clickable(onClick = onClick)
        .padding(14.dp),
  ) {
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(26.dp),
    )
    Text(
      text = title,
      color = Color.White,
      fontSize = 16.sp,
      fontWeight = FontWeight.ExtraBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
