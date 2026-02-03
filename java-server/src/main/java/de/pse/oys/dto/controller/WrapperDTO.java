package de.pse.oys.dto.controller;

import java.util.UUID;

/**
 * Ein generischer Container für Datentransferobjekte, der eine ID mit
 * den eigentlichen Nutzdaten verknüpft.
 * <p>
 * Diese Klasse wird verwendet, um die vom Client erwartete Struktur
 * { "id": "...", "data": { ... } } abzubilden, unabhängig vom konkreten DTO-Typ.
 * </p>
 *
 * @param <T> Der Typ des enthaltenen DTOs.
 */
public class WrapperDTO<T> {

    /** Die eindeutige Kennung des Objekts. */
    private UUID id;

    /** Das eigentliche Datentransferobjekt mit den fachlichen Attributen. */
    private T data;

    /**
     * Erzeugt einen neuen Wrapper mit ID und Daten.
     * @param id   Die UUID des Objekts.
     * @param data Das eingebettete DTO.
     */
    public WrapperDTO(UUID id, T data) {
        this.id = id;
        this.data = data;
    }

    /**
     * @return Die ID des Objekts.
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id Die neu zu setzende ID.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return Das eingebettete Datenobjekt.
     */
    public T getData() {
        return data;
    }

    /**
     * @param data Das neu zu setzende Datenobjekt.
     */
    public void setData(T data) {
        this.data = data;
    }
}