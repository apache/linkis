/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.metadatamanager.server.service.impl;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoQueryRequest;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoResponse;
import org.apache.linkis.metadatamanager.common.MdmConfiguration;
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadatamanager.server.service.MetadataAppService;
import org.apache.linkis.metadatamanager.common.protocol.*;
import org.apache.linkis.rpc.Sender;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetadataAppServiceImpl implements MetadataAppService {
    private Sender dataSourceRpcSender;

    @PostConstruct
    public void init(){
        dataSourceRpcSender = Sender.getSender(MdmConfiguration.DATA_SOURCE_SERVICE_APPLICATION.getValue());
    }
    @Override
    public List<String> getDatabasesByDsId(String dataSourceId, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            MetadataResponse metaQueryResponse = doAndGetMetaInfo(dsInfoResponse.dsType(),
                    new MetaGetDatabases(dsInfoResponse.params(), dsInfoResponse.creator()));
            return Json.fromJson(metaQueryResponse.data(), List.class, String.class);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getTablesByDsId(String dataSourceId, String database, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            MetadataResponse metaQueryResponse = doAndGetMetaInfo(dsInfoResponse.dsType(),
                    new MetaGetTables(dsInfoResponse.params(), database, dsInfoResponse.creator()));
            return Json.fromJson(metaQueryResponse.data(), List.class, String.class);
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, String> getTablePropsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            MetadataResponse metaQueryResponse = doAndGetMetaInfo(dsInfoResponse.dsType(),
                    new MetaGetTableProps(dsInfoResponse.params(), database, table, dsInfoResponse.creator()));
            return Json.fromJson(metaQueryResponse.data(), Map.class, String.class, String.class);
        }
        return new HashMap<>();
    }

    @Override
    public MetaPartitionInfo getPartitionsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            MetadataResponse metaQueryResponse = doAndGetMetaInfo(dsInfoResponse.dsType(),
                    new MetaGetPartitions(dsInfoResponse.params(), database, table, dsInfoResponse.creator()));
            return Json.fromJson(metaQueryResponse.data(), MetaPartitionInfo.class);
        }
        return new MetaPartitionInfo();
    }

    @Override
    public List<MetaColumnInfo> getColumns(String dataSourceId, String database, String table, String system) throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system);
        if(StringUtils.isNotBlank(dsInfoResponse.dsType())){
            MetadataResponse metaQueryResponse = doAndGetMetaInfo(dsInfoResponse.dsType(),
                    new MetaGetColumns(dsInfoResponse.params(), database, table, dsInfoResponse.creator()));
            return Json.fromJson(metaQueryResponse.data(), List.class, MetaColumnInfo.class);
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

    /**
     * Request to get meta information
     * @param dataSourceType
     * @param request
     * @return
     */
    public MetadataResponse doAndGetMetaInfo(String dataSourceType, MetadataQueryProtocol request) throws ErrorException {
        Sender sender = Sender.getSender(MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue() + "-" + dataSourceType.toLowerCase());
        Object rpcResult = null;
        try{
            rpcResult = sender.ask(request);
        }catch(Exception e){
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
        if(rpcResult instanceof MetadataResponse){
            MetadataResponse response = (MetadataResponse)rpcResult;
            if(!response.status()){
                throw new ErrorException(-1, "Error in ["+dataSourceType.toUpperCase()+"] Metadata Service Server[元数据服务出错], " +
                        "[" +response.data() + "]");
            }
            return response;
        }else{
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
    }
}
