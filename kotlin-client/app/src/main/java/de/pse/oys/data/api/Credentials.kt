package de.pse.oys.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Credentials {
    @Serializable
    @SerialName("basic")
    data class UsernamePassword(val username: String, val password: String) : Credentials()

    @Serializable
    @SerialName("oidc")
    data class OIDC(val token: String, val type: OIDCType) : Credentials()
}

@Serializable
enum class OIDCType {
    GOOGLE
}