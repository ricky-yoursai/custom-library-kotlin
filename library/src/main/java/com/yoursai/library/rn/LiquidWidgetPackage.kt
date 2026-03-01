package com.yoursai.library.rn

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

// JIT_PACK@ ReactPackage entry for registering Liquid widgets in RN.
class LiquidWidgetPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return emptyList()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(
            LiquidTabBarManager(),
            LiquidGlassViewManager()
        )
    }
}
