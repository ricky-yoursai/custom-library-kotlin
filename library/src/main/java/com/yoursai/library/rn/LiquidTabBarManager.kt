package com.yoursai.library.rn

import android.content.Context
import android.graphics.Color
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.yoursai.library.liquid.widget.LiquidTabBar
import com.yoursai.library.liquid.widget.TabItem

// JIT_PACK@ React Native ViewManager for LiquidTabBar widget.
class LiquidTabBarManager : SimpleViewManager<LiquidTabBar>() {
    override fun getName(): String = "LiquidTabBar" // JIT_PACK@ JS component name

    override fun createViewInstance(reactContext: ThemedReactContext): LiquidTabBar {
        val view = LiquidTabBar(reactContext)
        // JIT_PACK@ Bridge native selection events to JS.
        view.setOnTabSelectedListener { index ->
            val event = Arguments.createMap().apply {
                putInt("index", index)
            }
            reactContext.getJSModule(RCTEventEmitter::class.java)
                .receiveEvent(view.id, "onTabSelected", event)
        }
        return view
    }

    @ReactProp(name = "items")
    fun setItems(view: LiquidTabBar, items: ReadableArray?) {
        if (items == null) {
            view.setItems(emptyList())
            return
        }
        val resolved = ArrayList<TabItem>(items.size())
        for (i in 0 until items.size()) {
            val map = items.getMap(i) ?: continue
            val iconResId = resolveIcon(view.context, map)
            if (iconResId == 0) continue
            val title = if (map.hasKey("title") && !map.isNull("title")) {
                map.getString("title")
            } else {
                null
            }
            resolved.add(TabItem(iconResId, title))
        }
        view.setItems(resolved)
    }

    @ReactProp(name = "selectedIndex", defaultInt = 0)
    fun setSelectedIndex(view: LiquidTabBar, index: Int) {
        view.setSelectedIndex(index)
    }

    @ReactProp(name = "selectedColor", customType = "Color")
    fun setSelectedColor(view: LiquidTabBar, color: Int?) {
        if (color != null) {
            view.selectedColor = color
        }
    }

    @ReactProp(name = "unselectedColor", customType = "Color")
    fun setUnselectedColor(view: LiquidTabBar, color: Int?) {
        if (color != null) {
            view.unselectedColor = color
        }
    }

    @ReactProp(name = "animationDuration", defaultInt = 300)
    fun setAnimationDuration(view: LiquidTabBar, durationMs: Int) {
        view.animationDuration = durationMs.toLong()
    }

    @ReactProp(name = "cornerRadius", defaultFloat = -1f)
    fun setCornerRadius(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidCornerRadius(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "refractionHeight", defaultFloat = -1f)
    fun setRefractionHeight(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidRefractionHeight(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "refractionOffset", defaultFloat = -1f)
    fun setRefractionOffset(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidRefractionOffset(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "blurRadius", defaultFloat = -1f)
    fun setBlurRadius(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidBlurRadius(PixelUtil.toPixelFromDIP(value))
        }
    }

    @ReactProp(name = "dispersion", defaultFloat = -1f)
    fun setDispersion(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidDispersion(value)
        }
    }

    @ReactProp(name = "tintAlpha", defaultFloat = -1f)
    fun setTintAlpha(view: LiquidTabBar, value: Float) {
        if (value >= 0f) {
            view.setLiquidTintAlpha(value)
        }
    }

    @ReactProp(name = "tintColor", customType = "Color")
    fun setTintColor(view: LiquidTabBar, color: Int?) {
        if (color == null) return
        view.setLiquidTintColorRed(Color.red(color) / 255f)
        view.setLiquidTintColorGreen(Color.green(color) / 255f)
        view.setLiquidTintColorBlue(Color.blue(color) / 255f)
        view.setLiquidTintAlpha(Color.alpha(color) / 255f)
    }

    @ReactProp(name = "draggableEnabled", defaultBoolean = true)
    fun setDraggableEnabled(view: LiquidTabBar, enabled: Boolean) {
        view.setLiquidDraggableEnabled(enabled)
    }

    @ReactProp(name = "elasticEnabled", defaultBoolean = true)
    fun setElasticEnabled(view: LiquidTabBar, enabled: Boolean) {
        view.setLiquidElasticEnabled(enabled)
    }

    @ReactProp(name = "touchEffectEnabled", defaultBoolean = false)
    fun setTouchEffectEnabled(view: LiquidTabBar, enabled: Boolean) {
        view.setLiquidTouchEffectEnabled(enabled)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return mapOf(
            "onTabSelected" to mapOf("registrationName" to "onTabSelected") // JIT_PACK@ JS event hook
        )
    }

    private fun resolveIcon(context: Context, map: ReadableMap): Int {
        if (!map.hasKey("icon") || map.isNull("icon")) return 0
        return when (map.getType("icon")) {
            ReadableType.Number -> map.getInt("icon")
            ReadableType.String -> resolveDrawableId(context, map.getString("icon"))
            else -> 0
        }
    }

    private fun resolveDrawableId(context: Context, name: String?): Int {
        if (name.isNullOrBlank()) return 0
        val res = context.resources
        val pkg = context.packageName
        var id = res.getIdentifier(name, "drawable", pkg)
        if (id == 0) {
            id = res.getIdentifier(name, "mipmap", pkg)
        }
        return id
    }
}
