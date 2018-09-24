package com.github.sumimakito.awesomeqr.util

import android.graphics.Rect
import android.graphics.RectF

object RectUtils {
    fun round(rectF: RectF): Rect {
        return Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom))
    }
}
