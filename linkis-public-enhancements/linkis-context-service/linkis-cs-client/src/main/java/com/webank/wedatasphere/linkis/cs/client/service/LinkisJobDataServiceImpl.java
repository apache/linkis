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
import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.client.utils.SerializeHelper;
import com.webank.wedatasphere.linkis.cs.common.entity.data.LinkisJobData;
import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peacewong
 * @date 2020/3/13 22:12
 */
public class LinkisJobDataServiceImpl implements LinkisJobDataService {

    private final static Logger logger = LoggerFactory.getLogger(LinkisJobDataServiceImpl.class);

    private SearchService searchService = DefaultSearchService.getInstance();

    private static LinkisJobDataService linkisJobDataService;

    private LinkisJobDataServiceImpl() {

    }

    public static LinkisJobDataService getInstance() {
        if (null == linkisJobDataService) {
            synchronized (LinkisJobDataServiceImpl.class) {
                if (null == linkisJobDataService) {
                    linkisJobDataService = new LinkisJobDataServiceImpl();
                }
            }
        }
        return linkisJobDataService;
    }

    @Override
    public LinkisJobData getLinkisJobData(String contextIDStr, String contextKeyStr) throws CSErrorException {
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(contextKeyStr)) {
            return null;
        }
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
            return searchService.getContextValue(contextID, contextKey, LinkisJobData.class);
        } catch (ErrorException e) {
            logger.error("Deserialize failed, invalid contextId : " + contextIDStr + ", or contextKey : " + contextKeyStr + ", e : " + e.getMessage());
            logger.error("exception ", e);
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize failed, invalid contextId : " + contextIDStr + ", or contextKey : " + contextKeyStr + ", e : " + e.getMessage());
        }
    }

    @Override
    public void putLinkisJobData(String contextIDStr, String contextKeyStr, LinkisJobData linkisJobData) throws CSErrorException {
        ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
            ContextValue contextValue = new CommonContextValue();
            contextValue.setValue(linkisJobData);
            contextClient.update(contextID, contextKey, contextValue);
        } catch (ErrorException e) {
            logger.error("Deserialize error. e ", e);
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize error. e : " + e.getDesc());
        }
    }
}
