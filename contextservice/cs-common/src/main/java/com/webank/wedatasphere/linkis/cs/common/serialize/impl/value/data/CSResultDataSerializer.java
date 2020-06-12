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
package com.webank.wedatasphere.linkis.cs.common.serialize.impl.value.data;

import com.webank.wedatasphere.linkis.cs.common.entity.data.CSResultData;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.AbstractSerializer;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;

/**
 * @author peacewong
 * @date 2020/3/13 19:35
 */
public class CSResultDataSerializer extends AbstractSerializer<CSResultData> {
    @Override
    public CSResultData fromJson(String json) throws CSErrorException {
        return CSCommonUtils.gson.fromJson(json, CSResultData.class);
    }

    @Override
    public String getType() {
        return "CSResultData";
    }

    @Override
    public boolean accepts(Object obj) {
        if (null != obj && obj.getClass().getName().equals(CSResultData.class.getName())){
            return true;
        }
        return false;
    }
}