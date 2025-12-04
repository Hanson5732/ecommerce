package com.rabbuy.ecommerce.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListToJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        // 将 List<String> 转换为 JSON 字符串以便存入数据库
        if (attribute == null || attribute.isEmpty()) {
            return "[]"; // 返回空的 JSON 数组字符串
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            // 在实际应用中，应该记录日志或抛出更具体的运行时异常
            throw new IllegalArgumentException("Error converting List<String> to JSON string", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        // 将数据库中的 JSON 字符串转换为 List<String>
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>(); // 返回空列表
        }
        try {
            // 使用 TypeReference 来正确反序列化泛型列表
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 记录日志或抛出异常
            throw new IllegalArgumentException("Error converting JSON string to List<String>", e);
        }
    }
}