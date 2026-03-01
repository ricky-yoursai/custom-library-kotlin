package com.yoursai.library.liquid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.yoursai.library.Config
import com.yoursai.library.liquid.impl.Impl
import com.yoursai.library.liquid.impl.LiquidGlassimpl
import java.lang.ref.WeakReference

@SuppressLint("ViewConstructor")
// LiquidGlass：负责在指定背景之上绘制“液态玻璃”效果的容器
class LiquidGlass(
    context: Context,
    private val config: Config
) : FrameLayout(context) {

    private var impl: Impl? = null
    private var target: View? = null
    private var listenerAdded = false

    // 预绘制回调：每帧刷新采样和渲染
    private class PreDrawListener(liquidGlass: LiquidGlass) : ViewTreeObserver.OnPreDrawListener {
        private val liquidGlassRef = WeakReference(liquidGlass)

        override fun onPreDraw(): Boolean {
            val liquidGlass = liquidGlassRef.get()
            liquidGlass?.impl?.onPreDraw()
            return true
        }
    }

    // 圆角裁剪的 OutlineProvider（系统层裁剪）
    private class RoundRectOutlineProvider(private val cornerRadius: Float) : ViewOutlineProvider() {
        override fun getOutline(v: View, o: Outline) {
            o.setRoundRect(0, 0, v.width, v.height, cornerRadius)
        }
    }

    private val preDrawListener = PreDrawListener(this)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        init()
    }

    /**
     * 绑定采样目标 View（背景源）
     */
    fun init(target: View?) {
        this.target?.let { removePreDrawListener() }

        this.target = target

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && target != null) {
            impl = LiquidGlassimpl(this, target, config)
            addPreDrawListener()
            requestLayout()
            invalidate()
        } else {
            impl?.dispose()
            impl = null
            removePreDrawListener()
        }
    }

    private fun init() {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        updateOutlineProvider()
    }

    // 委托给实现类进行绘制
    override fun onDraw(canvas: Canvas) {
        impl?.draw(canvas)
    }

    // 当参数变更时，触发内部刷新
    fun updateParameters() {
        impl?.let {
            it.onPreDraw()
            invalidate()
        }
        updateOutlineProvider()
    }

    // 更新圆角裁剪
    private fun updateOutlineProvider() {
        if (config.CORNER_RADIUS_PX > 0) {
            outlineProvider = RoundRectOutlineProvider(config.CORNER_RADIUS_PX)
            clipToOutline = true
            invalidateOutline()
        } else {
            outlineProvider = null
            clipToOutline = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        impl?.onSizeChanged(w, h)
        updateOutlineProvider()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addPreDrawListener()
    }

    override fun onDetachedFromWindow() {
        removePreDrawListener()
        impl?.dispose()
        super.onDetachedFromWindow()
    }

    // 添加预绘制监听，确保每帧更新采样
    private fun addPreDrawListener() {
        target?.let { target ->
            if (!listenerAdded) {
                target.viewTreeObserver.addOnPreDrawListener(preDrawListener)
                listenerAdded = true
            }
        }
    }

    // 移除预绘制监听，避免泄漏
    private fun removePreDrawListener() {
        target?.let { target ->
            if (listenerAdded) {
                target.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
                listenerAdded = false
            }
        }
    }
}
