package com.yoursai.custom_library.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import java.io.IOException

class LiquidGlassViewActivity : AppCompatActivity() {

    private lateinit var controls: LinearLayout
    private lateinit var liquidGlassView: LiquidGlassView

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
