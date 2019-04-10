package com.github.sumimakito.awesomeqr.option.color

class ColorQR(var auto: Boolean = false,
            var background: Int = 0xFFFFBBAA.toInt(),
            var light: Int = 0xFFFFFFFF.toInt(),
            var dark: Int = 0xFFE57373.toInt(),
            var topLeftColor: Int = 0xFF000000.toInt(),
            var topRigntColor: Int = 0xFF000000.toInt(),
            var bottomLeftColor: Int = 0xFF000000.toInt()
      ) {
    fun duplicate(): Color {
        return Color(auto, background, light, dark, topLeftColor,
          topRigntColor, bottomLeftColor)
    }
}