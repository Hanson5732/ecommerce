package com.rabbuy.ecommerce.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;


@Converter(autoApply = true)
public class UUIDStringConverter implements AttributeConverter<UUID, String> {


    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return (uuid == null) ? null : uuid.toString().replace("-", "");
    }


    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        if (dbData.length() == 36) {
            return UUID.fromString(dbData);
        }

        if (dbData.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string in database (must be 32 chars): " + dbData);
        }

        String formatted = String.format("%s-%s-%s-%s-%s",
                dbData.substring(0, 8),
                dbData.substring(8, 12),
                dbData.substring(12, 16),
                dbData.substring(16, 20),
                dbData.substring(20, 32)
        );
        return UUID.fromString(formatted);
    }
}