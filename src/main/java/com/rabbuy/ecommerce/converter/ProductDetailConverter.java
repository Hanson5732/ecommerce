package com.rabbuy.ecommerce.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbuy.ecommerce.dto.ProductDetailItem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class ProductDetailConverter implements AttributeConverter<List<ProductDetailItem>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ProductDetailItem> attribute) {
        // 将 List<ProductDetailItem> 序列化为 JSON 字符串
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting List<ProductDetailItem> to JSON", e);
        }
    }

    @Override
    public List<ProductDetailItem> convertToEntityAttribute(String dbData) {
        // 将 JSON 字符串反序列化为 List<ProductDetailItem>
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            // 这将正确地把 [{"key":...}] 转换为对象列表
            return objectMapper.readValue(dbData, new TypeReference<List<ProductDetailItem>>() {});
        } catch (JsonProcessingException e) {
            // 如果数据库里存的是 ["key: value"] 这种旧格式，这里反而会失败
            throw new IllegalArgumentException("Error converting JSON string to List<ProductDetailItem>", e);
        }
    }
}