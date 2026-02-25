package com.yoursai.custom_library.liquid.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.yoursai.custom_library.Config
import com.yoursai.custom_library.liquid.LiquidGlass
import com.yoursai.custom_library.liquid.util.LiquidTracker
import com.yoursai.custom_library.liquid.util.Utils

class LiquidGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var glass: LiquidGlass? = null
    private var customSource: ViewGroup? = null
    private val context: Context = context

    private var cornerRadius = Utils.dp2px(resources, 40f)
    private var refractionHeight = Utils.dp2px(resources, 20f)
    private var refractionOffset = -Utils.dp2px(resources, 70f)
    private var tintAlpha = 0.0f
    private var tintColorRed = 1.0f
    private var tintColorGreen = 1.0f
    private var tintColorBlue = 1.0f
    private var blurRadius = 0.01f
    private var dispersion = 0.5f
    private var downX = 0f
    private var downY = 0f
    private var startTx = 0f
    private var startTy = 0f

    private var draggableEnabled = false
    private var elasticEnabled = false
    private var touchEffectEnabled = false
    private var config: Config? = null
    private var liquidTracker: LiquidTracker? = null

    // Glow effect variables
    private lateinit var glowPaint: Paint
    private var glowX = 0f
    private var glowY = 0f
    private var isTouching = false

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        init()
    }

    private fun init() {
        clipToPadding = false
        clipChildren = false
        liquidTracker = LiquidTracker(this)

        glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (touchEffectEnabled && isTouching) {
            val path = Path()
            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)

            canvas.save()
            canvas.clipPath(path)

            val radius = maxOf(width, height) * 0.8f
            val colors = intArrayOf(Color.argb(60, 255, 255, 255), Color.TRANSPARENT)
            val stops = floatArrayOf(0f, 1f)
            val gradient = RadialGradient(glowX, glowY, radius, colors, stops, Shader.TileMode.CLAMP)
            glowPaint.shader = gradient
            canvas.drawRect(rect, glowPaint)

            canvas.restore()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Layout all child views
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                // Layout children to fill the entire view
                child.layout(0, 0, width, height)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Measure all child views
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    /**
     * Bind sampling source
     *
     * @param source ViewGroup
     */
    fun bind(source: ViewGroup?) {
        this.customSource = source
        if (glass != null && source != null) {
            glass!!.init(source)
        }
    }

    /**
     * Set the corner radius px
     *
     * @param px float
     */
    fun setCornerRadius(px: Float) {
        val maxPx = if (height > 0) height / 2f else Utils.dp2px(resources, 99f)
        cornerRadius = maxOf(0f, minOf(px, maxPx))
        updateConfig()
    }

    /**
     * Set the refraction height px
     *
     * @param px float
     */
    fun setRefractionHeight(px: Float) {
        val minPx = Utils.dp2px(resources, 12f)
        val maxPx = Utils.dp2px(resources, 50f)
        refractionHeight = maxOf(minPx, minOf(maxPx, px))
        updateConfig()
    }

    /**
     * Set the refraction offset px
     * Positive value will be converted to negative
     *
     * @param px float
     */
    fun setRefractionOffset(px: Float) {
        val minPx = Utils.dp2px(resources, 20f)
        val maxPx = Utils.dp2px(resources, 120f)
        val adjustedPx = maxOf(minPx, minOf(maxPx, px))
        refractionOffset = -adjustedPx
        updateConfig()
    }

    /**
     * Set the tint color (R)
     *
     * @param red float (0f-1f)
     */
    fun setTintColorRed(red: Float) {
        tintColorRed = red
        updateConfig()
    }

    /**
     * Set the tint color (G)
     *
     * @param green float (0f-1f)
     */
    fun setTintColorGreen(green: Float) {
        tintColorGreen = green
        updateConfig()
    }

    /**
     * Set the tint color (B)
     *
     * @param blue float (0f-1f)
     */
    fun setTintColorBlue(blue: Float) {
        tintColorBlue = blue
        updateConfig()
    }

    /**
     * Set the tint Alpha
     *
     * @param alpha float (0f-1f)
     */
    fun setTintAlpha(alpha: Float) {
        tintAlpha = alpha
        updateConfig()
    }

    /**
     * Set dispersion
     *
     * @param dispersion float (0f-1f)
     */
    fun setDispersion(dispersion: Float) {
        this.dispersion = maxOf(0f, minOf(1f, dispersion))
        updateConfig()
    }

    /**
     * Set the blur radius
     *
     * @param radius float
     */
    fun setBlurRadius(radius: Float) {
        blurRadius = maxOf(0.01f, minOf(50f, radius))
        updateConfig()
    }

    /**
     * Set whether the View is draggable or not
     *
     * @param enabled boolean
     */
    fun setDraggableEnabled(enabled: Boolean) {
        draggableEnabled = enabled
        if (!enabled) {
            liquidTracker?.recycle()
        }
    }

    /**
     * Set whether elastic effect is needed or not
     * @param enabled boolean
     */
    fun setElasticEnabled(enabled: Boolean) {
        elasticEnabled = enabled
        if (!enabled) {
            liquidTracker?.recycle()
        }
    }

    /**
     * Set whether the touch effect (iOS style press animation) is enabled
     * @param enabled boolean
     */
    fun setTouchEffectEnabled(enabled: Boolean) {
        touchEffectEnabled = enabled
    }

    private fun updateConfig() {
        if (glass == null) {
            rebuild()
            return
        }

        var w = width
        var h = height
        if (w <= 0) w = Utils.getDeviceWidthPx(context)
        if (h <= 0) h = resources.displayMetrics.heightPixels

        config?.let {
            it.CORNER_RADIUS_PX = cornerRadius
            it.REFRACTION_HEIGHT = refractionHeight
            it.REFRACTION_OFFSET = refractionOffset
            it.BLUR_RADIUS = blurRadius
            it.WIDTH = w
            it.HEIGHT = h
            it.DISPERSION = dispersion
            it.TINT_ALPHA = tintAlpha
            it.TINT_COLOR_BLUE = tintColorBlue
            it.TINT_COLOR_GREEN = tintColorGreen
            it.TINT_COLOR_RED = tintColorRed
        }

        glass?.post { glass?.updateParameters() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { ensureGlass() }
    }

    override fun onDetachedFromWindow() {
        removeGlass()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            if (w > 0 && h > 0) {
                val maxPx = h / 2f
                if (cornerRadius > maxPx) {
                    cornerRadius = maxPx
                }
                rebuild()
            }
        }
    }

    private fun rebuild() {
        removeGlass()
        post { ensureGlass() }
    }

    private fun ensureGlass() {
        if (glass != null) return

        var w = width
        var h = height
        if (w <= 0) w = Utils.getDeviceWidthPx(context)
        if (h <= 0) h = resources.displayMetrics.heightPixels

        config = Config().apply {
            configure(
                Config.Overrides()
                    .noFilter()
                    .contrast(0f)
                    .whitePoint(0f)
                    .chromaMultiplier(1f)
                    .blurRadius(blurRadius)
                    .cornerRadius(cornerRadius)
                    .refractionHeight(refractionHeight)
                    .refractionOffset(refractionOffset)
                    .tintAlpha(tintAlpha)
                    .tintColorRed(tintColorRed)
                    .tintColorGreen(tintColorGreen)
                    .tintColorBlue(tintColorBlue)
                    .dispersion(dispersion)
                    .size(w, h)
            )
        }

        glass = LiquidGlass(context, config!!)

        val lp = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        addView(glass, lp)

        val source = customSource
        if (source == null && parent is ViewGroup) {
            return
        }
        glass?.init(source)
    }

    private fun removeGlass() {
        glass?.let {
            removeView(it)
            glass = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!draggableEnabled && !touchEffectEnabled) return super.onTouchEvent(e)
        if (elasticEnabled) liquidTracker?.applyMovement(e)

        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (touchEffectEnabled) {
                    isTouching = true
                    liquidTracker?.animateScale(1.02f)

                    glowX = e.x
                    glowY = e.y
                    invalidate()
                }

                if (draggableEnabled) {
                    downX = e.rawX
                    downY = e.rawY
                    startTx = translationX
                    startTy = translationY
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchEffectEnabled) {
                    glowX = e.x
                    glowY = e.y
                    invalidate()
                }

                if (draggableEnabled) {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    var tx = startTx + dx  // 改为 Float 类型
                    var ty = startTy + dy  // 改为 Float 类型

                    val parent = parent as? ViewGroup
                    parent?.let {
                        val pw = it.width.toFloat()
                        val ph = it.height.toFloat()
                        val w = width.toFloat()
                        val h = height.toFloat()
                        if (pw > 0 && ph > 0 && w > 0 && h > 0) {
                            val minX = -left.toFloat()
                            val maxX = pw - left - w
                            val minY = -top.toFloat()
                            val maxY = ph - top - h
                            if (tx < minX) tx = minX
                            if (tx > maxX) tx = maxX
                            if (ty < minY) ty = minY
                            if (ty > maxY) ty = maxY
                        }
                    }
                    translationX = tx
                    translationY = ty
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (touchEffectEnabled) {
                    isTouching = false
                    liquidTracker?.animateScale(1f)
                    invalidate()
                }
                if (draggableEnabled) return true
            }
        }

        val superResult = super.onTouchEvent(e)
        return touchEffectEnabled || superResult
    }
}