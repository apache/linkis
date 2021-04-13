/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.flink.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liangqilang on 01 20, 2021
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
