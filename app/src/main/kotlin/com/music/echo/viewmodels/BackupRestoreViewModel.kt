package echo.music.iad1tya.viewmodels

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.widget.Toast
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import echo.music.iad1tya.MainActivity
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AccountChannelHandleKey
import echo.music.iad1tya.constants.AccountEmailKey
import echo.music.iad1tya.constants.AccountNameKey
import echo.music.iad1tya.constants.DataSyncIdKey
import echo.music.iad1tya.constants.DeeplApiKey
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.constants.ListenTogetherSessionTokenKey
import echo.music.iad1tya.constants.OpenRouterApiKey
import echo.music.iad1tya.constants.ProxyPasswordKey
import echo.music.iad1tya.constants.SavedAccountsKey
import echo.music.iad1tya.constants.SpotifyAccessTokenExpiresAtKey
import echo.music.iad1tya.constants.SpotifyAccessTokenKey
import echo.music.iad1tya.constants.SpotifyAccountEmailKey
import echo.music.iad1tya.constants.SpotifyAccountNameKey
import echo.music.iad1tya.constants.SpotifySpDcKey
import echo.music.iad1tya.constants.SpotifySpKeyKey
import echo.music.iad1tya.constants.VisitorDataKey
import echo.music.iad1tya.db.InternalDatabase
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.db.entities.ArtistEntity
import echo.music.iad1tya.db.entities.Song
import echo.music.iad1tya.db.entities.SongEntity
import echo.music.iad1tya.extensions.div
import echo.music.iad1tya.extensions.tryOrNull
import echo.music.iad1tya.extensions.zipInputStream
import echo.music.iad1tya.extensions.zipOutputStream
import echo.music.iad1tya.playback.MusicService
import echo.music.iad1tya.playback.MusicService.Companion.PERSISTENT_QUEUE_FILE
import echo.music.iad1tya.utils.dataStore
import echo.music.iad1tya.utils.reportException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import javax.inject.Inject
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

data class CsvImportState(
  val previewRows: List<List<String>> = emptyList(),
  val artistColumnIndex: Int = 0,
  val titleColumnIndex: Int = 1,
  val urlColumnIndex: Int = -1,
  val hasHeader: Boolean = true,
)

data class ConvertedSongLog(
  val title: String,
  val artists: String,
)

