package de.pse.oys.ui.util

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    onColorChanged: (Color) -> Unit,
) {
    var color by remember { mutableStateOf(HSV.Zero) }

    Column(modifier = modifier.fillMaxSize()) {
        Row {
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(color.toColor())
            )
            ColorGrid(
                Modifier
                    .weight(2f)
                    .aspectRatio(2f), color.hue
            ) { s, v ->
                color = color.copy(saturation = s, value = v)
                onColorChanged(color.toColor())
            }
        }
        ColorSlider(
            modifier = Modifier
                .fillMaxWidth()
        ) { h ->
            color = color.copy(hue = h)
            onColorChanged(color.toColor())
        }
    }
}

@Composable
private fun ColorGrid(
    modifier: Modifier = Modifier,
    hue: Float,
    onColorChanged: (Float, Float) -> Unit
) {
    var size by remember { mutableStateOf(Size.Zero) }
    var selectedPoint by remember { mutableStateOf(Offset.Zero) }
    var selectedColor by remember { mutableStateOf(0f to 0f) }

    fun select(offset: Offset) {
        if (selectedPoint != offset) {
            selectedPoint = Offset(
                offset.x.coerceIn(0f..size.width),
                offset.y.coerceIn(0f..size.height)
            )

            selectedColor = selectedPoint.x / size.width to 1f - selectedPoint.y / size.height
            onColorChanged(selectedColor.first, selectedColor.second)
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { (w, h) ->
                size = Size(w.toFloat(), h.toFloat())
            }
            .pointerInput(onColorChanged) { detectTapGestures(onTap = ::select) }
            .pointerInput(onColorChanged) { detectDragGestures { change, _ -> select(change.position) } }
    ) {
        drawRect(
            Brush.horizontalGradient(
                0f to HSV(hue, 0f, 1f).toColor(),
                1f to HSV(hue, 1f, 1f).toColor()
            )
        )
        drawRect(
            Brush.verticalGradient(
                0f to Color(0, 0, 0, 0x00),
                1f to Color(0, 0, 0, 0xFF)
            )
        )
        drawCircle(
            HSV(hue, selectedColor.first, selectedColor.second).toColor(),
            radius = 10f * density,
            center = selectedPoint
        )
        drawCircle(
            Color.White,
            radius = 10f * density,
            center = selectedPoint,
            style = Stroke(width = 3f * density)
        )
        drawCircle(
            Color.Black,
            radius = 10f * density,
            center = selectedPoint,
            style = Stroke(width = 1f * density)
        )
    }
}

@Composable
private fun ColorSlider(
    modifier: Modifier = Modifier,
    onHueChanged: (Float) -> Unit
) {
    var sizeX by remember { mutableStateOf(0f) }
    var selectedX by remember { mutableStateOf(0f) }
    var selectedHue by remember { mutableStateOf(0f) }

    fun select(offset: Offset) {
        if (selectedX != offset.x) {
            selectedX = offset.x.coerceIn(0f..sizeX)

            selectedHue = (selectedX / sizeX).coerceIn(0f..1f) * 360f
            onHueChanged(selectedHue)
        }
    }

    Canvas(
        modifier
            .height(30.dp)
            .padding(10.dp)
            .onSizeChanged { (w, _) ->
                sizeX = w.toFloat()
            }
            .pointerInput(onHueChanged) { detectTapGestures(onTap = ::select) }
            .pointerInput(onHueChanged) { detectDragGestures { change, _ -> select(change.position) } }
    ) {
        drawRoundRect(
            Brush.horizontalGradient(
                0f to Color.Red,
                1f / 6f to Color.Yellow,
                2f / 6f to Color.Green,
                3f / 6f to Color.Cyan,
                4f / 6f to Color.Blue,
                5f / 6f to Color.Magenta,
                1f to Color.Red
            ),
            cornerRadius = CornerRadius(50f, 50f)
        )
        drawCircle(
            HSV(selectedHue, 1f, 1f).toColor(),
            radius = 10f * density,
            center = Offset(selectedX, 5f * density)
        )
        drawCircle(
            Color.White,
            radius = 10f * density,
            center = Offset(selectedX, 5f * density),
            style = Stroke(width = 3f * density)
        )
        drawCircle(
            Color.Black,
            radius = 10f * density,
            center = Offset(selectedX, 5f * density),
            style = Stroke(width = 1f * density)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerPreview() {
    OrganizeYourStudiesTheme {
        ColorPicker(onColorChanged = { Log.i("ColorPicker", "onColorChanged: $it") })
    }
}