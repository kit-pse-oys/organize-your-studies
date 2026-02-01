package de.pse.oys.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

/**
 * Speichert DayOfWeek als String (MONDAY..SUNDAY) in der DB (TEXT) und liest ihn wieder zurück.
 */
@Converter(autoApply = false)
public class DayOfWeekStringConverter implements AttributeConverter<DayOfWeek, String> {

    @Override
    public String convertToDatabaseColumn(DayOfWeek attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DayOfWeek.valueOf(dbData);
    }
}
