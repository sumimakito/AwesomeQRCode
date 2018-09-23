package com.github.sumimakito.awesomeqr.option.logo

import android.graphics.Bitmap
import android.graphics.RectF

class Logo(var bitmap: Bitmap? = null,
           var scale: Float = 0.2f,
           var borderRadius: Int = 8,
           var borderWidth: Int = 10,
           var clippingRect: RectF? = null) {
    fun recycle() {
        if (bitmap == null) return
        if (bitmap!!.isRecycled) return
        bitmap!!.recycle()
        bitmap = null
    }

    fun duplicate(): Logo {
        return Logo(
                if (bitmap != null) bitmap!!.copy(Bitmap.Config.ARGB_8888, true) else null,
                scale,
                borderRadius,
                borderWidth,
                clippingRect
        )
    }
}