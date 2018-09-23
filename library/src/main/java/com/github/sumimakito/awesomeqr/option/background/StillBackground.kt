package com.github.sumimakito.awesomeqr.option.background

import android.graphics.Bitmap
import android.graphics.Rect

class StillBackground(alpha: Float = 0.6f,
                      clippingRect: Rect? = null,
                      bitmap: Bitmap? = null,
                      internalPreview: Bitmap? = null) : Background(alpha, clippingRect, bitmap, internalPreview) {
    override fun duplicate(): StillBackground {
        return StillBackground(
                alpha,
                clippingRect,
                if (bitmap != null) bitmap!!.copy(Bitmap.Config.ARGB_8888, true) else null,
                internalPreview
        )
    }
}