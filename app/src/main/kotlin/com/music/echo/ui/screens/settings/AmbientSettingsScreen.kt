package echo.music.iad1tya.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.*
import echo.music.iad1tya.ui.component.*
import echo.music.iad1tya.utils.rememberEnumPreference
import echo.music.iad1tya.utils.rememberPreference

@Composable
fun AmbientSettingsContent(modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()

  var showArt by rememberPreference(AmbientShowArtKey, true)
  var artScale by rememberPreference(AmbientArtScaleKey, AmbientArtScaleDefault)
  var showTitle by rememberPreference(AmbientShowTitleKey, false)
  var showArtist by rememberPreference(AmbientShowArtistKey, false)
  var showLyrics by rememberPreference(AmbientShowLyricsKey, true)
  var spacing by rememberPreference(AmbientSpacingKey, AmbientSpacingDefault)

  var lyricsTextSize by rememberPreference(LyricsTextSizeKey, AmbientLyricsTextSizeDefault)
  var lyricsLineSpacing by rememberPreference(LyricsLineSpacingKey, AmbientLyricsLineSpacingDefault)
  var lyricsAnimationStyle by
    rememberEnumPreference(LyricsAnimationStyleKey, LyricsAnimationStyle.echomusic_1)

  Column(modifier = modifier.fillMaxWidth().verticalScroll(scrollState)) {
    Column(modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)) {
      Material3SettingsGroup(
        scrollState = scrollState,
        items =
          listOf(
            Material3SettingsItem(
              title = { Text("Show Music Art") },
              trailingContent = {
                StageSwitch(checked = showArt, onCheckedChange = { showArt = it })
              },
              onClick = { showArt = !showArt }
            ),
            Material3SettingsItem(
              title = { Text("Show Song Name") },
              trailingContent = {
                StageSwitch(checked = showTitle, onCheckedChange = { showTitle = it })
              },
              onClick = { showTitle = !showTitle }
            ),
            Material3SettingsItem(
              title = { Text("Show Artist Name") },
              trailingContent = {
                StageSwitch(checked = showArtist, onCheckedChange = { showArtist = it })
              },
              onClick = { showArtist = !showArtist }
            ),
            Material3SettingsItem(
              title = { Text("Show Lyrics") },
              trailingContent = {
                StageSwitch(checked = showLyrics, onCheckedChange = { showLyrics = it })
              },
              onClick = { showLyrics = !showLyrics }
            )
          )
      )
    }

    if (showArt) {
      Column(modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)) {
        StageSliderRow(
          title = "Music Art Size",
          valueText = "${(artScale * 100).toInt()}%",
          value = artScale,
          onValueChange = { artScale = it },
          valueRange = 0.3f..1.0f,
        )
      }
    }

    if (showLyrics) {
      Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        StageSettingsHeading(text = "Lyrics Customization")
      }
    }

    Column(modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)) {
      if (showLyrics) {
        StageSliderRow(
          title = "Lyrics Font Size",
          valueText = "${lyricsTextSize.toInt()} sp",
          value = lyricsTextSize,
          onValueChange = { lyricsTextSize = it },
          valueRange = 12f..48f,
        )
        StageSliderRow(
          title = "Lyrics Line Spacing",
          valueText = String.format("%.1f", lyricsLineSpacing),
          value = lyricsLineSpacing,
          onValueChange = { lyricsLineSpacing = it },
          valueRange = 0.5f..2.5f,
        )
      }
      StageSliderRow(
        title = "Spacing Between Art and Lyrics",
        valueText = "${spacing.toInt()} dp",
        value = spacing,
        onValueChange = { spacing = it },
        valueRange = 0f..100f,
      )
    }

    if (showLyrics) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
          Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp))
            .padding(14.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Lyrics Animation Style",
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text =
              when (lyricsAnimationStyle) {
                LyricsAnimationStyle.NONE -> stringResource(R.string.none)
                LyricsAnimationStyle.APPLE -> stringResource(R.string.apple_music_style)
                LyricsAnimationStyle.APPLE_V2 -> stringResource(R.string.apple_music_style_letter)
                LyricsAnimationStyle.echomusic_1 -> stringResource(R.string.echomusic_1)
                LyricsAnimationStyle.LYRICS_V2 -> stringResource(R.string.lyrics_v2_fluid)
                LyricsAnimationStyle.METRO_LYRICS -> stringResource(R.string.lyrics_animation_metro)
                else -> lyricsAnimationStyle.name
              },
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Button(
          onClick = {
            val values = LyricsAnimationStyle.values()
            lyricsAnimationStyle = values[(lyricsAnimationStyle.ordinal + 1) % values.size]
          },
          modifier = Modifier.heightIn(min = 44.dp),
          contentPadding = PaddingValues(horizontal = 18.dp),
          shape = RoundedCornerShape(25.dp),
        ) {
          Text("Next", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientSettingsScreen(navController: NavController) {
  Scaffold(
    topBar = {
      StageTopBar(
        title = { Text("Ambient Mode Settings") },
        navigationIcon = {
          IconButton(onClick = navController::navigateUp) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
      )
    }
  ) { innerPadding ->
    AmbientSettingsContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
  }
}
