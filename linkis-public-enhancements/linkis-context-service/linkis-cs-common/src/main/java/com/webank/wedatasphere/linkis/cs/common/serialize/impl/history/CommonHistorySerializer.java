/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.cs.common.serialize.impl.history;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;

import java.util.HashMap;
import java.util.Map;

public interface CommonHistorySerializer {

    default Map<String, String> getMapValue(ContextHistory history) {
        Map<String, String> map = new HashMap<>();
        if (history.getId() != null) {
            map.put("id", String.valueOf(history.getId()));
        }
        map.put("source", history.getSource());
        map.put("contextType", history.getContextType().name());
        return map;
    }

    default Map<String, String> getMapValue(String json) {
        return CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
    }

    default <T extends ContextHistory> T get(Map<String, String> map, T t) {
        if (map.get("id") != null) {
            t.setId(Integer.valueOf(map.get("id")));
        }
        t.setContextType(ContextType.valueOf(map.get("contextType")));
        t.setSource(map.get("source"));
        return t;
    }

}
