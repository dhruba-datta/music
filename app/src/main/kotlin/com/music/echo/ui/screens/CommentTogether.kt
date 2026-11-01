package echo.music.iad1tya.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.*
import java.text.SimpleDateFormat
import java.util.*

private fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
