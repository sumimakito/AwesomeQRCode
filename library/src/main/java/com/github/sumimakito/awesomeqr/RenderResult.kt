package com.github.sumimakito.awesomeqr

import android.graphics.Bitmap
import java.io.File

class RenderResult @JvmOverloads constructor(val bitmap: Bitmap?,
                                             val gifOutputFile: File?,
                                             val type: OutputType = OutputType.Still) {
    enum class OutputType {
        Still, GIF, Blend
    }
}
