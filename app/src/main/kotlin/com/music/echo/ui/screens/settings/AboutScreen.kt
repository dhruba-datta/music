@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package echo.music.iad1tya.ui.screens.settings

import echo.music.iad1tya.ui.component.StageTopBar
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.utils.backToMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  onBack: (() -> Unit)? = null,
  highlightKey: String? = null
) {
  val uriHandler = LocalUriHandler.current

  data class Contributor(val login: String, val avatarUrl: String, val htmlUrl: String)
  var contributors by remember { mutableStateOf<List<Contributor>>(emptyList()) }

  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      try {
        val url = java.net.URL("https://api.github.com/repos/EchoMusicApp/Echo-Music/contributors")
        val json = url.openStream().bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val list = mutableListOf<Contributor>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(
            Contributor(
              obj.getString("login"),
              obj.getString("avatar_url"),
              obj.getString("html_url")
            )
          )
        }
        contributors = list
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
  val context = LocalContext.current

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      StageTopBar(
        title = {
          Text(
            text = stringResource(R.string.about),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = { onBack?.invoke() ?: navController.navigateUp() },
            onLongClick = navController::backToMain,
          ) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        windowInsets = TopAppBarDefaults.windowInsets
)
    },
  ) { innerPadding ->
    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(
              WindowInsetsSides.Horizontal,
            ),
          ),
      contentPadding =
        PaddingValues(
          start = 18.dp,
          top = innerPadding.calculateTopPadding(),
          end = 18.dp,
          bottom =
            androidx.compose.foundation.layout.WindowInsets.systemBars
              .asPaddingValues()
              .calculateBottomPadding() + 32.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item { AboutAppCard() }

      item {
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
          if (contributors.isNotEmpty()) {
            AboutExpandableRow(title = "Contributors") {
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                contributors.forEach { contributor ->
                  coil3.compose.AsyncImage(
                    model = contributor.avatarUrl,
                    contentDescription = contributor.login,
                    modifier =
                      Modifier.size(48.dp).clip(CircleShape).clickable {
                        uriHandler.openUri(contributor.htmlUrl)
                      },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
                }
              }
            }
          }

          AboutExpandableRow(title = "Developer", subtitle = "Website, Instagram, X (Twitter)") {
            AboutLinkRow(painterResource(R.drawable.website), "Website", "iad1tya.cyou") {
              uriHandler.openUri("https://iad1tya.cyou")
            }
            AboutLinkRow(painterResource(R.drawable.ic_instagram_new), "Instagram", "@iad1tya") {
              uriHandler.openUri("https://instagram.com/iad1tya")
            }
            AboutLinkRow(painterResource(R.drawable.ic_x_new), "X (Twitter)", "@xad1tya") {
              uriHandler.openUri("https://x.com/xad1tya")
            }
          }

          AboutExpandableRow(title = "Support", subtitle = "Buy Me a Coffee, Patreon, UPI") {
            AboutLinkRow(painterResource(R.drawable.coffee), "Buy Me a Coffee", "buymeacoffee.com/iad1tya") {
              uriHandler.openUri("https://buymeacoffee.com/iad1tya")
            }
            AboutLinkRow(painterResource(R.drawable.ic_patreon_new), "Patreon", "patreon.com/cw/iad1tya") {
              uriHandler.openUri("https://www.patreon.com/cw/iad1tya")
            }
            AboutLinkRow(painterResource(R.drawable.upi_new), "UPI", "iad1tya@upi") {
              uriHandler.openUri(
                "https://intradeus.github.io/http-protocol-redirector/?r=upi://pay?pa=iad1tya@upi&pn=Aditya%20Yadav&am=&tn=Thank%20You%20so%20much%20for%20this%20support"
              )
            }
          }

          AboutExpandableRow(title = "Community", subtitle = "Discord") {
            AboutLinkRow(painterResource(R.drawable.ic_discord_new), "Discord", "discord.gg/Xt5hgsJJuA") {
              uriHandler.openUri("https://discord.gg/Xt5hgsJJuA")
            }
          }

          AboutExpandableRow(title = "App", subtitle = "GitHub, Telegram") {
            AboutLinkRow(painterResource(R.drawable.github), "GitHub", "EchoMusicApp/Echo-Music") {
              uriHandler.openUri("https://github.com/EchoMusicApp/Echo-Music")
            }
            AboutLinkRow(painterResource(R.drawable.ic_telegram_new), "Telegram", "t.me/EchoMusicApp") {
              uriHandler.openUri("https://t.me/EchoMusicApp")
            }
          }
        }
      }

      /* item {
          AboutSectionCard(title = "App") {
              AboutActionRow(
                  icon = painterResource(R.drawable.github),
                  title = "GitHub",
                  subtitle = "EchoMusicApp/Echo-Music",
                  onClick = { uriHandler.openUri("https://github.com/EchoMusicApp/Echo-Music") },
              )
              AboutDivider()
              AboutActionRow(
                  icon = painterResource(R.drawable.ic_discord_new),
                  title = "Discord",
                  subtitle = "discord.gg/EcfV3AxH5c",
                  onClick = { uriHandler.openUri("https://discord.gg/EcfV3AxH5c") },
              )
              AboutDivider()
              AboutActionRow(
                  icon = painterResource(R.drawable.ic_telegram_new),
                  title = "Telegram",
                  subtitle = "t.me/EchoMusicApp",
                  onClick = { uriHandler.openUri("https://t.me/EchoMusicApp") },
              )
          }
      } */

    }
  }
}

