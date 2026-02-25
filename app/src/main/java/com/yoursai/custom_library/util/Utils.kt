package com.yoursai.custom_library.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Insets
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


object Utils {

    fun getDeviceWidthPx(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            wm?.let {
                val metrics = it.currentWindowMetrics
                val bounds = metrics.bounds
                bounds.width()
            } ?: run {
                // Fallback to legacy method if WindowManager is null
                val res = context.resources
                val dm = res.displayMetrics
                dm.widthPixels
            }
        } else {
            val res = context.resources
            val dm = res.displayMetrics
            dm.widthPixels
        }
    }

    /**
     * Convert dp to px
     *
     * @param res Resources
     * @param dp  The dp value to be converted
     * @return The px value after the conversion is completed
     */
    fun dp2px(res: Resources, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics)
    }

    fun transparentStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.getDecorView().getSystemUiVisibility()
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.getDecorView().setSystemUiVisibility(systemUiVisibility)
        window.setStatusBarColor(Color.TRANSPARENT)
    }

    fun transparentNavigationBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.getDecorView().getSystemUiVisibility()
        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.getDecorView().setSystemUiVisibility(systemUiVisibility)
        window.setNavigationBarColor(Color.TRANSPARENT)
    }

    fun getStatusBarHeight(context: Context): Int {
        @SuppressLint("InternalInsetResource", "DiscouragedApi") val resId =
            context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android"
            )
        return context.getResources().getDimensionPixelSize(resId)
    }

    fun getNavigationBarHeight(view: View): Int {
        val rootWindowInsets = ViewCompat.getRootWindowInsets(view)
        if (rootWindowInsets != null) {
            val navigationBars: androidx.core.graphics.Insets =
                rootWindowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            return navigationBars.bottom
        }
        return 0
    }
}