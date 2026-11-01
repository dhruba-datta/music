package echo.music.iad1tya.ui.utils

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import echo.music.iad1tya.ui.screens.Screens

fun NavController.backToMain() {
  val mainRoutes = Screens.MainScreens.map { it.route }

  while (
    previousBackStackEntry != null && currentBackStackEntry?.destination?.route !in mainRoutes
  ) {
    popBackStack()
  }
}

/**
 * Opens [screen] at its own top page. Pages opened from the current tab are closed first, so a
 * tab's saved state never brings one of them back in place of the tab.
 */
fun NavController.navigateToTab(screen: Screens, currentTabRoute: String?) {
  if (currentTabRoute == screen.route && popBackStack(screen.route, inclusive = false)) return

  currentTabRoute?.let { popBackStack(it, inclusive = false) }
  navigate(screen.route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}
