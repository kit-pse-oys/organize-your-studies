package de.pse.oys.data.api

sealed class Credentials {
    data class UsernamePassword(val username: String, val password: String) : Credentials()

    data class OIDC(val token: String, val type: OIDCType) : Credentials()
}

enum class OIDCType {
    GOOGLE
}