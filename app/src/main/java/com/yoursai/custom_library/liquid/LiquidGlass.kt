package com.yoursai.custom_library.liquid

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
import com.yoursai.custom_library.Config
import com.yoursai.custom_library.liquid.impl.Impl
import com.yoursai.custom_library.liquid.impl.LiquidGlassimpl
import java.lang.ref.WeakReference

@SuppressLint("ViewConstructor")
class LiquidGlass(
    context: Context,
    private val config: Config
) : FrameLayout(context) {

    private var impl: Impl? = null
    private var target: ViewGroup? = null
    private var listenerAdded = false

    private class PreDrawListener(liquidGlass: LiquidGlass) : ViewTreeObserver.OnPreDrawListener {
        private val liquidGlassRef = WeakReference(liquidGlass)

        override fun onPreDraw(): Boolean {
            val liquidGlass = liquidGlassRef.get()
            liquidGlass?.impl?.onPreDraw()
            return true
        }
    }

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

    fun init(target: ViewGroup?) {
        this.target?.let { removePreDrawListener() }

        this.target = target

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            impl = LiquidGlassimpl(this, target!!, config)
            addPreDrawListener()
            requestLayout()
            invalidate()
        } else {
            removePreDrawListener()
        }
    }

    private fun init() {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        updateOutlineProvider()
    }

    override fun onDraw(canvas: Canvas) {
        impl?.draw(canvas)
    }

    fun updateParameters() {
        impl?.let {
            it.onPreDraw()
            invalidate()
        }
        updateOutlineProvider()
    }

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

    private fun addPreDrawListener() {
        target?.let { target ->
            if (!listenerAdded) {
                target.viewTreeObserver.addOnPreDrawListener(preDrawListener)
                listenerAdded = true
            }
        }
    }

    private fun removePreDrawListener() {
        target?.let { target ->
            if (listenerAdded) {
                target.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
                listenerAdded = false
            }
        }
    }
}
