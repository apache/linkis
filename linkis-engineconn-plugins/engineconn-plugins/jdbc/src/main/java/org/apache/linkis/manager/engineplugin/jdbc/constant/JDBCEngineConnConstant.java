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

package org.apache.linkis.manager.engineplugin.jdbc.constant;

public class JDBCEngineConnConstant {
    private JDBCEngineConnConstant() {}

    public static final String JDBC_DEFAULT_DATASOURCE_TAG = "jdbc";
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
    public static final String JDBC_SCRIPTS_EXEC_USER = "execUser";
    public static final String JDBC_ENGINE_RUN_TIME_DS = "wds.linkis.engine.runtime.datasource";

    public static final String JDBC_POOL_TEST_ON_BORROW = "wds.linkis.jdbc.pool.testOnBorrow";
    public static final String JDBC_POOL_TEST_ON_CREATE = "wds.linkis.jdbc.pool.testOnCreate";
    public static final String JDBC_POOL_TEST_ON_RETURN = "wds.linkis.jdbc.pool.testOnReturn";
    public static final String JDBC_POOL_TEST_WHILE_IDLE = "wds.linkis.jdbc.pool.testWhileIdle";
    public static final String JDBC_POOL_VALIDATION_QUERY = "wds.linkis.jdbc.pool.validationQuery";
    public static final String JDBC_POOL_DEFAULT_VALIDATION_QUERY = "select 1";
    public static final String JDBC_POOL_TIME_BETWEEN_EVIC_RUNS_MS =
            "wds.linkis.jdbc.pool.timeBetweenEvictionRunsMillis";
    public static final String JDBC_POOL_MAX_WAIT = "wds.linkis.jdbc.pool.maxWaitMillis";
    public static final String JDBC_POOL_MAX_IDLE = "wds.linkis.jdbc.pool.maxIdle";
    public static final String JDBC_POOL_MIN_IDLE = "wds.linkis.jdbc.pool.minIdle";
    public static final String JDBC_POOL_MAX_TOTAL = "wds.linkis.jdbc.pool.maxTotal";
    public static final String JDBC_POOL_MAX_CONN_LIFE_TIME =
            "wds.linkis.jdbc.pool.maxConnLifetime";

    public static final String DBCP2_POOL_URL = "url";
    public static final String DBCP2_POOL_USER = "user";
    public static final String DBCP2_POOL_PASSWORD = "password";
    public static final String DBCP2_POOL_DRIVER_CLASS_NAME = "driverClassName";

    public static final String JDBC_ENGINE_MEMORY_UNIT = "g";
}
