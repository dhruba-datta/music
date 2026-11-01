package echo.music.iad1tya.echomusic.updater

import echo.music.iad1tya.ui.component.StagePillButton
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.ChangelogItem
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.detachedItemShape
import echo.music.iad1tya.ui.component.endItemShape
import echo.music.iad1tya.ui.component.leadingItemShape
import echo.music.iad1tya.ui.component.middleItemShape
import echo.music.iad1tya.ui.utils.parseMarkdownToSections
import echo.music.iad1tya.ui.utils.parseSimpleMarkdown
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ChangelogSection(val title: String, val items: List<String>)

const val PREFS_NAME = "settings"
const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
const val KEY_BETA_UPDATES = "beta_updates"
const val KEY_UPDATE_AVAILABLE = "update_available"

fun getUpdateAvailableState(context: Context): Boolean {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  return sharedPrefs.getBoolean(KEY_UPDATE_AVAILABLE, false)
}

fun saveUpdateAvailableState(context: Context, available: Boolean) {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  sharedPrefs.edit().putBoolean(KEY_UPDATE_AVAILABLE, available).apply()
}

fun getAutoUpdateCheckSetting(context: Context): Boolean {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  return sharedPrefs.getBoolean(KEY_AUTO_UPDATE_CHECK, true)
}

const val KEY_UPDATE_NOTIFICATIONS = "update_notifications"

fun getUpdateNotificationsSetting(context: Context): Boolean {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  return sharedPrefs.getBoolean(KEY_UPDATE_NOTIFICATIONS, true)
}

fun getBetaUpdatesSetting(context: Context): Boolean {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  return sharedPrefs.getBoolean(KEY_BETA_UPDATES, false)
}

private fun formatGitHubDate(githubDate: String): String =
  try {
    val githubFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")
    val dateTime = LocalDateTime.parse(githubDate, githubFormatter)
    dateTime.format(displayFormatter)
  } catch (e: Exception) {
    githubDate
  }

fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
  val latestVersionClean = latestVersion.removePrefix("b").removePrefix("v")
  val currentVersionClean = currentVersion.removePrefix("b").removePrefix("v")

  val latestParts = latestVersionClean.split(".").map { it.toIntOrNull() ?: 0 }
  val currentParts = currentVersionClean.split(".").map { it.toIntOrNull() ?: 0 }

  for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
    val latest = latestParts.getOrElse(i) { 0 }
    val current = currentParts.getOrElse(i) { 0 }
    when {
      latest > current -> return true
      latest < current -> return false
    }
  }

  if (latestVersionClean == currentVersionClean) {
    val latestIsBeta = latestVersion.startsWith("b")
    val currentIsBeta = currentVersion.startsWith("b")

    if (currentIsBeta && !latestIsBeta) return true
  }

  return false
}

