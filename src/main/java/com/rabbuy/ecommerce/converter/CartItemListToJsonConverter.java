package com.rabbuy.ecommerce.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbuy.ecommerce.dto.CartItem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class CartItemListToJsonConverter implements AttributeConverter<List<CartItem>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CartItem> attribute) {
        // 将 List<CartItem> 转换为 JSON 字符串
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting List<CartItem> to JSON string", e);
        }
    }

    @Override
    public List<CartItem> convertToEntityAttribute(String dbData) {
        // 将 JSON 字符串转换为 List<CartItem>
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            // 使用 TypeReference 处理泛型列表
            return objectMapper.readValue(dbData, new TypeReference<List<CartItem>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON string to List<CartItem>", e);
        }
    }
}