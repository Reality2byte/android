package mega.privacy.android.feature.mediaplayer.presentation

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Forces the status bar to use light icons (suitable for dark backgrounds) for as long as
 * this effect stays in composition, then restores the original appearance on dispose.
 */
@Composable
internal fun DarkStatusBarEffect() {
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalLightIcons = insetsController.isAppearanceLightStatusBars

        insetsController.isAppearanceLightStatusBars = false

        onDispose {
            insetsController.isAppearanceLightStatusBars = originalLightIcons
        }
    }
}

/**
 * Forces the navigation bar fully transparent for as long as this effect stays in composition,
 * then restores the original navigation bar appearance on dispose.
 */
@Composable
internal fun TransparentNavigationBarEffect() {
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalNavBarColor = window.navigationBarColor
        val originalLightIcons = insetsController.isAppearanceLightNavigationBars
        val originalContrastEnforced = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced
        } else {
            false
        }

        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        insetsController.isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        onDispose {
            window.navigationBarColor = originalNavBarColor
            insetsController.isAppearanceLightNavigationBars = originalLightIcons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = originalContrastEnforced
            }
        }
    }
}
