/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.query.server.service.impl;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.DataSources;
import org.apache.linkis.datasourcemanager.common.auth.AuthContext;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoQueryRequest;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoResponse;
import org.apache.linkis.metadata.query.common.MdmConfiguration;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.common.exception.MetaMethodInvokeException;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;
import org.apache.linkis.metadata.query.server.loader.MetaClassLoaderManager;
import org.apache.linkis.metadata.query.server.service.MetadataQueryService;
import org.apache.linkis.rpc.Sender;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.metadata.query.common.errorcode.LinkisMetadataQueryErrorCodeSummary.FAILED_METADATA_SERVICE;
import static org.apache.linkis.metadata.query.common.errorcode.LinkisMetadataQueryErrorCodeSummary.INVOKE_METHOD_FAIL;

@Service
public class MetadataQueryServiceImpl implements MetadataQueryService {
  private Sender dataSourceRpcSender;
  private MetaClassLoaderManager metaClassLoaderManager;
  private static final Logger logger = LoggerFactory.getLogger(MetadataQueryServiceImpl.class);

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
            dataSourceType, "getConnection", new Object[] {operator, params}, Map.class);
    if (Objects.nonNull(metadataConnection)) {
      Closeable connection = metadataConnection.getConnection();
      try {
        connection.close();
      } catch (IOException e) {
        logger.warn("Fail to close connection[关闭连接失败], [" + e.getMessage() + "]", e);
      }
    }
  }

  @Override
  @Deprecated
  public List<String> getDatabasesByDsId(String dataSourceId, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getDatabases",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams()},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  @Deprecated
  public List<String> getTablesByDsId(
      String dataSourceId, String database, String system, String userName) throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getTables",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  @Deprecated
  public Map<String, String> getPartitionPropsByDsId(
      String dataSourceId,
      String database,
      String table,
      String partition,
      String system,
      String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getPartitionProps",
          new Object[] {
            dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table, partition
          },
          Map.class);
    }
    return new HashMap<>();
  }

  @Override
  @Deprecated
  public Map<String, String> getTablePropsByDsId(
      String dataSourceId, String database, String table, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getTableProps",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table},
          Map.class);
    }
    return new HashMap<>();
  }

  @Override
  @Deprecated
  public MetaPartitionInfo getPartitionsByDsId(
      String dataSourceId,
      String database,
      String table,
      String system,
      Boolean traverse,
      String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getPartitions",
          new Object[] {
            dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table, traverse
          },
          MetaPartitionInfo.class);
    }
    return new MetaPartitionInfo();
  }

  @Override
  @Deprecated
  public List<MetaColumnInfo> getColumnsByDsId(
      String dataSourceId, String database, String table, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = reqToGetDataSourceInfo(dataSourceId, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getColumns",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> getDatabasesByDsName(String dataSourceName, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getDatabases",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams()},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> getDatabasesByDsNameAndEnvId(
      String dataSourceName, String system, String userName, String envId) throws ErrorException {
    DsInfoResponse dsInfoResponse =
        queryDataSourceInfoByNameAndEnvId(dataSourceName, system, userName, envId);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getDatabases",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams()},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getConnectionInfoByDsName(
      String dataSourceName, Map<String, String> queryParams, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getConnectionInfo",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), queryParams},
          Map.class);
    }
    return new HashMap<>();
  }

  @Override
  public List<String> getTablesByDsName(
      String dataSourceName, String database, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getTables",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> getTablesByDsNameAndEnvId(
      String dataSourceName, String database, String system, String userName, String envId)
      throws ErrorException {
    DsInfoResponse dsInfoResponse =
        queryDataSourceInfoByNameAndEnvId(dataSourceName, system, userName, envId);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getTables",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getPartitionPropsByDsName(
      String dataSourceName,
      String database,
      String table,
      String partition,
      String system,
      String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getPartitionProps",
          new Object[] {
            dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table, partition
          },
          Map.class);
    }
    return new HashMap<>();
  }

  @Override
  public Map<String, String> getTablePropsByDsName(
      String dataSourceName, String database, String table, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getTableProps",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table},
          Map.class);
    }
    return new HashMap<>();
  }

  @Override
  public MetaPartitionInfo getPartitionsByDsName(
      String dataSourceName,
      String database,
      String table,
      String system,
      Boolean traverse,
      String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getPartitions",
          new Object[] {
            dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table, traverse
          },
          MetaPartitionInfo.class);
    }
    return new MetaPartitionInfo();
  }

  @Override
  public List<MetaColumnInfo> getColumnsByDsName(
      String dataSourceName, String database, String table, String system, String userName)
      throws ErrorException {
    DsInfoResponse dsInfoResponse = queryDataSourceInfoByName(dataSourceName, system, userName);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getColumns",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table},
          List.class);
    }
    return new ArrayList<>();
  }

  @Override
  public List<MetaColumnInfo> getColumnsByDsNameAndEnvId(
      String dataSourceName,
      String database,
      String table,
      String system,
      String userName,
      String envId)
      throws ErrorException {
    DsInfoResponse dsInfoResponse =
        queryDataSourceInfoByNameAndEnvId(dataSourceName, system, userName, envId);
    if (StringUtils.isNotBlank(dsInfoResponse.getDsType())) {
      return invokeMetaMethod(
          dsInfoResponse.getDsType(),
          "getColumns",
          new Object[] {dsInfoResponse.getCreator(), dsInfoResponse.getParams(), database, table},
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
  @Deprecated
  public DsInfoResponse reqToGetDataSourceInfo(String dataSourceId, String system, String userName)
      throws ErrorException {
    Object rpcResult = null;
    try {
      rpcResult = dataSourceRpcSender.ask(new DsInfoQueryRequest(dataSourceId, null, system));
    } catch (Exception e) {
      throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
    }
    if (rpcResult instanceof DsInfoResponse) {
      DsInfoResponse response = (DsInfoResponse) rpcResult;
      if (!response.getStatus()) {
        throw new ErrorException(
            -1, "Error in Data Source Manager Server[数据源服务出错]: " + response.getErrorMsg());
      }
      boolean hasPermission =
          (AuthContext.isAdministrator(userName)
              || (StringUtils.isNotBlank(response.getCreator())
                  && userName.equals(response.getCreator())));
      if (!hasPermission) {
        throw new ErrorException(-1, "Don't have query permission for data source [没有数据源的查询权限]");
      } else if (response.getParams().isEmpty()) {
        throw new ErrorException(-1, "Have you published the data source? [数据源未发布或者参数为空]");
      }
      return response;
    } else {
      throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
    }
  }

  /**
   * Request to get data source information (type and connection parameters)
   *
   * @param dataSourceName data source name
   * @param system system
   * @return
   * @throws ErrorException
   */
  public DsInfoResponse queryDataSourceInfoByName(
      String dataSourceName, String system, String userName) throws ErrorException {
    return queryDataSourceInfoByNameAndEnvId(dataSourceName, system, userName, null);
  }

  /**
   * Request to get data source information (type and connection parameters)
   *
   * @param dataSourceName
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  public DsInfoResponse queryDataSourceInfoByNameAndEnvId(
      String dataSourceName, String system, String userName, String envId) throws ErrorException {
    Object rpcResult = null;
    boolean useDefault = false;
    try {
      rpcResult = reqGetDefaultDataSource(dataSourceName);
      if (Objects.isNull(rpcResult)) {
        rpcResult =
            dataSourceRpcSender.ask(new DsInfoQueryRequest(null, dataSourceName, system, envId));
      } else {
        useDefault = true;
      }
    } catch (Exception e) {
      throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
    }
    if (rpcResult instanceof DsInfoResponse) {
      DsInfoResponse response = (DsInfoResponse) rpcResult;
      if (!response.getStatus()) {
        throw new ErrorException(
            -1, "Error in Data Source Manager Server[数据源服务出错]: " + response.getErrorMsg());
      }
      boolean hasPermission =
          (AuthContext.isAdministrator(userName)
              || (StringUtils.isNotBlank(response.getCreator())
                  && userName.equals(response.getCreator())));
      if (!hasPermission) {
        throw new ErrorException(-1, "Don't have query permission for data source [没有数据源的查询权限]");
      } else if (!useDefault && response.getParams().isEmpty()) {
        throw new ErrorException(-1, "Have you published the data source? [数据源未发布或者参数为空]");
      }
      return response;
    } else {
      throw new ErrorException(-1, "Remote Service Error[远端服务出错, 联系运维处理]");
    }
  }

  /**
   * Request to get default data source
   *
   * @param dataSourceName data source name
   * @return response
   */
  private DsInfoResponse reqGetDefaultDataSource(String dataSourceName) {
    DataSource dataSource = DataSources.getDefault(dataSourceName);
    return (Objects.nonNull(dataSource))
        ? new DsInfoResponse(
            true,
            dataSource.getDataSourceType().getName(),
            dataSource.getConnectParams(),
            dataSource.getCreateUser(),
            "")
        : null;
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
          FAILED_METADATA_SERVICE.getErrorCode(),
          "Load meta service for " + dsType + " fail 加载 [" + dsType + "] 元数据服务失败",
          e);
    }
    if (Objects.nonNull(invoker)) {
      try {
        Object returnObj = invoker.apply(method, methodArgs);
        if (Objects.nonNull(returnType)) {
          return (T) returnObj;
        }
      } catch (Exception e) {
        if (e instanceof MetaRuntimeException) {
          throw new MetaMethodInvokeException(method, methodArgs, -1, e.getMessage(), e);
        }
        // TODO ERROR CODE
        throw new MetaMethodInvokeException(
            method,
            methodArgs,
            INVOKE_METHOD_FAIL.getErrorCode(),
            MessageFormat.format(INVOKE_METHOD_FAIL.getErrorDesc(), method, e.getMessage()),
            e);
      }
    }
    return null;
  }
}
