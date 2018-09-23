package com.github.sumimakito.awesomeqr.option.background

import android.graphics.Bitmap
import android.graphics.Rect

class BlendBackground(var borderRadius: Int = 10,
                      alpha: Float = 0.6f,
                      clippingRect: Rect? = null,
                      bitmap: Bitmap? = null,
                      internalPreview: Bitmap? = null) : Background(alpha, clippingRect, bitmap, internalPreview) {
    override fun duplicate(): BlendBackground {
        return BlendBackground(borderRadius,
                alpha,
                clippingRect,
                if (bitmap != null) bitmap!!.copy(Bitmap.Config.ARGB_8888, true) else null,
                internalPreview
        )
    }
}