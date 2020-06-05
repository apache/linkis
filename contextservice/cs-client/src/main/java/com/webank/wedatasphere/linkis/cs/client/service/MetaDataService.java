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
package com.webank.wedatasphere.linkis.cs.client.service;

import com.webank.wedatasphere.linkis.common.io.MetaData;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/3/9
 */
public interface MetaDataService {

    /**
     * 通过ContextID和NodeName，获取上游的所有Metadata数据
     * @param contextIDStr
     * @param nodeName
     * @return
     * @throws CSErrorException
     */
    Map<ContextKey, MetaData> getAllUpstreamMetaData(String contextIDStr, String nodeName) throws CSErrorException;

}
