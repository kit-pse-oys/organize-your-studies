package de.pse.oys.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AuthProvider – Enum für die verschieden Authentifikationsprovider
 *
 * @author uhupo
 * @version 1.0
 */
public enum AuthProvider {
    /**
     * BASIC - Ein Nutzer will sich beim Standardauthentifikator authentifizieren (lokale Authentifikation).
     */
    @JsonProperty("basic")
    BASIC,

    /**
     * OIDC - Ein Nutzer versucht sich durch einen Identity Provider zu authentifizieren.
     */
    @JsonProperty("oidc")
    OIDC
}
