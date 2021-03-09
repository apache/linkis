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
package com.webank.wedatasphere.linkis.cs.common.serialize.impl.context;

import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.AbstractSerializer;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;

/**
 * @author peacewong
 * @date 2020/2/27 23:26
 */
public class CommonContextKeySerializer extends AbstractSerializer<CommonContextKey> {


    @Override
    public CommonContextKey fromJson(String json) throws CSErrorException {
        return CSCommonUtils.gson.fromJson(json, CommonContextKey.class);
    }

    @Override
    public String getType() {
        return "commonContextKey";
    }

    @Override
    public boolean accepts(Object obj) {
        if (null != obj && obj instanceof CommonContextKey){
            return true;
        }
        return false;
    }
}
