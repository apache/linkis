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

import com.webank.wedatasphere.linkis.cs.common.entity.history.CommonResourceHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.Resource;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.AbstractSerializer;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;

import java.util.Map;


public class CommonResourceHistorySerializer extends AbstractSerializer<CommonResourceHistory> implements CommonHistorySerializer {
    @Override
    public String getType() {
        return "commonResourceHistory";
    }

    @Override
    public boolean accepts(Object obj) {
        return obj != null && obj instanceof CommonResourceHistory;
    }

    @Override
    public CommonResourceHistory fromJson(String json) throws CSErrorException {
        Map<String, String> map = getMapValue(json);
        CommonResourceHistory history = get(map, new CommonResourceHistory());
        history.setResource((Resource) ContextSerializationHelper.getInstance().deserialize(map.get("resource")));
        return history;
    }

    @Override
    public String getJsonValue(CommonResourceHistory commonResourceHistory) throws CSErrorException {
        Resource resource = commonResourceHistory.getResource();
        String resourceStr = ContextSerializationHelper.getInstance().serialize(resource);
        Map<String, String> mapValue = getMapValue(commonResourceHistory);
        mapValue.put("resource", resourceStr);
        return CSCommonUtils.gson.toJson(mapValue);
    }
}
