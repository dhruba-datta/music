package echo.music.iad1tya.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebViewDatabase
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

suspend fun clearWebAuthSession(context: Context) {
  withContext(Dispatchers.Main.immediate) {
    clearWebAuthStorage(context)
    val cookieManager = CookieManager.getInstance()
    suspendCancellableCoroutine<Unit> { continuation ->
      cookieManager.removeSessionCookies {
        cookieManager.removeAllCookies {
          cookieManager.flush()
          if (continuation.isActive) {
            continuation.resume(Unit)
          }
        }
      }
    }
  }
}

private fun clearWebAuthStorage(context: Context) {
  val appContext = context.applicationContext
  WebStorage.getInstance().deleteAllData()
  WebViewDatabase.getInstance(appContext).apply {
    clearFormData()
    clearHttpAuthUsernamePassword()
    clearUsernamePassword()
  }
}
