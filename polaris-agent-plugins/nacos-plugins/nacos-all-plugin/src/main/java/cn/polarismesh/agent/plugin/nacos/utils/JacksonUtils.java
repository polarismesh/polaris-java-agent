package cn.polarismesh.agent.plugin.nacos.utils;

import cn.polarismesh.agent.plugin.nacos.exception.NacosAgentException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JacksonUtils {

    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new NacosAgentException(e);
        }
    }

    public static <T> T toObj(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (IOException e) {
            throw new NacosAgentException(e);
        }
    }

    public static <T> T toObj(String json, TypeReference<T> valueTypeRef) {
        try {

            return mapper.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new NacosAgentException(e);
        }
    }

    public static Map<String, String> toStrMap(String json) {
        return toObj(json, new TypeReference<Map<String, String>>() {
        });
    }

}
