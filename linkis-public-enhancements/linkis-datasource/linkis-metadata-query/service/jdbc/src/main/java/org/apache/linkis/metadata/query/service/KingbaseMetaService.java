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

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadata.query.common.domain.GenerateSqlInfo;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractDbMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;
import org.apache.linkis.metadata.query.service.conf.SqlParamsMapper;
import org.apache.linkis.metadata.query.service.kingbase.SqlConnection;

import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KingbaseMetaService extends AbstractDbMetaService<SqlConnection> {

  @Override
  public MetadataConnection<SqlConnection> getConnection(
      String operator, Map<String, Object> params) throws Exception {
    String host =
        String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_HOST.getValue(), ""));
    // After deserialize, Integer will be Double, Why?
    Integer port =
        (Double.valueOf(
                String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PORT.getValue(), 0))))
            .intValue();
    String username =
        String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_USERNAME.getValue(), ""));
    String password =
        String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PASSWORD.getValue(), ""));

    String database =
        String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_DATABASE.getValue(), ""));
    Map<String, Object> extraParams = new HashMap<>();
    Object sqlParamObj = params.get(SqlParamsMapper.PARAM_SQL_EXTRA_PARAMS.getValue());
    if (null != sqlParamObj) {
      if (!(sqlParamObj instanceof Map)) {
        String paramStr = String.valueOf(sqlParamObj);
        if (StringUtils.isNotBlank(paramStr)) {
          extraParams = Json.fromJson(paramStr, Map.class, String.class, Object.class);
        }
      } else {
        extraParams = (Map<String, Object>) sqlParamObj;
      }
    }
    assert extraParams != null;
    return new MetadataConnection<>(
        new SqlConnection(host, port, username, password, database, extraParams));
  }

  @Override
  public List<String> queryDatabases(SqlConnection connection) {
    try {
      return connection.getAllDatabases();
    } catch (SQLException e) {
      throw new RuntimeException("Fail to get Sql databases(获取数据库列表失败)", e);
    }
  }

  @Override
  public List<String> queryTables(SqlConnection connection, String schemaname) {
    try {
      return connection.getAllTables(schemaname);
    } catch (SQLException e) {
      throw new RuntimeException("Fail to get Sql tables(获取表列表失败)", e);
    }
  }

  @Override
  public List<MetaColumnInfo> queryColumns(
      SqlConnection connection, String schemaname, String table) {
    try {
      return connection.getColumns(schemaname, table);
    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException("Fail to get Sql columns(获取字段列表失败)", e);
    }
  }

  @Override
  public String querySqlConnectUrl(SqlConnection connection) {
    return connection.getSqlConnectUrl();
  }

  @Override
  public GenerateSqlInfo queryJdbcSql(SqlConnection connection, String database, String table) {
    try {
      return connection.queryJdbcSql(database, table);
    } catch (Exception e) {
      throw new MetaRuntimeException("Fail to get jdbc sql (获取jdbcSql失败)", e);
    }
  }
}