suspend fun checkForUpdate(
  context: Context,
  onSuccess:
    (
      tag: String,
      isAvailable: Boolean,
      changelog: List<ChangelogSection>,
      size: String,
      date: String,
      description: String?,
      imageUrl: String?,
      apkUrl: String?
    ) -> Unit,
  onError: () -> Unit,
) {
  withContext(Dispatchers.IO) {
    try {
      val url = URL("https://api.github.com/repos/EchoMusicApp/Echo-Music/releases/latest")
      val json = url.openStream().bufferedReader().use { it.readText() }
      val targetRelease = JSONObject(json)

      val currentVersion = BuildConfig.VERSION_NAME
      val targetTagName = targetRelease.getString("tag_name")
      val currentClean = currentVersion.removePrefix("b").removePrefix("v").trim()
      val targetClean = targetTagName.removePrefix("b").removePrefix("v").trim()
      val shouldShow = currentClean != targetClean

      if (shouldShow) {
        val tagWithPrefix = targetRelease.getString("tag_name")
        val displayTag = tagWithPrefix

        val changelogList = mutableListOf<ChangelogSection>()
        var description: String? = null
        var imageUrl: String? = null
        try {
          val changelogUrl =
            URL(
              "https://github.com/EchoMusicApp/Echo-Music/releases/download/$tagWithPrefix/changelog.json"
            )
          val changelogJson = changelogUrl.openStream().bufferedReader().use { it.readText() }
          val changelogData = JSONObject(changelogJson)

          description = changelogData.optString("description").takeIf { it.isNotEmpty() }
          imageUrl = changelogData.optString("image").takeIf { it.isNotEmpty() }

          val changelogArray = changelogData.getJSONArray("changelog")
          for (j in 0 until changelogArray.length()) {
            val sectionObj = changelogArray.getJSONObject(j)
            val title = sectionObj.getString("title")
            val itemsArray = sectionObj.getJSONArray("items")
            val itemsList = mutableListOf<String>()
            for (k in 0 until itemsArray.length()) {
              itemsList.add(itemsArray.getString(k))
            }
            changelogList.add(ChangelogSection(title, itemsList))
          }
        } catch (e: Exception) {
          var body =
            targetRelease.optString("body", context.getString(R.string.no_changelog_available))

          val imageRegex = Regex("!\\[(.*?)\\]\\((.*?)\\)")
          val match = imageRegex.find(body)
          if (match != null) {
            imageUrl = match.groupValues[2]
            body = body.replace(match.value, "").trim()
          }
          val (parsedDesc, parsedSections) = parseMarkdownToSections(body)
          description = parsedDesc ?: body.takeIf { parsedSections.isEmpty() }
          changelogList.addAll(parsedSections)
        }

        val publishedAt = targetRelease.getString("published_at")
        val formattedReleaseDate = formatGitHubDate(publishedAt)
        val assets = targetRelease.getJSONArray("assets")

        var apkSizeInMB = ""
        var apkDownloadUrl = ""
        for (j in 0 until assets.length()) {
          val asset = assets.getJSONObject(j)
          val assetName = asset.getString("name")
          if (
            assetName.endsWith(".apk", ignoreCase = true) &&
              !assetName.lowercase().contains("debug")
          ) {
            val apkSizeInBytes = asset.getLong("size")
            apkSizeInMB = String.format("%.1f", apkSizeInBytes / (1024.0 * 1024.0))
            apkDownloadUrl = asset.getString("browser_download_url")
            break
          }
        }

        if (apkDownloadUrl.isNotEmpty()) {
          withContext(Dispatchers.Main) {
            onSuccess(
              displayTag,
              true,
              changelogList,
              apkSizeInMB,
              formattedReleaseDate,
              description,
              imageUrl,
              apkDownloadUrl
            )
          }
          return@withContext
        }
      }

      withContext(Dispatchers.Main) {
        onSuccess(currentVersion, false, emptyList(), "", "", null, null, null)
      }
    } catch (e: Exception) {
      Log.e("UpdateCheck", "Error checking for updates: ${e.message}", e)
      withContext(Dispatchers.Main) { onError() }
    }
  }
}

const val KEY_LAST_SEEN_CHANGELOG_VERSION = "last_seen_changelog_version"

fun getLastSeenChangelogVersion(context: Context): String {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  return sharedPrefs.getString(KEY_LAST_SEEN_CHANGELOG_VERSION, "") ?: ""
}

fun saveLastSeenChangelogVersion(context: Context, version: String) {
  val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  sharedPrefs.edit().putString(KEY_LAST_SEEN_CHANGELOG_VERSION, version).apply()
}

data class WhatsNewInfo(
  val changelog: List<ChangelogSection>,
  val description: String?,
)

/**
 * URLConnection defaults both connect and read timeouts to 0 (infinite) — opening a stream without
 * setting them can hang this Dispatchers.IO call indefinitely on a stalled request.
 */
private fun openTimedStream(url: String): java.io.InputStream =
  (URL(url).openConnection() as java.net.URLConnection)
    .apply {
      connectTimeout = 15_000
      readTimeout = 15_000
    }
    .getInputStream()

/**
 * Looks up the release whose tag matches [currentVersion] (not necessarily the latest — the user
 * may be a version or two behind "latest" right after installing an update) and returns its
 * changelog, mirroring the parsing [checkForUpdate] does for the newest release.
 */
