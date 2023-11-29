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

package org.apache.linkis.manager.engineplugin.jdbc;

import org.apache.linkis.common.utils.SecurityUtils;
import org.apache.linkis.hadoop.common.utils.KerberosUtils;
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;
import org.apache.linkis.manager.engineplugin.jdbc.utils.JdbcParamUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;

import javax.sql.DataSource;

import java.io.Closeable;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType.USERNAME;
import static org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType.of;
import static org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.*;

public class ConnectionManager {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

  private final Map<String, DataSource> dataSourceFactories;
  private final JDBCDataSourceConfigurations jdbcDataSourceConfigurations;

  private static volatile ConnectionManager connectionManager;
  private ScheduledExecutorService scheduledExecutorService;
  private Integer kinitFailCount = 0;

  private ConnectionManager() {
    jdbcDataSourceConfigurations = new JDBCDataSourceConfigurations();
    dataSourceFactories = new HashMap<>();
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
    } catch (Exception e) {
      LOG.error("Error while closing...", e);
    }
    for (DataSource dataSource : this.dataSourceFactories.values()) {
      try {
        if (dataSource instanceof Closeable) {
          ((Closeable) dataSource).close();
        }
      } catch (Exception e) {
        LOG.error("Error while closing datasource...", e);
      }
    }
  }

  protected DataSource buildDataSource(String dbUrl, Map<String, String> properties)
      throws JDBCParamsIllegalException {

    String driverClassName =
        JDBCPropertiesParser.getString(properties, JDBCEngineConnConstant.JDBC_DRIVER, "");
    if (StringUtils.isBlank(driverClassName)) {
      LOG.error("The driver class name is required.");
      throw new JDBCParamsIllegalException(
          DRIVER_CLASS_NAME_ERROR.getErrorCode(), DRIVER_CLASS_NAME_ERROR.getErrorDesc());
    }

    String username = JdbcParamUtils.getJdbcUsername(properties);
    String password = JdbcParamUtils.getJdbcPassword(properties);
    JdbcAuthType jdbcAuthType = getJdbcAuthType(properties);
    switch (jdbcAuthType) {
      case USERNAME:
        LOG.info("The jdbc auth type is username and password.");
        break;
      case SIMPLE:
        LOG.info("The jdbc auth type is simple.");
        break;
      case KERBEROS:
        LOG.info("The jdbc auth type is kerberos.");
        break;
      default:
        throw new JDBCParamsIllegalException(
            UNSUPPORT_JDBC_AUTHENTICATION_TYPES.getErrorCode(),
            MessageFormat.format(
                UNSUPPORT_JDBC_AUTHENTICATION_TYPES.getErrorDesc(), jdbcAuthType.getAuthType()));
    }

    boolean testOnBorrow =
        JDBCPropertiesParser.getBool(
            properties, JDBCEngineConnConstant.JDBC_POOL_TEST_ON_BORROW, false);
    boolean testOnReturn =
        JDBCPropertiesParser.getBool(
            properties, JDBCEngineConnConstant.JDBC_POOL_TEST_ON_RETURN, false);
    boolean testWhileIdle =
        JDBCPropertiesParser.getBool(
            properties, JDBCEngineConnConstant.JDBC_POOL_TEST_WHILE_IDLE, true);
    int minEvictableIdleTimeMillis =
        JDBCPropertiesParser.getInt(
            properties, JDBCEngineConnConstant.JDBC_POOL_TIME_BETWEEN_MIN_EVIC_IDLE_MS, 300000);
    long timeBetweenEvictionRunsMillis =
        JDBCPropertiesParser.getLong(
            properties, JDBCEngineConnConstant.JDBC_POOL_TIME_BETWEEN_EVIC_RUNS_MS, 60000);

    long maxWait =
        JDBCPropertiesParser.getLong(properties, JDBCEngineConnConstant.JDBC_POOL_MAX_WAIT, 6000);
    int maxActive =
        JDBCPropertiesParser.getInt(properties, JDBCEngineConnConstant.JDBC_POOL_MAX_ACTIVE, 20);
    int minIdle =
        JDBCPropertiesParser.getInt(properties, JDBCEngineConnConstant.JDBC_POOL_MIN_IDLE, 1);
    int initialSize =
        JDBCPropertiesParser.getInt(properties, JDBCEngineConnConstant.JDBC_POOL_INIT_SIZE, 1);
    String validationQuery =
        JDBCPropertiesParser.getString(
            properties,
            JDBCEngineConnConstant.JDBC_POOL_VALIDATION_QUERY,
            JDBCEngineConnConstant.JDBC_POOL_DEFAULT_VALIDATION_QUERY);

    boolean poolPreparedStatements =
        JDBCPropertiesParser.getBool(
            properties, JDBCEngineConnConstant.JDBC_POOL_PREPARED_STATEMENTS, true);
    boolean removeAbandoned =
        JDBCPropertiesParser.getBool(
            properties, JDBCEngineConnConstant.JDBC_POOL_REMOVE_ABANDONED_ENABLED, true);
    int removeAbandonedTimeout =
        JDBCPropertiesParser.getInt(
            properties, JDBCEngineConnConstant.JDBC_POOL_REMOVE_ABANDONED_TIMEOUT, 300);

    DruidDataSource datasource = new DruidDataSource();
    LOG.info("Database connection address information(数据库连接地址信息)=" + dbUrl);
    datasource.setUrl(dbUrl);
    datasource.setUsername(username);
    datasource.setPassword(password);
    datasource.setDriverClassName(driverClassName);
    datasource.setInitialSize(initialSize);
    datasource.setMinIdle(minIdle);
    datasource.setMaxActive(maxActive);
    datasource.setMaxWait(maxWait);
    datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    datasource.setValidationQuery(validationQuery);
    datasource.setTestWhileIdle(testWhileIdle);
    datasource.setTestOnBorrow(testOnBorrow);
    datasource.setTestOnReturn(testOnReturn);
    datasource.setPoolPreparedStatements(poolPreparedStatements);
    datasource.setRemoveAbandoned(removeAbandoned);
    datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    return datasource;
  }

  private Connection getConnectionFromDataSource(
      String dataSourceIdentifier, String url, Map<String, String> prop)
      throws SQLException, JDBCParamsIllegalException {
    DataSource dataSource = dataSourceFactories.get(dataSourceIdentifier);
    if (dataSource == null) {
      synchronized (dataSourceFactories) {
        if (dataSource == null) {
          dataSource = buildDataSource(url, prop);
          dataSourceFactories.put(dataSourceIdentifier, dataSource);
        }
      }
    }
    return dataSource.getConnection();
  }

  public Connection getConnection(String dataSourceIdentifier, Map<String, String> properties)
      throws SQLException, JDBCParamsIllegalException {
    String execUser =
        JDBCPropertiesParser.getString(
            properties, JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER, "");
    if (StringUtils.isBlank(execUser)) {
      LOG.warn("execUser is empty!");
      throw new JDBCParamsIllegalException(
          NO_EXEC_USER_ERROR.getErrorCode(), NO_EXEC_USER_ERROR.getErrorDesc());
    }
    Connection connection = null;
    final String jdbcUrl = getJdbcUrl(properties);
    JdbcAuthType jdbcAuthType = getJdbcAuthType(properties);
    switch (jdbcAuthType) {
      case SIMPLE:
      case USERNAME:
        connection = getConnectionFromDataSource(dataSourceIdentifier, jdbcUrl, properties);
        break;
      case KERBEROS:
        LOG.debug(
            "Calling createKerberosSecureConfiguration(); this will do loginUserFromKeytab() if required");
        final String keytab =
            JDBCPropertiesParser.getString(
                properties, JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION, "");
        final String principal =
            JDBCPropertiesParser.getString(
                properties, JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL, "");
        KerberosUtils.createKerberosSecureConfiguration(keytab, principal);
        LOG.debug("createKerberosSecureConfiguration() returned");
        boolean isProxyEnabled =
            JDBCPropertiesParser.getBool(
                properties, JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_PROXY_ENABLE, true);

        if (isProxyEnabled) {
          final String jdbcUrlWithProxyUser =
              appendProxyUserToJDBCUrl(jdbcUrl, execUser, properties);
          LOG.info(
              String.format(
                  "Try to Create a new %s JDBC with url(%s), kerberos, proxyUser(%s).",
                  dataSourceIdentifier, jdbcUrlWithProxyUser, execUser));
          connection =
              getConnectionFromDataSource(dataSourceIdentifier, jdbcUrlWithProxyUser, properties);
        } else {
          UserGroupInformation ugi;
          try {
            ugi =
                UserGroupInformation.createProxyUser(
                    execUser, UserGroupInformation.getCurrentUser());
          } catch (Exception e) {
            LOG.error("Error in getCurrentUser", e);
            throw new JDBCParamsIllegalException(
                GET_CURRENT_USER_ERROR.getErrorCode(), GET_CURRENT_USER_ERROR.getErrorDesc());
          }

          try {
            connection =
                ugi.doAs(
                    (PrivilegedExceptionAction<Connection>)
                        () ->
                            getConnectionFromDataSource(dataSourceIdentifier, jdbcUrl, properties));
          } catch (Exception e) {
            throw new JDBCParamsIllegalException(
                DOAS_FOR_GET_CONNECTION_ERROR.getErrorCode(),
                DOAS_FOR_GET_CONNECTION_ERROR.getErrorDesc());
          }
        }
        break;
      default:
        throw new JDBCParamsIllegalException(
            UNSUPPORT_JDBC_AUTHENTICATION_TYPES.getErrorCode(),
            MessageFormat.format(
                UNSUPPORT_JDBC_AUTHENTICATION_TYPES.getErrorDesc(), jdbcAuthType.getAuthType()));
    }
    return connection;
  }

  private String getJdbcUrl(Map<String, String> properties) throws SQLException {
    String url = properties.get(JDBCEngineConnConstant.JDBC_URL);
    if (StringUtils.isBlank(url)) {
      throw new SQLException(JDBCEngineConnConstant.JDBC_URL + " cannot be empty.");
    }
    url = JdbcParamUtils.clearJdbcUrl(url);
    SecurityUtils.checkJdbcConnUrl(url);
    url = SecurityUtils.getJdbcUrl(url);
    return url;
  }

  private String appendProxyUserToJDBCUrl(
      String jdbcUrl, String execUser, Map<String, String> properties) {
    StringBuilder jdbcUrlSb = new StringBuilder(jdbcUrl);
    String proxyUserProperty =
        JDBCPropertiesParser.getString(
            properties, JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, "");
    if (execUser != null
        && !JDBCEngineConnConstant.JDBC_PROXY_ANONYMOUS_USER.equals(execUser)
        && StringUtils.isNotBlank(proxyUserProperty)) {

      int lastIndexOfUrl = jdbcUrl.indexOf("?");
      if (lastIndexOfUrl == -1) {
        lastIndexOfUrl = jdbcUrl.length();
      }
      LOG.info("Using proxy user as: {}", execUser);
      LOG.info("Using proxy property for user as: {}", proxyUserProperty);
      jdbcUrlSb.insert(lastIndexOfUrl, ";" + proxyUserProperty + "=" + execUser + ";");
    }

    return jdbcUrlSb.toString();
  }

  private JdbcAuthType getJdbcAuthType(Map<String, String> properties) {
    String authType =
        properties.getOrDefault(JDBCEngineConnConstant.JDBC_AUTH_TYPE, USERNAME.getAuthType());
    if (authType == null || authType.trim().length() == 0) {
      return of(USERNAME.getAuthType());
    }
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
                  this, KerberosUtils.getKerberosRefreshInterval(), TimeUnit.MILLISECONDS);
            } else {
              kinitFailCount++;
              LOG.info("runRefreshKerberosLogin failed for {} time(s).", kinitFailCount);
              if (kinitFailCount >= KerberosUtils.kinitFailTimesThreshold()) {
                LOG.error(
                    "runRefreshKerberosLogin failed for max attempts, calling close executor.");
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
