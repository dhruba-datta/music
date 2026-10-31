package echo.music.iad1tya.ui.menu

import echo.music.iad1tya.ui.component.StagePillButton
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import echo.music.iad1tya.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.component.DefaultDialog
import echo.music.iad1tya.ui.component.StageRowValue
import echo.music.iad1tya.ui.component.StageSwitch
import echo.music.iad1tya.viewmodels.ConvertedSongLog
import echo.music.iad1tya.viewmodels.CsvImportState

@Composable
fun CsvColumnMappingDialog(
  isVisible: Boolean,
  csvState: CsvImportState,
  onDismiss: () -> Unit,
  onConfirm: (CsvImportState) -> Unit,
) {
  if (!isVisible) return

  var artistColumnIndex by remember { mutableIntStateOf(csvState.artistColumnIndex) }
  var titleColumnIndex by remember { mutableIntStateOf(csvState.titleColumnIndex) }
  var urlColumnIndex by remember { mutableIntStateOf(csvState.urlColumnIndex) }
  var hasHeader by remember { mutableStateOf(csvState.hasHeader) }

  val maxColumns = csvState.previewRows.firstOrNull()?.size ?: 0
  val previewRow = csvState.previewRows.getOrNull(if (hasHeader) 1 else 0)

  DefaultDialog(
    onDismiss = onDismiss,
    title = { Text(stringResource(R.string.map_csv_columns)) },
    buttons = {
      StagePillButton(onClick = {
          onConfirm(
            CsvImportState(
              previewRows = csvState.previewRows,
              artistColumnIndex = artistColumnIndex,
              titleColumnIndex = titleColumnIndex,
              urlColumnIndex = urlColumnIndex,
              hasHeader = hasHeader,
            )
          )
        }) {
        Text(stringResource(R.string.continue_action))
      }
      StagePillButton(onClick = onDismiss, primary = false) { Text(stringResource(R.string.cancel)) }
    },
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(13.dp),
      modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
      Text(
        text = stringResource(R.string.first_row_is_header),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      StageSwitch(checked = hasHeader, onCheckedChange = { hasHeader = it })
    }

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
      ColumnSelector(
        label = stringResource(R.string.song_title_column),
        selectedIndex = titleColumnIndex,
        maxColumns = maxColumns,
        onSelected = { titleColumnIndex = it },
      )
      ColumnSelector(
        label = stringResource(R.string.artist_name_column),
        selectedIndex = artistColumnIndex,
        maxColumns = maxColumns,
        onSelected = { artistColumnIndex = it },
      )
      ColumnSelector(
        label = stringResource(R.string.youtube_url_column),
        selectedIndex = urlColumnIndex,
        maxColumns = maxColumns,
        allowNone = true,
        onSelected = { urlColumnIndex = it },
      )
    }

    if (previewRow != null) {
      val preview =
        listOfNotNull(previewRow.getOrNull(titleColumnIndex), previewRow.getOrNull(artistColumnIndex))
          .joinToString(", ")
      Text(
        text = "${stringResource(R.string.preview)}: $preview",
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** A setting row that steps through the file's columns each time it is tapped. */
@Composable
private fun ColumnSelector(
  label: String,
  selectedIndex: Int,
  maxColumns: Int,
  allowNone: Boolean = false,
  onSelected: (Int) -> Unit,
) {
  val first = if (allowNone) -1 else 0
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clickable(enabled = maxColumns > 0) {
          onSelected(if (selectedIndex + 1 >= maxColumns) first else selectedIndex + 1)
        }
        .heightIn(min = 54.dp)
        .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Text(
      text = label,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    StageRowValue(
      text =
        if (selectedIndex < 0) stringResource(R.string.none)
        else "Column ${columnLetter(selectedIndex)}"
    )
  }
}

private fun columnLetter(index: Int): String {
  val letter = 'A' + (index % 26)
  return if (index < 26) letter.toString() else "$letter${index / 26}"
}

@Composable
fun CsvImportProgressDialog(
  isVisible: Boolean,
  progress: Int,
  recentLogs: List<ConvertedSongLog>,
  onDismiss: () -> Unit,
) {
  if (!isVisible) return

  Dialog(
    onDismissRequest = onDismiss,
    properties =
      DialogProperties(
        usePlatformDefaultWidth = false,
        dismissOnBackPress = false,
        dismissOnClickOutside = false
      ),
  ) {
    Column(
      modifier =
        Modifier.padding(18.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(28.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = stringResource(R.string.importing_csv),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      LinearProgressIndicator(
        progress = { progress / 100f },
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
      )

      Text(
        text = stringResource(R.string.percentage_format, progress),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (recentLogs.isNotEmpty()) {
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = stringResource(R.string.recently_converted),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          recentLogs.forEach { log ->
            Column(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(MaterialTheme.colorScheme.background)
                  .padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = log.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = log.artists,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}