suspend fun fetchChangelogForVersion(currentVersion: String): WhatsNewInfo? =
  withContext(Dispatchers.IO) {
    try {
      val cleanCurrent = currentVersion.removePrefix("b").removePrefix("v").trim()
      val releasesJson =
        openTimedStream("https://api.github.com/repos/EchoMusicApp/Echo-Music/releases")
          .bufferedReader()
          .use { it.readText() }
      val releases = JSONArray(releasesJson)

      var matchedRelease: JSONObject? = null
      for (i in 0 until releases.length()) {
        val release = releases.getJSONObject(i)
        val tagClean = release.getString("tag_name").removePrefix("b").removePrefix("v").trim()
        if (tagClean == cleanCurrent) {
          matchedRelease = release
          break
        }
      }
      val release = matchedRelease ?: return@withContext null
      val tag = release.getString("tag_name")

      val changelogList = mutableListOf<ChangelogSection>()
      var description: String? = null
      try {
        val changelogJson =
          openTimedStream(
              "https://github.com/EchoMusicApp/Echo-Music/releases/download/$tag/changelog.json"
            )
            .bufferedReader()
            .use { it.readText() }
        val changelogData = JSONObject(changelogJson)
        description = changelogData.optString("description").takeIf { it.isNotEmpty() }
        val changelogArray = changelogData.getJSONArray("changelog")
        for (j in 0 until changelogArray.length()) {
          val sectionObj = changelogArray.getJSONObject(j)
          val title = sectionObj.getString("title")
          val itemsArray = sectionObj.getJSONArray("items")
          val itemsList = mutableListOf<String>()
          for (k in 0 until itemsArray.length()) {
            itemsList.add(itemsArray.getString(k))
          }
          changelogList.add(ChangelogSection(title, itemsList))
        }
      } catch (e: Exception) {
        var body = release.optString("body", "")
        val imageRegex = Regex("!\\[(.*?)\\]\\((.*?)\\)")
        imageRegex.find(body)?.let { match -> body = body.replace(match.value, "").trim() }
        val (parsedDesc, parsedSections) = parseMarkdownToSections(body)
        description = parsedDesc ?: body.takeIf { parsedSections.isEmpty() }
        changelogList.addAll(parsedSections)
      }

      if (changelogList.isEmpty() && description.isNullOrBlank()) return@withContext null
      WhatsNewInfo(changelogList, description)
    } catch (e: Exception) {
      Log.e("WhatsNew", "Failed to fetch changelog for version $currentVersion: ${e.message}", e)
      null
    }
  }

@Composable
fun WhatsNewDialog(
  version: String,
  info: WhatsNewInfo,
  onDismiss: () -> Unit,
) {
  DefaultDialog(
    onDismiss = onDismiss,
    title = { Text(stringResource(R.string.whats_new_title, version)) },
    buttons = {
        StagePillButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
      }
  ) {
    val (effectiveDescription, effectiveSections) =
      remember(info) {
        if (info.changelog.isNotEmpty()) {
          Pair(info.description?.takeIf { it.isNotBlank() }, info.changelog)
        } else if (!info.description.isNullOrBlank()) {
          parseMarkdownToSections(info.description)
        } else {
          Pair(null, emptyList())
        }
      }

    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
      effectiveDescription?.let { description ->
        item {
          Text(
            text = parseSimpleMarkdown(description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
          )
        }
      }
      effectiveSections.forEach { section ->
        if (section.title.isNotBlank()) {
          item {
            Text(
              text = section.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
          }
        }
        itemsIndexed(section.items) { index, changelogItem ->
          val shape =
            when {
              section.items.size == 1 -> detachedItemShape()
              index == 0 -> leadingItemShape()
              index == section.items.size - 1 -> endItemShape()
              else -> middleItemShape()
            }
          ChangelogItem(text = changelogItem, shape = shape)
          if (index != section.items.size - 1) {
            Spacer(modifier = Modifier.height(2.dp))
          }
        }
      }
    }
  }
}
