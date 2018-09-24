package com.github.sumimakito.awesomeqr.option.background

import android.graphics.Bitmap
import android.graphics.Rect

class StillBackground @JvmOverloads constructor(alpha: Float = 0.6f,
                                                clippingRect: Rect? = null,
                                                bitmap: Bitmap? = null) : Background(alpha, clippingRect, bitmap) {
    override fun duplicate(): StillBackground {
        return StillBackground(
                alpha,
                clippingRect,
                if (bitmap != null) bitmap!!.copy(Bitmap.Config.ARGB_8888, true) else null
        )
    }
}