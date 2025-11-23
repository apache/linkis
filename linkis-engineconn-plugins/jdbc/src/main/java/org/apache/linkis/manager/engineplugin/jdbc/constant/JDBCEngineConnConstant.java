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

package org.apache.linkis.manager.engineplugin.jdbc.constant;

public class JDBCEngineConnConstant {
  private JDBCEngineConnConstant() {}

  public static final String JDBC_DEFAULT_DATASOURCE_TAG = "jdbc";
  public static final String JDBC_PROXY_ANONYMOUS_USER = "anonymous";
  public static final String JDBC_URL = "wds.linkis.jdbc.connect.url";
  public static final String JDBC_DRIVER = "wds.linkis.jdbc.driver";
  public static final String JDBC_USERNAME = "wds.linkis.jdbc.username";
  public static final String JDBC_PASSWORD = "wds.linkis.jdbc.password";
  public static final String JDBC_AUTH_TYPE = "wds.linkis.jdbc.auth.type";
  public static final String JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL = "wds.linkis.jdbc.principal";
  public static final String JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION =
      "wds.linkis.jdbc.keytab.location";
  public static final String JDBC_KERBEROS_AUTH_PROXY_ENABLE =
      "wds.linkis.jdbc.auth.kerberos.proxy.enable";
  public static final String JDBC_PROXY_USER_PROPERTY = "wds.linkis.jdbc.proxy.user.property";
  public static final String JDBC_PROXY_USER = "wds.linkis.jdbc.proxy.user";

  public static final String JDBC_CONNECTION_TIMEOUT = "wds.linkis.jdbc.connection.timeout";

  public static final String JDBC_SOCKET_TIMEOUT = "wds.linkis.jdbc.socket.timeout";

  public static final String JDBC_SCRIPTS_EXEC_USER = "execUser";
  public static final String JDBC_ENGINE_RUN_TIME_DS = "wds.linkis.engine.runtime.datasource";
  public static final String JDBC_ENGINE_RUN_TIME_DS_MAX_VERSION_ID =
      "wds.linkis.engine.runtime.datasource.maxVersionId";
  public static final String JDBC_ENGINE_RUN_TIME_DS_SYSTEM_QUERY_PARAM =
      "wds.linkis.engine.runtime.datasource.systemQueryParam";

  public static final String JDBC_ENGINE_RUN_TIME_DS_TYPE = "linkis.datasource.type";
  public static final String JDBC_ENGINE_RUN_TIME_DS_PARAM_HOST = "linkis.datasource.params.host";
  public static final String JDBC_ENGINE_RUN_TIME_DS_PARAM_PORT = "linkis.datasource.params.port";
  public static final String JDBC_ENGINE_RUN_TIME_DS_PARAM_USERNAME =
      "linkis.datasource.params.username";

  public static final String JDBC_POOL_TEST_ON_BORROW = "wds.linkis.jdbc.pool.testOnBorrow";
  public static final String JDBC_POOL_TEST_ON_RETURN = "wds.linkis.jdbc.pool.testOnReturn";
  public static final String JDBC_POOL_TEST_WHILE_IDLE = "wds.linkis.jdbc.pool.testWhileIdle";
  public static final String JDBC_POOL_VALIDATION_QUERY = "wds.linkis.jdbc.pool.validationQuery";
  public static final String JDBC_POOL_DEFAULT_VALIDATION_QUERY = "SELECT 1";
  public static final String JDBC_POOL_TIME_BETWEEN_MIN_EVIC_IDLE_MS =
      "wds.linkis.jdbc.pool.minEvictableIdleTimeMillis";
  public static final String JDBC_POOL_TIME_BETWEEN_EVIC_RUNS_MS =
      "wds.linkis.jdbc.pool.timeBetweenEvictionRunsMillis";
  public static final String JDBC_POOL_MAX_WAIT = "wds.linkis.jdbc.pool.maxWaitMillis";
  public static final String JDBC_POOL_MAX_ACTIVE = "wds.linkis.jdbc.pool.maxActive";
  public static final String JDBC_POOL_INIT_SIZE = "wds.linkis.jdbc.pool.initialSize";
  public static final String JDBC_POOL_MIN_IDLE = "wds.linkis.jdbc.pool.minIdle";
  public static final String JDBC_POOL_PREPARED_STATEMENTS =
      "wds.linkis.jdbc.pool.poolPreparedStatements";
  public static final String JDBC_POOL_REMOVE_ABANDONED_ENABLED =
      "wds.linkis.jdbc.pool.remove.abandoned.enabled";
  public static final String JDBC_POOL_REMOVE_ABANDONED_LOG_ENABLED =
      "wds.linkis.jdbc.pool.remove.abandoned.log.enabled";
  public static final String JDBC_POOL_REMOVE_ABANDONED_TIMEOUT = "linkisJDBCPoolAbandonedTimeout";

  public static final String DS_JDBC_HOST = "host";
  public static final String DS_JDBC_PORT = "port";
  public static final String DS_JDBC_DB_NAME = "databaseName";
  public static final String DS_JDBC_USERNAME = "username";
  public static final String DS_JDBC_PASSWORD = "password";
  public static final String DS_JDBC_ENABLE_KERBEROS = "enableKerberos";
  public static final String DS_JDBC_KERBEROS_PRINCIPAL = "kerberosPrincipal";
  public static final String DS_JDBC_KERBEROS_KEYTAB = "kerberosKeytab";
  public static final String DS_JDBC_ENABLE_KERBEROS_PROXY_USER = "enableKerberosProxyUser";
  public static final String DS_JDBC_KERBEROS_PROXY_USER_PROPERTY = "kerberosProxyUserProperty";
  public static final String DS_JDBC_PARAMS = "params";
  public static final String DS_JDBC_DRIVER = "driverClassName";

  public static final String JDBC_ENGINE_MEMORY_UNIT = "g";
}