/** A row that opens to show the links it stands for. */
@Composable
private fun AboutExpandableRow(
  title: String,
  subtitle: String? = null,
  content: @Composable () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clickable { expanded = !expanded }
        .heightIn(min = 54.dp)
        .padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
      Text(
        text = title,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      subtitle?.let {
        Text(
          text = it,
          fontSize = 13.sp,
          lineHeight = 17.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Icon(
      painter = painterResource(if (expanded) R.drawable.expand_less else R.drawable.navigate_next),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(18.dp),
    )
  }
  AnimatedVisibility(visible = expanded) { Column { content() } }
}

@Composable
private fun AboutLinkRow(
  icon: Painter,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .heightIn(min = 54.dp)
        .padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    Icon(
      painter = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(22.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun AboutAppCard() {
  Column(
    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 18.dp, end = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    var isEasterEggActive by remember { mutableStateOf(false) }
    val rotation by
      animateFloatAsState(
        targetValue = if (isEasterEggActive) 180f else 0f,
        animationSpec =
          spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "flip"
      )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
      animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
          ),
        label = "scale"
      )

    Box(
      modifier =
        Modifier.size(84.dp)
          .graphicsLayer {
            rotationY = rotation
            scaleX = scale
            scaleY = scale
            cameraDistance = 12f * density
          }
          .clip(CircleShape)
          .background(Color.White)
          .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { isEasterEggActive = !isEasterEggActive }
          ),
      contentAlignment = Alignment.Center
    ) {
      if (rotation <= 90f) {
        Image(
          painter = painterResource(R.drawable.ic_launcher_nobg),
          contentDescription = null,
          colorFilter = ColorFilter.tint(Color(0xFF111111)),
          modifier = Modifier.fillMaxSize(0.62f)
        )
      } else {
        coil3.compose.AsyncImage(
          model = "https://avatars.githubusercontent.com/u/147871321?v=4",
          contentDescription = null,
          modifier =
            Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }, // Un-flip the backside image
          contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    Text(
      text = if (rotation <= 90f) "Music" else "Developed by Aditya",
      fontSize = 23.sp,
      lineHeight = 29.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(6.dp))
    Text(
      text =
        buildString {
          append("Version ${BuildConfig.VERSION_NAME}")
          if (BuildConfig.DEBUG) append(" · DEBUG")
        },
      fontSize = 15.sp,
      lineHeight = 21.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
  }
}
