package com.yoursai.library.rn

import android.view.View
import android.view.ViewGroup

/**
 * Expo / RN：TabBar 往往与主内容同父，且在子节点中排在后面。
 * [LiquidGlassimpl] 需要对「不包含 TabBar 自身」的子树做 target.draw，优先取同父下第一个子 View。
 */
internal object ReactNativeSiblingBackgroundFinder {

    fun findFirstSiblingBehind(tabBar: View): View? {
        var v: View? = tabBar
        while (v != null) {
            val p = v.parent as? ViewGroup ?: break
            val idx = p.indexOfChild(v)
            if (idx > 0) {
                return p.getChildAt(0)
            }
            v = p as? View
        }
        return null
    }
}
