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

import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.client.utils.SerializeHelper;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peacewong
 * @date 2020/3/21 19:18
 */
public class CSNodeServiceImpl implements CSNodeService{

    private final static Logger logger = LoggerFactory.getLogger(CSNodeServiceImpl.class);

    private SearchService searchService = DefaultSearchService.getInstance();

    private static CSNodeService csNodeService;

    private CSNodeServiceImpl() {

    }

    public static CSNodeService getInstance() {
        if (null == csNodeService) {
            synchronized (CSNodeServiceImpl.class) {
                if (null == csNodeService) {
                    csNodeService = new CSNodeServiceImpl();
                }
            }
        }
        return csNodeService;
    }

    @Override
    public void initNodeCSInfo(String contextIDStr, String ndeName) {

        try {
            ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            contextClient.removeAllValueByKeyPrefixAndContextType(contextID, ContextType.METADATA, CSCommonUtils.NODE_PREFIX + ndeName);
        } catch (Exception e) {
            logger.error("Failed to init node cs Info", e);
        }
    }
}
