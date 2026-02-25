//package com.yoursai.custom_library
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.yoursai.custom_library.ui.theme.Custom_libraryTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            Custom_libraryTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Custom_libraryTheme {
//        Greeting("Android")
//    }
//}


//package com.yoursai.custom_library
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import com.yoursai.custom_library.util.Utils
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 使用原生方法替代 EdgeToEdge
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                )
//        window.statusBarColor = android.graphics.Color.TRANSPARENT
//        window.navigationBarColor = android.graphics.Color.TRANSPARENT
//
//        setContentView(R.layout.activity_main)
//        Utils.transparentStatusBar(window)
//        Utils.transparentNavigationBar(window)
//
//        MaterialAlertDialogBuilder(this)
//            .setTitle("Hello")
//            .setMessage(getString(R.string.a2))
//            .setNegativeButton("OK", null)
//            .show()
//
//        findViewById<android.view.View>(R.id.liquidglassview).setOnClickListener {
//            startActivity(Intent(this, LiquidGlassViewActivity::class.java))
//        }
//        findViewById<android.view.View>(R.id.elasticliquidglassview).setOnClickListener {
//            startActivity(Intent(this, ElasticLiquidGlassViewActivity::class.java))
//        }
//        findViewById<android.view.View>(R.id.toucheffectview).setOnClickListener {
//            startActivity(Intent(this, TouchEffectActivity::class.java))
//        }
//        findViewById<android.view.View>(R.id.github).setOnClickListener {
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/QmDeve/AndroidLiquidGlassView")))
//        }
//    }
//}

package com.yoursai.custom_library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yoursai.custom_library.liquid.activity.LiquidGlassViewActivity
import com.yoursai.custom_library.liquid.activity.ElasticLiquidGlassViewActivity
import com.yoursai.custom_library.liquid.activity.TouchEffectActivity
import com.yoursai.custom_library.liquid.util.Utils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 使用原生方法替代 EdgeToEdge
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            setContentView(R.layout.activity_main)
            Utils.transparentStatusBar(window)
            Utils.transparentNavigationBar(window)

            // 检查 BlurButtonView 兼容性
            checkBlurButtonViewCompatibility()

            MaterialAlertDialogBuilder(this)
                .setTitle("Hello")
                .setMessage(getString(R.string.a2))
                .setNegativeButton("OK", null)
                .show()

            setupClickListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            showErrorDialog("应用启动失败: ${e.message}")
        }
    }

    /**
     * 检查 BlurButtonView 兼容性
     */
    private fun checkBlurButtonViewCompatibility() {
        try {
            // 尝试创建 BlurButtonView 实例来测试原生库是否可用
            val testView = com.qmdeve.blurview.widget.BlurButtonView(this)
            Log.d(TAG, "BlurButtonView is compatible")

            // 如果兼容，隐藏预览按钮，显示真实按钮
            hidePreviewButtons()

        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "BlurButtonView native library not found", e)
            // 如果原生库不可用，显示错误信息并只使用预览按钮
            showErrorDialog(
                "设备不支持 BlurButtonView 功能\n\n" +
                        "原因: 原生库加载失败\n" +
                        "建议: 使用支持的设备或更新应用",
                showOnly = true
            )
            // 保持预览按钮可见，隐藏真实按钮
            keepPreviewButtonsOnly()

        } catch (e: Exception) {
            Log.e(TAG, "BlurButtonView compatibility check failed", e)
            showErrorDialog(
                "BlurButtonView 兼容性检查失败\n\n" +
                        "错误: ${e.message}",
                showOnly = true
            )
            keepPreviewButtonsOnly()
        }
    }

    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        try {
            // 只有当 BlurButtonView 可用时才设置这些监听器
            if (isBlurButtonViewAvailable()) {
                findViewById<View>(R.id.liquidglassview)?.setOnClickListener {
                    startActivity(Intent(this, LiquidGlassViewActivity::class.java))
                }
                findViewById<View>(R.id.elasticliquidglassview)?.setOnClickListener {
                    startActivity(Intent(this, ElasticLiquidGlassViewActivity::class.java))
                }
                findViewById<View>(R.id.toucheffectview)?.setOnClickListener {
                    startActivity(Intent(this, TouchEffectActivity::class.java))
                }
            }

            // GitHub 按钮始终可用（使用预览按钮）
            findViewById<View>(R.id.github_preview)?.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/QmDeve/AndroidLiquidGlassView")))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    /**
     * 检查 BlurButtonView 是否可用
     */
    private fun isBlurButtonViewAvailable(): Boolean {
        return try {
            findViewById<View>(R.id.liquidglassview) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 隐藏预览按钮，显示真实的 BlurButtonView
     */
    private fun hidePreviewButtons() {
        try {
            // 隐藏预览按钮
            findViewById<Button>(R.id.liquidglassview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.elasticliquidglassview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.toucheffectview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.github_preview)?.visibility = View.GONE

            // 显示真实按钮
            findViewById<View>(R.id.liquidglassview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.elasticliquidglassview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.toucheffectview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.github)?.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding preview buttons", e)
        }
    }

    /**
     * 只保持预览按钮可见，隐藏真实按钮
     */
    private fun keepPreviewButtonsOnly() {
        try {
            // 显示预览按钮
            findViewById<Button>(R.id.liquidglassview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.elasticliquidglassview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.toucheffectview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.github_preview)?.visibility = View.VISIBLE

            // 隐藏真实按钮
            findViewById<View>(R.id.liquidglassview)?.visibility = View.GONE
            findViewById<View>(R.id.elasticliquidglassview)?.visibility = View.GONE
            findViewById<View>(R.id.toucheffectview)?.visibility = View.GONE
            findViewById<View>(R.id.github)?.visibility = View.GONE

        } catch (e: Exception) {
            Log.e(TAG, "Error keeping preview buttons only", e)
        }
    }

    /**
     * 显示错误对话框
     */
    private fun showErrorDialog(message: String, showOnly: Boolean = false) {
        try {
            val builder = MaterialAlertDialogBuilder(this)
                .setTitle("错误")
                .setMessage(message)
                .setPositiveButton("确定", null)

            if (!showOnly) {
                builder.setNegativeButton("忽略", null)
            }

            builder.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog", e)
        }
    }
}
