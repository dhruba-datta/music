/** vivimusic Project (C) 2026 Licensed under GPL-3.0 | See git history for contributors */
package com.music.echo.ui.screens.settings

import echo.music.iad1tya.ui.component.StageTopBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.echo.ui.components.AnimatedRadioButton
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppFont
import echo.music.iad1tya.constants.CustomFontPathKey
import echo.music.iad1tya.constants.SelectedFontKey
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.theme.GoogleSansFontFamily
import echo.music.iad1tya.ui.theme.OutfitFontFamily
import echo.music.iad1tya.ui.theme.PlusJakartaSansFontFamily
import echo.music.iad1tya.ui.theme.SansFlexFontFamily
import echo.music.iad1tya.utils.launchSafely
import echo.music.iad1tya.utils.rememberPreference
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSelectionScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
) {
  val selectedFontState = rememberPreference(SelectedFontKey, defaultValue = AppFont.MANROPE.value)
  val selectedFont = selectedFontState.value
  val onSelectedFontChange: (String) -> Unit = { selectedFontState.value = it }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val customFontPathState = rememberPreference(CustomFontPathKey, defaultValue = "")
  val customFontPath = customFontPathState.value
  val onCustomFontPathChange: (String) -> Unit = { customFontPathState.value = it }

  val fontPickerLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        coroutineScope.launch {
          withContext(Dispatchers.IO) {
            try {
              val inputStream = context.contentResolver.openInputStream(it)
              val file = File(context.filesDir, "custom_font.ttf")
              val outputStream = FileOutputStream(file)
              inputStream?.copyTo(outputStream)
              inputStream?.close()
              outputStream.close()

              onCustomFontPathChange(file.absolutePath)
              onSelectedFontChange(AppFont.CUSTOM.value)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }
        }
      }
    }

  val activeFontFamily =
    remember(selectedFont, customFontPath) {
      when (AppFont.fromValue(selectedFont)) {
        AppFont.SYSTEM -> FontFamily.Default
        AppFont.MANROPE -> echo.music.iad1tya.ui.theme.ManropeFontFamily
        AppFont.GOOGLE_SANS -> GoogleSansFontFamily
        AppFont.SANS_FLEX -> SansFlexFontFamily
        AppFont.OUTFIT -> OutfitFontFamily
        AppFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
        AppFont.CUSTOM -> {
          try {
            if (customFontPath.isNotEmpty() && File(customFontPath).exists()) {
              val typeface = android.graphics.Typeface.createFromFile(customFontPath)
              FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
            } else {
              FontFamily.Default
            }
          } catch (e: Exception) {
            FontFamily.Default
          }
        }
      }
    }

  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(
          androidx.compose.foundation.layout.WindowInsetsSides.Horizontal
        )
      )
      .fillMaxSize()
      .padding(horizontal = 18.dp)
  ) {
    Spacer(
      Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(
          androidx.compose.foundation.layout.WindowInsetsSides.Top
        )
      )
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Typography Preview Card
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            MaterialTheme.colorScheme.surfaceContainer,
            androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
          )
          .padding(14.dp)
    ) {
      Text(
        text = stringResource(R.string.typography_preview),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = stringResource(R.string.preview_text_quote),
        fontFamily = activeFontFamily,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      // Options settings group
      Material3SettingsGroup(
        items =
          listOf(
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.SYSTEM.value) },
              showChevron = false,
              title = {
                Text(text = stringResource(R.string.font_system), fontFamily = FontFamily.Default)
              },
              onClick = { onSelectedFontChange(AppFont.SYSTEM.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.GOOGLE_SANS.value) },
              showChevron = false,
              title = {
                Text(
                  text = stringResource(R.string.font_google_sans),
                  fontFamily = GoogleSansFontFamily
                )
              },
              onClick = { onSelectedFontChange(AppFont.GOOGLE_SANS.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.SANS_FLEX.value) },
              showChevron = false,
              title = {
                Text(
                  text = stringResource(R.string.font_sans_flex),
                  fontFamily = SansFlexFontFamily
                )
              },
              onClick = { onSelectedFontChange(AppFont.SANS_FLEX.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.MANROPE.value) },
              showChevron = false,
              title = {
                Text(text = "Manrope", fontFamily = echo.music.iad1tya.ui.theme.ManropeFontFamily)
              },
              description = {
                Text(text = "Stage default", fontFamily = echo.music.iad1tya.ui.theme.ManropeFontFamily)
              },
              onClick = { onSelectedFontChange(AppFont.MANROPE.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.OUTFIT.value) },
              showChevron = false,
              title = {
                Text(text = stringResource(R.string.font_outfit), fontFamily = OutfitFontFamily)
              },
              onClick = { onSelectedFontChange(AppFont.OUTFIT.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.PLUS_JAKARTA_SANS.value) },
              showChevron = false,
              title = {
                Text(
                  text = stringResource(R.string.font_plus_jakarta_sans),
                  fontFamily = PlusJakartaSansFontFamily
                )
              },
              onClick = { onSelectedFontChange(AppFont.PLUS_JAKARTA_SANS.value) }
            ),
            Material3SettingsItem(
              trailingContent = { FontCheck(selectedFont == AppFont.CUSTOM.value) },
              showChevron = false,
              title = {
                Text(text = stringResource(R.string.font_custom), fontFamily = FontFamily.Default)
              },
              onClick = {
                if (selectedFont != AppFont.CUSTOM.value) {
                  if (customFontPath.isNotEmpty() && File(customFontPath).exists()) {
                    onSelectedFontChange(AppFont.CUSTOM.value)
                  } else {
                    fontPickerLauncher.launchSafely("*/*", context)
                  }
                } else {
                  fontPickerLauncher.launchSafely("*/*", context)
                }
              }
            ),
            Material3SettingsItem(
              title = { Text(stringResource(R.string.import_custom_font)) },
              onClick = { fontPickerLauncher.launchSafely("*/*", context) }
            )
          )
      )
      Spacer(modifier = Modifier.height(36.dp))
      Spacer(
        Modifier.windowInsetsPadding(
          LocalPlayerAwareWindowInsets.current.only(
            androidx.compose.foundation.layout.WindowInsetsSides.Bottom
          )
        )
      )
    }
  }

  StageTopBar(
    title = { Text(stringResource(R.string.app_font)) },
    navigationIcon = {
      IconButton(
        onClick = navController::navigateUp,
        onLongClick = { navController.popBackStack() },
      ) {
        Icon(
          painter = painterResource(R.drawable.arrow_back),
          contentDescription = null,
        )
      }
    }
)
}

/** The check mark at the end of the chosen font's row. */
@Composable
private fun FontCheck(selected: Boolean) {
  if (selected) {
    Icon(
      painter = painterResource(R.drawable.check),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(24.dp),
    )
  }
}
