@file:OptIn(ExperimentalMaterial3Api::class)

package echo.music.iad1tya.ui.screens.settings

import echo.music.iad1tya.ui.component.StageSettingsHeading
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import echo.music.iad1tya.ui.component.StageTopBar
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppIconTypeKey
import echo.music.iad1tya.utils.AppIconType
import echo.music.iad1tya.utils.IconUtils
import echo.music.iad1tya.utils.rememberEnumPreference
import kotlinx.coroutines.launch

@Composable
fun AppIconSettingsScreen(
  navController: NavController,
  activity: Activity,
  snackbarHostState: SnackbarHostState
) {
  val (appIconType, onAppIconTypeChange) =
    rememberEnumPreference(AppIconTypeKey, defaultValue = AppIconType.DEFAULT)
  val coroutineScope = rememberCoroutineScope()

  fun handleIconChange(iconType: AppIconType) {
    if (appIconType == iconType) return
    onAppIconTypeChange(iconType)
    IconUtils.setIcon(activity, iconType)
    coroutineScope.launch {
      val result =
        snackbarHostState.showSnackbar(
          message = "Icon updated, restart to apply",
          actionLabel = "Restart"
        )
      if (result == SnackbarResult.ActionPerformed) {
        val packageManager = activity.packageManager
        val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
        if (intent != null) {
          val componentName = intent.component
          val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
          activity.startActivity(mainIntent)
          Runtime.getRuntime().exit(0)
        }
      }
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surface,
    topBar = {
      StageTopBar(
        title = { Text("App Icon") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painter = painterResource(R.drawable.arrow_back), contentDescription = "Back")
          }
        }
)
    }
  ) { innerPadding ->
    val icons =
      listOf(
        AppIconOption(AppIconType.CRAFT, "Craft", "Craft icon by CR3TO_", R.mipmap.ic_launcher_craft),
        AppIconOption(AppIconType.SADIE, "Sadie Sink", "Sadie Sink on community demand", R.mipmap.ic_launcher_sadie),
        AppIconOption(
          AppIconType.BILLIE_EILISH,
          "Billie Eilish",
          "billie eilish suggested by Lalo salamanca",
          R.mipmap.billie_eilish_icon
        ),
        AppIconOption(AppIconType.CAT, "Cat Icon", "A Pinkish cat-themed icon", R.mipmap.cat_icon),
        AppIconOption(
          AppIconType.CRAZY_BLUE,
          "Crazy Blue Icon",
          "A vibrant crazy blue icon",
          R.mipmap.crazy_blue_icon
        ),
        AppIconOption(
          AppIconType.ECHO_CAT,
          "Echo Cat",
          "A playful cat by Alarp_Svc",
          R.mipmap.echo_cat_icon
        ),
        AppIconOption(
          AppIconType.LEGACY,
          "Legacy Icon",
          "The OG Monochrome Icon",
          R.mipmap.legacy_icon
        ),
        AppIconOption(
          AppIconType.DEFAULT,
          "New Icon",
          "The standard vibrant icon",
          R.mipmap.ic_launcher
        ),
        AppIconOption(AppIconType.POOKIE, "Pookie Icon", "A Cute Pink icon", R.mipmap.pookie_icon),
        AppIconOption(
          AppIconType.SABRINA_CARPENTER,
          "Sabrina Carpenter",
          "Suggested by notdieinganymore",
          R.mipmap.ic_launcher_sabrina
        ),
        AppIconOption(
          AppIconType.SABRINA_CARPENTER_2,
          "Sabrina Carpenter 2",
          "Suggested by Falvik",
          R.mipmap.ic_launcher_sabrina2
        ),
        AppIconOption(AppIconType.SKY, "Sky Icon", "A beautiful sky-themed icon", R.mipmap.sky_icon)
      ).sortedBy { it.title.lowercase() }

    Column(
      modifier =
        Modifier.fillMaxSize()
          .background(MaterialTheme.colorScheme.surface)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
          )
          .padding(innerPadding)
          .padding(horizontal = 18.dp)
    ) {
      StageSettingsHeading(text = "Choose your launcher icon")

      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        icons.chunked(3).forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            row.forEach { option ->
              AppIconTile(
                option = option,
                isSelected = (appIconType == option.type),
                onClick = { handleIconChange(option.type) },
                modifier = Modifier.weight(1f),
              )
            }
            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
          }
        }
      }

      Spacer(
        modifier =
          Modifier.windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
          )
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

data class AppIconOption(
  val type: AppIconType,
  val title: String,
  val description: String,
  val iconRes: Int
)

/** A 72dp launcher icon over its name; the current icon has a white ring. */
@Composable
fun AppIconTile(
  option: AppIconOption,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val bitmap =
    remember(option.iconRes) {
      val drawable = ContextCompat.getDrawable(context, option.iconRes)
      drawable?.toBitmap(width = 192, height = 192)?.asImageBitmap()
    }
  val shape = RoundedCornerShape(20.dp)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier =
      modifier
        .semantics(mergeDescendants = true) { contentDescription = option.description }
        .clickable(onClick = onClick),
  ) {
    Box(
      modifier =
        Modifier.size(78.dp)
          .then(
            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(23.dp))
            else Modifier
          )
          .padding(3.dp),
      contentAlignment = Alignment.Center,
    ) {
      if (bitmap != null) {
        Image(
          bitmap = bitmap,
          contentDescription = null,
          modifier = Modifier.size(72.dp).clip(shape),
        )
      }
    }
    Text(
      text = option.title,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 2,
    )
  }
}
