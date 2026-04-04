package com.yoursai.library.rn

import android.graphics.Color
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.yoursai.library.liquid.widget.LiquidGlassView

// JIT_PACK@ React Native ViewManager for LiquidGlassView widget.
class LiquidGlassViewManager : SimpleViewManager<LiquidGlassView>() {
    override fun getName(): String = "LiquidGlassView" // JIT_PACK@ JS component name

    override fun createViewInstance(reactContext: ThemedReactContext): LiquidGlassView {
        return LiquidGlassView(reactContext)
    }

    @ReactProp(name = "cornerRadius", defaultFloat = -1f)
    fun setCornerRadius(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setCornerRadius(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "refractionHeight", defaultFloat = -1f)
    fun setRefractionHeight(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setRefractionHeight(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "refractionOffset", defaultFloat = -1f)
    fun setRefractionOffset(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setRefractionOffset(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "blurRadius", defaultFloat = -1f)
    fun setBlurRadius(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setBlurRadius(value)
        }
    }

    @ReactProp(name = "dispersion", defaultFloat = -1f)
    fun setDispersion(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setDispersion(value)
        }
    }

    @ReactProp(name = "tintAlpha", defaultFloat = -1f)
    fun setTintAlpha(view: LiquidGlassView, value: Float) {
        if (value >= 0f) {
            view.setTintAlpha(value)
        }
    }

    @ReactProp(name = "tintColor", customType = "Color")
    fun setTintColor(view: LiquidGlassView, color: Int?) {
        if (color == null) return
        view.setTintColorRed(Color.red(color) / 255f)
        view.setTintColorGreen(Color.green(color) / 255f)
        view.setTintColorBlue(Color.blue(color) / 255f)
    }

    @ReactProp(name = "draggableEnabled", defaultBoolean = false)
    fun setDraggableEnabled(view: LiquidGlassView, enabled: Boolean) {
        view.setDraggableEnabled(enabled)
    }

    @ReactProp(name = "elasticEnabled", defaultBoolean = false)
    fun setElasticEnabled(view: LiquidGlassView, enabled: Boolean) {
        view.setElasticEnabled(enabled)
    }

    @ReactProp(name = "touchEffectEnabled", defaultBoolean = false)
    fun setTouchEffectEnabled(view: LiquidGlassView, enabled: Boolean) {
        view.setTouchEffectEnabled(enabled)
    }

    @ReactProp(name = "bindToDefaultBackground", defaultBoolean = false)
    fun bindToDefaultBackground(view: LiquidGlassView, enabled: Boolean) {
        if (enabled) {
            val reactContext = view.context as? ThemedReactContext
            val window = reactContext?.currentActivity?.window
            view.post {
                if (window != null) {
                    view.bindToActivityWindow(window)
                } else {
                    view.bindToDefaultBackground()
                }
            }
        }
    }
}
