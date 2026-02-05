package de.pse.oys.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator(discriminator = "authType")
sealed class Credentials {
    @Serializable
    @SerialName("BASIC")
    data class UsernamePassword(val username: String, val password: String) : Credentials()

    @Serializable
    @SerialName("OIDC")
    data class OIDC(val externalToken: String, val provider: OIDCType) : Credentials()
}

@Serializable
enum class OIDCType {
    GOOGLE
}
