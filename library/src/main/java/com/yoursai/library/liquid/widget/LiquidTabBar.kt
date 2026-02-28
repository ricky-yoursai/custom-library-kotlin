package com.yoursai.library.liquid.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import com.yoursai.library.Config
import com.yoursai.library.liquid.LiquidGlass
import com.yoursai.library.liquid.util.Utils
import kotlin.math.abs

class LiquidTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var glass: LiquidGlass? = null
    private var config: Config? = null
    private var customSource: View? = null
    private var hasExplicitSourceBinding = false

    private var cornerRadius = Utils.dp2px(resources, 40f)
    private var refractionHeight = Utils.dp2px(resources, 20f)
    private var refractionOffset = -Utils.dp2px(resources, 70f)
    private var tintAlpha = 0.0f
    private var tintColorRed = 1.0f
    private var tintColorGreen = 1.0f
    private var tintColorBlue = 1.0f
    private var blurRadius = 0.01f
    private var dispersion = 0.5f

    private val tabItems = mutableListOf<TabItem>()
    private var selectedIndex = 0
    private var onTabSelectedListener: ((Int) -> Unit)? = null

    var selectedColor = Color.BLUE
    var unselectedColor = Color.GRAY
    var animationDuration = 300L
    var tabPadding = 16f

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * resources.displayMetrics.density
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPath = Path()
    private val clipPath = Path()
    private var animator: ValueAnimator? = null
    private var animatedIndex = 0f
    private val tabRects = mutableListOf<RectF>()

    init {
        setWillNotDraw(false)
    }

    fun bindBackground(source: View?) {
        hasExplicitSourceBinding = true
        customSource = when {
            source != null -> chooseBestSource(source)
            else -> findDefaultBackgroundSource()
        }
        glass?.init(customSource)
    }

    fun setItems(items: List<TabItem>) {
        tabItems.clear()
        tabItems.addAll(items)
        tabRects.clear()
        invalidate()
    }

    fun setSelectedIndex(index: Int) {
        if (index in 0 until tabItems.size && index != selectedIndex) {
            animateToIndex(index)
        }
    }

    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelectedListener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ensureGlass()
        if (!hasExplicitSourceBinding && customSource == null) {
            customSource = findDefaultBackgroundSource()
        }
        glass?.init(customSource)
    }

    override fun onDetachedFromWindow() {
        removeGlass()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTabRects()
        if (w > 0 && h > 0) {
            if (cornerRadius > h / 2f) {
                cornerRadius = h / 2f
            }
            updateConfig()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (tabItems.isEmpty()) return

        val tabWidth = width / tabItems.size.toFloat()
        val selectedCenterX = (animatedIndex + 0.5f) * tabWidth

        selectedPaint.color = selectedColor
        selectedPath.reset()
        selectedPath.addCircle(selectedCenterX, height / 2f, tabWidth * 0.3f, Path.Direction.CW)

        clipPath.reset()
        clipPath.addRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawPath(selectedPath, selectedPaint)
        canvas.restore()

        for (i in tabItems.indices) {
            val rect = tabRects.getOrNull(i) ?: continue
            val icon = context.getDrawable(tabItems[i].icon)
            icon?.let {
                val iconSize = (rect.width() * 0.4f).toInt()
                val iconLeft = rect.centerX() - iconSize / 2
                val iconTop = (height - iconSize) / 2
                it.setBounds(iconLeft.toInt(), iconTop, iconLeft.toInt() + iconSize, iconTop + iconSize)
                it.draw(canvas)
            }

            tabItems[i].title?.let { title ->
                textPaint.color = if (abs(i - animatedIndex) < 0.5f) Color.WHITE else unselectedColor
                val textY = height - 8f * resources.displayMetrics.density
                canvas.drawText(title, rect.centerX(), textY, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (tabItems.isEmpty()) return true
                val tabWidth = width / tabItems.size.toFloat()
                val index = (event.x / tabWidth).toInt().coerceIn(0, tabItems.size - 1)
                if (index != selectedIndex) {
                    setSelectedIndex(index)
                    onTabSelectedListener?.invoke(index)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setLiquidCornerRadius(px: Float) {
        val maxPx = if (height > 0) height / 2f else Utils.dp2px(resources, 99f)
        cornerRadius = maxOf(0f, minOf(px, maxPx))
        updateConfig()
        invalidate()
    }

    fun setLiquidRefractionHeight(px: Float) {
        val minPx = Utils.dp2px(resources, 12f)
        val maxPx = Utils.dp2px(resources, 50f)
        refractionHeight = maxOf(minPx, minOf(maxPx, px))
        updateConfig()
    }

    fun setLiquidRefractionOffset(px: Float) {
        val minPx = Utils.dp2px(resources, 20f)
        val maxPx = Utils.dp2px(resources, 120f)
        val adjustedPx = maxOf(minPx, minOf(maxPx, px))
        refractionOffset = -adjustedPx
        updateConfig()
    }

    fun setLiquidBlurRadius(radius: Float) {
        blurRadius = maxOf(0.01f, minOf(50f, radius))
        updateConfig()
    }

    fun setLiquidDispersion(value: Float) {
        dispersion = maxOf(0f, minOf(1f, value))
        updateConfig()
    }

    fun setLiquidTintAlpha(alpha: Float) {
        tintAlpha = maxOf(0f, minOf(1f, alpha))
        updateConfig()
    }

    fun setLiquidTintColorRed(red: Float) {
        tintColorRed = maxOf(0f, minOf(1f, red))
        updateConfig()
    }

    fun setLiquidTintColorGreen(green: Float) {
        tintColorGreen = maxOf(0f, minOf(1f, green))
        updateConfig()
    }

    fun setLiquidTintColorBlue(blue: Float) {
        tintColorBlue = maxOf(0f, minOf(1f, blue))
        updateConfig()
    }

    // Kept for API compatibility. Direct LiquidGlass mode has no drag/elastic/touch handlers.
    fun setLiquidDraggableEnabled(enabled: Boolean) = Unit
    fun setLiquidElasticEnabled(enabled: Boolean) = Unit
    fun setLiquidTouchEffectEnabled(enabled: Boolean) = Unit

    private fun ensureGlass() {
        if (glass != null) return

        val w = if (width > 0) width else Utils.getDeviceWidthPx(context)
        val h = if (height > 0) height else resources.displayMetrics.heightPixels

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
        addView(glass, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun removeGlass() {
        glass?.let {
            removeView(it)
            glass = null
        }
    }

    private fun updateConfig() {
        ensureGlass()
        val currentConfig = config ?: return

        var w = width
        var h = height
        if (w <= 0) w = Utils.getDeviceWidthPx(context)
        if (h <= 0) h = resources.displayMetrics.heightPixels

        currentConfig.CORNER_RADIUS_PX = cornerRadius
        currentConfig.REFRACTION_HEIGHT = refractionHeight
        currentConfig.REFRACTION_OFFSET = refractionOffset
        currentConfig.BLUR_RADIUS = blurRadius
        currentConfig.WIDTH = w
        currentConfig.HEIGHT = h
        currentConfig.DISPERSION = dispersion
        currentConfig.TINT_ALPHA = tintAlpha
        currentConfig.TINT_COLOR_RED = tintColorRed
        currentConfig.TINT_COLOR_GREEN = tintColorGreen
        currentConfig.TINT_COLOR_BLUE = tintColorBlue

        glass?.post { glass?.updateParameters() }
    }

    private fun animateToIndex(targetIndex: Int) {
        animator?.cancel()
        val start = selectedIndex.toFloat()
        val end = targetIndex.toFloat()
        animator = ValueAnimator.ofFloat(start, end).apply {
            duration = animationDuration
            addUpdateListener { animation ->
                animatedIndex = animation.animatedValue as Float
                invalidate()
            }
            doOnEnd {
                selectedIndex = targetIndex
                animatedIndex = selectedIndex.toFloat()
            }
            start()
        }
    }

    private fun updateTabRects() {
        if (tabItems.isEmpty()) return
        tabRects.clear()
        val tabWidth = width / tabItems.size.toFloat()
        for (i in tabItems.indices) {
            val left = i * tabWidth
            val right = (i + 1) * tabWidth
            tabRects.add(RectF(left, 0f, right, height.toFloat()))
        }
    }

    private fun chooseBestSource(source: View): View {
        if (source === this || source === glass) {
            findSiblingBackgroundSource(this)?.let { return it }
            return source
        }

        if (source is ViewGroup) {
            if (containsView(source, this)) {
                findSiblingBackgroundSource(this)?.let { return it }
                return source
            }
            val isLikelyMaskLayer = source.childCount == 0 && source.background is ColorDrawable
            if (isLikelyMaskLayer) {
                findSiblingBackgroundSource(source)?.let { return it }
            }
        }

        return source
    }

    private fun findDefaultBackgroundSource(): View? {
        return findSiblingBackgroundSource(this)
    }

    private fun findSiblingBackgroundSource(anchor: View): View? {
        val parentGroup = anchor.parent as? ViewGroup ?: return null
        val anchorIndex = parentGroup.indexOfChild(anchor)
        if (anchorIndex <= 0) return null
        for (i in anchorIndex - 1 downTo 0) {
            val sibling = parentGroup.getChildAt(i)
            if (sibling === this || sibling === glass) continue
            if (containsView(sibling, this)) continue
            if (sibling.visibility != View.VISIBLE) continue
            return sibling
        }
        return null
    }

    private fun containsView(root: View, target: View): Boolean {
        if (root === target) return true
        if (root !is ViewGroup) return false
        for (i in 0 until root.childCount) {
            if (containsView(root.getChildAt(i), target)) return true
        }
        return false
    }
}
