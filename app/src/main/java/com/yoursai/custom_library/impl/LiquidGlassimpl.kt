package com.yoursai.custom_library.impl


import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.yoursai.custom_library.Config
import com.yoursai.custom_library.R
import org.intellij.lang.annotations.Language
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.max

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
class LiquidGlassimpl(
    private val host: View,
    private val target: View,
    private val config: Config
) : Impl {

    private val node = RenderNode("AndroidLiquidGlassView")
    private var cachedBlurEffect: RenderEffect? = null
    private val tp = IntArray(2)
    private val hp = IntArray(2)
    private val liquidShader: RuntimeShader

    private var lastCornerRadius = Float.NaN
    private var lastEccentricFactor = Float.NaN
    private var lastRefractionHeight = Float.NaN
    private var lastRefractionAmount = Float.NaN
    private var lastContrast = Float.NaN
    private var lastWhitePoint = Float.NaN
    private var lastChromaMultiplier = Float.NaN
    private var lastSigma = Float.NaN
    private var lastChromaticAberration = Float.NaN
    private var lastDepthEffect = Float.NaN
    private var lastBlurLevel = Float.NaN
    private var lastTintRed = Float.NaN
    private var lastTintGreen = Float.NaN
    private var lastTintBlue = Float.NaN
    private var lastTintAlpha = Float.NaN

    private var needsUpdate = true
    private var lastBlurUpdateTime = 0L

    init {
        liquidShader = loadAgsl(target.resources, R.raw.liquidglass_effect)
        host.post { applyRenderEffect() }
    }

    override fun onSizeChanged(w: Int, h: Int) {
        node.setPosition(0, 0, w, h)
        record()
        applyRenderEffect()
    }

    override fun onPreDraw() {
        record()

        val cornerRadius = config.CORNER_RADIUS_PX
        val eccentricFactor = config.ECCENTRIC_FACTOR
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
                lastEccentricFactor != eccentricFactor ||
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
            lastEccentricFactor = eccentricFactor
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

    private fun record() {
        val w = target.width
        val h = target.height
        if (w == 0 || h == 0) return

        val rec = node.beginRecording(w, h)
        target.getLocationInWindow(tp)
        host.getLocationInWindow(hp)
        rec.translate(-(hp[0] - tp[0]).toFloat(), -(hp[1] - tp[1]).toFloat())
        target.draw(rec)
        node.endRecording()
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return
        canvas.drawRenderNode(node)
    }

    private fun applyRenderEffect() {
        val width = target.width
        val height = target.height
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
        val cornerRadii = floatArrayOf(
            cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx
        )

        var contentEffect: RenderEffect? = null
        if (blurLevel > 0.01f) {
            val now = System.currentTimeMillis()
            if (cachedBlurEffect == null || abs(blurLevel - lastSigma) > 0.3f || now - lastBlurUpdateTime > 120) {
                try {
                    contentEffect = RenderEffect.createBlurEffect(blurLevel, blurLevel, Shader.TileMode.CLAMP)
                    cachedBlurEffect = contentEffect
                    lastSigma = blurLevel
                    lastBlurUpdateTime = now
                } catch (e: Exception) {
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

    private fun loadAgsl(resources: Resources, resourceId: Int): RuntimeShader {
        @Language("AGSL")
        val shaderCode = loadRaw(resources, resourceId)
        return RuntimeShader(shaderCode)
    }

    private fun loadRaw(resources: Resources, resourceId: Int): String {
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