@HiltViewModel
class BackupRestoreViewModel
@Inject
constructor(
  val database: MusicDatabase,
) : ViewModel() {

  suspend fun backup(context: Context, uri: Uri) =
    withContext(Dispatchers.IO) {
      runCatching {
          context.applicationContext.contentResolver.openOutputStream(uri)?.use {
            it.buffered().zipOutputStream().use { outputStream ->
              val settings = context.dataStore.data.first().toMutablePreferences()
              AccountKeys.forEach { settings.remove(it) }
              val settingsBytes =
                ByteArrayOutputStream().also { PreferencesFileSerializer.writeTo(settings, it) }
              outputStream.putNextEntry(ZipEntry(SETTINGS_FILENAME))
              settingsBytes.writeTo(outputStream)
              database.checkpoint()
              FileInputStream(database.openHelper.writableDatabase.path).use { inputStream ->
                outputStream.putNextEntry(ZipEntry(InternalDatabase.DB_NAME))
                inputStream.copyTo(outputStream)
              }
            }
          }
        }
        .onSuccess {
          withContext(Dispatchers.Main) {
            Toast.makeText(context, R.string.backup_create_success, Toast.LENGTH_SHORT).show()
          }
        }
        .onFailure {
          reportException(it)
          withContext(Dispatchers.Main) {
            Toast.makeText(context, R.string.backup_create_failed, Toast.LENGTH_SHORT).show()
          }
        }
    }

  suspend fun restore(context: Context, uri: Uri) =
    withContext(Dispatchers.IO) {
      runCatching {
          Timber.tag("RESTORE").i("Starting restore from URI: $uri")
          context.applicationContext.contentResolver.openInputStream(uri)?.use { raw ->
            raw.zipInputStream().use { inputStream ->
              var entry = tryOrNull { inputStream.nextEntry }
              var foundAny = false
              while (entry != null) {
                Timber.tag("RESTORE").i("Found zip entry: ${entry.name}")
                when (entry.name) {
                  SETTINGS_FILENAME -> {
                    Timber.tag("RESTORE").i("Restoring settings to datastore")
                    foundAny = true
                    val restored =
                      PreferencesFileSerializer.readFrom(
                          ByteArrayInputStream(inputStream.readBytes())
                        )
                        .toMutablePreferences()
                    // Backups leave out sign-ins, so a restore must not end the current ones.
                    val current = context.dataStore.data.first()
                    AccountKeys.forEach { restored.keepFrom(current, it) }
                    (context.filesDir / "datastore" / SETTINGS_FILENAME).outputStream().use {
                      outputStream ->
                      PreferencesFileSerializer.writeTo(restored, outputStream)
                    }
                    java.io.File(context.filesDir, "clear_export_state").createNewFile()
                  }
                  InternalDatabase.DB_NAME -> {
                    Timber.tag("RESTORE").i("Restoring DB (entry = ${entry.name})")
                    foundAny = true

                    val tempFile = java.io.File(context.cacheDir, "temp_restore.db")
                    java.io.FileOutputStream(tempFile).use { outputStream ->
                      inputStream.copyTo(outputStream)
                    }

                    if (!isUsableDatabase(tempFile)) {
                      Timber.tag("RESTORE").e("Backup database is not usable")
                      withContext(Dispatchers.Main) {
                        Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_LONG).show()
                      }
                      tempFile.delete()
                      return@withContext
                    }

                    var backupVersion = 0
                    runCatching {
                      java.io.RandomAccessFile(tempFile, "r").use { raf ->
                        raf.seek(60)
                        backupVersion = raf.readInt()
                      }
                    }

                    val currentVersion = database.openHelper.writableDatabase.version
                    if (backupVersion > currentVersion) {
                      Timber.tag("RESTORE")
                        .e("Backup version ($backupVersion) > current version ($currentVersion)")
                      withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.restore_failed) +
                              ": Backup is from a newer app version.",
                            Toast.LENGTH_LONG
                          )
                          .show()
                      }
                      tempFile.delete()
                      return@withContext
                    }

                    try {
                      val dbPath = database.openHelper.writableDatabase.path
                      database.checkpoint()
                      database.close()
                      Timber.tag("RESTORE").i("Overwriting DB at path: $dbPath")

                      val dbFile = java.io.File(dbPath)
                      val walFile = java.io.File(dbPath + "-wal")
                      val shmFile = java.io.File(dbPath + "-shm")

                      if (walFile.exists()) {
                        walFile.delete()
                        Timber.tag("RESTORE").i("Deleted existing WAL file")
                      }
                      if (shmFile.exists()) {
                        shmFile.delete()
                        Timber.tag("RESTORE").i("Deleted existing SHM file")
                      }

                      tempFile.copyTo(dbFile, overwrite = true)
                      tempFile.delete()
                      Timber.tag("RESTORE").i("DB overwrite complete")
                    } catch (e: Exception) {
                      Timber.tag("RESTORE")
                        .e(e, "Due to new architecture this backup can't be restored")
                      withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Due to new architecture, this backup can't be restored",
                            Toast.LENGTH_LONG
                          )
                          .show()
                      }
                      tempFile.delete()
                      return@withContext
                    }
                  }
                  else -> {
                    Timber.tag("RESTORE").i("Skipping unexpected entry: ${entry.name}")
                  }
                }
                entry = tryOrNull { inputStream.nextEntry }
              }
              if (!foundAny) {
                Timber.tag("RESTORE").w("No expected entries found in archive")
              }
            }
          } ?: run { Timber.tag("RESTORE").e("Could not open input stream for uri: $uri") }

          withContext(Dispatchers.Main) {
            context.stopService(Intent(context, MusicService::class.java))
            context.filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
            val restartIntent =
              Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
              }
            context.startActivity(restartIntent)
            exitProcess(0)
          }
        }
        .onFailure {
          reportException(it)
          Timber.tag("RESTORE").e(it, "Due to new architecture this backup can't be restored")
          withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Due to new architecture, this backup can't be restored",
                Toast.LENGTH_SHORT
              )
              .show()
          }
        }
    }

  private fun <T> MutablePreferences.keepFrom(current: Preferences, key: Preferences.Key<T>) {
    current[key]?.let { this[key] = it }
  }

  /** Checks a copy, because opening a database can change its file. */
  private fun isUsableDatabase(file: File): Boolean {
    val header = file.inputStream().use { it.readNBytes(SQLITE_HEADER.size) }
    if (!header.contentEquals(SQLITE_HEADER)) return false

    val copy = File(file.parentFile, "${file.name}.check")
    return try {
      file.copyTo(copy, overwrite = true)
      SQLiteDatabase.openDatabase(
          copy.path,
          null,
          SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS,
        )
        .use { database ->
          val intact =
            database.rawQuery("PRAGMA quick_check(1)", null).use {
              it.moveToFirst() && it.getString(0) == "ok"
            }
          val hasSongs =
            database
              .rawQuery("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'song'", null)
              .use { it.moveToFirst() }
          intact && hasSongs
        }
    } catch (e: Exception) {
      Timber.tag("RESTORE").e(e, "Could not open backup database")
      false
    } finally {
      SQLiteDatabase.deleteDatabase(copy)
    }
  }

  suspend fun previewCsvFile(context: Context, uri: Uri): CsvImportState =
    withContext(Dispatchers.IO) {
      var csvState = CsvImportState()
      runCatching {
          context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().useLines { linesSeq ->
              val rowsToPreviewRaw = linesSeq.take(6).toList()
              val previewRows = rowsToPreviewRaw.map { parseCsvLine(it) }
              val hasHeader = rowsToPreviewRaw.isNotEmpty() && rowsToPreviewRaw[0].contains(",")
              csvState =
                CsvImportState(
                  previewRows = previewRows,
                  hasHeader = hasHeader,
                )
            }
          }
        }
        .onFailure {
          reportException(it)
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to preview CSV file", Toast.LENGTH_SHORT).show()
          }
        }
      csvState
    }

  suspend fun importPlaylistFromCsv(
    context: Context,
    uri: Uri,
    columnMapping: CsvImportState,
    onProgress: (Int) -> Unit = {},
    onLogUpdate: (List<ConvertedSongLog>) -> Unit = {},
  ): ArrayList<Song> =
    withContext(Dispatchers.IO) {
      val songs = arrayListOf<Song>()
      val recentLogs = mutableListOf<ConvertedSongLog>()

      runCatching {
          val totalLines =
            context.contentResolver.openInputStream(uri)?.use { stream ->
              stream.bufferedReader().useLines { it.count() }
            } ?: 0

          context.contentResolver.openInputStream(uri)?.use { stream ->
            val startIndex = if (columnMapping.hasHeader) 1 else 0
            val linesToProcess = totalLines - startIndex

            stream.bufferedReader().useLines { linesSeq ->
              linesSeq.drop(startIndex).forEachIndexed { index, line ->
                val parts = parseCsvLine(line)

                if (parts.isNotEmpty()) {
                  if (
                    columnMapping.artistColumnIndex < parts.size &&
                      columnMapping.titleColumnIndex < parts.size
                  ) {
                    val title = parts[columnMapping.titleColumnIndex].trim()
                    val artistStr = parts[columnMapping.artistColumnIndex].trim()
                    val url =
                      if (
                        columnMapping.urlColumnIndex >= 0 &&
                          columnMapping.urlColumnIndex < parts.size
                      ) {
                        parts[columnMapping.urlColumnIndex].trim()
                      } else {
                        ""
                      }

                    if (title.isNotEmpty() && artistStr.isNotEmpty()) {
                      val artists =
                        artistStr
                          .split(";", ",")
                          .map { it.trim() }
                          .filter { it.isNotEmpty() }
                          .map { ArtistEntity(id = "", name = it) }

                      val mockSong =
                        Song(
                          song =
                            SongEntity(
                              id = "",
                              title = title,
                            ),
                          artists = artists,
                        )
                      songs.add(mockSong)

                      val logEntry =
                        ConvertedSongLog(
                          title = title,
                          artists = artists.joinToString(", ") { it.name },
                        )
                      recentLogs.add(0, logEntry)
                      if (recentLogs.size > 3) {
                        recentLogs.removeAt(recentLogs.size - 1)
                      }
                    }
                  }
                }

                if (linesToProcess > 0) {
                  if (index % 50 == 0 || index == linesToProcess - 1) {
                    val progress = ((index + 1) * 100) / linesToProcess
                    val currentLogs = recentLogs.toList()
                    withContext(Dispatchers.Main) {
                      onLogUpdate(currentLogs)
                      onProgress(progress.coerceIn(0, 99))
                    }
                  }
                }
              }
            }
          }
        }
        .onFailure {
          reportException(it)
          Timber.tag("CSV_IMPORT").e(it, "CSV import failed")
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to import CSV file", Toast.LENGTH_SHORT).show()
          }
        }

      withContext(Dispatchers.Main) {
        if (songs.isEmpty()) {
          Toast.makeText(
              context,
              "No songs found. Invalid file, or perhaps no song matches were found.",
              Toast.LENGTH_SHORT
            )
            .show()
        }
      }

      songs
    }

  suspend fun importPlaylistFromCsv(context: Context, uri: Uri): ArrayList<Song> {
    return importPlaylistFromCsv(context, uri, CsvImportState())
  }

  private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false

    for (char in line) {
      when {
        char == '"' -> inQuotes = !inQuotes
        char == ',' && !inQuotes -> {
          result.add(current.toString())
          current = StringBuilder()
        }
        else -> current.append(char)
      }
    }
    result.add(current.toString())
    return result.map { it.trim().trim('"') }
  }

  suspend fun loadM3UOnline(
    context: Context,
    uri: Uri,
  ): ArrayList<Song> =
    withContext(Dispatchers.IO) {
      val songs = ArrayList<Song>()

      runCatching {
        context.applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
          stream.bufferedReader().useLines { linesSeq ->
            val lines = linesSeq.iterator()
            if (lines.hasNext() && lines.next().startsWith("#EXTM3U")) {
              while (lines.hasNext()) {
                val rawLine = lines.next()
                if (rawLine.startsWith("#EXTINF:")) {
                  val artists =
                    rawLine
                      .substringAfter("#EXTINF:")
                      .substringAfter(',')
                      .substringBefore(" - ")
                      .split(';')
                  val title =
                    rawLine.substringAfter("#EXTINF:").substringAfter(',').substringAfter(" - ")

                  val mockSong =
                    Song(
                      song =
                        SongEntity(
                          id = "",
                          title = title,
                        ),
                      artists = artists.map { ArtistEntity("", it) },
                    )
                  songs.add(mockSong)
                }
              }
            }
          }
        }
      }

      withContext(Dispatchers.Main) {
        if (songs.isEmpty()) {
          Toast.makeText(
              context,
              "No songs found. Invalid file, or perhaps no song matches were found.",
              Toast.LENGTH_SHORT
            )
            .show()
        }
      }

      songs
    }

  companion object {
    const val SETTINGS_FILENAME = "settings.preferences_pb"

    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray()

    /** Sign-ins and secrets. A backup zip is an ordinary file, so these never go into one. */
    private val AccountKeys: List<Preferences.Key<*>> =
      listOf(
        InnerTubeCookieKey,
        VisitorDataKey,
        DataSyncIdKey,
        AccountNameKey,
        AccountEmailKey,
        AccountChannelHandleKey,
        SavedAccountsKey,
        SpotifySpDcKey,
        SpotifySpKeyKey,
        SpotifyAccountNameKey,
        SpotifyAccountEmailKey,
        SpotifyAccessTokenKey,
        SpotifyAccessTokenExpiresAtKey,
        ProxyPasswordKey,
        ListenTogetherSessionTokenKey,
        OpenRouterApiKey,
        DeeplApiKey,
      )
  }
}
