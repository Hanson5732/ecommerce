package com.rabbuy.ecommerce.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbuy.ecommerce.dto.ProductSnapshot;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductSnapshotConverter implements AttributeConverter<ProductSnapshot, String> {

    // 确保 ObjectMapper 注册了 JSR310 模块 (虽然这个 POJO 里没有日期，但这是个好习惯)
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(ProductSnapshot attribute) {
        // 将 ProductSnapshot 对象 序列化为 JSON 字符串
        if (attribute == null) {
            return null; // 或者 "{}"
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting ProductSnapshot to JSON string", e);
        }
    }

    @Override
    public ProductSnapshot convertToEntityAttribute(String dbData) {
        // 将 JSON 字符串 反序列化为 ProductSnapshot 对象
        if (dbData == null || dbData.trim().isEmpty()) {
            return null; // 或者 new ProductSnapshot()
        }
        try {
            return objectMapper.readValue(dbData, ProductSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON string to ProductSnapshot", e);
        }
    }
}