package de.pse.oys.data.facade

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import de.pse.oys.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias Module = Identified<ModuleData>

@Serializable
data class ModuleData(
    val title: String,
    val description: String,
    val priority: Priority,
    @Serializable(with = ColorAsStringSerializer::class)
    val color: Color
)

@Serializable
enum class Priority {
    LOW,
    NEUTRAL,
    HIGH;

    fun getLabelRes(): Int {
        return when (this) {
            LOW -> R.string.priority_low
            NEUTRAL -> R.string.priority_neutral
            HIGH -> R.string.priority_high
        }
    }
}

object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("androidx.compose.ui.graphics.Color", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Color
    ) {
        val string = "#" + value.copy(alpha = 0.0f).toArgb().toString(16).uppercase().padStart(6, '0')
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Color {
        val string = decoder.decodeString()
        if (string.length != 7 || string[0] != '#')
            throw SerializationException("Invalid color string: $string")
        return Color(string.removePrefix("#").toLong(16) or 0xFF00_0000L)
    }
}