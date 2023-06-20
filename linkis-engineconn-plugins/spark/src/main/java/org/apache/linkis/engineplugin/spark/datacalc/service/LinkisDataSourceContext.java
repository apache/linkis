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

package org.apache.linkis.engineplugin.spark.datacalc.service;

import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.engineplugin.spark.datacalc.exception.DatabaseNotSupportException;
import org.apache.linkis.engineplugin.spark.datacalc.model.DataCalcDataSource;
import org.apache.linkis.engineplugin.spark.datacalc.service.strategy.*;
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LinkisDataSourceContext {

  private static final Map<String, DataSourceStrategy> dsStrategyMap = new HashMap<>();

  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    dsStrategyMap.put("mysql", new MySqlStrategy());
    // https://docs.pingcap.com/tidb/dev/dev-guide-connect-to-tidb
    dsStrategyMap.put("tidb", new TiDBStrategy());
    dsStrategyMap.put("doris", new DorisStrategy());
    dsStrategyMap.put("starrocks", new DorisStrategy());
    dsStrategyMap.put("dm", new DmStrategy());
    dsStrategyMap.put("kingbase", new KingbaseStrategy());
    // https://jdbc.postgresql.org/documentation/use/
    dsStrategyMap.put("postgresql", new PostgreSqlStrategy());
    dsStrategyMap.put("gaussdb", new PostgreSqlStrategy());
    // https://github.com/ClickHouse/clickhouse-jdbc/tree/master/clickhouse-jdbc
    dsStrategyMap.put("clickhouse", new ClickHouseStrategy());
    // https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/jdbc/OracleDriver.html
    dsStrategyMap.put("oracle", new OracleStrategy());
    // https://learn.microsoft.com/zh-cn/sql/connect/jdbc/building-the-connection-url?redirectedfrom=MSDN&view=sql-server-ver16
    dsStrategyMap.put("sqlserver", new SqlServerStrategy());
    // https://www.ibm.com/docs/en/db2/11.5?topic=cdsudidsdjs-url-format-data-server-driver-jdbc-sqlj-type-4-connectivity
    dsStrategyMap.put("db2", new DB2Strategy());
  }

  private final DataSourceStrategy dataSourceStrategy;

  private final DataSource datasource;

  public LinkisDataSourceContext(DataSource ds) {
    this.datasource = ds;
    String databaseType = ds.getDataSourceType() == null ? "" : ds.getDataSourceType().getName();
    this.dataSourceStrategy = getDataSourceStrategy(databaseType);
  }

  private DataSourceStrategy getDataSourceStrategy(String databaseType) {
    if (dsStrategyMap.containsKey(databaseType)) {
      return dsStrategyMap.get(databaseType);
    } else {
      int code = SparkErrorCodeSummary.DATA_CALC_DATABASE_NOT_SUPPORT.getErrorCode();
      String errDesc = SparkErrorCodeSummary.DATA_CALC_DATABASE_NOT_SUPPORT.getErrorDesc();
      String msg = MessageFormat.format(errDesc, datasource.getDataSourceName(), databaseType);
      throw new DatabaseNotSupportException(code, msg);
    }
  }

  public DataCalcDataSource getDataCalcDataSource() {
    Map<String, Object> connectParams =
        datasource.getConnectParams(); // this should return Map<String, String>
    Map<String, String> params = new HashMap<>(0);
    if (connectParams != null) {
      params =
          connectParams.entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }
    String defaultDriver = dataSourceStrategy.defaultDriver();
    String paramsJson = params.getOrDefault("params", "{}");
    String defaultPort = dataSourceStrategy.defaultPort();

    String address = params.getOrDefault("address", "");
    if (StringUtils.isBlank(address)) {
      String host = params.getOrDefault("host", "");
      String port = params.getOrDefault("port", defaultPort);
      address = host + ":" + port;
    }

    DataCalcDataSource ds = new DataCalcDataSource();
    ds.setDriver(params.getOrDefault("driverClassName", defaultDriver));
    ds.setUser(params.getOrDefault("username", ""));
    ds.setPassword(params.getOrDefault("password", ""));
    ds.setUrl(dataSourceStrategy.getJdbcUrl(address, params, paramsJson));
    return ds;
  }
}
