package com.github.sumimakito.awesomeqr.option.color

class Color(var auto: Boolean = false,
            var background: Int = 0xFFFFBBAA.toInt(),
            var light: Int = 0xFFFFFFFF.toInt(),
            var dark: Int = 0xFFE57373.toInt()) {
    fun duplicate(): Color {
        return Color(auto, background, light, dark)
    }
}