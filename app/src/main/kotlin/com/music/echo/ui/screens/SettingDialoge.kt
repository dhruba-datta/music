package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.innertube.utils.parseCookieString
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.InnerTubeCookieKey
import echo.music.iad1tya.ui.component.StageSheetCards
import echo.music.iad1tya.ui.component.StageSheetHandle
import echo.music.iad1tya.ui.component.StageSheetRow
import echo.music.iad1tya.ui.component.StageSheetShape
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialoge(
  onDismissRequest: () -> Unit,
  onNavigate: (String) -> Unit,
  homeViewModel: HomeViewModel
) {
  val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
  val isLoggedIn =
    remember(innerTubeCookie) {
      innerTubeCookie.isNotEmpty() && "SAPISID" in parseCookieString(innerTubeCookie)
    }

  val accountName by homeViewModel.accountName.collectAsState()
  val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    shape = StageSheetShape,
    dragHandle = { StageSheetHandle() },
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 8.dp)
          .clip(RoundedCornerShape(18.dp))
          .clickable {
            onDismissRequest()
            onNavigate(if (isLoggedIn) "settings/account" else "login")
          }
          .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
      StageAccountAvatar(
        imageUrl = accountImageUrl.takeIf { isLoggedIn },
        modifier = Modifier.size(52.dp)
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isLoggedIn) accountName else "Anonymous",
          fontSize = 19.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text =
            stringResource(if (isLoggedIn) R.string.stage_manage_account else R.string.stage_sign_in),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(
        painter = painterResource(R.drawable.navigate_next),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    StageSheetCards {
      StageSheetRow(
        text = stringResource(R.string.settings),
        icon = R.drawable.stage_settings,
        onClick = {
          onDismissRequest()
          onNavigate("settings")
        }
      )
      StageSheetRow(
        text = "About",
        icon = R.drawable.info,
        onClick = {
          onDismissRequest()
          onNavigate("settings/about")
        },
        trailing = {
          Text(
            BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      )
    }
  }
}

/** The signed-in account's photo, or the app's gradient circle when there isn't one. */
@Composable
fun StageAccountAvatar(imageUrl: String?, modifier: Modifier = Modifier) {
  val accountLabel = stringResource(R.string.account)
  if (!imageUrl.isNullOrBlank()) {
    AsyncImage(
      model = imageUrl,
      contentDescription = accountLabel,
      contentScale = ContentScale.Crop,
      modifier = modifier.clip(CircleShape)
    )
  } else {
    Box(
      modifier =
        modifier
          .clip(CircleShape)
          .semantics { contentDescription = accountLabel }
          .drawBehind {
            // CSS linear-gradient(145deg, #e04f7f, #6a4c93).
            val half = size.width * 0.6965f
            val dx = 0.5736f * half
            val dy = 0.8192f * half
            drawRect(
              Brush.linearGradient(
                colors = listOf(Color(0xFFE04F7F), Color(0xFF6A4C93)),
                start = Offset(center.x - dx, center.y - dy),
                end = Offset(center.x + dx, center.y + dy)
              )
            )
          }
    )
  }
}
