package com.yoursai.library.liquid.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import com.yoursai.library.Config
import com.yoursai.library.R
import org.intellij.lang.annotations.Language
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.max

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
class WindowPixelCopyLiquidGlassImpl(
    private val host: View,
    private val window: Window,
    private val config: Config
) : Impl {

    private val node = RenderNode("AndroidLiquidGlassWindowPixelCopy")
    private val liquidShader: RuntimeShader
    private val hostLocation = IntArray(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var windowBitmap: Bitmap? = null
    private var copyInFlight = false
    private var lastCopyTime = 0L
    private var cachedBlurEffect: RenderEffect? = null
    private var lastSigma = Float.NaN
    private var lastBlurUpdateTime = 0L
    private var needsUpdate = true

    private var lastCornerRadius = Float.NaN
    private var lastRefractionHeight = Float.NaN
    private var lastRefractionAmount = Float.NaN
    private var lastContrast = Float.NaN
    private var lastWhitePoint = Float.NaN
    private var lastChromaMultiplier = Float.NaN
    private var lastBlurLevel = Float.NaN
    private var lastChromaticAberration = Float.NaN
    private var lastDepthEffect = Float.NaN
    private var lastTintRed = Float.NaN
    private var lastTintGreen = Float.NaN
    private var lastTintBlue = Float.NaN
    private var lastTintAlpha = Float.NaN

    init {
        liquidShader = loadAgsl(host.resources, R.raw.liquidglass_effect)
        host.post {
            requestPixelCopy(force = true)
            applyRenderEffect()
        }
    }

    override fun onSizeChanged(w: Int, h: Int) {
        node.setPosition(0, 0, w, h)
        requestPixelCopy(force = true)
        recordFromBitmap()
        applyRenderEffect()
    }

    override fun onPreDraw() {
        recordFromBitmap()
        requestPixelCopy(force = false)

        val cornerRadius = config.CORNER_RADIUS_PX
        val refractionHeight = config.REFRACTION_HEIGHT
        val refractionAmount = config.REFRACTION_OFFSET
        val contrast = config.CONTRAST
        val whitePoint = config.WHITE_POINT
        val chromaMultiplier = config.CHROMA_MULTIPLIER
        val blurLevel = config.BLUR_RADIUS
        val chromaticAberration = config.DISPERSION
        val depthEffect = config.DEPTH_EFFECT
        val tintRed = config.TINT_COLOR_RED
        val tintGreen = config.TINT_COLOR_GREEN
        val tintBlue = config.TINT_COLOR_BLUE
        val tintAlpha = config.TINT_ALPHA

        val paramsChanged = lastCornerRadius != cornerRadius ||
            lastRefractionHeight != refractionHeight ||
            lastRefractionAmount != refractionAmount ||
            lastContrast != contrast ||
            lastWhitePoint != whitePoint ||
            lastChromaMultiplier != chromaMultiplier ||
            lastBlurLevel != blurLevel ||
            lastChromaticAberration != chromaticAberration ||
            lastDepthEffect != depthEffect ||
            lastTintRed != tintRed ||
            lastTintGreen != tintGreen ||
            lastTintBlue != tintBlue ||
            lastTintAlpha != tintAlpha ||
            needsUpdate

        if (paramsChanged) {
            lastCornerRadius = cornerRadius
            lastRefractionHeight = refractionHeight
            lastRefractionAmount = refractionAmount
            lastContrast = contrast
            lastWhitePoint = whitePoint
            lastChromaMultiplier = chromaMultiplier
            lastBlurLevel = blurLevel
            lastChromaticAberration = chromaticAberration
            lastDepthEffect = depthEffect
            lastTintRed = tintRed
            lastTintGreen = tintGreen
            lastTintBlue = tintBlue
            lastTintAlpha = tintAlpha
            needsUpdate = false
            applyRenderEffect()
        }
    }

    override fun draw(c: Canvas) {
        if (!c.isHardwareAccelerated) return
        c.drawRenderNode(node)
    }

    override fun dispose() {
        copyInFlight = false
        windowBitmap?.recycle()
        windowBitmap = null
    }

    private fun requestPixelCopy(force: Boolean) {
        val decorView = window.decorView ?: return
        val copyWidth = decorView.width
        val copyHeight = decorView.height
        if (copyWidth <= 0 || copyHeight <= 0 || copyInFlight) return

        val now = System.currentTimeMillis()
        if (!force && now - lastCopyTime < 33) return

        val bitmap = ensureBitmap(copyWidth, copyHeight)
        copyInFlight = true
        lastCopyTime = now

        PixelCopy.request(window, bitmap, { result ->
            copyInFlight = false
            if (result == PixelCopy.SUCCESS) {
                recordFromBitmap()
                host.invalidate()
            }
        }, mainHandler)
    }

    private fun ensureBitmap(width: Int, height: Int): Bitmap {
        val current = windowBitmap
        if (current != null && current.width == width && current.height == height && !current.isRecycled) {
            return current
        }

        current?.recycle()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            windowBitmap = it
        }
    }

    private fun recordFromBitmap() {
        val bitmap = windowBitmap ?: return
        val width = host.width
        val height = host.height
        if (width <= 0 || height <= 0) return

        val recordingCanvas = node.beginRecording(width, height)
        host.getLocationInWindow(hostLocation)
        recordingCanvas.translate(-hostLocation[0].toFloat(), -hostLocation[1].toFloat())
        recordingCanvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
        node.endRecording()
    }

    private fun applyRenderEffect() {
        val width = host.width
        val height = host.height
        if (width == 0 || height == 0) return

        val cornerRadiusPx = config.CORNER_RADIUS_PX
        val refractionHeight = config.REFRACTION_HEIGHT
        val refractionAmount = config.REFRACTION_OFFSET
        val contrast = config.CONTRAST
        val whitePoint = config.WHITE_POINT
        val chromaMultiplier = config.CHROMA_MULTIPLIER
        val blurLevel = max(0f, config.BLUR_RADIUS)
        val chromaticAberration = config.DISPERSION
        val depthEffect = config.DEPTH_EFFECT
        val tintRed = config.TINT_COLOR_RED
        val tintGreen = config.TINT_COLOR_GREEN
        val tintBlue = config.TINT_COLOR_BLUE
        val tintAlpha = config.TINT_ALPHA
        val size = floatArrayOf(config.WIDTH.toFloat(), config.HEIGHT.toFloat())
        val offset = floatArrayOf(0f, 0f)
        val cornerRadii = floatArrayOf(cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx)

        var contentEffect: RenderEffect? = null
        if (blurLevel > 0.01f) {
            val now = System.currentTimeMillis()
            if (cachedBlurEffect == null || abs(blurLevel - lastSigma) > 0.3f || now - lastBlurUpdateTime > 120) {
                try {
                    contentEffect = RenderEffect.createBlurEffect(blurLevel, blurLevel, Shader.TileMode.CLAMP)
                    cachedBlurEffect = contentEffect
                    lastSigma = blurLevel
                    lastBlurUpdateTime = now
                } catch (_: Exception) {
                    contentEffect = cachedBlurEffect
                }
            } else {
                contentEffect = cachedBlurEffect
            }
        }

        liquidShader.setFloatUniform("size", size)
        liquidShader.setFloatUniform("offset", offset)
        liquidShader.setFloatUniform("cornerRadii", cornerRadii)
        liquidShader.setFloatUniform("refractionHeight", refractionHeight)
        liquidShader.setFloatUniform("refractionAmount", refractionAmount)
        liquidShader.setFloatUniform("depthEffect", depthEffect)
        liquidShader.setFloatUniform("chromaticAberration", chromaticAberration)
        liquidShader.setFloatUniform("contrast", contrast)
        liquidShader.setFloatUniform("whitePoint", whitePoint)
        liquidShader.setFloatUniform("chromaMultiplier", chromaMultiplier)
        liquidShader.setFloatUniform("tintColor", floatArrayOf(tintRed, tintGreen, tintBlue))
        liquidShader.setFloatUniform("tintAlpha", tintAlpha)

        val shaderEffect = RenderEffect.createRuntimeShaderEffect(liquidShader, "content")
        val finalEffect = contentEffect?.let {
            RenderEffect.createChainEffect(shaderEffect, it)
        } ?: shaderEffect

        node.setRenderEffect(finalEffect)
    }

    private fun loadAgsl(resources: android.content.res.Resources, resourceId: Int): RuntimeShader {
        @Language("AGSL")
        val shaderCode = loadRaw(resources, resourceId)
        return RuntimeShader(shaderCode)
    }

    private fun loadRaw(resources: android.content.res.Resources, resourceId: Int): String {
        return try {
            resources.openRawResource(resourceId).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append('\n')
                    }
                    sb.toString()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Error loading shader: $resourceId", e)
        }
    }
}
