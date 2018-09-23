package com.github.sumimakito.awesomeqr.option.background

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import java.io.File

class GifBackground(var outputFile: File? = null,
                    var inputUri: Uri? = null,
                    var inputFile: File? = null,
                    alpha: Float = 0.6f,
                    clippingRect: Rect,
                    bitmap: Bitmap? = null,
                    internalPreview: Bitmap? = null) : Background(alpha, clippingRect, bitmap, internalPreview) {
    override fun duplicate(): GifBackground {
        return GifBackground(
                outputFile,
                inputUri,
                inputFile,
                alpha,
                clippingRect!!,
                if (bitmap != null) bitmap!!.copy(Bitmap.Config.ARGB_8888, true) else null,
                internalPreview
        )
    }
}