package com.yoursai.custom_library.liquid.util

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import androidx.annotation.NonNull
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs
import kotlin.math.sqrt

class LiquidTracker(view: View) {
    private var velocityTracker: VelocityTracker? = null
    private val springAnimX: SpringAnimation
    private val springAnimY: SpringAnimation
    private val springAnimRotX: SpringAnimation
    private val springAnimRotY: SpringAnimation
    private val liquidHandler: Handler

    init {
        val springX = SpringForce().apply {
            stiffness = 180f
            dampingRatio = 0.35f
        }
        springAnimX = SpringAnimation(view, DynamicAnimation.SCALE_X).apply {
            setSpring(springX)
        }

        val springY = SpringForce().apply {
            stiffness = 180f
            dampingRatio = 0.35f
        }
        springAnimY = SpringAnimation(view, DynamicAnimation.SCALE_Y).apply {
            setSpring(springY)
        }

        val springRot = SpringForce().apply {
            stiffness = 180f
            dampingRatio = 0.5f
        }

        springAnimRotX = SpringAnimation(view, DynamicAnimation.ROTATION_X).apply {
            setSpring(springRot)
        }

        springAnimRotY = SpringAnimation(view, DynamicAnimation.ROTATION_Y).apply {
            setSpring(springRot)
        }

        liquidHandler = Handler(Looper.getMainLooper())
    }

    fun applyMovement(@NonNull e: MotionEvent) {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                ensureAddMovement(e)
            }
            MotionEvent.ACTION_MOVE -> {
                ensureAddMovement(e)

                val scaleXY = getLiquidScale()
                animateToFinalPosition(scaleXY[0], scaleXY[1])

                liquidHandler.removeCallbacksAndMessages(null)
                liquidHandler.postDelayed({
                    animateToFinalPosition(1f, 1f)
                }, 200)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                recycle()
                animateToFinalPosition(1f, 1f)
            }
        }
    }

    fun recycle() {
        velocityTracker?.let {
            it.recycle()
            velocityTracker = null
        }
    }

    private fun getVelocity(): Float {
        velocityTracker ?: return 0f

        velocityTracker!!.computeCurrentVelocity(1)
        val velocityX = velocityTracker!!.xVelocity
        val velocityY = velocityTracker!!.yVelocity
        return (sqrt(velocityX * velocityX + velocityY * velocityY) * if (velocityX > 0f) 1f else -1f)
    }

    private fun getLiquidScale(): FloatArray {
        velocityTracker ?: return floatArrayOf(1f, 1f)

        velocityTracker!!.computeCurrentVelocity(1)
        val velocityX = velocityTracker!!.xVelocity
        val velocityY = velocityTracker!!.yVelocity

        // Calculate deformation separately for X and Y directions
        // Stretch along movement direction, compress perpendicular to it
        val absVx = abs(velocityX)
        val absVy = abs(velocityY)

        // Deformation intensity factor, adjust this value to control the degree of deformation
        val stretchFactor = 0.5f

        val (scaleX, scaleY) = if (absVx > absVy) {
            // Horizontal movement dominant: stretch X axis, compress Y axis
            1f + absVx * stretchFactor to 1f - absVx * stretchFactor * 0.5f
        } else {
            // Vertical movement dominant: stretch Y axis, compress X axis
            1f - absVy * stretchFactor * 0.5f to 1f + absVy * stretchFactor
        }

        return floatArrayOf(
            scaleX.coerceIn(0.6f, 1.4f),
            scaleY.coerceIn(0.6f, 1.4f)
        )
    }

    private fun ensureAddMovement(e: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(e)
    }

    fun animateScale(scale: Float) {
        animateToFinalPosition(scale, scale)
    }

    fun animateTilt(rotX: Float, rotY: Float) {
        springAnimRotX.animateToFinalPosition(rotX)
        springAnimRotY.animateToFinalPosition(rotY)
    }

    private fun animateToFinalPosition(x: Float, y: Float) {
        springAnimX.animateToFinalPosition(x)
        springAnimY.animateToFinalPosition(y)
    }
}