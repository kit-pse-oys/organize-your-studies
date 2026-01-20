package de.pse.oys.ui.util

import androidx.compose.ui.graphics.Color

data class HSV(
    val hue: Float, // [0..360)
    val saturation: Float, // [0..1]
    val value: Float // [0..1]
) {
    companion object {
        val Zero = HSV(0f, 0f, 0f)
    }
}

fun HSV.toColor(): Color = Color.hsv(hue, saturation, value)