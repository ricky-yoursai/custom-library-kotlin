package com.yoursai.custom_library.liquid.activity


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yoursai.custom_library.R
import com.yoursai.custom_library.liquid.util.Utils
import com.yoursai.custom_library.liquid.widget.LiquidGlassView
import java.io.IOException
import java.io.InputStream

class ElasticLiquidGlassViewActivity : AppCompatActivity() {

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
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

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

        setContentView(R.layout.activity_elastic_liquid_glass_view)
        Utils.transparentStatusBar(window)
        Utils.transparentNavigationBar(window)

        val button: Button = findViewById(R.id.button)
        images = findViewById(R.id.images)

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

        val liquidGlassView: LiquidGlassView = findViewById(R.id.liquidGlassView)
        val content: ViewGroup = findViewById(R.id.content_container)

        liquidGlassView.bind(content)
        liquidGlassView.setDraggableEnabled(true)
        liquidGlassView.setElasticEnabled(true)
    }
}
