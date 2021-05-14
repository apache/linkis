/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContextHistoryClientServiceImpl implements ContextHistoryClientService {

    private final static Logger logger = LoggerFactory.getLogger(ContextHistoryClientServiceImpl.class);

    private ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();

    private static ContextHistoryClientService contextHistoryClientService;

    private ContextHistoryClientServiceImpl() {

    }

    public static ContextHistoryClientService getInstance() {
        if (null == contextHistoryClientService) {
            synchronized (ContextHistoryClientServiceImpl.class) {
                if (null == contextHistoryClientService) {
                    contextHistoryClientService = new ContextHistoryClientServiceImpl();
                }
            }
        }
        return contextHistoryClientService;
    }

    @Override
    public void createHistory(String contextIDStr, ContextHistory history) throws CSErrorException {
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextValue contextValue = new CommonContextValue();
            if (contextID instanceof CombinedNodeIDContextID) {
                contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
            }
            contextClient.createHistory(contextID, history);
        } catch (ErrorException e) {
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "createHistory error ", e);
        }
    }

}
