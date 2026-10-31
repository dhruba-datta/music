package echo.music.iad1tya.ui.screens

import echo.music.iad1tya.ui.component.StageTopBar
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import echo.music.iad1tya.R
import androidx.compose.foundation.layout.heightIn
import echo.music.iad1tya.ui.theme.echomusicTheme
import echo.music.iad1tya.utils.CrashHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val crashLog =
      intent.getStringExtra(CrashHandler.EXTRA_CRASH_LOG) ?: getString(R.string.crash_no_log)

    setContent {
      val darkTheme = isSystemInDarkTheme()
      echomusicTheme(darkTheme = darkTheme) {
        CrashScreen(
          crashLog = crashLog,
          onClose = { finishAffinity() },
          onShare = { shareCrashLog(crashLog) },
          onCopy = { copyToClipboard(crashLog) }
        )
      }
    }
  }

  private fun copyToClipboard(crashLog: String) {
    val cm =
      getSystemService(android.content.Context.CLIPBOARD_SERVICE)
        as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("CrashLog", crashLog)
    cm.setPrimaryClip(clip)
    android.widget.Toast.makeText(
        this,
        R.string.copied_to_clipboard,
        android.widget.Toast.LENGTH_SHORT
      )
      .show()
  }

  private fun shareCrashLog(crashLog: String) {
    try {

      val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val fileName = "echomusic_crash_$timestamp.txt"
      val crashFile = File(cacheDir, fileName)
      crashFile.writeText(crashLog)

      val uri = FileProvider.getUriForFile(this, "${packageName}.FileProvider", crashFile)

      val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crash_report_subject))
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

      startActivity(Intent.createChooser(shareIntent, getString(R.string.crash_share_title)))
    } catch (e: Exception) {

      val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, crashLog)
          putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crash_report_subject))
        }
      startActivity(Intent.createChooser(shareIntent, getString(R.string.crash_share_title)))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(crashLog: String, onClose: () -> Unit, onShare: () -> Unit, onCopy: () -> Unit) {
  val context = LocalContext.current

  Scaffold(
    topBar = {
      StageTopBar(
        title = {
          Text(
            text = stringResource(R.string.crash_title)
          )
        },
        navigationIcon = {
          IconButton(onClick = onClose) {
            Icon(
              painter = painterResource(R.drawable.arrow_back),
              contentDescription = stringResource(R.string.crash_close)
            )
          }
        },
        actions = {
          IconButton(onClick = onCopy) {
            Icon(
              painter = painterResource(R.drawable.content_copy),
              contentDescription = stringResource(R.string.copy_logs)
            )
          }
          IconButton(onClick = onClose) {
            Icon(
              painter = painterResource(R.drawable.close),
              contentDescription = stringResource(R.string.crash_close)
            )
          }
        }
)
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onShare,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(
          painter = painterResource(R.drawable.share),
          contentDescription = stringResource(R.string.crash_share_logs)
        )
      }
    },
    containerColor = MaterialTheme.colorScheme.surface
  ) { paddingValues ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 18.dp)
          .verticalScroll(rememberScrollState())
    ) {
      Text(
        text = stringResource(R.string.crash_description),
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, end = 4.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 430.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp)
      ) {
        Text(
          text = crashLog,
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              lineHeight = 20.sp
            ),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.horizontalScroll(rememberScrollState())
        )
      }

      Spacer(modifier = Modifier.height(88.dp))
    }
  }
}
