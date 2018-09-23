package com.github.sumimakito.awesomeqr.option

import com.github.sumimakito.awesomeqr.option.background.Background
import com.github.sumimakito.awesomeqr.option.color.Color
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class RenderOption {
    var content = "Makes QR Codes Great Again."
    var size = 600
    var borderWidth = 30
    var clearBorder = true
    var patternScale = 0.4f
    var roundedPatterns = false
    var color: Color = Color()
    var ecl = ErrorCorrectionLevel.M

    var background: Background? = null
        set(value) {
            if (field != null) {
                field!!.recycle()
            }
            field = value
        }

    var logo: Logo? = null
        set(value) {
            if (field != null) {
                field!!.recycle()
            }
            field = value
        }

    fun recycle() {
        if (background != null) {
            background!!.recycle()
            background = null
        }
        if (logo != null) {
            logo!!.recycle()
            logo = null
        }
    }

    fun duplicate(): RenderOption {
        val renderConfig = RenderOption()
        renderConfig.content = content
        renderConfig.size = size
        renderConfig.borderWidth = borderWidth
        renderConfig.clearBorder = clearBorder
        renderConfig.patternScale = patternScale
        renderConfig.roundedPatterns = roundedPatterns
        renderConfig.color = color.duplicate()
        renderConfig.ecl = ecl

        renderConfig.background = background?.duplicate()
        renderConfig.logo = logo?.duplicate()
        return renderConfig
    }
}