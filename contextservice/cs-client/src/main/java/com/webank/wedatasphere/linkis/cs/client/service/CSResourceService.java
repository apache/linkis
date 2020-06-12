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

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.cs.client.utils.SerializeHelper;
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.CSTable;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.BMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/3/9
 */
public class CSResourceService implements ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(CSResourceService.class);

    private static CSResourceService csResourceService;

    private CSResourceService() {

    }

    public static CSResourceService getInstance() {
        if (null == csResourceService) {
            synchronized (CSResourceService.class) {
                if (null == csResourceService) {
                    csResourceService = new CSResourceService();
                }
            }
        }
        return csResourceService;
    }

    @Override
    public Map<ContextKey, BMLResource> getAllUpstreamBMLResource(String contextIDStr, String nodeName) throws CSErrorException {
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return null;
        }
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            return DefaultSearchService.getInstance().searchUpstreamContextMap(contextID, nodeName, Integer.MAX_VALUE, BMLResource.class);
        } catch (ErrorException e) {
            logger.error("Deserialize contextid error. contextID : " + contextIDStr + ", e ", e);
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize contextid error. contextID : " + contextIDStr + ", e " + e.getDesc());
        }
    }

    @Override
    public List<BMLResource> getUpstreamBMLResource(String contextIDStr, String nodeName) throws CSErrorException {
        List<BMLResource> rsList = new ArrayList<>();
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return rsList;
        }
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            if (null != contextID) {
                rsList = DefaultSearchService.getInstance().searchUpstreamContext(contextID, nodeName, Integer.MAX_VALUE, BMLResource.class);
            }
            return rsList;
        } catch (ErrorException e) {
            logger.error("Failed to get Resource: " + e.getMessage());
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize contextID error. contextIDStr : ", e);
        }
    }
}
