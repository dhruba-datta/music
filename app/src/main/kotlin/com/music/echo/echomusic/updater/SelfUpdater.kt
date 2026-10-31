package echo.music.iad1tya.echomusic.updater

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.work.WorkManager
import echo.music.iad1tya.BuildConfig
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

/**
 * Finds, downloads and installs updates from this mod's own GitHub releases. Nothing happens
 * without the user: [SelfUpdateDialog] drives each step.
 */
object SelfUpdater {
  private const val TAG = "SelfUpdater"
  private const val PREFS = "self_update"
  private const val KEY_LATER_VERSION = "later_version"
  private const val KEY_LATER_AT = "later_at"
  private val SNOOZE_MILLIS = TimeUnit.HOURS.toMillis(1)

  data class Release(
    val tag: String,
    val apkUrl: String,
    val size: Long,
    val sha256: String?,
    val notes: String,
  )

  sealed interface InstallEvent {
    data class NeedsConfirmation(val intent: Intent) : InstallEvent

    data class Failed(val message: String) : InstallEvent
  }

  val installEvents = MutableStateFlow<InstallEvent?>(null)

  /** Older versions updated from background work, which would otherwise keep running. */
  fun cancelBackgroundWork(context: Context) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork("self_update_periodic")
    workManager.cancelUniqueWork("self_update_startup")
  }

  /** The newest release if it is newer than this build. Throws when GitHub can't be reached. */
  fun findUpdate(context: Context): Release? {
    val release = fetchLatestRelease()
    val isAvailable = release != null && isNewerVersion(release.tag, BuildConfig.VERSION_NAME)
    saveUpdateAvailableState(context, isAvailable)
    return release.takeIf { isAvailable }
  }

  /** Like [findUpdate], for the pop-up at app start: stays quiet offline or after "Later". */
  fun findOfferedUpdate(context: Context): Release? {
    val release =
      try {
        findUpdate(context)
      } catch (e: IOException) {
        Log.w(TAG, "Startup check failed: ${e.message}")
        null
      } ?: return null
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val snoozed =
      prefs.getString(KEY_LATER_VERSION, null) == release.tag &&
        System.currentTimeMillis() - prefs.getLong(KEY_LATER_AT, 0) < SNOOZE_MILLIS
    return release.takeUnless { snoozed }
  }

  fun snooze(context: Context, release: Release) {
    context
      .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_LATER_VERSION, release.tag)
      .putLong(KEY_LATER_AT, System.currentTimeMillis())
      .apply()
  }

  private fun fetchLatestRelease(): Release? {
    val connection =
      URL("https://api.github.com/repos/${BuildConfig.UPDATE_REPOSITORY}/releases/latest")
        .openConnection() as HttpURLConnection
    connection.connectTimeout = 15_000
    connection.readTimeout = 15_000
    connection.setRequestProperty("Accept", "application/vnd.github+json")
    try {
      when (connection.responseCode) {
        HttpURLConnection.HTTP_OK -> Unit
        HttpURLConnection.HTTP_NOT_FOUND -> return null
        else -> throw IOException("HTTP ${connection.responseCode}")
      }
      val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
      val assets = release.getJSONArray("assets")
      for (index in 0 until assets.length()) {
        val asset = assets.getJSONObject(index)
        if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
          return Release(
            tag = release.getString("tag_name"),
            apkUrl = asset.getString("browser_download_url"),
            size = asset.getLong("size"),
            sha256 = asset.optString("digest").removePrefix("sha256:").ifEmpty { null },
            notes = release.optString("body"),
          )
        }
      }
      return null
    } finally {
      connection.disconnect()
    }
  }

  fun download(
    context: Context,
    release: Release,
    onProgress: (received: Long, total: Long) -> Unit,
  ): File {
    val directory = File(context.cacheDir, "self_update")
    directory.deleteRecursively()
    directory.mkdirs()
    val target = File(directory, "${release.tag}.apk")
    val digest = MessageDigest.getInstance("SHA-256")
    val connection = URL(release.apkUrl).openConnection()
    connection.connectTimeout = 15_000
    connection.readTimeout = 30_000
    val total = connection.contentLengthLong.takeIf { it > 0 } ?: release.size
    var received = 0L
    var reported = -1L
    connection.getInputStream().use { input ->
      target.outputStream().use { output ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
          val read = input.read(buffer)
          if (read < 0) break
          digest.update(buffer, 0, read)
          output.write(buffer, 0, read)
          received += read
          val percent = received * 100 / total
          if (percent != reported) {
            reported = percent
            onProgress(received, total)
          }
        }
      }
    }
    val actual = digest.digest().joinToString("") { "%02x".format(it) }
    if (release.sha256 != null && !actual.equals(release.sha256, ignoreCase = true)) {
      target.delete()
      throw IOException("the file was corrupted")
    }
    return target
  }

  fun install(context: Context, apk: File) {
    val installer = context.packageManager.packageInstaller
    val params =
      PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
        setAppPackageName(context.packageName)
        setSize(apk.length())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
      }
    val sessionId = installer.createSession(params)
    installer.openSession(sessionId).use { session ->
      apk.inputStream().use { input ->
        session.openWrite("update.apk", 0, apk.length()).use { output ->
          input.copyTo(output)
          session.fsync(output)
        }
      }
      val status =
        PendingIntent.getBroadcast(
          context,
          sessionId,
          Intent(context, SelfUpdateResultReceiver::class.java).setPackage(context.packageName),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
      session.commit(status.intentSender)
    }
  }
}

class SelfUpdateResultReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
      PackageInstaller.STATUS_PENDING_USER_ACTION -> {
        // Some Android versions and phone brands still insist on a confirmation tap.
        @Suppress("DEPRECATION")
        val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
        SelfUpdater.installEvents.value =
          SelfUpdater.InstallEvent.NeedsConfirmation(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
      }
      PackageInstaller.STATUS_SUCCESS -> Unit
      else -> {
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "unknown error"
        Log.e("SelfUpdater", "Install failed: $message")
        SelfUpdater.installEvents.value = SelfUpdater.InstallEvent.Failed(message)
      }
    }
  }
}
