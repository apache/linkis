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

package org.apache.linkis.engineplugin.doris.executor;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient;
import org.apache.linkis.datasource.client.request.GetInfoPublishedByDataSourceNameAction;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.engineplugin.doris.constant.DorisConstant;

import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DorisDatasourceParser {
  private static final Logger logger = LoggerFactory.getLogger(DorisDatasourceParser.class);

  public static Map<String, String> queryDatasourceInfoByName(
      String datasourceName, String username, String system) {
    logger.info(
        "Starting query ["
            + system
            + ", "
            + username
            + ", "
            + datasourceName
            + "] datasource info ......");
    LinkisDataSourceRemoteClient dataSourceClient = new LinkisDataSourceRemoteClient();
    DataSource dataSource =
        dataSourceClient
            .getInfoPublishedByDataSourceName(
                GetInfoPublishedByDataSourceNameAction.builder()
                    .setSystem(system)
                    .setDataSourceName(datasourceName)
                    .setUser(username)
                    .build())
            .getDataSource();

    return queryDatasourceParam(datasourceName, dataSource);
  }

  private static Map<String, String> queryDatasourceParam(
      String datasourceName, DataSource dataSource) {
    Map<String, String> paramMap = new HashMap<>();

    if (dataSource == null) {
      logger.warn("Doris dataSource is null: {}", datasourceName);
      return paramMap;
    }

    if (dataSource.isExpire()) {
      logger.warn("Doris dataSource of datasource name: {} is expired", datasourceName);
      return paramMap;
    }

    Map<String, Object> connectParams = dataSource.getConnectParams();
    if (MapUtils.isEmpty(connectParams)) {
      logger.warn("Doris dataSource connectParams is empty: {}", datasourceName);
      return paramMap;
    }

    paramMap.put(
        DorisConstant.DS_JDBC_HOST,
        String.valueOf(connectParams.getOrDefault(DorisConstant.DS_JDBC_HOST, "")));
    paramMap.put(
        DorisConstant.DS_JDBC_PORT,
        String.valueOf(connectParams.getOrDefault(DorisConstant.DS_JDBC_PORT, "")));
    paramMap.put(
        DorisConstant.DS_JDBC_USERNAME,
        String.valueOf(connectParams.getOrDefault(DorisConstant.DS_JDBC_USERNAME, "")));
    paramMap.put(
        DorisConstant.DS_JDBC_PASSWORD,
        String.valueOf(connectParams.getOrDefault(DorisConstant.DS_JDBC_PASSWORD, "")));
    paramMap.put(
        DorisConstant.DS_JDBC_DB_NAME,
        String.valueOf(
            connectParams.getOrDefault(
                DorisConstant.DS_JDBC_DB_NAME,
                connectParams.getOrDefault(DorisConstant.DS_JDBC_DB_INSTANCE, ""))));

    try {
      HashMap<String, String> printMap = new HashMap<>();
      printMap.putAll(paramMap);

      // To hide the password and prevent leaks
      if (printMap.containsKey(DorisConstant.DS_JDBC_PASSWORD)) {
        printMap.put(DorisConstant.DS_JDBC_PASSWORD, DorisConstant.DS_JDBC_PASSWORD_HIDE_VALUE);
      }
      String printMapString = JsonUtils.jackson().writeValueAsString(printMap);
      logger.info("Load dataSource param: {}", printMapString);
    } catch (JsonProcessingException e) {

    }

    return paramMap;
  }
}
