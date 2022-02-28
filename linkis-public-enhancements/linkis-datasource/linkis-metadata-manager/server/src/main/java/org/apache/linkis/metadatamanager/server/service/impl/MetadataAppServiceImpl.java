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
import org.apache.linkis.datasourcemanager.common.auth.AuthContext;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoQueryRequest;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoResponse;
import org.apache.linkis.metadatamanager.common.MdmConfiguration;
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadatamanager.common.exception.MetaMethodInvokeException;
import org.apache.linkis.metadatamanager.common.exception.MetaRuntimeException;
import org.apache.linkis.metadatamanager.common.service.MetadataConnection;
import org.apache.linkis.metadatamanager.server.loader.MetaClassLoaderManager;
import org.apache.linkis.metadatamanager.server.service.MetadataAppService;
import org.apache.linkis.rpc.Sender;

import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

@Service
public class MetadataAppServiceImpl implements MetadataAppService {
    private Sender dataSourceRpcSender;
    private MetaClassLoaderManager metaClassLoaderManager;
    private static final Logger LOG = LoggerFactory.getLogger(MetadataAppServiceImpl.class);

    @PostConstruct
    public void init() {
        dataSourceRpcSender =
                Sender.getSender(MdmConfiguration.DATA_SOURCE_SERVICE_APPLICATION.getValue());
        metaClassLoaderManager = new MetaClassLoaderManager();
    }

    @Override
    public void getConnection(String dataSourceType, String operator, Map<String, Object> params)
            throws Exception {
        MetadataConnection<Closeable> metadataConnection =
                invokeMetaMethod(
                        dataSourceType,
                        "getConnection",
                        new Object[] {operator, params},
                        Map.class);
        if (Objects.nonNull(metadataConnection)) {
            Closeable connection = metadataConnection.getConnection();
            try {
                connection.close();
            } catch (IOException e) {
                LOG.warn("Fail to close connection[关闭连接失败], [" + e.getMessage() + "]", e);
            }
        }
    }

    @Override
    public List<String> getDatabasesByDsId(String dataSourceId, String system, String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getDatabases",
                    new Object[] {dsInfoResponse.creator(), dsInfoResponse.params()},
                    List.class);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getTablesByDsId(
            String dataSourceId, String database, String system, String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getTables",
                    new Object[] {dsInfoResponse.creator(), dsInfoResponse.params(), database},
                    List.class);
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, String> getPartitionPropsByDsId(
            String dataSourceId,
            String database,
            String table,
            String partition,
            String system,
            String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getPartitionProps",
                    new Object[] {
                        dsInfoResponse.creator(),
                        dsInfoResponse.params(),
                        database,
                        table,
                        partition
                    },
                    Map.class);
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getTablePropsByDsId(
            String dataSourceId, String database, String table, String system, String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getTableProps",
                    new Object[] {
                        dsInfoResponse.creator(), dsInfoResponse.params(), database, table
                    },
                    Map.class);
        }
        return new HashMap<>();
    }

    @Override
    public MetaPartitionInfo getPartitionsByDsId(
            String dataSourceId, String database, String table, String system, String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getPartitions",
                    new Object[] {
                        dsInfoResponse.creator(), dsInfoResponse.params(), database, table
                    },
                    MetaPartitionInfo.class);
        }
        return new MetaPartitionInfo();
    }

    @Override
    public List<MetaColumnInfo> getColumns(
            String dataSourceId, String database, String table, String system, String userName)
            throws ErrorException {
        DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
        if (StringUtils.isNotBlank(dsInfoResponse.dsType())) {
            return invokeMetaMethod(
                    dsInfoResponse.dsType(),
                    "getColumns",
                    new Object[] {
                        dsInfoResponse.creator(), dsInfoResponse.params(), database, table
                    },
                    List.class);
        }
        return new ArrayList<>();
    }

    /**
     * Request to get data source information (type and connection parameters)
     *
     * @param dataSourceId data source id
     * @param system system
     * @return
     * @throws ErrorException
     */
    public DsInfoResponse reqToGetDataSourceInfo(
            String dataSourceId, String system, String userName) throws ErrorException {
        Object rpcResult = null;
        try {
            rpcResult = dataSourceRpcSender.ask(new DsInfoQueryRequest(dataSourceId, null, system));
        } catch (Exception e) {
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
        if (rpcResult instanceof DsInfoResponse) {
            DsInfoResponse response = (DsInfoResponse) rpcResult;
            if (!response.status()) {
                throw new ErrorException(-1, "Error in Data Source Manager Server[数据源服务出错]");
            }
            boolean hasPermission =
                    (AuthContext.isAdministrator(userName)
                            || (StringUtils.isNotBlank(response.creator())
                                    && userName.equals(response.creator())));
            if (!hasPermission) {
                throw new ErrorException(
                        -1, "Don't have query permission for data source [没有数据源的查询权限]");
            } else if (response.params().isEmpty()) {
                throw new ErrorException(-1, "Have you published the data source? [数据源未发布或者参数为空]");
            }
            return response;
        } else {
            throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
        }
    }

    /**
     * Invoke method in meta service
     *
     * @param method method name
     * @param methodArgs arguments
     */
    @SuppressWarnings("unchecked")
    private <T> T invokeMetaMethod(
            String dsType, String method, Object[] methodArgs, Class<?> returnType)
            throws MetaMethodInvokeException {
        BiFunction<String, Object[], Object> invoker;
        try {
            invoker = metaClassLoaderManager.getInvoker(dsType);
        } catch (Exception e) {
            // TODO ERROR CODE
            throw new MetaMethodInvokeException(
                    -1, "Load meta service for " + dsType + " fail 加载 [" + dsType + "] 元数据服务失败", e);
        }
        if (Objects.nonNull(invoker)) {
            try {
                Object returnObj = invoker.apply(method, methodArgs);
                if (Objects.nonNull(returnType)) {
                    return (T) returnObj;
                }
            } catch (Exception e) {
                if (e instanceof MetaRuntimeException) {
                    throw new MetaMethodInvokeException(
                            method, methodArgs, -1, e.getMessage(), e.getCause());
                }
                // TODO ERROR CODE
                throw new MetaMethodInvokeException(
                        method,
                        methodArgs,
                        -1,
                        "Invoke method [" + method + "] fail, message:[" + e.getMessage() + "]",
                        e);
            }
        }
        return null;
    }
}
