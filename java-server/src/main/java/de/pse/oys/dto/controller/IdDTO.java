package de.pse.oys.dto.controller;

import java.util.UUID;

/**
 * Ein einfaches Datentransferobjekt zur Übertragung einer einzelnen Kennung.
 * <p>
 * Diese Klasse wird primär für Löschvorgänge verwendet, bei denen der Client
 * lediglich die ID eines Objekts im Request-Body übermittelt.
 * </p>
 */
public class IdDTO {

    /** Die ID als String-Repräsentation. */
    private String id;

    /**
     * Standardkonstruktor für die JSON-Deserialisierung.
     */
    public IdDTO() {
    }

    /**
     * Erzeugt ein neues IdDTO.
     * @param id Die zu übertragende ID.
     */
    public IdDTO(String id) {
        this.id = id;
    }

    /**
     * Gibt die ID als String zurück.
     * @return die ID-Zeichenkette.
     */
    public String getId() {
        return id;
    }

    /**
     * Setzt die ID als String.
     * @param id die zu setzende ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Hilfsmethode, die den internen String direkt als UUID-Objekt zurückgibt.
     * @return die ID als {@link UUID}.
     * @throws IllegalArgumentException falls der String kein gültiges UUID-Format hat.
     */
    public UUID asUuid() {
        return id != null ? UUID.fromString(id) : null;
    }
}