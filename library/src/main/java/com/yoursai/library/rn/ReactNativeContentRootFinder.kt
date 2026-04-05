package com.yoursai.library.rn

import android.view.View
import android.view.ViewGroup
import android.view.Window

/**
 * 不直接依赖 react-android 类名常量，避免版本差异；用于 sibling 策略失败时的兜底采样根。
 */
internal object ReactNativeContentRootFinder {

    fun findReactRootView(window: Window?): View? {
        val decor = window?.decorView as? ViewGroup ?: return null
        val content = decor.findViewById<ViewGroup>(android.R.id.content) ?: return null
        return depthFirstReactRoot(content)
    }

    private fun depthFirstReactRoot(v: View): View? {
        if (isReactRootLike(v)) return v
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                depthFirstReactRoot(v.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    private fun isReactRootLike(v: View): Boolean {
        val n = v.javaClass.name
        return n == "com.facebook.react.ReactRootView" ||
            n == "com.facebook.react.runtime.ReactSurfaceView" ||
            (n.contains("ReactSurfaceView")) ||
            (n.contains("ReactRootView") && !n.contains("ReactRootViewTag"))
    }
}
