package com.yoursai.custom_library.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yoursai.custom_library.R
import com.yoursai.library.liquid.util.Utils
import com.yoursai.library.liquid.widget.LiquidTabBar
import com.yoursai.library.liquid.widget.TabItem

class LiquidTabBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liquid_tab_bar)

        Utils.transparentStatusBar(window)
        Utils.transparentNavigationBar(window)

        val liquidTabBar: LiquidTabBar = findViewById(R.id.liquidTabBar)
        liquidTabBar.bindBackground(findViewById(R.id.pageBackground))

        liquidTabBar.selectedColor = Color.RED
        liquidTabBar.unselectedColor = Color.parseColor("#D9D9D9")
        liquidTabBar.animationDuration = 250L

        liquidTabBar.setItems(
            listOf(
                TabItem(android.R.drawable.ic_menu_camera, "Camera"),
                TabItem(android.R.drawable.ic_menu_gallery, "Gallery"),
                TabItem(android.R.drawable.ic_menu_manage, "Tools"),
                TabItem(android.R.drawable.ic_menu_info_details, "Info")
            )
        )

        liquidTabBar.setOnTabSelectedListener { index ->
            val title = when (index) {
                0 -> "Camera"
                1 -> "Gallery"
                2 -> "Tools"
                else -> "Info"
            }
            Toast.makeText(this, "$title tab selected", Toast.LENGTH_SHORT).show()
        }

        liquidTabBar.setLiquidCornerRadius(Utils.dp2px(resources, 28f))
        liquidTabBar.setLiquidRefractionHeight(Utils.dp2px(resources, 20f))
        liquidTabBar.setLiquidRefractionOffset(Utils.dp2px(resources, 70f))
        liquidTabBar.setLiquidBlurRadius(0.01f)
        liquidTabBar.setLiquidDispersion(0.5f)
        liquidTabBar.setLiquidTintAlpha(0f)
    }
}
