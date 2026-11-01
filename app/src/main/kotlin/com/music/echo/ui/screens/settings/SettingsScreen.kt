package echo.music.iad1tya.ui.screens.settings

import android.net.Uri
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
import androidx.compose.runtime.remember
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
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.R
import echo.music.iad1tya.echomusic.updater.SelfUpdateDialog
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.echomusic.updater.getUpdateAvailableState
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.StageSearchField
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
  val isUpdateAvailable =
    getUpdateAvailableState(context) &&
      echo.music.iad1tya.echomusic.updater.getAutoUpdateCheckSetting(context)

  var showUpdateDialog by remember { mutableStateOf(false) }
  if (showUpdateDialog) {
    SelfUpdateDialog(onDismiss = { showUpdateDialog = false })
  }

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

  val accountDesc = stringResource(R.string.setting_desc_account)
  val appearanceDesc = stringResource(R.string.setting_desc_appearance)
  val playerDesc = stringResource(R.string.setting_desc_player)
  val listenTogetherDesc = stringResource(R.string.setting_desc_listen_together)
  val contentDesc = stringResource(R.string.setting_desc_content)
  val aiLyricsDesc = stringResource(R.string.setting_desc_ai)
  val privacyDesc = stringResource(R.string.setting_desc_privacy)
  val storageDesc = stringResource(R.string.setting_desc_storage)
  val backupDesc = stringResource(R.string.setting_desc_backup)

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
            title = { Text(accountText) },
            description = { Text(accountDesc) },
            onClick = { navController.navigate("settings/account") }
          )
        )
      }

      if ("listening summary".contains(searchLower)) {
        add(
          Material3SettingsItem(
            icon = painterResource(echo.music.iad1tya.R.drawable.stage_history),
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
            title = { Text(appearanceText) },
            description = { Text(appearanceDesc) },
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
            title = { Text(playerText) },
            description = { Text(playerDesc) },
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
            title = { Text(musicFlowText) },
            description = { Text(musicFlowDesc) },
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
            title = { Text(listenTogetherText) },
            description = { Text(listenTogetherDesc) },
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
            title = { Text(contentText) },
            description = { Text(contentDesc) },
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
            title = { Text(privacyText) },
            description = { Text(privacyDesc) },
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
            title = { Text(storageText) },
            description = { Text(storageDesc) },
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
            title = { Text(backupText) },
            description = { Text(backupDesc) },
            onClick = { navController.navigate("settings/backup_restore") }
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
      Spacer(modifier = Modifier.height(12.dp))
      if (searchQuery.isNotEmpty()) {
        Material3SettingsGroup(scrollState = scrollState, items = finalItemsList)
      } else {
        // Account, Listening summary, then the look, data and system groups.
        listOf(
            itemsList.take(1),
            itemsList.drop(1).take(1),
            itemsList.drop(2).take(4),
            itemsList.drop(6).take(4),
          )
          .forEach { group ->
            Material3SettingsGroup(scrollState = scrollState, items = group)
            Spacer(modifier = Modifier.height(16.dp))
          }

        StageButton(
          text = stringResource(R.string.stage_check_updates),
          onClick = { showUpdateDialog = true },
          modifier = Modifier.padding(horizontal = 18.dp),
        )
        Text(
          text =
            if (isUpdateAvailable) stringResource(R.string.stage_update_available)
            else "Version ${BuildConfig.VERSION_NAME}",
          style = MaterialTheme.typography.bodyMedium,
          color =
            if (isUpdateAvailable) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
      }
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
