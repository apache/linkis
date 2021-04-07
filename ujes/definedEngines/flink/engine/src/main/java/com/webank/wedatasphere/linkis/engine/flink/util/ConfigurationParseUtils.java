package com.webank.wedatasphere.linkis.engine.flink.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author gongzhiyao
 * @date 2020-10-13 11:47
 * @Description:
 */
public final class ConfigurationParseUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private ConfigurationParseUtils() {
    }

    public static Configuration parseConfiguration(String confJson) {
        Configuration configuration = new Configuration();
        if (StringUtils.isNotBlank(confJson)) {
            try {
                Map<String, String> confMap = objectMapper.readValue(confJson, Map.class);
                if (Objects.nonNull(confMap)) {
                    confMap.keySet().stream().forEach(key -> configuration.setString(key, confMap.get(key)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configuration;
    }
}
