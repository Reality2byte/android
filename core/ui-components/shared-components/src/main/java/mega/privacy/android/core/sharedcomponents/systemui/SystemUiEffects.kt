package mega.privacy.android.core.sharedcomponents.systemui

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Locks the hosting activity to portrait for as long as this effect stays in
 * composition, then restores the original orientation on dispose.
 */
@Composable
fun LockPortraitOrientationEffect() {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val original = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            if (original != null) {
                activity.requestedOrientation = original
            }
        }
    }
}

/**
 * Forces the status bar to use light icons (suitable for dark backgrounds) for
 * as long as this effect stays in composition, then restores the original
 * appearance on dispose.
 *
 * Call this on screens that have a dark background but run in a window where
 * [androidx.activity.enableEdgeToEdge] may have set light status-bar icons by
 * default.
 */
@Composable
fun DarkStatusBarEffect() {
    val view = LocalView.current
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalLightStatusBars = insetsController.isAppearanceLightStatusBars

        insetsController.isAppearanceLightStatusBars = false

        onDispose {
            insetsController.isAppearanceLightStatusBars = originalLightStatusBars
        }
    }
}

/**
 * Forces both the status and navigation bars to use light icons (suitable for
 * dark backgrounds) for as long as this effect stays in composition, then
 * restores the original appearance on dispose.
 */
@Composable
fun DarkSystemBarsEffect() {
    val view = LocalView.current
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalLightStatusBars = insetsController.isAppearanceLightStatusBars
        val originalLightNavigationBars = insetsController.isAppearanceLightNavigationBars

        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        onDispose {
            insetsController.isAppearanceLightStatusBars = originalLightStatusBars
            insetsController.isAppearanceLightNavigationBars = originalLightNavigationBars
        }
    }
}

/**
 * Forces the navigation bar fully transparent (including disabling the
 * system's contrast scrim on API 29+) for as long as this effect stays in
 * composition, then restores the original navigation bar appearance on
 * dispose.
 */
@Composable
fun TransparentNavigationBarEffect() {
    val view = LocalView.current
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalNavBarColor = window.navigationBarColor
        val originalLightIcons = insetsController.isAppearanceLightNavigationBars
        val originalContrastEnforced = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced
        } else {
            false
        }

        window.navigationBarColor = Color.TRANSPARENT
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
