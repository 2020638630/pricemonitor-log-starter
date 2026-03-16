package com.pricemonitor.starter.bizlog.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;

/**
 * JSON工具类
 * 线程安全的 JSON 序列化和反序列化工具
 *
 * @author pricemonitor
 */
@UtilityClass
public class JsonUtil {

    /**
     * 线程安全的 ObjectMapper 实例
     * 配置为忽略未知属性、忽略空值等
     */
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 创建并配置 ObjectMapper
     *
     * @return 配置好的 ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 反序列化时忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时忽略空值
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        // 禁用日期时间序列化为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串，如果对象为null则返回null
     */
    public String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON失败: " + e.getMessage(), e);
        }
    }

    /**
     * 对象转格式化的JSON字符串（美化输出）
     *
     * @param obj 对象
     * @return 格式化的JSON字符串，如果对象为null则返回null
     */
    public String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 对象，如果JSON字符串为空则返回null
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 ObjectMapper 实例
     * 用于需要自定义序列化/反序列化的场景
     *
     * @return ObjectMapper 实例
     */
    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
