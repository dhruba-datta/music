package echo.music.iad1tya.ui.screens

import echo.music.iad1tya.ui.component.LocalMenuState
import echo.music.iad1tya.ui.component.StageButton
import echo.music.iad1tya.ui.component.StageRoomHero
import echo.music.iad1tya.ui.component.StageRoomCopyButtons
import echo.music.iad1tya.ui.component.StageRoomJoinForm
import echo.music.iad1tya.ui.component.StageRoomManageRows
import echo.music.iad1tya.ui.component.StageRoomRequestRow
import echo.music.iad1tya.ui.component.StageRoomSuggestionRow
import echo.music.iad1tya.ui.component.StageRoomUserRow
import echo.music.iad1tya.ui.component.StageSheetTitle
import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.ui.component.stageLabel
import echo.music.iad1tya.ui.component.StageTopBar
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import echo.music.iad1tya.LocalListenTogetherManager
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppBarHeight
import echo.music.iad1tya.constants.ListenTogetherInTopBarKey
import echo.music.iad1tya.constants.ListenTogetherUsernameKey
import echo.music.iad1tya.listentogether.ConnectionState
import echo.music.iad1tya.listentogether.JoinRequestPayload
import echo.music.iad1tya.listentogether.ListenTogetherEvent
import echo.music.iad1tya.listentogether.SuggestionReceivedPayload
import echo.music.iad1tya.listentogether.UserInfo
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.component.Material3SettingsGroup
import echo.music.iad1tya.ui.component.Material3SettingsItem
import echo.music.iad1tya.ui.component.StageBarTitle
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberPreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListenTogetherScreen(navController: NavController, showTopBar: Boolean = false) {
  val context = LocalContext.current
  val listenTogetherManager = LocalListenTogetherManager.current
  val windowInsets = LocalPlayerAwareWindowInsets.current

  if (listenTogetherManager == null) {
    NotConfiguredContent()
    return
  }

  val connectionState by listenTogetherManager.connectionState.collectAsState()
  val roomState by listenTogetherManager.roomState.collectAsState()
  val userId by listenTogetherManager.userId.collectAsState()
  val pendingJoinRequests by listenTogetherManager.pendingJoinRequests.collectAsState()
  val pendingSuggestions by listenTogetherManager.pendingSuggestions.collectAsState()

  val (listenTogetherInTopBar) = rememberPreference(ListenTogetherInTopBarKey, defaultValue = true)
  val shouldShowTopBar = showTopBar || listenTogetherInTopBar

  var savedUsername by rememberPreference(ListenTogetherUsernameKey, "")
  var roomCodeInput by rememberSaveable { mutableStateOf("") }
  var usernameInput by rememberSaveable { mutableStateOf(savedUsername) }

  var isCreatingRoom by rememberSaveable { mutableStateOf(false) }
  var isJoiningRoom by rememberSaveable { mutableStateOf(false) }
  var joinErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

  val menuState = LocalMenuState.current
  val invalidRoomCodeText = stringResource(R.string.invalid_room_code)
  val joinRequestDeniedText = stringResource(R.string.join_request_denied)

  LaunchedEffect(savedUsername) {
    if (usernameInput.isBlank() && savedUsername.isNotBlank()) {
      usernameInput = savedUsername
    }
  }

  LaunchedEffect(listenTogetherManager) {
    if (
      connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR
    ) {
      listenTogetherManager.connect()
    }
    listenTogetherManager.events.collect { event ->
      when (event) {
        is ListenTogetherEvent.JoinRejected -> {
          val reason = event.reason
          joinErrorMessage =
            when {
              reason.isNullOrBlank() -> joinRequestDeniedText
              reason.contains("invalid", ignoreCase = true) -> invalidRoomCodeText
              else -> "$joinRequestDeniedText: $reason"
            }
          isJoiningRoom = false
          isCreatingRoom = false
        }
        is ListenTogetherEvent.JoinApproved -> {
          isJoiningRoom = false
          joinErrorMessage = null
        }
        is ListenTogetherEvent.RoomCreated -> {
          isCreatingRoom = false
          val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
          val clip = android.content.ClipData.newPlainText("ListenTogetherRoom", event.roomCode)
          clipboard.setPrimaryClip(clip)
        }
        else -> {}
      }
    }
  }

  val isInRoom = listenTogetherManager.isInRoom
  val isHost = roomState?.hostId == userId

  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val bringIntoViewRequester = remember { BringIntoViewRequester() }

  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      lazyListState.animateScrollToItem(0)
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  LazyColumn(
    state = lazyListState,
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding(),
    contentPadding =
      PaddingValues(
        start = 18.dp,
        end = 18.dp,
        top = windowInsets.asPaddingValues().calculateTopPadding() + 4.dp,
        bottom = windowInsets.asPaddingValues().calculateBottomPadding() + 16.dp + AppBarHeight
      ),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (connectionState == ConnectionState.CONNECTED && !isInRoom) {
      item {
        Text(
          text = stringResource(R.string.listen_together_background_disconnect_note),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    if (isInRoom) {

      roomState?.let { room ->
        item {
          val status =
            if (isHost) {
              "${stringResource(R.string.stage_your_room)} · ${connectionState.stageLabel()}"
            } else if (room.allowParticipantControl) {
              stringResource(R.string.listen_together_participant_control_enabled)
            } else {
              stringResource(R.string.listen_together_you_are_guest)
            }
          StageRoomHero(roomCode = room.roomCode, label = status)
        }

        if (isHost) {
          item { StageRoomCopyButtons(roomCode = room.roomCode) }
          item {
            ParticipantControlCard(
              allowParticipantControl = room.allowParticipantControl,
              onAllowParticipantControlChange = { enabled ->
                listenTogetherManager.updateRoomSettings(enabled)
              }
            )
          }
        }

        val connectedUsers = room.users.filter { it.isConnected }
        val currentUserIdValue = userId ?: ""
        item { StageRoomHeading(stringResource(R.string.stage_in_the_room)) }
        items(connectedUsers, key = { it.userId }) { user ->
          val canManage = isHost && user.userId != currentUserIdValue
          StageRoomUserRow(
            user = user,
            isCurrentUser = user.userId == currentUserIdValue,
            enabled = canManage,
            onClick = {
              menuState.show {
                StageSheetTitle(title = user.username)
                StageRoomManageRows(
                  onKick = {
                    listenTogetherManager.kickUser(user.userId, "Removed by host")
                    menuState.dismiss()
                  },
                  onBlock = {
                    listenTogetherManager.blockUser(user.username)
                    listenTogetherManager.kickUser(
                      user.userId,
                      R.string.user_blocked_by_host.toString()
                    )
                    menuState.dismiss()
                  },
                  onTransfer = {
                    listenTogetherManager.transferHost(user.userId)
                    menuState.dismiss()
                  },
                  modifier = Modifier.padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 18.dp),
                )
              }
            },
            modifier = Modifier.padding(horizontal = 4.dp),
          )
        }

        if (isHost && pendingJoinRequests.isNotEmpty()) {
          item { StageRoomHeading(stringResource(R.string.listen_together_join_requests)) }
          items(pendingJoinRequests, key = { it.userId }) { request ->
            StageRoomRequestRow(
              username = request.username,
              onApprove = { listenTogetherManager.approveJoin(request.userId) },
              onReject = { listenTogetherManager.rejectJoin(request.userId, "Rejected by host") },
              modifier = Modifier.padding(horizontal = 4.dp),
            )
          }
        }

        if (isHost && pendingSuggestions.isNotEmpty()) {
          item { StageRoomHeading(stringResource(R.string.pending_suggestions)) }
          items(pendingSuggestions, key = { it.suggestionId }) { suggestion ->
            StageRoomSuggestionRow(
              title = suggestion.trackInfo.title,
              fromUsername = suggestion.fromUsername,
              onApprove = { listenTogetherManager.approveSuggestion(suggestion.suggestionId) },
              onReject = {
                listenTogetherManager.rejectSuggestion(suggestion.suggestionId, "Rejected by host")
              },
              modifier = Modifier.padding(horizontal = 4.dp),
            )
          }
        }

        item {
          StageButton(
            text = stringResource(R.string.leave_room),
            primary = false,
            onClick = { listenTogetherManager.leaveRoom() },
            modifier = Modifier.padding(vertical = 8.dp),
          )
        }
      }
    } else {

      item {
        JoinCreateRoomSection(
          usernameInput = usernameInput,
          onUsernameChange = { usernameInput = it },
          roomCodeInput = roomCodeInput,
          onRoomCodeChange = { roomCodeInput = it },
          savedUsername = savedUsername,
          isJoiningRoom = isJoiningRoom,
          isCreatingRoom = isCreatingRoom,
          joinErrorMessage = joinErrorMessage,
          bringIntoViewRequester = bringIntoViewRequester,
          onCreateRoom = {
            val username = usernameInput.takeIf { it.isNotBlank() } ?: savedUsername
            val finalUsername = username.trim()
            if (finalUsername.isNotBlank()) {
              savedUsername = finalUsername
              Toast.makeText(context, R.string.creating_room, Toast.LENGTH_SHORT).show()
              isCreatingRoom = true
              isJoiningRoom = false
              joinErrorMessage = null

              listenTogetherManager.connect()
              listenTogetherManager.createRoom(finalUsername)

              // Simulate a second click in the background to handle dropped first messages
              coroutineScope.launch {
                kotlinx.coroutines.delay(600)
                if (isCreatingRoom) {
                  listenTogetherManager.connect()
                  listenTogetherManager.createRoom(finalUsername)
                }
              }
            } else {
              Toast.makeText(context, R.string.error_username_empty, Toast.LENGTH_SHORT).show()
            }
          },
          onJoinRoom = {
            val username = usernameInput.takeIf { it.isNotBlank() } ?: savedUsername
            val finalUsername = username.trim()
            val finalRoomCode = roomCodeInput.trim()

            if (finalUsername.isNotBlank() && finalRoomCode.length == 8) {
              savedUsername = finalUsername
              Toast.makeText(
                  context,
                  context.getString(R.string.joining_room, finalRoomCode),
                  Toast.LENGTH_SHORT
                )
                .show()
              isJoiningRoom = true
              isCreatingRoom = false
              joinErrorMessage = null

              listenTogetherManager.connect()
              listenTogetherManager.joinRoom(finalRoomCode, finalUsername)

              // Simulate a second click in the background to handle dropped first messages
              coroutineScope.launch {
                kotlinx.coroutines.delay(600)
                if (isJoiningRoom) {
                  listenTogetherManager.connect()
                  listenTogetherManager.joinRoom(finalRoomCode, finalUsername)
                }
              }
            } else {
              Toast.makeText(context, R.string.error_username_empty, Toast.LENGTH_SHORT).show()
            }
          },
          onFieldFocused = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } }
        )
      }
    }

    if (!shouldShowTopBar) {
      item {
        SettingsLinkCard(
          onClick = { navController.navigate("settings/integrations/listen_together") }
        )
      }
    }

    if (!isInRoom) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Text(
              text = "How it Works",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            InstructionStep(
              title = "1. Create a Room",
              description = "Start a session and share the unique room code with your friends."
            )

            InstructionStep(
              title = "2. Join a Friend",
              description = "Enter their room code to join their session instantly."
            )

            InstructionStep(
              title = "3. Sync Playback",
              description = "The host controls the music. Everyone listens in perfect sync!"
            )
          }
        }
      }
    }
  }

  if (shouldShowTopBar) {
    StageTopBar(
      title = { StageBarTitle(stringResource(R.string.listen_together)) },
      navigationIcon = {
        IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
          Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
        }
      },
      actions = {
        IconButton(
          onClick = { navController.navigate("settings/integrations/listen_together") },
          onLongClick = {},
        ) {
          Icon(
            painterResource(R.drawable.settings),
            contentDescription = stringResource(R.string.settings)
          )
        }
        if (
          connectionState == ConnectionState.DISCONNECTED ||
            connectionState == ConnectionState.ERROR
        ) {
          TextButton(onClick = { listenTogetherManager.connect() }) {
            Text(stringResource(R.string.connect))
          }
        } else if (connectionState == ConnectionState.CONNECTED) {
          TextButton(onClick = { listenTogetherManager.disconnect() }) {
            Text(stringResource(R.string.disconnect))
          }
        } else {
          CircularProgressIndicator(
            modifier = Modifier.padding(end = 16.dp).size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
)
  }
}

@Composable
private fun InstructionStep(title: String, description: String) {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 20.sp
    )
  }
}

