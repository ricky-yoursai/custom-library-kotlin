package com.yoursai.library.liquid.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import kotlin.math.min

data class TabItem(
    val icon: Int = 0, // drawable resource id (0 means no icon)
    val title: String? = null
)

// LiquidTabBar：底部液态玻璃风格 TabBar（含可拖拽的“液体气泡”）
class LiquidTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // 背景液态玻璃（整条 TabBar）
    private var glass: LiquidGlass? = null
    private var config: Config? = null
    private var customSource: View? = null
    private var hasExplicitSourceBinding = false

    // 液态玻璃参数
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
    private var pendingSelectedIndex: Int? = null // JIT_PACK@ handle RN prop order (selectedIndex before items)
    private var onTabSelectedListener: ((Int) -> Unit)? = null

    // 选中/未选中颜色（选中仅影响文字颜色，不再画背景）
    var selectedColor = Color.RED
    var unselectedColor = Color.GRAY
    var animationDuration = 300L
    var tabPadding = 16f

    // 文本绘制
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * resources.displayMetrics.density
    }
    private var animator: ValueAnimator? = null
    private var animatedIndex = 0f
    private val tabRects = mutableListOf<RectF>()

    // 可拖拽液体气泡（用于 iOS 风格效果）
    private var bubbleView: LiquidGlassView? = null
    private var bubbleSizePx = 0f
    private var liquidDraggableEnabled = true
    private var liquidElasticEnabled = true
    private var liquidTouchEffectEnabled = false

    init {
        setWillNotDraw(false)
    }

    // 绑定采样背景（决定玻璃采样的来源）
    fun bindBackground(source: View?) {
        hasExplicitSourceBinding = true
        customSource = when {
            source != null -> chooseBestSource(source)
            else -> findDefaultBackgroundSource()
        }
        glass?.init(customSource)
        bubbleView?.bind(customSource)
    }

    // 设置 TabItem 列表
    fun setItems(items: List<TabItem>) {
        tabItems.clear()
        tabItems.addAll(items)
        tabRects.clear()
        val pending = pendingSelectedIndex // JIT_PACK@ apply pending index if provided before items
        if (pending != null && pending in tabItems.indices) {
            selectedIndex = pending
            pendingSelectedIndex = null
        } else if (selectedIndex !in tabItems.indices) {
            selectedIndex = 0
        }
        animatedIndex = selectedIndex.toFloat()
        if (width > 0 && height > 0) {
            updateTabRects()
            updateBubbleGeometry()
        }
        invalidate()
    }

    // 设置选中项
    fun setSelectedIndex(index: Int) {
        if (tabItems.isEmpty()) { // JIT_PACK@ defer until items are set
            pendingSelectedIndex = index
            return
        }
        if (index in 0 until tabItems.size && index != selectedIndex) {
            animateToIndex(index)
        }
    }

    // 选中回调
    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelectedListener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ensureGlass()
        ensureBubble()
        if (!hasExplicitSourceBinding && customSource == null) {
            customSource = findDefaultBackgroundSource()
        }
        glass?.init(customSource)
        bubbleView?.bind(customSource)
    }

    override fun onDetachedFromWindow() {
        removeGlass()
        removeBubble()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTabRects()
        updateBubbleGeometry()
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

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (tabItems.isEmpty()) return super.onInterceptTouchEvent(ev)
        if (ev.actionMasked != MotionEvent.ACTION_DOWN) return super.onInterceptTouchEvent(ev)
        if (liquidDraggableEnabled && isPointInsideBubble(ev.x, ev.y)) {
            return false
        }
        return true
    }

    // 只绘制图标和文字（选中文字为 selectedColor）
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (tabItems.isEmpty()) return

        for (i in tabItems.indices) {
            val rect = tabRects.getOrNull(i) ?: continue
            val iconResId = tabItems[i].icon
            val icon = if (iconResId != 0) context.getDrawable(iconResId) else null
            val hasIcon = icon != null
            icon?.let {
                val iconSize = (rect.width() * 0.4f).toInt()
                val iconLeft = rect.centerX() - iconSize / 2
                val iconTop = (height - iconSize) / 2
                it.setBounds(iconLeft.toInt(), iconTop, iconLeft.toInt() + iconSize, iconTop + iconSize)
                it.draw(canvas)
            }

            tabItems[i].title?.let { title ->
                textPaint.color = if (abs(i - animatedIndex) < 0.5f) selectedColor else unselectedColor
                val textY = if (hasIcon) {
                    height - 8f * resources.displayMetrics.density
                } else {
                    rect.centerY() + textPaint.textSize * 0.35f
                }
                canvas.drawText(title, rect.centerX(), textY, textPaint)
            }
        }
    }

    // 点击切换 Tab（并触发气泡动画）
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (tabItems.isEmpty()) return true
                val tabWidth = width / tabItems.size.toFloat()
                val index = (event.x / tabWidth).toInt().coerceIn(0, tabItems.size - 1)
                val changed = index != selectedIndex
                animateToIndex(index, force = true)
                if (changed) onTabSelectedListener?.invoke(index)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // 外层玻璃圆角
    fun setLiquidCornerRadius(px: Float) {
        val maxPx = if (height > 0) height / 2f else Utils.dp2px(resources, 99f)
        cornerRadius = maxOf(0f, minOf(px, maxPx))
        updateConfig()
        invalidate()
    }

    // 折射高度
    fun setLiquidRefractionHeight(px: Float) {
        val minPx = Utils.dp2px(resources, 12f)
        val maxPx = Utils.dp2px(resources, 50f)
        refractionHeight = maxOf(minPx, minOf(maxPx, px))
        updateConfig()
    }

    // 折射偏移（正值会转为负值）
    fun setLiquidRefractionOffset(px: Float) {
        val minPx = Utils.dp2px(resources, 20f)
        val maxPx = Utils.dp2px(resources, 120f)
        val adjustedPx = maxOf(minPx, minOf(maxPx, px))
        refractionOffset = -adjustedPx
        updateConfig()
    }

    // 模糊半径
    fun setLiquidBlurRadius(radius: Float) {
        blurRadius = maxOf(0.01f, minOf(50f, radius))
        updateConfig()
    }

    // 色散
    fun setLiquidDispersion(value: Float) {
        dispersion = maxOf(0f, minOf(1f, value))
        updateConfig()
    }

    // 色调透明度
    fun setLiquidTintAlpha(alpha: Float) {
        tintAlpha = maxOf(0f, minOf(1f, alpha))
        updateConfig()
    }

    // 色调 R
    fun setLiquidTintColorRed(red: Float) {
        tintColorRed = maxOf(0f, minOf(1f, red))
        updateConfig()
    }

    // 色调 G
    fun setLiquidTintColorGreen(green: Float) {
        tintColorGreen = maxOf(0f, minOf(1f, green))
        updateConfig()
    }

    // 色调 B
    fun setLiquidTintColorBlue(blue: Float) {
        tintColorBlue = maxOf(0f, minOf(1f, blue))
        updateConfig()
    }

    // 是否允许气泡拖动
    fun setLiquidDraggableEnabled(enabled: Boolean) {
        liquidDraggableEnabled = enabled
        ensureBubble()
        bubbleView?.setDraggableEnabled(enabled)
    }

    // 是否开启弹性变形
    fun setLiquidElasticEnabled(enabled: Boolean) {
        liquidElasticEnabled = enabled
        ensureBubble()
        bubbleView?.setElasticEnabled(enabled)
    }

    // 是否开启触摸高亮（iOS press 效果）
    fun setLiquidTouchEffectEnabled(enabled: Boolean) {
        liquidTouchEffectEnabled = enabled
        ensureBubble()
        bubbleView?.setTouchEffectEnabled(enabled)
    }

    // 创建整条 TabBar 的液态玻璃背景
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

    // 创建可拖拽气泡
    private fun ensureBubble() {
        if (bubbleView != null) return
        val bubble = LiquidGlassView(context).apply {
            visibility = View.INVISIBLE
            setDraggableEnabled(liquidDraggableEnabled)
            setElasticEnabled(liquidElasticEnabled)
            setTouchEffectEnabled(liquidTouchEffectEnabled)
            setOnTouchListener { _, event ->
                if (tabItems.isEmpty()) return@setOnTouchListener false
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        animator?.cancel()
                        updateAnimatedIndexFromBubble()
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updateAnimatedIndexFromBubble()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val targetIndex = indexFromBubbleCenter()
                        val changed = targetIndex != selectedIndex
                        animateToIndex(targetIndex, force = true)
                        if (changed) onTabSelectedListener?.invoke(targetIndex)
                    }
                }
                false
            }
        }
        addView(bubble)
        bubbleView = bubble
        customSource?.let { bubble.bind(it) }
    }

    private fun removeBubble() {
        bubbleView?.let {
            removeView(it)
            bubbleView = null
        }
    }

    // 刷新玻璃参数
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
        syncBubbleConfig()
    }

    // 动画切换 Tab
    private fun animateToIndex(targetIndex: Int, force: Boolean = false) {
        if (tabItems.isEmpty()) return
        if (!force && targetIndex == selectedIndex) return
        animator?.cancel()
        val start = animatedIndex
        val end = targetIndex.toFloat()
        animator = ValueAnimator.ofFloat(start, end).apply {
            duration = animationDuration
            addUpdateListener { animation ->
                animatedIndex = animation.animatedValue as Float
                moveBubbleToIndex(animatedIndex)
                invalidate()
            }
            doOnEnd {
                selectedIndex = targetIndex
                animatedIndex = selectedIndex.toFloat()
                moveBubbleToIndex(animatedIndex)
            }
            start()
        }
    }

    // 更新每个 Tab 的点击区域
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

    // 更新气泡大小和位置（椭圆尺寸在这里调）
    private fun updateBubbleGeometry() {
        if (tabItems.isEmpty() || width <= 0 || height <= 0) {
            bubbleView?.visibility = View.INVISIBLE
            return
        }
        ensureBubble()
        val tabWidth = width / tabItems.size.toFloat()
        val base = min(tabWidth, height.toFloat())
        // @@@ 这里设置椭圆的宽高比例：改这两个值即可调整“椭圆”形状
        val targetWidth = maxOf(Utils.dp2px(resources, 30f), base * 0.88f)
        val targetHeight = maxOf(Utils.dp2px(resources, 24f), base * 0.68f)
        val bubble = bubbleView ?: return
        val widthInt = targetWidth.toInt()
        val heightInt = targetHeight.toInt()
        val lp = bubble.layoutParams as? LayoutParams
        if (lp == null || lp.width != widthInt || lp.height != heightInt) {
            bubble.layoutParams = LayoutParams(widthInt, heightInt)
        }
        bubbleSizePx = maxOf(targetWidth, targetHeight)
        // 椭圆的圆角半径取高度一半，即“胶囊”形状
        bubble.setCornerRadius(targetHeight / 2f)
        moveBubbleToIndex(animatedIndex)
        bubble.visibility = View.VISIBLE
        syncBubbleConfig()
    }

    // 同步气泡的玻璃参数
    private fun syncBubbleConfig() {
        val bubble = bubbleView ?: return
        if (bubbleSizePx <= 0f) return
        bubble.setBlurRadius(blurRadius)
        bubble.setDispersion(dispersion)
        bubble.setTintAlpha(tintAlpha)
        bubble.setTintColorRed(tintColorRed)
        bubble.setTintColorGreen(tintColorGreen)
        bubble.setTintColorBlue(tintColorBlue)
        bubble.setRefractionHeight(min(refractionHeight, bubbleSizePx * 0.45f))
        bubble.setRefractionOffset(min(abs(refractionOffset), bubbleSizePx * 0.7f))
    }

    // 根据 animatedIndex 移动气泡
    private fun moveBubbleToIndex(index: Float) {
        val bubble = bubbleView ?: return
        if (tabItems.isEmpty() || width <= 0 || bubbleSizePx <= 0f) return
        val tabWidth = width / tabItems.size.toFloat()
        val clampedIndex = index.coerceIn(0f, (tabItems.size - 1).toFloat())
        val centerX = (clampedIndex + 0.5f) * tabWidth
        // @@@ 这里修正初始位置：优先用 layoutParams 的尺寸，避免首次布局时宽高为 0
        val bubbleWidth = if (bubble.width > 0) bubble.width.toFloat() else {
            (bubble.layoutParams as? LayoutParams)?.width?.toFloat() ?: bubbleSizePx
        }
        val bubbleHeight = if (bubble.height > 0) bubble.height.toFloat() else {
            (bubble.layoutParams as? LayoutParams)?.height?.toFloat() ?: bubbleSizePx
        }
        val tx = centerX - bubbleWidth / 2f
        val ty = (height - bubbleHeight) / 2f
        bubble.translationX = tx
        bubble.translationY = ty
    }

    // 从气泡中心位置反推当前动画索引
    private fun updateAnimatedIndexFromBubble() {
        val bubble = bubbleView ?: return
        if (tabItems.isEmpty() || width <= 0) return
        val centerX = bubble.x + bubble.width / 2f
        val tabWidth = width / tabItems.size.toFloat()
        animatedIndex = ((centerX / tabWidth) - 0.5f)
            .coerceIn(0f, (tabItems.size - 1).toFloat())
        invalidate()
    }

    // 根据气泡中心位置计算 TabIndex
    private fun indexFromBubbleCenter(): Int {
        val bubble = bubbleView ?: return selectedIndex
        if (tabItems.isEmpty() || width <= 0) return selectedIndex
        val centerX = bubble.x + bubble.width / 2f
        val tabWidth = width / tabItems.size.toFloat()
        return (centerX / tabWidth).toInt().coerceIn(0, tabItems.size - 1)
    }

    // 选择最合适的采样源（避免自己采样自己）
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

    // 默认取同层级前一个可见 View 作为采样源
    private fun findDefaultBackgroundSource(): View? {
        return findSiblingBackgroundSource(this) ?: (parent as? ViewGroup)
    }

    // 找同级之前的兄弟 View 作为采样源
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

    // 判断 root 是否包含 target
    private fun containsView(root: View, target: View): Boolean {
        if (root === target) return true
        if (root !is ViewGroup) return false
        for (i in 0 until root.childCount) {
            if (containsView(root.getChildAt(i), target)) return true
        }
        return false
    }

    private fun isPointInsideBubble(x: Float, y: Float): Boolean {
        val bubble = bubbleView ?: return false
        if (bubble.visibility != View.VISIBLE) return false
        val left = bubble.x
        val top = bubble.y
        val right = left + bubble.width
        val bottom = top + bubble.height
        return x >= left && x <= right && y >= top && y <= bottom
    }
}
