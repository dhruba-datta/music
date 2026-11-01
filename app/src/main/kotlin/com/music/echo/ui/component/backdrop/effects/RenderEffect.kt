/*
 * Vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 * Vendored so the library ships as source with this app (binary AARs compiled
 * against older Compose broke at runtime) and to add a backdrop resolution
 * scale for cheaper effect rendering. KMP expect/actual declarations were
 * merged into this single Android source set. Package renamed accordingly.
 */
package echo.music.iad1tya.ui.component.backdrop.effects

import androidx.compose.ui.graphics.RenderEffect
import echo.music.iad1tya.ui.component.backdrop.BackdropEffectScope
import echo.music.iad1tya.ui.component.backdrop.internal.chain
import echo.music.iad1tya.ui.component.backdrop.isRenderEffectSupported

fun BackdropEffectScope.effect(effect: RenderEffect) {
  if (!isRenderEffectSupported()) return

  renderEffect = renderEffect.chain(effect)
}