@Composable
private fun NotConfiguredContent() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
      Icon(
        painter = painterResource(R.drawable.group),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(64.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(R.string.listen_together),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = stringResource(R.string.listen_together_not_configured),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

/** A section title in the room, 19sp ExtraBold on the page's 22dp edge. */
@Composable
private fun StageRoomHeading(text: String) {
  Text(
    text = text,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.ExtraBold,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
  )
}

@Composable
private fun JoinCreateRoomSection(
  usernameInput: String,
  onUsernameChange: (String) -> Unit,
  roomCodeInput: String,
  onRoomCodeChange: (String) -> Unit,
  savedUsername: String,
  isJoiningRoom: Boolean,
  isCreatingRoom: Boolean,
  joinErrorMessage: String?,
  bringIntoViewRequester: BringIntoViewRequester,
  onCreateRoom: () -> Unit,
  onJoinRoom: () -> Unit,
  onFieldFocused: () -> Unit = {}
) {
  val hasUsername = usernameInput.trim().isNotBlank() || savedUsername.isNotBlank()
  val hasRoomCode = roomCodeInput.length == 8

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    StageRoomJoinForm(
      roomCode = roomCodeInput,
      onRoomCodeChange = { if (it.length <= 8) onRoomCodeChange(it.uppercase()) },
      username = usernameInput,
      onUsernameChange = onUsernameChange,
      joinEnabled = hasUsername && hasRoomCode && !isCreatingRoom && !isJoiningRoom,
      createEnabled = hasUsername && !isCreatingRoom && !isJoiningRoom,
      isJoining = isJoiningRoom,
      errorMessage = joinErrorMessage,
      onJoin = onJoinRoom,
      onCreate = onCreateRoom,
      modifier = Modifier.padding(14.dp),
      fieldModifier =
        Modifier.bringIntoViewRequester(bringIntoViewRequester).onFocusChanged {
          if (it.isFocused) onFieldFocused()
        },
    )
  }
}

@Composable
private fun SettingsLinkCard(onClick: () -> Unit) {
  Material3SettingsGroup(
    items =
      listOf(
        Material3SettingsItem(
          icon = painterResource(R.drawable.settings),
          title = { Text(stringResource(R.string.settings)) },
          description = { Text(stringResource(R.string.listen_together_settings_desc)) },
          onClick = onClick
        )
      )
  )
}

@Composable
private fun ParticipantControlCard(
  allowParticipantControl: Boolean,
  onAllowParticipantControlChange: (Boolean) -> Unit
) {
  Material3SettingsGroup(
    items =
      listOf(
        Material3SettingsItem(
          title = { Text(stringResource(R.string.listen_together_allow_participant_control)) },
          trailingContent = {
            StageSwitch(checked = allowParticipantControl, onCheckedChange = onAllowParticipantControlChange)
          },
          onClick = { onAllowParticipantControlChange(!allowParticipantControl) }
        )
      )
  )
}
