package com.yoursai.custom_library.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.yoursai.custom_library.R
import com.yoursai.library.liquid.util.Utils
import com.yoursai.library.liquid.widget.LiquidGlassView
import com.yoursai.library.liquid.widget.LiquidTabBar
import com.yoursai.library.liquid.widget.TabBar
import com.yoursai.library.liquid.widget.TabItem
import java.io.IOException
import java.io.InputStream

class LiquidGlassViewActivity : AppCompatActivity() {

    private lateinit var controls: LinearLayout
    private lateinit var liquidGlassView: LiquidGlassView
    private lateinit var tabBar: TabBar
    private lateinit var liquidTabBar: LiquidTabBar

    private lateinit var setCorners: Slider
    private lateinit var setRefractionHeight: Slider
    private lateinit var setRefractionOffset: Slider
    private lateinit var setBlurRadius: Slider
    private lateinit var setDispersion: Slider
    private lateinit var button: Button
    private lateinit var images: ImageView
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) 

        // 使用原生方法替代 EdgeToEdge
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                try {
                    val inputStream = contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    bitmap?.let { images.setImageBitmap(it) }

                    inputStream?.close()
                } catch (ignored: IOException) {
                    // Handle exception if needed
                }
            }
        }

        setContentView(R.layout.activity_liquid_glass_view)

        Utils.transparentStatusBar(window)
        Utils.transparentNavigationBar(window)

        liquidGlassView = findViewById(R.id.liquidGlassView)
        val content: ViewGroup = findViewById(R.id.content_container)

        liquidGlassView.bind(content)
        liquidGlassView.setDraggableEnabled(true)
        liquidGlassView.setElasticEnabled(false)

        initView()
        setView()
    }

    private fun initView() {
        controls = findViewById(R.id.controls)
//        tabBar = findViewById(R.id.tabBar)
        liquidTabBar = findViewById(R.id.liquidTabBar)
        button = findViewById(R.id.button)
        images = findViewById(R.id.images)

        setCorners = findViewById(R.id.setCorner)
        setRefractionHeight = findViewById(R.id.setRefractionHeight)
        setRefractionOffset = findViewById(R.id.setRefractionOffset)
        setBlurRadius = findViewById(R.id.setBlurRadius)
        setDispersion = findViewById(R.id.setDispersion)

        setCorners.valueFrom = 0f
        setCorners.valueTo = 99f
        setCorners.value = 40f

        setRefractionHeight.valueFrom = 12f
        setRefractionHeight.valueTo = 50f
        setRefractionHeight.value = 20f

        setRefractionOffset.valueFrom = 20f
        setRefractionOffset.valueTo = 120f
        setRefractionOffset.value = 70f

        setBlurRadius.valueFrom = 0f
        setBlurRadius.valueTo = 50f
        setBlurRadius.value = 0f

        setDispersion.valueFrom = 0f
        setDispersion.valueTo = 1f
        setDispersion.value = 0.5f
        
//        setupTabBar()
        setupLiquidTabBar()
    }

    private fun setupTabBar() {
        // Customize TabBar appearance
        tabBar.tabBackgroundColor = Color.parseColor("#80000000")
        tabBar.selectedColor = Color.parseColor("#FF4081")
        tabBar.unselectedColor = Color.parseColor("#CCCCCC")
        tabBar.cornerRadius = 30f
        tabBar.animationDuration = 250L

        val tabItems = listOf(
            TabItem(android.R.drawable.ic_menu_camera, "Camera"),
            TabItem(android.R.drawable.ic_menu_gallery, "Gallery"),
            TabItem(android.R.drawable.ic_menu_gallery, "Settings"),
            TabItem(android.R.drawable.ic_menu_info_details, "Info")
        )
        
        tabBar.setItems(tabItems)

        tabBar.setOnTabSelectedListener { index ->
            when (index) {
                0 -> Toast.makeText(this, "Camera tab selected", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(this, "Gallery tab selected", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "Settings tab selected", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "Info tab selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLiquidTabBar() {
//        val backgroundView = ImageView(this).apply {
//            setImageResource(R.drawable.image) // 使用你的图片资源
//            scaleType = ImageView.ScaleType.CENTER_CROP
//        }
//        liquidTabBar.bindBackground(backgroundView)
        liquidTabBar.bindBackground(findViewById(R.id.liquidGlassBackground))
//        liquidTabBar.bindBackground(findViewById(R.id.tabBarBackground))
//        liquidTabBar.bindBackground(null)
        // Customize TabBar appearance
        liquidTabBar.selectedColor = Color.RED
        liquidTabBar.unselectedColor = Color.parseColor("#CCCCCC")
        liquidTabBar.animationDuration = 250L

        val tabItems = listOf(
            TabItem(android.R.drawable.ic_menu_camera, "Camera"),
            TabItem(android.R.drawable.ic_menu_gallery, "Gallery"),
            TabItem(android.R.drawable.ic_menu_gallery, "Settings"),
            TabItem(android.R.drawable.ic_menu_info_details, "Info")
        )

        liquidTabBar.setItems(tabItems)

        liquidTabBar.setOnTabSelectedListener { index ->
            when (index) {
                0 -> Toast.makeText(this, "Camera tab selected", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(this, "Gallery tab selected", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "Settings tab selected", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "Info tab selected", Toast.LENGTH_SHORT).show()
            }
        }
//        // 可选：自定义液态玻璃效果
        liquidTabBar.setLiquidCornerRadius(30f)
        liquidTabBar.setLiquidRefractionHeight(15f)
        liquidTabBar.setLiquidRefractionOffset(50f)
        liquidTabBar.setLiquidBlurRadius(2f)
        liquidTabBar.setLiquidDispersion(0.3f)
        liquidTabBar.setLiquidTintAlpha(0.1f)
    }

    private fun setView() {
        val controlsParams = controls.layoutParams as ViewGroup.MarginLayoutParams
        controlsParams.bottomMargin = Utils.getNavigationBarHeight(findViewById(android.R.id.content))
        controls.layoutParams = controlsParams

        val buttonParams = button.layoutParams as ViewGroup.MarginLayoutParams
        buttonParams.topMargin = (Utils.getStatusBarHeight(this) + Utils.dp2px(resources, 6f)).toInt()
        button.layoutParams = buttonParams

        button.setOnClickListener {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
                pickMedia.launch(
                    PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        .build()
                )
            } else {
                Toast.makeText(this, getString(R.string.a1), Toast.LENGTH_SHORT).show()
            }
        }

        setCorners.addOnChangeListener { slider, v, b ->
            if (b) {
                liquidGlassView.setCornerRadius(Utils.dp2px(resources, v))
            }
        }

        setRefractionHeight.addOnChangeListener { slider, v, b ->
            if (b) {
                liquidGlassView.setRefractionHeight(Utils.dp2px(resources, v))
            }
        }

        setRefractionOffset.addOnChangeListener { slider, v, b ->
            if (b) {
                liquidGlassView.setRefractionOffset(Utils.dp2px(resources, v))
            }
        }

        setBlurRadius.addOnChangeListener { slider, v, b ->
            if (b) {
                liquidGlassView.setBlurRadius(v)
            }
        }

        setDispersion.addOnChangeListener { slider, v, b ->
            if (b) {
                liquidGlassView.setDispersion(v)
            }
        }
    }
}
