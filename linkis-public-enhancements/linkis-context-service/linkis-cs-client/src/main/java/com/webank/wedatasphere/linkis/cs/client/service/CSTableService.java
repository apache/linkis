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
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.CSTable;
import com.webank.wedatasphere.linkis.cs.common.entity.source.*;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.AbstractFunction1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class CSTableService implements TableService {

    private final static Logger logger = LoggerFactory.getLogger(CSTableService.class);

    private SearchService searchService = DefaultSearchService.getInstance();

    private static CSTableService csTableService;

    private CSTableService() {

    }

    public static CSTableService getInstance() {
        if (null == csTableService) {
            synchronized (CSTableService.class) {
                if (null == csTableService) {
                    csTableService = new CSTableService();
                }
            }
        }
        return csTableService;
    }

    @Override
    public CSTable getCSTable(ContextID contextID, ContextKey contextKey) throws CSErrorException {
        if (null == contextID || null == contextKey) {
            return null;
        }
        if (contextID instanceof CombinedNodeIDContextID) {
            contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
        }
        CSTable csTable = searchService.getContextValue(contextID, contextKey, CSTable.class);
        return csTable;
    }

    @Override
    public List<CSTable> getUpstreamTables(String contextIDStr, String nodeName) throws CSErrorException {
        List<CSTable> rsList = new ArrayList<>();
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return rsList;
        }
        return Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            List<CSTable> tmpRsList = new ArrayList<>();
            if (null != contextID) {
                if (contextID instanceof CombinedNodeIDContextID) {
                    contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
                }
                tmpRsList = searchService.searchUpstreamContext(contextID, nodeName, Integer.MAX_VALUE, CSTable.class);
            }
            return tmpRsList;
        }),Utils.JFunction1(e->{
            logger.error("Deserialize contextID error. contextIDStr : " + contextIDStr);
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getUpstreamTables error ", e);
        }));
    }

    @Override
    public CSTable getUpstreamSuitableTable(String contextIDStr, String nodeName, String keyword) throws CSErrorException {
        CSTable csTable = null;
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return csTable;
        }
        csTable = Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            CSTable tmpCsTable = null;
            if (null != contextID) {
                if (contextID instanceof CombinedNodeIDContextID) {
                    contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
                }
                tmpCsTable = searchService.searchContext(contextID, keyword, nodeName, CSTable.class);
            }
            return tmpCsTable;
        }),Utils.JFunction1(e->{
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getUpstreamSuitableTable error ", e);
        }));
        return csTable;
    }

    @Override
    public List<ContextKeyValue> searchUpstreamTableKeyValue(String contextIDStr, String nodeName) throws CSErrorException {
       return Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            if (contextID instanceof CombinedNodeIDContextID) {
                contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
            }
            return searchService.searchUpstreamKeyValue(contextID, nodeName, Integer.MAX_VALUE, CSTable.class);
        }),Utils.JFunction1(v1->{
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Failed to searchUpstreamTableKeyValue ", v1);
        }));
    }


    @Override
    public void putCSTable(String contextIDStr, String contextKeyStr, CSTable csTable) throws CSErrorException {
        ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
        Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
            ContextValue contextValue = new CommonContextValue();
            // todo check keywords
            contextValue.setKeywords("");
            contextValue.setValue(csTable);
            if (contextID instanceof CombinedNodeIDContextID) {
                contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
            }
            contextClient.update(contextID, contextKey, contextValue);
            return null;
        }),Utils.JFunction1(v1->{
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "putCSTable error ", v1);
        }));
    }

    @Override
    public CSTable getCSTable(String contextIDStr, String contextKeyStr) throws CSErrorException {
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(contextKeyStr)) {
            return null;
        }
       return Utils.tryCatch(Utils.JFunction0(()->{
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
            if (contextID instanceof CombinedNodeIDContextID) {
                contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
            }
            return getCSTable(contextID, contextKey);
        }),Utils.JFunction1(e->{
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getCSTable error ", e);
        }));
    }


    @Override
    public void registerCSTable(String contextIDStr, String nodeName, String alias, CSTable csTable) throws CSErrorException {

        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return;
        }
        AtomicReference<String> tableName = new AtomicReference<>("");
        if (StringUtils.isNotBlank(alias)) {
            tableName.set(CSCommonUtils.CS_TMP_TABLE_PREFIX + nodeName + "_" + alias);
        } else {
            for (int i = 1; i < 10; i++) {
                String tmpTable = CSCommonUtils.CS_TMP_TABLE_PREFIX + nodeName + "_rs" + i;
                Integer status = Utils.tryCatch(Utils.JFunction0(()->{
                    ContextKey contextKey = new CommonContextKey();
                    contextKey.setContextScope(ContextScope.PUBLIC);
                    contextKey.setContextType(ContextType.METADATA);
                    contextKey.setKey(CSCommonUtils.getTableKey(nodeName, tmpTable));
                    CSTable oldCsTable = getCSTable(contextIDStr, SerializeHelper.serializeContextKey(contextKey));
                    if (null == oldCsTable) {
                        tableName.set(tmpTable);
                        return 0;
                    }
                    return null;
                }),new AbstractFunction1<Throwable,Integer>(){
                    @Override
                    public Integer apply(Throwable v1) {
                        tableName.set(tmpTable);
                        logger.warn("Failed to build tmp tableName", v1);
                        return 0;
                    }
                });
                if(status != null){
                    break;
                }
            }
        }

        Utils.tryCatch(Utils.JFunction0(()->{
            csTable.setName(tableName.get());
            ContextKey contextKey = new CommonContextKey();
            contextKey.setContextScope(ContextScope.PUBLIC);
            contextKey.setContextType(ContextType.METADATA);
            contextKey.setKey(CSCommonUtils.getTableKey(nodeName, tableName.get()));
            putCSTable(contextIDStr, SerializeHelper.serializeContextKey(contextKey), csTable);
            return null;
        }),Utils.JFunction1(e->{
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Failed to register cs tmp table ", e);
        }));

    }
}
