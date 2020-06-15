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
package com.webank.wedatasphere.linkis.cs.common.serialize;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/2/19
 */
public abstract class AbstractSerializer<T> implements ContextSerializer<T> {





    public String getJsonValue(T t) throws CSErrorException {
        if (null != t) {
            return CSCommonUtils.gson.toJson(t);
        }
        return null;
    }

    public abstract T fromJson(String json)  throws CSErrorException;


    @Override
    public boolean accepts(String json) {
        if (StringUtils.isNotBlank(json)) {
            Map<String, String> value = CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
            if (getType().equals(value.get("type"))) {
                return true;
            }
        }
        return false;
    }



    @Override
    public String serialize(T t) throws CSErrorException {

        if (accepts(t)) {
            Map<String, String> map = new HashMap<>();
            map.put("type", getType());
            map.put("value", getJsonValue(t));
            return  CSCommonUtils.gson.toJson(map);
        }
        return null;
    }

    @Override
    public T deserialize(String json) throws CSErrorException {
        if (accepts(json)) {
            Map<String, String> jsonObj = CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
            String value = jsonObj.get("value");
            return fromJson(value);
        }
        return null;
    }

    @Override
    public boolean isType(String type) {
        return getType().equals(type);
    }
}
