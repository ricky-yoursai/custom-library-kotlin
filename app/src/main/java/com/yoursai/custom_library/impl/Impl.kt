package com.yoursai.custom_library.impl

import android.graphics.Canvas

interface Impl {
    fun onSizeChanged(w: Int, h: Int)
    fun onPreDraw()
    fun draw(c: Canvas)
    fun dispose() {}
}