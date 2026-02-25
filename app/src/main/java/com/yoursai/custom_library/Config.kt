package com.yoursai.custom_library

class Config {
    @Volatile var CORNER_RADIUS_PX: Float = 0f
    @Volatile var ECCENTRIC_FACTOR: Float = 1.0f
    @Volatile var REFRACTION_HEIGHT: Float = 0f
    @Volatile var REFRACTION_OFFSET: Float = 0f
    @Volatile var CONTRAST: Float = 0f
    @Volatile var WHITE_POINT: Float = 0f
    @Volatile var CHROMA_MULTIPLIER: Float = 0f
    @Volatile var BLUR_RADIUS: Float = 0f

    var DISPERSION: Float = 0f
    var DEPTH_EFFECT: Float = 0.3f
    var WIDTH: Int = 0
    var HEIGHT: Int = 0
    var TINT_ALPHA: Float = 0f
    var TINT_COLOR_RED: Float = 0f
    var TINT_COLOR_GREEN: Float = 0f
    var TINT_COLOR_BLUE: Float = 0f

    fun configure(overrides: Overrides?) {
        overrides?.apply(this)
    }

    class Overrides {
        var cornerRadius: Float? = null
        var refractionHeight: Float? = null
        var refractionOffset: Float? = null
        var contrast: Float? = null
        var whitePoint: Float? = null
        var chromaMultiplier: Float? = null
        var blurRadius: Float? = null
        var tintAlpha: Float? = null
        var tintColorRed: Float? = null
        var tintColorGreen: Float? = null
        var tintColorBlue: Float? = null
        var dispersion: Float? = null
        var width: Int? = null
        var height: Int? = null

        fun tintAlpha(v: Float): Overrides {
            tintAlpha = v
            return this
        }

        fun tintColorRed(v: Float): Overrides {
            tintColorRed = v
            return this
        }

        fun tintColorGreen(v: Float): Overrides {
            tintColorGreen = v
            return this
        }

        fun tintColorBlue(v: Float): Overrides {
            tintColorBlue = v
            return this
        }

        fun noFilter(): Overrides {
            contrast(0f)
            whitePoint(0f)
            chromaMultiplier(1f)
            blurRadius(0f)
            refractionHeight(0f)
            refractionOffset(0f)
            return this
        }

        fun cornerRadius(v: Float): Overrides {
            cornerRadius = v
            return this
        }

        fun refractionHeight(v: Float): Overrides {
            refractionHeight = v
            return this
        }

        fun refractionOffset(v: Float): Overrides {
            refractionOffset = v
            return this
        }

        fun contrast(v: Float): Overrides {
            contrast = v
            return this
        }

        fun whitePoint(v: Float): Overrides {
            whitePoint = v
            return this
        }

        fun chromaMultiplier(v: Float): Overrides {
            chromaMultiplier = v
            return this
        }

        fun blurRadius(v: Float): Overrides {
            blurRadius = v
            return this
        }

        fun dispersion(v: Float): Overrides {
            dispersion = v
            return this
        }

        fun size(w: Int, h: Int): Overrides {
            width = w
            height = h
            return this
        }

        fun apply(c: Config) {
            cornerRadius?.let { c.CORNER_RADIUS_PX = it }
            refractionHeight?.let { c.REFRACTION_HEIGHT = it }
            refractionOffset?.let { c.REFRACTION_OFFSET = it }
            contrast?.let { c.CONTRAST = it }
            whitePoint?.let { c.WHITE_POINT = it }
            chromaMultiplier?.let { c.CHROMA_MULTIPLIER = it }
            blurRadius?.let { c.BLUR_RADIUS = it }
            width?.let { c.WIDTH = it }
            height?.let { c.HEIGHT = it }
            tintAlpha?.let { c.TINT_ALPHA = it }
            tintColorRed?.let { c.TINT_COLOR_RED = it }
            tintColorGreen?.let { c.TINT_COLOR_GREEN = it }
            tintColorBlue?.let { c.TINT_COLOR_BLUE = it }
            dispersion?.let { c.DISPERSION = it }
        }
    }
}