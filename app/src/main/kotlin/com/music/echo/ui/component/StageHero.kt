package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.offline.Download
import coil3.compose.AsyncImage
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppBarHeight
import echo.music.iad1tya.ui.utils.resize

private val HeroGutter = 22.dp
private val HeroActionSize = 44.dp
private val HeroPlaySize = 52.dp
// The boards' 300dp hero, measured from below the 64dp top bar: title mid-way, actions at the foot.
private val HeroTitleOffset = 52.dp
private val HeroActionsOffset = 58.dp
private val HeroBottomPadding = 14.dp

/** The size `ItemThumbnail` loads, so the hero can reuse what a list already fetched. */
private const val HeroPreviewSize = 544

/**
 * Stage collection header: full-width artwork that runs under the status bar and top bar, a fade
 * into the page background, a bold left-aligned title, a subtitle, then the page's action row.
 *
 * Every page passes its own actions; the hero has no behaviour of its own.
 */
@Composable
fun StageHero(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  artwork: @Composable BoxScope.() -> Unit,
  titleLeading: @Composable RowScope.() -> Unit = {},
  subtitleContent: @Composable ColumnScope.() -> Unit = {},
  belowActions: @Composable ColumnScope.() -> Unit = {},
  leadingActions: @Composable RowScope.() -> Unit = {},
  trailingActions: @Composable RowScope.() -> Unit = {},
) {
  val density = LocalDensity.current
  val bleed = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + AppBarHeight
  val background = MaterialTheme.colorScheme.background

  Box(modifier = modifier.fillMaxWidth()) {
    Box(
      modifier =
        Modifier.matchParentSize().layout { measurable, constraints ->
          val bleedPx = with(density) { bleed.roundToPx() }
          val height = constraints.maxHeight + bleedPx
          val placeable =
            measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
          layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, -bleedPx) }
        }
    ) {
      artwork()
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                0f to Color.Transparent,
                0.3f to Color.Transparent,
                0.62f to background.copy(alpha = 0.72f),
                1f to background,
              )
            )
      )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
      Spacer(Modifier.height(HeroTitleOffset))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = HeroGutter),
      ) {
        titleLeading()
        Text(
          text = title,
          fontSize = 32.sp,
          lineHeight = 35.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Start,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
      }

      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = HeroGutter).padding(top = 6.dp),
      ) {
        if (subtitle != null) {
          Text(
            text = subtitle,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
        subtitleContent()
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = HeroGutter)
            .padding(top = HeroActionsOffset, bottom = HeroBottomPadding),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
          content = leadingActions,
        )
        trailingActions()
      }

      belowActions()
    }
  }
}

/**
 * A cropped [thumbnailUrl] for the [StageHero] artwork slot, or a plain tile when there is none.
 *
 * The list thumbnail is usually cached already, so it fills the hero at once and the full-size image
 * fades in over it, instead of the page sitting black until that image arrives.
 */
@Composable
fun BoxScope.StageHeroArtwork(thumbnailUrl: String?) {
  Box(modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceVariant))
  if (thumbnailUrl.isNullOrEmpty()) return

  val preview = thumbnailUrl.resize(HeroPreviewSize, HeroPreviewSize)
  if (preview != thumbnailUrl) {
    AsyncImage(
      model = preview,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize(),
    )
  }
  AsyncImage(
    model = thumbnailUrl,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.matchParentSize(),
  )
}

/** Round action on the hero's row. Use for anything that is not a plain icon. */
@Composable
fun StageHeroAction(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(
    onClick = onClick,
    enabled = enabled,
    shape = CircleShape,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = modifier.size(HeroActionSize),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize(), content = content)
  }
}

@Composable
fun StageHeroIconButton(
  icon: Int,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  StageHeroAction(onClick = onClick, modifier = modifier, enabled = enabled) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription,
      modifier = Modifier.size(22.dp),
    )
  }
}

@Composable
fun StageHeroShuffleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  StageHeroIconButton(
    icon = R.drawable.shuffle,
    contentDescription = stringResource(R.string.shuffle),
    onClick = onClick,
    modifier = modifier,
  )
}

/** The hero's white play button. Shows pause while this collection is the one playing. */
@Composable
fun StageHeroPlayButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPlaying: Boolean = false,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier.size(HeroPlaySize),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Icon(
        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

/** Download action whose icon follows the collection's [downloadState]. */
@Composable
fun StageHeroDownloadButton(
  downloadState: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  StageHeroAction(onClick = onClick, modifier = modifier) {
    when (downloadState) {
      Download.STATE_COMPLETED ->
        Icon(
          painter = painterResource(R.drawable.offline),
          contentDescription = stringResource(R.string.saved),
          modifier = Modifier.size(22.dp),
        )
      Download.STATE_DOWNLOADING ->
        CircularProgressIndicator(
          strokeWidth = 2.dp,
          modifier = Modifier.size(20.dp),
          color = MaterialTheme.colorScheme.onSurface,
        )
      else ->
        Icon(
          painter = painterResource(R.drawable.download),
          contentDescription = stringResource(R.string.action_download),
          modifier = Modifier.size(22.dp),
        )
    }
  }
}

/** Text pill for the hero's row, used where an action needs its label (Subscribe, Radio, Block). */
@Composable
fun StageHeroChip(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  leading: @Composable () -> Unit = {},
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color =
      if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    contentColor =
      if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    modifier = modifier.height(44.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      leading()
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
      )
    }
  }
}

/**
 * True while the hero is still behind the top bar, so the bar stays see-through over the artwork
 * and turns solid once the songs scroll under it.
 */
@Composable
fun rememberStageHeroUnderBar(state: LazyListState, threshold: Dp = 180.dp): State<Boolean> {
  val thresholdPx = with(LocalDensity.current) { threshold.roundToPx() }
  return remember(state, thresholdPx) {
    derivedStateOf {
      state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset < thresholdPx
    }
  }
}

/** Bold title for a page's top bar. */
@Composable
fun StageBarTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.ExtraBold,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
  )
}

/** Bold section heading used inside a page. */
@Composable
fun StageSectionTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.ExtraBold,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
  )
}

/** Section heading with an optional "More" link on the right. */
@Composable
fun StageSectionHeader(title: String, modifier: Modifier = Modifier, onMore: (() -> Unit)? = null) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 8.dp),
  ) {
    StageSectionTitle(text = title, modifier = Modifier.weight(1f))
    if (onMore != null) {
      Text(
        text = stringResource(R.string.more_label),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable(onClick = onMore).padding(vertical = 6.dp),
      )
    }
  }
}

/** Keeps a top bar icon legible over the hero artwork. */
@Composable
fun Modifier.stageBarButton(): Modifier =
  background(Color.Black.copy(alpha = 0.3f), CircleShape)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun stageHeroBarColors(transparent: Boolean): TopAppBarColors =
  if (transparent) {
    TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
  } else {
    TopAppBarDefaults.topAppBarColors()
  }
