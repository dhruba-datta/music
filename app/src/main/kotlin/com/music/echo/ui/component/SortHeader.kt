package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.PlaylistSongSortType

/** The sort link above a list. It opens the "Sort by" sheet, which also holds the sort order. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : Enum<T>> SortHeader(
  sortType: T,
  sortDescending: Boolean,
  crossinline onSortTypeChange: (T) -> Unit,
  crossinline onSortDescendingChange: (Boolean) -> Unit,
  crossinline sortTypeText: (T) -> Int,
  modifier: Modifier = Modifier,
  showDescending: Boolean? = true,
) {
  var sheetVisible by remember { mutableStateOf(false) }

  val displayDescending = showDescending == true && sortType != PlaylistSongSortType.CUSTOM

  Text(
    text = stringResource(sortTypeText(sortType)),
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier.clickable { sheetVisible = true }.padding(vertical = 6.dp),
  )

  if (sheetVisible) {
    ModalBottomSheet(
      onDismissRequest = { sheetVisible = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      StageSheetTitle(stringResource(R.string.sort_by))
      StageSheetGroup {
        enumValues<T>().forEach { type ->
          StageSortRow(
            title = stringResource(sortTypeText(type)),
            selected = sortType == type,
            onClick = {
              onSortTypeChange(type)
              sheetVisible = false
            },
          )
        }
        if (displayDescending) {
          StageSortOrderRow(
            descending = sortDescending,
            onClick = { onSortDescendingChange(!sortDescending) },
          )
        }
      }
    }
  }
}

/** Title of a Stage bottom sheet. */
@Composable
fun StageSheetTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 22.sp,
    fontWeight = FontWeight.ExtraBold,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
  )
}

/** The rounded group of rows inside a Stage bottom sheet. */
@Composable
fun StageSheetGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 24.dp)
        .clip(RoundedCornerShape(18.dp))
  ) {
    content()
  }
}

/** Plain row for a [StageSheetGroup]: an optional leading slot, then a bold title. */
@Composable
fun StageSheetRow(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leading: @Composable () -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp)
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 6.dp),
  ) {
    leading()
    Text(
      text = title,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun StageSortRow(title: String, selected: Boolean, onClick: () -> Unit) {
  StageSheetRow(title = title, onClick = onClick) {
    if (selected) {
      Icon(
        painter = painterResource(R.drawable.check),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
      )
    } else {
      Spacer(Modifier.width(22.dp))
    }
  }
}

@Composable
fun StageSortOrderRow(descending: Boolean, onClick: () -> Unit) {
  val orderName = if (descending) "Descending" else "Ascending"
  StageSheetRow(
    title = orderName,
    onClick = onClick,
    modifier =
      Modifier.semantics {
        stateDescription = orderName
        contentDescription = "Toggle sort order"
      },
  ) {
    Icon(
      imageVector = Icons.Filled.KeyboardArrowDown,
      contentDescription = null,
      modifier = Modifier.size(22.dp).rotate(if (descending) 0f else 180f),
    )
  }
}

/**
 * Section row of the My top / My bottom pages: the current period as the heading and a "Change
 * period" link that opens the radio dialog.
 */
@Composable
fun <T> StagePeriodHeader(
  selected: T,
  options: List<T>,
  label: @Composable (T) -> String,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  var dialogVisible by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 8.dp),
  ) {
    StageSectionTitle(text = label(selected), modifier = Modifier.weight(1f))
    Text(
      text = "Change period",
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.clickable { dialogVisible = true }.padding(vertical = 6.dp),
    )
  }

  if (dialogVisible) {
    Dialog(onDismissRequest = { dialogVisible = false }) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth().padding(22.dp),
        ) {
          Text(
            text = "Period",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Column(
            modifier =
              Modifier.clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
          ) {
            options.forEach { option ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .clickable {
                      onSelect(option)
                      dialogVisible = false
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
              ) {
                StageRadioMark(selected = option == selected)
                Text(
                  text = label(option),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
          Button(
            onClick = { dialogVisible = false },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
          ) {
            Text(
              text = stringResource(R.string.close),
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StageRadioMark(selected: Boolean) {
  Box(
    modifier =
      Modifier.size(22.dp)
        .border(
          width = if (selected) 7.dp else 2.dp,
          color =
            if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
          shape = CircleShape,
        )
  )
}
