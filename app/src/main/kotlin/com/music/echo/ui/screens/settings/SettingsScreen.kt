package echo.music.iad1tya.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.echomusic.updater.getUpdateAvailableState
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.StageSearchField
import echo.music.iad1tya.ui.component.StageTile
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.screens.Screens
import echo.music.iad1tya.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  val uriHandler = LocalUriHandler.current
  val context = LocalContext.current
  val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val isUpdateAvailable =
    getUpdateAvailableState(context) &&
      echo.music.iad1tya.echomusic.updater.getAutoUpdateCheckSetting(context)

  var searchQuery by rememberSaveable { mutableStateOf("") }
  val searchLower = searchQuery.lowercase()

  val accountText = stringResource(R.string.account)
  val appearanceText = stringResource(R.string.appearance)
  val playerText = stringResource(R.string.player_and_audio)
  val listenTogetherText = stringResource(R.string.listen_together)
  val contentText = stringResource(R.string.content)
  val aiLyricsText = stringResource(R.string.ai_lyrics_translation)
  val privacyText = stringResource(R.string.privacy)
  val storageText = stringResource(R.string.storage)
  val backupText = stringResource(R.string.backup_restore)
  val systemUpdateText = stringResource(R.string.system_update)
  val aboutText = stringResource(R.string.about)

  val accountDesc = stringResource(R.string.setting_desc_account)
  val appearanceDesc = stringResource(R.string.setting_desc_appearance)
  val playerDesc = stringResource(R.string.setting_desc_player)
  val listenTogetherDesc = stringResource(R.string.setting_desc_listen_together)
  val contentDesc = stringResource(R.string.setting_desc_content)
  val aiLyricsDesc = stringResource(R.string.setting_desc_ai)
  val privacyDesc = stringResource(R.string.setting_desc_privacy)
  val storageDesc = stringResource(R.string.setting_desc_storage)
  val backupDesc = stringResource(R.string.setting_desc_backup)
  val systemUpdateDesc = stringResource(R.string.setting_desc_update)
  val aboutDesc = stringResource(R.string.setting_desc_about)

  val scrollState = rememberScrollState()
  Column(
    Modifier.windowInsetsPadding(
      LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
    )
  ) {
    Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(start = 22.dp, top = 20.dp, end = 12.dp),
    ) {
      Text(
        text = stringResource(R.string.settings),
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.68).sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
      )
      // The boards leave this corner empty, but the page must keep its way back.
      IconButton(
        onClick = navController::navigateUp,
        onLongClick = navController::backToMain,
        modifier = Modifier.size(44.dp),
      ) {
        Icon(
          painterResource(R.drawable.arrow_back),
          contentDescription = stringResource(R.string.cd_back),
        )
      }
    }

    StageSearchField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = stringResource(R.string.stage_search_settings),
      modifier = Modifier.padding(start = 18.dp, top = 4.dp, end = 18.dp),
    )

    Column(
      Modifier.weight(1f)
        .verticalScroll(scrollState)
        .padding(start = 18.dp, top = 8.dp, end = 18.dp)
    ) {
    val itemsList = buildList {
      if (
        accountText.lowercase().contains(searchLower) ||
          accountDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == accountText),
            icon = painterResource(R.drawable.stage_account),
            iconBackground = StageTile.Rose,
            title = { Text(accountText) },
            onClick = { navController.navigate("settings/account") }
          )
        )
      }

      if ("listening summary".contains(searchLower)) {
        add(
          Material3SettingsItem(
            icon = painterResource(echo.music.iad1tya.R.drawable.stage_history),
            iconBackground = StageTile.Blue,
            title = { Text(stringResource(echo.music.iad1tya.R.string.listening_summary)) },
            onClick = { navController.navigate("settings/listening_summary") }
          )
        )
      }

      if (
        appearanceText.lowercase().contains(searchLower) ||
          appearanceDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == appearanceText),
            icon = painterResource(R.drawable.stage_palette),
            iconBackground = StageTile.Violet,
            title = { Text(appearanceText) },
            onClick = { navController.navigate("settings/appearance") }
          )
        )
      }
      if (
        playerText.lowercase().contains(searchLower) || playerDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == playerText),
            icon = painterResource(R.drawable.stage_play),
            iconBackground = StageTile.Blue,
            title = { Text(playerText) },
            onClick = { navController.navigate("settings/player") }
          )
        )
      }
      val musicFlowText = stringResource(R.string.music_flow)
      val musicFlowDesc = stringResource(R.string.music_flow_settings_description)
      if (
        musicFlowText.lowercase().contains(searchLower) ||
          musicFlowDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == musicFlowText),
            icon = painterResource(R.drawable.stage_flow),
            iconBackground = StageTile.Green,
            title = { Text(musicFlowText) },
            onClick = { navController.navigate("settings/music_flow") }
          )
        )
      }
      if (
        listenTogetherText.lowercase().contains(searchLower) ||
          listenTogetherDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == listenTogetherText),
            icon = painterResource(R.drawable.stage_together),
            iconBackground = StageTile.Green,
            title = { Text(listenTogetherText) },
            onClick = { navController.navigate(Screens.ListenTogether.route) }
          )
        )
      }
      if (
        contentText.lowercase().contains(searchLower) ||
          contentDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == contentText),
            icon = painterResource(R.drawable.stage_language),
            iconBackground = StageTile.Slate,
            title = { Text(contentText) },
            onClick = { navController.navigate("settings/content") }
          )
        )
      }



      if (
        privacyText.lowercase().contains(searchLower) ||
          privacyDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == privacyText),
            icon = painterResource(R.drawable.stage_security),
            iconBackground = StageTile.Slate,
            title = { Text(privacyText) },
            onClick = { navController.navigate("settings/privacy") }
          )
        )
      }
      if (
        storageText.lowercase().contains(searchLower) ||
          storageDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == storageText),
            icon = painterResource(R.drawable.stage_storage),
            iconBackground = StageTile.Slate,
            title = { Text(storageText) },
            onClick = { navController.navigate("settings/storage") }
          )
        )
      }


      if (
        backupText.lowercase().contains(searchLower) || backupDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == backupText),
            icon = painterResource(R.drawable.stage_restore),
            iconBackground = StageTile.Slate,
            title = { Text(backupText) },
            onClick = { navController.navigate("settings/backup_restore") }
          )
        )
      }
      if (
        systemUpdateText.lowercase().contains(searchLower) ||
          systemUpdateDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == systemUpdateText),
            icon =
              painterResource(
                if (isUpdateAvailable) R.drawable.ic_launcher_nobg else R.drawable.stage_update
              ),
            iconBackground = StageTile.Slate,
            title = { Text(systemUpdateText) },
            description =
              if (isUpdateAvailable) {
                {
                  Text(
                    text = stringResource(R.string.update_available),
                    color = MaterialTheme.colorScheme.error
                  )
                }
              } else {
                null
              },
            onClick = { navController.navigate("settings/update") }
          )
        )
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if ("supported links".contains(searchLower)) {
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "supported links"),
              icon = painterResource(R.drawable.stage_link),
            iconBackground = StageTile.Slate,
              title = { Text("Supported Links") },
              onClick = {
                try {
                  val intent =
                    Intent(
                      Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                      Uri.parse("package:${context.packageName}")
                    )
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  context.startActivity(intent)
                } catch (e: Exception) {
                  when (e) {
                    is ActivityNotFoundException,
                    is SecurityException -> {
                      Toast.makeText(context, "Cannot open settings", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                      Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                  }
                }
              }
            )
          )
        }
      }
      if (
        aboutText.lowercase().contains(searchLower) || aboutDesc.lowercase().contains(searchLower)
      ) {
        add(
          Material3SettingsItem(
            isHighlighted = (highlightKey == aboutText),
            icon = painterResource(R.drawable.stage_info),
            iconBackground = StageTile.Slate,
            title = { Text(aboutText) },
            onClick = { navController.navigate("settings/about") }
          )
        )
      }
    }

    val finalItemsList =
      if (searchQuery.isNotEmpty()) {
        val subSettings = getAllSearchableSettings()

        val matchedSubSettings =
          subSettings
            .filter {
              it.title.lowercase().contains(searchLower) ||
                (it.description?.lowercase()?.contains(searchLower) == true)
            }
            .map { setting ->
              Material3SettingsItem(
                icon = painterResource(R.drawable.search),
                iconBackground = StageTile.Slate,
                title = { Text(setting.title) },
                description = {
                  if (setting.description != null) {
                    Text("${setting.category} • ${setting.description}")
                  } else {
                    Text(setting.category)
                  }
                },
                onClick = {
                  val encodedTitle = Uri.encode(setting.title)
                  val finalRoute =
                    if (setting.route.contains("?")) "${setting.route}&highlightKey=$encodedTitle"
                    else "${setting.route}?highlightKey=$encodedTitle"
                  navController.navigate(finalRoute)
                }
              )
            }

        itemsList + matchedSubSettings
      } else {
        itemsList
      }

    if (searchQuery.isNotEmpty() && finalItemsList.isEmpty()) {
      Spacer(modifier = Modifier.height(32.dp))
      Text(
        text = "No settings found for \"$searchQuery\"",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
      )
    } else {
      Material3SettingsGroup(tight = true, scrollState = scrollState, items = finalItemsList)
    }

    Spacer(modifier = Modifier.height(24.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
      )
    )
    }
  }
}
