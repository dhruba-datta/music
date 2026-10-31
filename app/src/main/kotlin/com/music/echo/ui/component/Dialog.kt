package echo.music.iad1tya.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import echo.music.iad1tya.R
import kotlinx.coroutines.delay

@Composable
fun DefaultDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  icon: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
  buttons: (@Composable ColumnScope.() -> Unit)? = null,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable ColumnScope.() -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.padding(18.dp),
      shape = StageDialogShape,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(22.dp),
      ) {
        if (icon != null) {
          CompositionLocalProvider(
            LocalContentColor provides AlertDialogDefaults.iconContentColor
          ) {
            Box(Modifier.align(Alignment.CenterHorizontally)) { icon() }
          }
        }
        if (title != null) {
          CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface
          ) {
            ProvideTextStyle(StageDialogTitleStyle) {
              Box(
                Modifier.align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally)
              ) {
                title()
              }
            }
          }
        }

        content()

        if (buttons != null) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            buttons()
          }
        }
      }
    }
  }
}

/** A short message dialog in the Stage card, with its buttons stacked as pills. */
@Composable
fun StageAlertDialog(
  onDismissRequest: () -> Unit,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  dismissButton: (@Composable () -> Unit)? = null,
  icon: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
  text: (@Composable () -> Unit)? = null,
) {
  DefaultDialog(
    onDismiss = onDismissRequest,
    modifier = modifier,
    icon = icon,
    title = title,
    buttons = {
      confirmButton()
      dismissButton?.invoke()
    },
  ) {
    text?.let { message ->
      ProvideTextStyle(
        TextStyle(
          fontSize = 14.sp,
          lineHeight = 20.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      ) {
        message()
      }
    }
  }
}

private val StageDialogShape = RoundedCornerShape(28.dp)

private val StageDialogTitleStyle =
  TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPromptDialog(
  title: String? = null,
  titleBar: @Composable (RowScope.() -> Unit)? = null,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  onReset: (() -> Unit)? = null,
  onCancel: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit = {}
) {
  DefaultDialog(
    onDismiss = onDismiss,
    title =
      if (titleBar != null) {
        { Row { titleBar() } }
      } else if (title != null) {
        {
          Text(
            text = title,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
        }
      } else null,
    buttons = {
      StageButton(text = stringResource(android.R.string.ok), onClick = { onConfirm() })

      if (onCancel != null) {
        StageButton(
          text = stringResource(android.R.string.cancel),
          onClick = { onCancel() },
          primary = false,
        )
      }

      if (onReset != null) {
        StageButton(text = stringResource(R.string.reset), onClick = { onReset() }, primary = false)
      }
    }
  ) {
    content()
  }
}

@Composable
fun ListDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceContainerHigh,
  title: (@Composable () -> Unit)? = null,
  content: LazyListScope.() -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.padding(18.dp),
      shape = StageDialogShape,
      color = color,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.imePadding(),
      ) {
        if (title != null) {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 8.dp)
          ) {
            ProvideTextStyle(StageDialogTitleStyle) { title() }
          }
        }
        LazyColumn(
          contentPadding = PaddingValues(top = if (title != null) 0.dp else 22.dp, bottom = 22.dp),
          content = content
        )
      }
    }
  }
}

@Composable
fun InfoLabel(text: String) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 8.dp)
  ) {
    Icon(
      painter = painterResource(id = R.drawable.info),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.secondary,
      modifier = Modifier.padding(4.dp)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 4.dp)
    )
  }

/** The filled, borderless look of a Stage text field on a dialog. */
@Composable
fun stageFieldColors() =
  OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
  )

@Composable
fun TextFieldDialog(
  modifier: Modifier = Modifier,
  icon: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
  initialTextFieldValue: TextFieldValue = TextFieldValue(),
  placeholder: @Composable (() -> Unit)? = null,
  singleLine: Boolean = true,
  autoFocus: Boolean = true,
  maxLines: Int = if (singleLine) 1 else 10,
  isInputValid: (String) -> Boolean = { it.isNotEmpty() },
  keyboardType: KeyboardType = KeyboardType.Text,
  onDone: (String) -> Unit = {},
  onReset: (() -> Unit)? = null,
  resetText: String? = null,
  textFields: List<Pair<String, TextFieldValue>>? = null,
  onTextFieldsChange: ((Int, TextFieldValue) -> Unit)? = null,
  onDoneMultiple: ((List<String>) -> Unit)? = null,
  onDismiss: () -> Unit,
  autoDismiss: Boolean = true,
  extraContent: (@Composable () -> Unit)? = null,
) {
  val legacyFieldState = remember { mutableStateOf(initialTextFieldValue) }

  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    if (autoFocus) {
      delay(300)
      focusRequester.requestFocus()
    }
  }

  DefaultDialog(
    onDismiss = onDismiss,
    modifier = modifier,
    icon = icon,
    title = title,
    buttons = {
      val isValid =
        textFields?.all { isInputValid(it.second.text) }
          ?: isInputValid(legacyFieldState.value.text)

      StagePillButton(
        enabled = isValid,
        onClick = {
          if (autoDismiss) onDismiss()
          if (textFields != null && onDoneMultiple != null) {
            onDoneMultiple(textFields.map { it.second.text })
          } else {
            onDone(legacyFieldState.value.text)
          }
        }
      ) {
        Text(text = stringResource(android.R.string.ok))
      }

      StageButton(
        text = stringResource(android.R.string.cancel),
        onClick = onDismiss,
        primary = false,
      )

      if (onReset != null) {
        StageButton(
          text = resetText ?: stringResource(R.string.clear),
          onClick = {
            if (autoDismiss) onDismiss()
            onReset()
          },
          primary = false,
        )
      }
    }
  ) {
    Column(modifier = Modifier.weight(weight = 1f, fill = false)) {
      if (textFields != null) {
        textFields.forEachIndexed { index, (label, value) ->
          OutlinedTextField(
            value = value,
            onValueChange = { onTextFieldsChange?.invoke(index, it) },
            placeholder = { Text(label) },
            singleLine = singleLine,
            maxLines = maxLines,
            colors = stageFieldColors(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions =
              KeyboardOptions(
                imeAction = if (singleLine) ImeAction.Done else ImeAction.None,
                keyboardType = keyboardType
              ),
            keyboardActions =
              KeyboardActions(
                onDone = {
                  if (onDoneMultiple != null) {
                    onDoneMultiple(textFields.map { it.second.text })
                    if (autoDismiss) onDismiss()
                  }
                }
              ),
            modifier =
              Modifier.fillMaxWidth()
                .padding(bottom = if (index < textFields.size - 1) 12.dp else 0.dp)
                .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier)
          )
        }
      } else {
        OutlinedTextField(
          value = legacyFieldState.value,
          onValueChange = { legacyFieldState.value = it },
          placeholder = placeholder,
          singleLine = singleLine,
          maxLines = maxLines,
          colors = stageFieldColors(),
          shape = RoundedCornerShape(24.dp),
          keyboardOptions =
            KeyboardOptions(
              imeAction = if (singleLine) ImeAction.Done else ImeAction.None,
              keyboardType = keyboardType
            ),
          keyboardActions =
            KeyboardActions(
              onDone = {
                onDone(legacyFieldState.value.text)
                if (autoDismiss) onDismiss()
              }
            ),
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        )
      }

      extraContent?.invoke()
    }
  }
}
