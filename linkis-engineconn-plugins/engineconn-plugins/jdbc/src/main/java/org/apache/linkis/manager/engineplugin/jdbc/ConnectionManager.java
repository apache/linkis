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

package org.apache.linkis.manager.engineplugin.jdbc;

import org.apache.linkis.hadoop.common.utils.KerberosUtils;
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.hadoop.security.UserGroupInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedExceptionAction;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType.*;

public class ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private static final String DBCP_STRING = "jdbc:apache:commons:dbcp:";
    private final JDBCDataSourceConfigurations jdbcDataSourceConfigurations;

    private static volatile ConnectionManager connectionManager;
    private ScheduledExecutorService scheduledExecutorService;
    private Integer kinitFailCount = 0;

    private ConnectionManager() {
        jdbcDataSourceConfigurations = new JDBCDataSourceConfigurations();
    }

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager();
                }
            }
        }
        return connectionManager;
    }

    public void initTaskStatementMap() {
        try {
            jdbcDataSourceConfigurations.initTaskIdStatementMap();
        } catch (Exception e) {
            LOG.error("Error while closing taskIdStatementMap statement...", e);
        }
    }

    private void initConnectionPoolMap() {
        for (String datasource : jdbcDataSourceConfigurations.getDBDriverPool().keySet()) {
            try {
                closeDBPool(datasource);
            } catch (SQLException e) {
                LOG.error("Error while closing database pool.", e);
            }
        }
        try {
            jdbcDataSourceConfigurations.initConnectionPoolMap();
        } catch (SQLException e) {
            LOG.error("Error while closing initConnectionPoolMap.", e);
        }
    }

    public void saveStatement(String taskId, Statement statement) {
        jdbcDataSourceConfigurations.saveStatement(taskId, statement);
    }

    public void removeStatement(String taskId) {
        jdbcDataSourceConfigurations.removeStatement(taskId);
    }

    public void cancelStatement(String taskId) {
        try {
            jdbcDataSourceConfigurations.cancelStatement(taskId);
        } catch (SQLException e) {
            LOG.error("Error while cancelling task is {} ...", taskId, e);
        }
    }

    public void close() {
        try {
            initTaskStatementMap();
            initConnectionPoolMap();
        } catch (Exception e) {
            LOG.error("Error while closing...", e);
        }
    }

    public void closeDBPool(String dataSource) throws SQLException {
        PoolingDriver poolingDriver = jdbcDataSourceConfigurations.removeDBDriverPool(dataSource);
        if (poolingDriver != null) {
            poolingDriver.closePool(dataSource);
        }
    }

    private void configConnectionPool(GenericObjectPool connectionPool, Map<String, String> prop) {

        boolean testOnBorrow =
                JDBCPropertiesParser.getBool(
                        prop, JDBCEngineConnConstant.JDBC_POOL_TEST_ON_BORROW, false);
        boolean testOnCreate =
                JDBCPropertiesParser.getBool(
                        prop, JDBCEngineConnConstant.JDBC_POOL_TEST_ON_CREATE, false);
        boolean testOnReturn =
                JDBCPropertiesParser.getBool(
                        prop, JDBCEngineConnConstant.JDBC_POOL_TEST_ON_RETURN, false);
        boolean testWhileIdle =
                JDBCPropertiesParser.getBool(
                        prop, JDBCEngineConnConstant.JDBC_POOL_TEST_WHILE_IDLE, true);
        long timeBetweenEvictionRunsMillis =
                JDBCPropertiesParser.getLong(
                        prop, JDBCEngineConnConstant.JDBC_POOL_TIME_BETWEEN_EVIC_RUNS_MS, -1L);

        long maxWaitMillis =
                JDBCPropertiesParser.getLong(prop, JDBCEngineConnConstant.JDBC_POOL_MAX_WAIT, -1L);
        int maxIdle =
                JDBCPropertiesParser.getInt(prop, JDBCEngineConnConstant.JDBC_POOL_MAX_IDLE, 8);
        int minIdle =
                JDBCPropertiesParser.getInt(prop, JDBCEngineConnConstant.JDBC_POOL_MIN_IDLE, 0);
        int maxTotal =
                JDBCPropertiesParser.getInt(prop, JDBCEngineConnConstant.JDBC_POOL_MAX_TOTAL, -1);

        connectionPool.setTestOnBorrow(testOnBorrow);
        connectionPool.setTestOnCreate(testOnCreate);
        connectionPool.setTestOnReturn(testOnReturn);
        connectionPool.setTestWhileIdle(testWhileIdle);
        connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMinIdle(minIdle);
        connectionPool.setMaxTotal(maxTotal);
        connectionPool.setMaxWaitMillis(maxWaitMillis);
    }

    private void createConnectionPool(
            String url,
            String driverClass,
            String execUser,
            String dataSourceName,
            Map<String, String> prop)
            throws SQLException, ClassNotFoundException {
        LOG.info(
                "Creating connection pool for url: {}, user:{}, dataSourceName: {}, properties: {}",
                url,
                execUser,
                dataSourceName,
                prop);
        Properties cdbp2Properties = new Properties();
        cdbp2Properties.put(JDBCEngineConnConstant.DBCP2_POOL_URL, url);
        cdbp2Properties.put(JDBCEngineConnConstant.DBCP2_POOL_DRIVER_CLASS_NAME, driverClass);
        cdbp2Properties.put(
                JDBCEngineConnConstant.DBCP2_POOL_USER,
                JDBCPropertiesParser.getString(prop, JDBCEngineConnConstant.JDBC_USERNAME, ""));
        cdbp2Properties.put(
                JDBCEngineConnConstant.DBCP2_POOL_PASSWORD,
                JDBCPropertiesParser.getString(prop, JDBCEngineConnConstant.JDBC_PASSWORD, ""));

        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(url, cdbp2Properties);
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);
        final long maxConnectionLifetime =
                JDBCPropertiesParser.getLong(
                        prop, JDBCEngineConnConstant.JDBC_POOL_MAX_CONN_LIFE_TIME, -1L);
        poolableConnectionFactory.setMaxConnLifetimeMillis(maxConnectionLifetime);
        poolableConnectionFactory.setValidationQuery(
                JDBCPropertiesParser.getString(prop, "validationQuery", "select 1"));
        ObjectPool connectionPool = new GenericObjectPool(poolableConnectionFactory);
        this.configConnectionPool((GenericObjectPool) connectionPool, prop);

        poolableConnectionFactory.setPool(connectionPool);
        Class.forName(driverClass);
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(dataSourceName, connectionPool);
        jdbcDataSourceConfigurations.saveDBDriverPool(dataSourceName, driver);
    }

    private Connection getConnectionFromPool(
            String url,
            String driverClass,
            String execUser,
            String dataSourceName,
            Map<String, String> prop)
            throws SQLException, ClassNotFoundException {
        String jdbcDriverName = DBCP_STRING + dataSourceName;
        if (!jdbcDataSourceConfigurations.isConnectionInDBDriverPool(dataSourceName)) {
            createConnectionPool(url, driverClass, execUser, dataSourceName, prop);
        }
        return DriverManager.getConnection(jdbcDriverName);
    }

    public Connection getConnection(String dataSource, Map<String, String> prop)
            throws SQLException, JDBCParamsIllegalException, ClassNotFoundException {
        String driverClass =
                JDBCPropertiesParser.getString(prop, JDBCEngineConnConstant.JDBC_DRIVER, "");
        if (StringUtils.isBlank(driverClass)) {
            LOG.error("No such driverClass: {}", driverClass);
            throw new JDBCParamsIllegalException("No driverClass");
        }
        String execUser =
                JDBCPropertiesParser.getString(
                        prop, JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER, "");
        if (StringUtils.isBlank(execUser)) {
            LOG.warn("No such execUser: {}", execUser);
            throw new JDBCParamsIllegalException("No execUser");
        }
        if (StringUtils.isBlank(dataSource)) {
            LOG.warn("No such dataSource: {}", dataSource);
            throw new JDBCParamsIllegalException("No dataSource");
        }
        Connection connection = null;
        final String url = getJdbcUrl(prop);
        JdbcAuthType jdbcAuthType = getJdbcAuthType(prop);
        switch (jdbcAuthType) {
            case SIMPLE:
            case USERNAME:
                connection = getConnectionFromPool(url, driverClass, execUser, dataSource, prop);
                break;
            case KERBEROS:
                LOG.debug(
                        "Calling createKerberosSecureConfiguration(); this will do loginUserFromKeytab() if required");
                final String keytab =
                        JDBCPropertiesParser.getString(
                                prop,
                                JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION,
                                "");
                final String principal =
                        JDBCPropertiesParser.getString(
                                prop, JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL, "");
                KerberosUtils.createKerberosSecureConfiguration(keytab, principal);
                LOG.debug("createKerberosSecureConfiguration() returned");
                boolean isProxyEnabled =
                        JDBCPropertiesParser.getBool(
                                prop, JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_PROXY_ENABLE, true);
                String proxyUserProperty =
                        JDBCPropertiesParser.getString(
                                prop, JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, "");
                if (isProxyEnabled && StringUtils.isNotBlank(proxyUserProperty)) {
                    String finalUrl = url.concat(";").concat(proxyUserProperty + "=" + execUser);
                    prop.put("url", finalUrl);
                    LOG.info(
                            String.format(
                                    "Try to Create a new %s JDBC DBCP with url(%s), kerberos, proxyUser(%s).",
                                    dataSource, finalUrl, execUser));
                    connection =
                            getConnectionFromPool(
                                    finalUrl, driverClass, execUser, dataSource, prop);
                } else {
                    UserGroupInformation ugi;
                    try {
                        ugi =
                                UserGroupInformation.createProxyUser(
                                        execUser, UserGroupInformation.getCurrentUser());
                    } catch (Exception e) {
                        LOG.error("Error in getCurrentUser", e);
                        throw new JDBCParamsIllegalException("Error in getCurrentUser");
                    }

                    try {
                        connection =
                                ugi.doAs(
                                        (PrivilegedExceptionAction<Connection>)
                                                () ->
                                                        getConnectionFromPool(
                                                                url,
                                                                driverClass,
                                                                execUser,
                                                                dataSource,
                                                                prop));
                    } catch (Exception e) {
                        throw new JDBCParamsIllegalException(
                                "Error in doAs to get one connection.");
                    }
                }
                break;
        }
        return connection;
    }

    private String getJdbcUrl(Map<String, String> prop) throws SQLException {
        String url = prop.get(JDBCEngineConnConstant.JDBC_URL);
        if (StringUtils.isEmpty(url)) {
            throw new SQLException(JDBCEngineConnConstant.JDBC_URL + " is not empty.");
        }
        url = clearUrl(url);
        validateURL(url);
        return url.trim();
    }

    private String clearUrl(String url) {
        if (url.startsWith("\"") && url.endsWith("\"")) {
            url = url.trim();
            return url.substring(1, url.length() - 1);
        }
        return url;
    }

    private void validateURL(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException(JDBCEngineConnConstant.JDBC_URL + " cannot be null.");
        }
        if (!url.matches("jdbc:\\w+://\\S+:[0-9]{2,6}(/\\S*)?") && !url.startsWith("jdbc:h2")) {
            throw new IllegalArgumentException("Unknown the jdbc url: " + url);
        }
    }

    private JdbcAuthType getJdbcAuthType(Map<String, String> properties) {
        String authType =
                properties.getOrDefault(
                        JDBCEngineConnConstant.JDBC_AUTH_TYPE, USERNAME.getAuthType());
        if (authType == null || authType.trim().length() == 0) return of(USERNAME.getAuthType());
        return of(authType.trim().toUpperCase());
    }

    public ScheduledExecutorService startRefreshKerberosLoginStatusThread() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.submit(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (KerberosUtils.runRefreshKerberosLogin()) {
                            LOG.info("Ran runRefreshKerberosLogin command successfully.");
                            kinitFailCount = 0;
                            LOG.info(
                                    "Scheduling Kerberos ticket refresh thread with interval {} ms",
                                    KerberosUtils.getKerberosRefreshInterval());
                            scheduledExecutorService.schedule(
                                    this,
                                    KerberosUtils.getKerberosRefreshInterval(),
                                    TimeUnit.MILLISECONDS);
                        } else {
                            kinitFailCount++;
                            LOG.info(
                                    "runRefreshKerberosLogin failed for {} time(s).",
                                    kinitFailCount);
                            if (kinitFailCount >= KerberosUtils.kinitFailTimesThreshold()) {
                                LOG.error(
                                        "runRefreshKerberosLogin failed for max attempts, calling close executor.");
                                // close();
                            } else {
                                // wait for 1 second before calling runRefreshKerberosLogin() again
                                scheduledExecutorService.schedule(this, 1, TimeUnit.SECONDS);
                            }
                        }
                        return null;
                    }
                });
        return scheduledExecutorService;
    }

    public void shutdownRefreshKerberosLoginService() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }
}
