package echo.music.iad1tya.ui.screens.recognition

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.shazamkit.models.RecognitionResult
import com.music.shazamkit.models.RecognitionStatus
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.db.entities.RecognitionHistory
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.IconButton
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(navController: NavController) {
  val context = LocalContext.current
  val database = LocalDatabase.current
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { echo.music.iad1tya.recognition.MusicRecognitionService.reset() }

  DisposableEffect(Unit) {
    onDispose { echo.music.iad1tya.recognition.MusicRecognitionService.reset() }
  }

  val recognitionStatus by
    echo.music.iad1tya.recognition.MusicRecognitionService.recognitionStatus.collectAsState()

  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
      isGranted ->
      hasPermission = isGranted
      if (isGranted) {
        coroutineScope.launch {
          echo.music.iad1tya.recognition.MusicRecognitionService.recognize(context)
        }
      }
    }

  fun startRecognition() {
    if (hasPermission) {
      coroutineScope.launch {
        echo.music.iad1tya.recognition.MusicRecognitionService.recognize(context)
      }
    } else {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  fun resetToReady() {
    echo.music.iad1tya.recognition.MusicRecognitionService.reset()
  }

  fun saveToHistory(result: RecognitionResult) {
    coroutineScope.launch(Dispatchers.IO) {
      database.query {
        insert(
          RecognitionHistory(
            trackId = result.trackId,
            title = result.title,
            artist = result.artist,
            album = result.album,
            coverArtUrl = result.coverArtUrl,
            coverArtHqUrl = result.coverArtHqUrl,
            genre = result.genre,
            releaseDate = result.releaseDate,
            label = result.label,
            shazamUrl = result.shazamUrl,
            appleMusicUrl = result.appleMusicUrl,
            spotifyUrl = result.spotifyUrl,
            isrc = result.isrc,
            youtubeVideoId = result.youtubeVideoId,
            recognizedAt = LocalDateTime.now()
          )
        )
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
      targetState = recognitionStatus,
      transitionSpec = { fadeIn().togetherWith(fadeOut()) },
      label = "main_content"
    ) { status ->
      if (status is RecognitionStatus.Success) {
        val playerConnection = LocalPlayerConnection.current
        SuccessState(
          result = status.result,
          onPlayOnApp = { result ->
            coroutineScope.launch(Dispatchers.IO) {
              val searchQuery = "${result.title} ${result.artist}"
              YouTube.search(searchQuery, YouTube.SearchFilter.FILTER_SONG).onSuccess { searchResult
                ->
                val song = searchResult.items.firstOrNull() as? SongItem
                song?.let {
                  kotlinx.coroutines.withContext(Dispatchers.Main) {
                    playerConnection?.playQueue(YouTubeQueue.radio(it.toMediaMetadata()))
                  }
                }
              }
            }
          },
          onTryAgain = { startRecognition() },
          onClose = ::resetToReady,
          onOpenHistory = { navController.navigate("recognition_history") },
          onSaveToHistory = ::saveToHistory
        )
      } else {
        Box(modifier = Modifier.fillMaxSize()) {
          Box(modifier = Modifier.fillMaxSize().background(Color.Transparent))

          Scaffold(
            containerColor = Color.Transparent,
            topBar = {
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                    .padding(start = 22.dp, end = 12.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = stringResource(R.string.recognize_music),
                  fontSize = 34.sp,
                  lineHeight = 40.sp,
                  letterSpacing = (-0.68).sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onBackground,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.IconButton(
                  onClick = { navController.navigate("recognition_history") }
                ) {
                  Icon(
                    painter = painterResource(R.drawable.history),
                    contentDescription = stringResource(R.string.recognition_history)
                  )
                }
              }
            }
          ) { paddingValues ->
            Column(
              modifier =
                Modifier.fillMaxSize()
                  .padding(paddingValues)
                  .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
                  )
                  .padding(start = 16.dp, end = 16.dp, top = 70.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Top
            ) {
              when (status) {
                is RecognitionStatus.Ready -> {
                  ReadyState(onStartRecognition = ::startRecognition)
                }
                is RecognitionStatus.Listening -> {
                  ListeningState(
                    onCancel = { echo.music.iad1tya.recognition.MusicRecognitionService.reset() }
                  )
                }
                is RecognitionStatus.Processing -> {
                  ProcessingState()
                }
                is RecognitionStatus.NoMatch -> {
                  NoMatchState(message = status.message, onTryAgain = { startRecognition() })
                }
                is RecognitionStatus.Error -> {
                  ErrorState(message = status.message, onTryAgain = { startRecognition() })
                }
                else -> Unit
              }
            }
          }
        }
      }
    }
  }
}

private val RecognitionDiscSize = 190.dp
private val ListeningDiscColor = Color(0xFFD9486A)
private val ProcessingDiscColor = Color(0xFF6A4C93)

/** The page's big round target, with the state's title and hint under it. */
@Composable
private fun RecognitionDisc(
  title: String,
  hint: String,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  discModifier: Modifier = Modifier,
  actions: @Composable ColumnScope.() -> Unit = {},
  content: @Composable BoxScope.() -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Box(
      modifier =
        discModifier
          .size(RecognitionDiscSize)
          .clip(CircleShape)
          .background(color)
          .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
      contentAlignment = Alignment.Center,
      content = content
    )

    Spacer(Modifier.height(18.dp))

    Text(
      text = title,
      fontSize = 23.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
    Text(
      text = hint,
      fontSize = 15.sp,
      lineHeight = 21.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 32.dp)
    )

    Spacer(Modifier.height(10.dp))

    actions()
  }
}

@Composable
private fun ReadyState(onStartRecognition: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val scale by
    infiniteTransition.animateFloat(
      initialValue = 0.97f,
      targetValue = 1.03f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1500, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        ),
      label = "scale"
    )

  RecognitionDisc(
    title = stringResource(R.string.tap_to_recognize),
    hint = stringResource(R.string.recognize_ready_hint),
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    onClick = onStartRecognition,
    discModifier = Modifier.scale(scale)
  ) {
    Icon(
      painter = painterResource(R.drawable.mic),
      contentDescription = stringResource(R.string.tap_to_recognize),
      modifier = Modifier.size(72.dp),
      tint = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun ListeningState(onCancel: () -> Unit) {
  RecognitionDisc(
    title = stringResource(R.string.listening),
    hint = stringResource(R.string.recognize_listening_hint),
    color = ListeningDiscColor,
    actions = {
      FilledTonalButton(
        onClick = onCancel,
        modifier = Modifier.height(50.dp).fillMaxWidth().padding(horizontal = 2.dp)
      ) {
        Text(stringResource(R.string.cancel), fontWeight = FontWeight.ExtraBold)
      }
    }
  ) {
    val bars = 5
    val animatables = remember { List(bars) { Animatable(0.2f) } }

    LaunchedEffect(Unit) {
      animatables.forEach { animatable ->
        launch {
          while (true) {
            animatable.animateTo(
              targetValue = kotlin.random.Random.nextFloat() * 0.8f + 0.2f,
              animationSpec =
                tween(
                  durationMillis = kotlin.random.Random.nextInt(300, 600),
                  easing = LinearEasing
                )
            )
          }
        }
      }
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.height(88.dp)
    ) {
      animatables.forEach { animatable ->
        Box(
          modifier =
            Modifier.width(12.dp)
              .fillMaxHeight(animatable.value)
              .clip(CircleShape)
              .background(Color.White)
        )
      }
    }
  }
}

@Composable
private fun ProcessingState() {
  RecognitionDisc(
    title = stringResource(R.string.processing),
    hint = stringResource(R.string.recognize_processing_hint),
    color = ProcessingDiscColor,
  ) {
    androidx.compose.material3.CircularProgressIndicator(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      color = Color.White,
      strokeWidth = 4.dp
    )
    Icon(
      painter = painterResource(R.drawable.music_note),
      contentDescription = null,
      modifier = Modifier.size(72.dp),
      tint = Color.White
    )
  }
}

@Composable
private fun SuccessState(
  result: RecognitionResult,
  onPlayOnApp: (RecognitionResult) -> Unit,
  onTryAgain: () -> Unit,
  onClose: () -> Unit,
  onOpenHistory: () -> Unit,
  onSaveToHistory: (RecognitionResult) -> Unit
) {
  LaunchedEffect(result) { onSaveToHistory(result) }

  val highResImageUrl =
    (result.coverArtHqUrl ?: result.coverArtUrl)?.replace("400x400", "1000x1000")

  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
          .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))
          .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 12.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.recognize_music),
          fontSize = 34.sp,
          lineHeight = 40.sp,
          letterSpacing = (-0.68).sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.IconButton(onClick = onClose) {
          Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = stringResource(R.string.close)
          )
        }
        androidx.compose.material3.IconButton(onClick = onOpenHistory) {
          Icon(
            painter = painterResource(R.drawable.history),
            contentDescription = stringResource(R.string.recognition_history)
          )
        }
      }

      Spacer(Modifier.height(16.dp))

      AsyncImage(
        model = highResImageUrl,
        contentDescription = null,
        modifier = Modifier.size(220.dp).clip(RoundedCornerShape(28.dp)),
        contentScale = ContentScale.Crop
      )

      Spacer(Modifier.height(16.dp))

      Text(
        text = result.title,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 22.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = listOfNotNull(result.artist, result.album).joinToString(" · "),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 22.dp)
      )

      Spacer(Modifier.height(12.dp))

      androidx.compose.material3.Button(
        onClick = { onPlayOnApp(result) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(50.dp)
      ) {
        Text(stringResource(R.string.play_on_app), fontWeight = FontWeight.ExtraBold)
      }

      Spacer(Modifier.height(8.dp))

      FilledTonalButton(
        onClick = onTryAgain,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(50.dp)
      ) {
        Text(stringResource(R.string.re_listen), fontWeight = FontWeight.ExtraBold)
      }

      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun RecognitionFailure(title: String, message: String, icon: Int, onTryAgain: () -> Unit) {
  RecognitionDisc(
    title = title,
    hint = message,
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    actions = {
      androidx.compose.material3.Button(
        onClick = onTryAgain,
        modifier = Modifier.height(50.dp).fillMaxWidth().padding(horizontal = 2.dp)
      ) {
        Text(stringResource(R.string.try_again), fontWeight = FontWeight.ExtraBold)
      }
    }
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      modifier = Modifier.size(72.dp),
      tint = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun NoMatchState(message: String, onTryAgain: () -> Unit) {
  RecognitionFailure(
    title = stringResource(R.string.no_match_found),
    message = message,
    icon = R.drawable.close,
    onTryAgain = onTryAgain
  )
}

@Composable
private fun ErrorState(message: String, onTryAgain: () -> Unit) {
  RecognitionFailure(
    title = stringResource(R.string.recognition_error),
    message = message,
    icon = R.drawable.error,
    onTryAgain = onTryAgain
  )
}
