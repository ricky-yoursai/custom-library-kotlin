package com.yoursai.custom_library

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.yoursai.custom_library.util.Utils
import com.yoursai.custom_library.widget.LiquidGlassView

class TouchEffectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用原生方法替代 EdgeToEdge
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContentView(R.layout.activity_touch_effect)
        Utils.transparentStatusBar(window)
        Utils.transparentNavigationBar(window)

        val liquidGlassView: LiquidGlassView = findViewById(R.id.liquid_glass_view)
        val contentContainer: ViewGroup = findViewById(R.id.content_container)

        // Bind content to be blurred
        liquidGlassView.bind(contentContainer)

        // Enable touch effect (iOS style animation)
        liquidGlassView.setTouchEffectEnabled(true)

        // Optional: Configure other properties for better look
        liquidGlassView.setCornerRadius(Utils.dp2px(resources, 40f))
        liquidGlassView.setBlurRadius(15f)
    }
}