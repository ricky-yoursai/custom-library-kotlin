package com.yoursai.library.liquid.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.abs

data class TabItem(
    val icon: Int, // drawable resource id
    val title: String? = null
)

class TabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val tabItems = mutableListOf<TabItem>()
    private var selectedIndex = 0
    private var onTabSelectedListener: ((Int) -> Unit)? = null

    // Configurable parameters
    var tabBackgroundColor = Color.WHITE
    var selectedColor = Color.BLUE
    var unselectedColor = Color.GRAY
    var animationDuration = 300L
    var cornerRadius = 20f
    var tabPadding = 16f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * resources.displayMetrics.density
    }

    private val backgroundPath = Path()
    private val selectedPath = Path()
    private var animator: ValueAnimator? = null
    private var animatedIndex = 0f

    private val tabRects = mutableListOf<RectF>()

    init {
        setWillNotDraw(false)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTabRects()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (tabItems.isEmpty()) return

        // Draw background
        paint.color = tabBackgroundColor
        backgroundPath.reset()
        backgroundPath.addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.drawPath(backgroundPath, paint)

        // Calculate selected position
        val tabWidth = width / tabItems.size.toFloat()
        val selectedCenterX = (animatedIndex + 0.5f) * tabWidth

        // Draw selected background with liquid effect
        paint.color = selectedColor
        selectedPath.reset()
        val radius = tabWidth * 0.3f
        selectedPath.addCircle(selectedCenterX, height / 2f, radius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(backgroundPath)
        canvas.drawPath(selectedPath, paint)
        canvas.restore()

        // Draw icons and text
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
            MotionEvent.ACTION_UP -> {
                val touchX = event.x
                val tabWidth = width / tabItems.size.toFloat()
                val index = (touchX / tabWidth).toInt().coerceIn(0, tabItems.size - 1)
                if (index != selectedIndex) {
                    setSelectedIndex(index)
                    onTabSelectedListener?.invoke(index)
                }
            }
        }
        return true
    }
}
