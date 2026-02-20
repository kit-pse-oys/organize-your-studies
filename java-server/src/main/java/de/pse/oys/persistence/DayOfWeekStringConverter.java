package de.pse.oys.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

/**
 * JPA-Converter für {@link DayOfWeek}.
 * <p>
 * Speichert {@link DayOfWeek} als String in der Datenbank
 * und konvertiert den gespeicherten Wert beim Laden wieder zurück.
 */
@Converter(autoApply = false)
public class DayOfWeekStringConverter implements AttributeConverter<DayOfWeek, String> {

    /**
     * Konvertiert den Entity-Wert in den Datenbankwert.
     *
     * @param attribute der gegebene Wochentag
     * @return String-Repräsentation für die DB oder {@code null}
     */
    @Override
    public String convertToDatabaseColumn(DayOfWeek attribute) {
        return attribute == null ? null : attribute.name();
    }

    /**
     * Konvertiert den Datenbankwert zurück in den Entity-Wert.
     *
     * @param dbData String aus der DB
     * @return {@link DayOfWeek} oder {@code null}
     * @throws IllegalArgumentException wenn {@code dbData} kein gültiger {@link DayOfWeek}-Name ist
     */
    @Override
    public DayOfWeek convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DayOfWeek.valueOf(dbData);
    }
}