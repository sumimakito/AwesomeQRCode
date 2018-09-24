package com.github.sumimakito.awesomeqr.option.background

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import com.github.sumimakito.awesomeqr.util.RectUtils

abstract class Background @JvmOverloads constructor(var alpha: Float = 0.6f,
                          var clippingRect: Rect? = null,
                          var bitmap: Bitmap? = null) {
    var clippingRectF: RectF?
        get() {
            return if (clippingRect == null) null else RectF(clippingRect)
        }
        set(value) {
            if (value != null) this.clippingRect = RectUtils.round(value)
            else clippingRect = null
        }

    fun recycle() {
        if (bitmap == null) return
        if (bitmap!!.isRecycled) return
        bitmap!!.recycle()
        bitmap = null
    }

    abstract fun duplicate(): Background
}