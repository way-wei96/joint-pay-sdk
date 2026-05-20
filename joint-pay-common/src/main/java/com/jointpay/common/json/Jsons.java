package com.jointpay.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;

import java.util.Map;

public final class Jsons {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Jsons() {
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new JointPayException(ErrorCode.INTERNAL_ERROR, "JSON 序列化失败", null, null, e);
        }
    }

    public static Map<String, Object> parseMap(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new JointPayException(ErrorCode.CHANNEL_ERROR, "JSON 解析失败", null, null, e);
        }
    }

    public static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
