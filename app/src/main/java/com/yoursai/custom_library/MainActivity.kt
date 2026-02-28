package com.yoursai.custom_library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yoursai.custom_library.activity.LiquidGlassViewActivity
import com.yoursai.custom_library.activity.ElasticLiquidGlassViewActivity
import com.yoursai.custom_library.activity.TouchEffectActivity
import com.yoursai.custom_library.activity.LiquidTabBarActivity
import com.yoursai.library.liquid.util.Utils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 浣跨敤鍘熺敓鏂规硶鏇夸唬 EdgeToEdge
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

            // 妫€鏌?BlurButtonView 鍏煎鎬?
            checkBlurButtonViewCompatibility()

            MaterialAlertDialogBuilder(this)
                .setTitle("Hello")
                .setMessage(getString(R.string.a2))
                .setNegativeButton("OK", null)
                .show()

            setupClickListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            showErrorDialog("搴旂敤鍚姩澶辫触: ${e.message}")
        }
    }

    /**
     * 妫€鏌?BlurButtonView 鍏煎鎬?
     */
    private fun checkBlurButtonViewCompatibility() {
        try {
            // 灏濊瘯鍒涘缓 BlurButtonView 瀹炰緥鏉ユ祴璇曞師鐢熷簱鏄惁鍙敤
            val testView = com.qmdeve.blurview.widget.BlurButtonView(this)
            Log.d(TAG, "BlurButtonView is compatible")

            // 濡傛灉鍏煎锛岄殣钘忛瑙堟寜閽紝鏄剧ず鐪熷疄鎸夐挳
            hidePreviewButtons()

        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "BlurButtonView native library not found", e)
            // 濡傛灉鍘熺敓搴撲笉鍙敤锛屾樉绀洪敊璇俊鎭苟鍙娇鐢ㄩ瑙堟寜閽?
            showErrorDialog(
                "璁惧涓嶆敮鎸?BlurButtonView 鍔熻兘\n\n" +
                        "鍘熷洜: 鍘熺敓搴撳姞杞藉け璐n" +
                        "寤鸿: 浣跨敤鏀寔鐨勮澶囨垨鏇存柊搴旂敤",
                showOnly = true
            )
            // 淇濇寔棰勮鎸夐挳鍙锛岄殣钘忕湡瀹炴寜閽?
            keepPreviewButtonsOnly()

        } catch (e: Exception) {
            Log.e(TAG, "BlurButtonView compatibility check failed", e)
            showErrorDialog(
                "BlurButtonView 鍏煎鎬ф鏌ュけ璐n\n" +
                        "閿欒: ${e.message}",
                showOnly = true
            )
            keepPreviewButtonsOnly()
        }
    }

    /**
     * 璁剧疆鐐瑰嚮鐩戝惉鍣?
     */
    private fun setupClickListeners() {
        try {
            // 鍙湁褰?BlurButtonView 鍙敤鏃舵墠璁剧疆杩欎簺鐩戝惉鍣?
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
                findViewById<View>(R.id.liquidtabbaractivity)?.setOnClickListener {
                    startActivity(Intent(this, LiquidTabBarActivity::class.java))
                }
            }

            findViewById<View>(R.id.liquidtabbaractivity_preview)?.setOnClickListener {
                startActivity(Intent(this, LiquidTabBarActivity::class.java))
            }

            // GitHub 鎸夐挳濮嬬粓鍙敤锛堜娇鐢ㄩ瑙堟寜閽級
            findViewById<View>(R.id.github_preview)?.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/QmDeve/AndroidLiquidGlassView")))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    /**
     * 妫€鏌?BlurButtonView 鏄惁鍙敤
     */
    private fun isBlurButtonViewAvailable(): Boolean {
        return try {
            findViewById<View>(R.id.liquidglassview) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 闅愯棌棰勮鎸夐挳锛屾樉绀虹湡瀹炵殑 BlurButtonView
     */
    private fun hidePreviewButtons() {
        try {
            // 闅愯棌棰勮鎸夐挳
            findViewById<Button>(R.id.liquidglassview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.elasticliquidglassview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.toucheffectview_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.liquidtabbaractivity_preview)?.visibility = View.GONE
            findViewById<Button>(R.id.github_preview)?.visibility = View.GONE

            // 鏄剧ず鐪熷疄鎸夐挳
            findViewById<View>(R.id.liquidglassview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.elasticliquidglassview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.toucheffectview)?.visibility = View.VISIBLE
            findViewById<View>(R.id.liquidtabbaractivity)?.visibility = View.VISIBLE
            findViewById<View>(R.id.github)?.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding preview buttons", e)
        }
    }

    /**
     * 鍙繚鎸侀瑙堟寜閽彲瑙侊紝闅愯棌鐪熷疄鎸夐挳
     */
    private fun keepPreviewButtonsOnly() {
        try {
            // 鏄剧ず棰勮鎸夐挳
            findViewById<Button>(R.id.liquidglassview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.elasticliquidglassview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.toucheffectview_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.liquidtabbaractivity_preview)?.visibility = View.VISIBLE
            findViewById<Button>(R.id.github_preview)?.visibility = View.VISIBLE

            // 闅愯棌鐪熷疄鎸夐挳
            findViewById<View>(R.id.liquidglassview)?.visibility = View.GONE
            findViewById<View>(R.id.elasticliquidglassview)?.visibility = View.GONE
            findViewById<View>(R.id.toucheffectview)?.visibility = View.GONE
            findViewById<View>(R.id.liquidtabbaractivity)?.visibility = View.GONE
            findViewById<View>(R.id.github)?.visibility = View.GONE

        } catch (e: Exception) {
            Log.e(TAG, "Error keeping preview buttons only", e)
        }
    }

    /**
     * 鏄剧ず閿欒瀵硅瘽妗?
     */
    private fun showErrorDialog(message: String, showOnly: Boolean = false) {
        try {
            val builder = MaterialAlertDialogBuilder(this)
                .setTitle("閿欒")
                .setMessage(message)
                .setPositiveButton("纭畾", null)

            if (!showOnly) {
                builder.setNegativeButton("蹇界暐", null)
            }

            builder.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog", e)
        }
    }
}

