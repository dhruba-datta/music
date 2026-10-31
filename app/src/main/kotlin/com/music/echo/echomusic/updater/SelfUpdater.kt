package echo.music.iad1tya.echomusic.updater

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Keeps the app current from this mod's own GitHub releases: no website, no second app. Updates are
 * only fetched on an unmetered network and only installed while nothing is playing, because
 * installing restarts the app.
 */
object SelfUpdater {
  private const val TAG = "SelfUpdater"
  private const val PERIODIC_WORK = "self_update_periodic"
  private const val STARTUP_WORK = "self_update_startup"
  private const val CHANNEL_ID = "updates"
  private const val NOTIFICATION_ID = 1002
  private const val CHECK_INTERVAL_HOURS = 6L

  private val constraints =
    Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()

  fun schedule(context: Context) {
    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniquePeriodicWork(
      PERIODIC_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      PeriodicWorkRequestBuilder<SelfUpdateWorker>(CHECK_INTERVAL_HOURS, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()
    )
    workManager.enqueueUniqueWork(
      STARTUP_WORK,
      ExistingWorkPolicy.KEEP,
      OneTimeWorkRequestBuilder<SelfUpdateWorker>().setConstraints(constraints).build()
    )
  }

  /** Same as the startup check, but also replaces a check that is already waiting. */
  fun checkNow(context: Context) {
    WorkManager.getInstance(context)
      .enqueueUniqueWork(
        STARTUP_WORK,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<SelfUpdateWorker>().setConstraints(constraints).build()
      )
  }

  fun isPlaybackActive(context: Context): Boolean =
    context.getSystemService(AudioManager::class.java).isMusicActive

  fun notify(context: Context, title: String, text: String, target: PendingIntent) {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      manager.createNotificationChannel(
        NotificationChannel(
          CHANNEL_ID,
          context.getString(R.string.app_updates_title),
          NotificationManager.IMPORTANCE_DEFAULT
        )
      )
    }
    val canPost =
      Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
    if (!canPost) return
    NotificationManagerCompat.from(context)
      .notify(
        NOTIFICATION_ID,
        NotificationCompat.Builder(context, CHANNEL_ID)
          .setSmallIcon(R.drawable.ic_launcher_nobg)
          .setContentTitle(title)
          .setContentText(text)
          .setContentIntent(target)
          .setAutoCancel(true)
          .build()
      )
  }

  internal data class Release(val tag: String, val apkUrl: String, val sha256: String?)

  internal fun fetchLatestRelease(): Release? {
    val connection =
      URL("https://api.github.com/repos/${BuildConfig.UPDATE_REPOSITORY}/releases/latest")
        .openConnection() as HttpURLConnection
    connection.connectTimeout = 15_000
    connection.readTimeout = 15_000
    connection.setRequestProperty("Accept", "application/vnd.github+json")
    try {
      if (connection.responseCode != HttpURLConnection.HTTP_OK) {
        Log.w(TAG, "No release available: HTTP ${connection.responseCode}")
        return null
      }
      val release =
        JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
      val assets = release.getJSONArray("assets")
      for (index in 0 until assets.length()) {
        val asset = assets.getJSONObject(index)
        if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
          return Release(
            tag = release.getString("tag_name"),
            apkUrl = asset.getString("browser_download_url"),
            sha256 = asset.optString("digest").removePrefix("sha256:").ifEmpty { null },
          )
        }
      }
      return null
    } finally {
      connection.disconnect()
    }
  }

  internal fun download(url: String, target: File, expectedSha256: String?): Boolean {
    val digest = MessageDigest.getInstance("SHA-256")
    target.parentFile?.mkdirs()
    URL(url).openStream().use { input ->
      target.outputStream().use { output ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
          val read = input.read(buffer)
          if (read < 0) break
          digest.update(buffer, 0, read)
          output.write(buffer, 0, read)
        }
      }
    }
    val actual = digest.digest().joinToString("") { "%02x".format(it) }
    return expectedSha256 == null || actual.equals(expectedSha256, ignoreCase = true)
  }

  internal fun install(context: Context, apk: File) {
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

class SelfUpdateWorker(context: Context, params: WorkerParameters) :
  CoroutineWorker(context, params) {

  override suspend fun doWork(): Result =
    withContext(Dispatchers.IO) {
      if (!getAutoUpdateCheckSetting(applicationContext)) return@withContext Result.success()
      try {
        val release = SelfUpdater.fetchLatestRelease() ?: return@withContext Result.success()
        if (!isNewerVersion(release.tag, BuildConfig.VERSION_NAME)) {
          return@withContext Result.success()
        }
        if (SelfUpdater.isPlaybackActive(applicationContext)) return@withContext Result.retry()

        if (
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !applicationContext.packageManager.canRequestPackageInstalls()
        ) {
          askForInstallPermission()
          return@withContext Result.success()
        }

        val apk = File(applicationContext.cacheDir, "self_update/${release.tag}.apk")
        if (!SelfUpdater.download(release.apkUrl, apk, release.sha256)) {
          Log.e("SelfUpdater", "Checksum mismatch for ${release.tag}")
          apk.delete()
          return@withContext Result.retry()
        }
        // Downloading takes a while, so the user may have started listening since the first check.
        if (SelfUpdater.isPlaybackActive(applicationContext)) return@withContext Result.retry()
        SelfUpdater.install(applicationContext, apk)
        Result.success()
      } catch (e: Exception) {
        Log.e("SelfUpdater", "Update failed: ${e.message}", e)
        Result.retry()
      }
    }

  private fun askForInstallPermission() {
    val settings =
      Intent(
          Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          Uri.parse("package:${applicationContext.packageName}"),
        )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    SelfUpdater.notify(
      applicationContext,
      "Allow automatic updates",
      "Tap once to let this app install its own updates",
      PendingIntent.getActivity(applicationContext, 0, settings, PendingIntent.FLAG_IMMUTABLE),
    )
  }
}

class SelfUpdateResultReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
      PackageInstaller.STATUS_PENDING_USER_ACTION -> {
        // Some Android versions and phone brands still insist on a confirmation tap.
        @Suppress("DEPRECATION")
        val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
        SelfUpdater.notify(
          context,
          "Update ready",
          "Tap to finish installing the update",
          PendingIntent.getActivity(
            context,
            0,
            confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
          ),
        )
      }
      PackageInstaller.STATUS_SUCCESS -> Unit
      else ->
        Log.e(
          "SelfUpdater",
          "Install failed: ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}",
        )
    }
  }
}
