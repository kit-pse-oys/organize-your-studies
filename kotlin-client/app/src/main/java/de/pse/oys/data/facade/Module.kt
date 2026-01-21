package de.pse.oys.data.facade

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
    HIGH,
}

object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("androidx.compose.ui.graphics.Color", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Color
    ) {
        val string = value.convert(ColorSpaces.Srgb).value.toString(16).padStart(8, '0')
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Color {
        val string = decoder.decodeString()
        return Color(string.toLong(16))
    }
}