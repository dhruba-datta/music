package echo.music.iad1tya.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R
import echo.music.iad1tya.listentogether.ConnectionState
import echo.music.iad1tya.listentogether.UserInfo

/** The warm artwork-style gradient behind the room code. */
private val StageRoomGradient =
  Brush.linearGradient(
    0f to Color(0xFFFF8A5C),
    0.55f to Color(0xFFD9486A),
    1f to Color(0xFF512A72),
    start = Offset.Zero,
    end = Offset.Infinite,
  )

/** The room card: a line of status over the code in large letters. */
@Composable
fun StageRoomHero(roomCode: String, label: String, modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 120.dp)
        .background(StageRoomGradient, RoundedCornerShape(28.dp))
        .padding(22.dp),
  ) {
    Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text(
      text = roomCode,
      fontSize = 32.sp,
      lineHeight = 35.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 2.56.sp,
      color = Color.White,
    )
  }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
  Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
}

/** The two grey pills that copy the invite link and the room code. */
@Composable
fun StageRoomCopyButtons(roomCode: String, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
    StageButton(
      text = stringResource(R.string.copy_link),
      primary = false,
      onClick = {
        copyToClipboard(
          context,
          "Listen Together Link",
          "https://metroserverx.meowery.eu/listen?code=$roomCode",
        )
      },
      modifier = Modifier.weight(1f),
    )
    StageButton(
      text = stringResource(R.string.copy_code),
      primary = false,
      onClick = { copyToClipboard(context, "Room Code", roomCode) },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun StageRoomAvatar(name: String, size: Int) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier.size(size.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
  ) {
    Text(
      text = name.take(1).uppercase(),
      fontSize = (size * 0.4f).sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/** A person in the room: a 56dp round avatar beside their name and role. */
@Composable
fun StageRoomUserRow(
  user: UserInfo,
  isCurrentUser: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).heightIn(min = 64.dp),
  ) {
    StageRoomAvatar(name = user.username, size = 56)
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
      Text(
        text = user.username,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text =
          stringResource(
            when {
              user.isHost -> R.string.host_label
              isCurrentUser -> R.string.you_label
              else -> R.string.stage_guest
            }
          ),
        fontSize = 13.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun StageRoomDecision(onApprove: () -> Unit, onReject: () -> Unit) {
  IconButton(onClick = onApprove, modifier = Modifier.size(44.dp)) {
    Icon(
      painter = painterResource(R.drawable.check),
      contentDescription = stringResource(R.string.approve),
      tint = MaterialTheme.colorScheme.onSurface,
    )
  }
  IconButton(onClick = onReject, modifier = Modifier.size(44.dp)) {
    Icon(
      painter = painterResource(R.drawable.close),
      contentDescription = stringResource(R.string.reject),
      tint = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/** A join request: a 44dp avatar, the name, and the approve and reject buttons. */
@Composable
fun StageRoomRequestRow(
  username: String,
  onApprove: () -> Unit,
  onReject: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
  ) {
    StageRoomAvatar(name = username, size = 44)
    Text(
      text = username,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    StageRoomDecision(onApprove = onApprove, onReject = onReject)
  }
}

/** A song a guest asked for, with the same approve and reject buttons as a join request. */
@Composable
fun StageRoomSuggestionRow(
  title: String,
  fromUsername: String,
  onApprove: () -> Unit,
  onReject: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.queue_music),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(24.dp),
    )
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
      Text(
        text = title,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = fromUsername,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    StageRoomDecision(onApprove = onApprove, onReject = onReject)
  }
}

/** Kick, Permanently Block and Transfer Ownership, as flat 52dp rows. */
@Composable
fun StageRoomManageRows(
  onKick: () -> Unit,
  onBlock: () -> Unit,
  onTransfer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    StageSheetRow(
      text = stringResource(R.string.kick_user),
      icon = R.drawable.close,
      onClick = onKick,
    )
    StageSheetRow(
      text = stringResource(R.string.permanently_kick_user),
      icon = R.drawable.remove,
      onClick = onBlock,
    )
    StageSheetRow(
      text = stringResource(R.string.transfer_ownership),
      icon = R.drawable.crown,
      onClick = onTransfer,
    )
  }
}

/** Room code and username fields over the Join and Create pills. */
@Composable
fun StageRoomJoinForm(
  roomCode: String,
  onRoomCodeChange: (String) -> Unit,
  username: String,
  onUsernameChange: (String) -> Unit,
  joinEnabled: Boolean,
  createEnabled: Boolean,
  isJoining: Boolean,
  errorMessage: String?,
  onJoin: () -> Unit,
  onCreate: () -> Unit,
  modifier: Modifier = Modifier,
  fieldModifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    StageSearchField(
      value = roomCode,
      onValueChange = onRoomCodeChange,
      placeholder = stringResource(R.string.room_code),
      leadingIcon = R.drawable.token,
      modifier = fieldModifier,
    )
    StageSearchField(
      value = username,
      onValueChange = onUsernameChange,
      placeholder = stringResource(R.string.username),
      leadingIcon = R.drawable.person,
      modifier = fieldModifier,
    )

    if (isJoining) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(18.dp),
          strokeWidth = 2.dp,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.waiting_for_approval),
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    errorMessage?.let { message ->
      Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      StagePillButton(onClick = onJoin, enabled = joinEnabled) {
        Text(stringResource(R.string.join_room))
      }
      StagePillButton(onClick = onCreate, enabled = createEnabled, primary = false) {
        Text(stringResource(R.string.create_room))
      }
    }
  }
}

/** The server link state with its Connect, Disconnect and Reconnect buttons. */
@Composable
fun StageRoomConnection(
  connectionState: ConnectionState,
  onConnect: () -> Unit,
  onDisconnect: () -> Unit,
  onReconnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = connectionState.stageLabel(),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color =
        if (connectionState == ConnectionState.ERROR) {
          MaterialTheme.colorScheme.error
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
    if (
      connectionState == ConnectionState.CONNECTING ||
        connectionState == ConnectionState.RECONNECTING
    ) {
      LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      if (
        connectionState == ConnectionState.DISCONNECTED ||
          connectionState == ConnectionState.ERROR
      ) {
        StageButton(
          text = stringResource(R.string.connect),
          primary = false,
          onClick = onConnect,
          modifier = Modifier.weight(1f),
        )
      } else {
        StageButton(
          text = stringResource(R.string.disconnect),
          primary = false,
          onClick = onDisconnect,
          modifier = Modifier.weight(1f),
        )
        StageButton(
          text = "Reconnect",
          primary = false,
          onClick = onReconnect,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

/** The state of the link to the Listen Together server, in words. */
@Composable
fun ConnectionState.stageLabel(): String =
  stringResource(
    when (this) {
      ConnectionState.CONNECTED -> R.string.listen_together_connected
      ConnectionState.CONNECTING -> R.string.listen_together_connecting
      ConnectionState.RECONNECTING -> R.string.listen_together_reconnecting
      ConnectionState.ERROR -> R.string.listen_together_error
      ConnectionState.DISCONNECTED -> R.string.listen_together_disconnected
    }
  )
