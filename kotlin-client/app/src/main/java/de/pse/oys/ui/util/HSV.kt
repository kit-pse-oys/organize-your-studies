package de.pse.oys.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class HSV(
    val hue: Float, // [0..360)
    val saturation: Float, // [0..1]
    val value: Float // [0..1]
) {
    init {
        require(hue in 0f..<360f)
        require(saturation in 0f..1f)
        require(value in 0f..1f)
    }

    companion object {
        val Zero = HSV(0f, 0f, 0f)
    }
}

fun HSV.toColor(): Color = Color.hsv(hue, saturation, value)

fun Color.toHSV(): HSV {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    return HSV(hsv[0], hsv[1], hsv[2])
}