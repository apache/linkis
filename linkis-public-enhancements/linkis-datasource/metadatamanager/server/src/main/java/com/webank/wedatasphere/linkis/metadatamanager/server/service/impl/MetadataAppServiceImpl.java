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

package com.webank.wedatasphere.linkis.metadatamanager.server.service.impl;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.datasourcemanager.common.protocol.DsInfoQueryRequest;
import com.webank.wedatasphere.linkis.datasourcemanager.common.protocol.DsInfoResponse;
import com.webank.wedatasphere.linkis.metadatamanager.common.MdmConfiguration;
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaColumnInfo;
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import com.webank.wedatasphere.linkis.metadatamanager.common.service.MetadataConnection;
import com.webank.wedatasphere.linkis.metadatamanager.server.loader.MetaClassLoaderManager;
import com.webank.wedatasphere.linkis.metadatamanager.server.service.MetadataAppService;
import com.webank.wedatasphere.linkis.rpc.Sender;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
public class MetadataAppServiceImpl implements MetadataAppService {
    private Sender dataSourceRpcSender;
    private MetaClassLoaderManager metaClassLoaderManager;

    @PostConstruct
    public void init(){
        dataSourceRpcSender = Sender.getSender(MdmConfiguration.DATA_SOURCE_SERVICE_APPLICATION.getValue());
        metaClassLoaderManager = new MetaClassLoaderManager();
    }


    @Override
    public void getConnection(String dataSourceType, String operator, Map<String, Object> params) throws Exception {
        BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dataSourceType);
        invoker.apply("getConnection", new Object[]{operator, params});
    }

    @Override
    public List<String> getDatabasesByDsId(String dataSourceId, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dsInfoResponse.dsType());
            return (List<String>)invoker.apply("getDatabases", new Object[]{dsInfoResponse.creator(), dsInfoResponse.params()});
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getTablesByDsId(String dataSourceId, String database, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dsInfoResponse.dsType());
            return (List<String>)invoker.apply("getTables", new Object[]{dsInfoResponse.creator(), dsInfoResponse.params(), database});
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, String> getTablePropsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dsInfoResponse.dsType());
            return (Map<String, String>)invoker.apply("getTableProps", new Object[]{dsInfoResponse.creator(), dsInfoResponse.params(), database, table});
        }
        return new HashMap<>();
    }

    @Override
    public MetaPartitionInfo getPartitionsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dsInfoResponse.dsType());
            return (MetaPartitionInfo)invoker.apply("getPartitions", new Object[]{dsInfoResponse.creator(), dsInfoResponse.params(), database, table});
        }
        return new MetaPartitionInfo();
    }

    @Override
    public List<MetaColumnInfo> getColumns(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            BiFunction<String, Object[], Object> invoker = metaClassLoaderManager.getInvoker(dsInfoResponse.dsType());
            return (List<MetaColumnInfo>)invoker.apply("getColumns", new Object[]{dsInfoResponse.creator(), dsInfoResponse.params(), database, table});
        }
        return new ArrayList<>();
    }

    /**
     * Request to get data source information
     * (type and connection parameters)
     * @param dataSourceId data source id
     * @param system system
     * @return
     * @throws ErrorException
     */
    public DsInfoResponse reqToGetDataSourceInfo(String dataSourceId, String system) throws ErrorException{
        Object rpcResult = null;
        try {
            rpcResult = dataSourceRpcSender.ask(new DsInfoQueryRequest(dataSourceId, system));
        }catch(Exception e){
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
        if(rpcResult instanceof DsInfoResponse){
            DsInfoResponse response = (DsInfoResponse)rpcResult;
            if(!response.status()){
                throw new ErrorException(-1, "Error in Data Source Manager Server[数据源服务出错]");
            }
            return response;
        }else{
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
    }

}
