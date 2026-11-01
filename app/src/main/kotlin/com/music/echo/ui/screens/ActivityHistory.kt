/** Echo Music Project (C) 2026 Licensed under GPL-3.0 | See git history for contributors */
package echo.music.iad1tya.ui.screens

/** Converts milliseconds to a human-readable "Xh Ym" string */
private fun formatDuration(ms: Long): String {
  if (ms <= 0L) return "0m"
  val totalMinutes = ms / 60_000
  val hours = totalMinutes / 60
  val minutes = totalMinutes % 60
  return when {
    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
    hours > 0 -> "${hours}h"
    else -> "${minutes}m"
  }
}
