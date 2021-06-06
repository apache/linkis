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
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.client.utils.SerializeHelper;
import com.webank.wedatasphere.linkis.cs.common.entity.object.LinkisVariable;
import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.AbstractFunction1;

import java.util.ArrayList;
import java.util.List;

public class CSVariableService  implements VariableService{

    private final static Logger logger = LoggerFactory.getLogger(CSVariableService.class);

    private SearchService searchService = DefaultSearchService.getInstance();

    private static CSVariableService csVariableService;

    private CSVariableService(){

    }

    @Override
    public List<LinkisVariable> getUpstreamVariables(String contextIDStr, String nodeName) throws CSErrorException{
        List<LinkisVariable> rsList = new ArrayList<>();
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return rsList;
        }

        rsList = Utils.tryCatch(Utils.JFunction0(() -> {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            List<LinkisVariable> tmpRsList = new ArrayList<>();
            if (null != contextID) {
                tmpRsList = searchService.searchUpstreamContext(contextID, nodeName, Integer.MAX_VALUE, LinkisVariable.class);
            }
            return tmpRsList;
        }), new AbstractFunction1<Throwable, List<LinkisVariable>>() {
            @Override
            public List<LinkisVariable> apply(Throwable v1) {
                logger.error("Failed to get variable : " + contextIDStr, v1);
                return new ArrayList<>();
            }
        });

        return rsList;
    }

    @Override
    public void putVariable(String contextIDStr, String contextKeyStr, LinkisVariable linkisVariable) throws CSErrorException {
        ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
        Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
            ContextValue contextValue = new CommonContextValue();
            contextValue.setValue(linkisVariable);
            contextClient.update(contextID, contextKey, contextValue);
            return null;
        }),Utils.JFunction1(e->{
            logger.error("Deserialize error. e ");
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize error. e : ", e);
        }));
    }

    public static CSVariableService getInstance() {
        if (null == csVariableService) {
            synchronized (CSVariableService.class) {
                if (null == csVariableService) {
                    csVariableService = new CSVariableService();
                }
            }
        }
        return csVariableService;
    }
}
