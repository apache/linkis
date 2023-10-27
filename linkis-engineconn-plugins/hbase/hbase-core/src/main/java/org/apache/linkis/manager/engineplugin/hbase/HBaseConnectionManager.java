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

package org.apache.linkis.manager.engineplugin.hbase;

import org.apache.linkis.manager.engineplugin.hbase.errorcode.HBaseErrorCodeSummary;
import org.apache.linkis.manager.engineplugin.hbase.exception.HBaseParamsIllegalException;
import org.apache.linkis.manager.engineplugin.hbase.exception.JobExecutorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.DEFAULT_HBASE_DFS_ROOT_DIR;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.DEFAULT_KRB5_CONF_PATH;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.DEFAULT_ZOOKEEPER_CLIENT_PORT;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.DEFAULT_ZOOKEEPER_NODE_PARENT;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.DEFAULT_ZOOKEEPER_QUORUM;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HADOOP_SECURITY_AUTH;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HBASE_AUTH;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HBASE_DFS_ROOT_DIR;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HBASE_MASTER_KERBEROS_PRINCIPAL;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HBASE_REGION_SERVER_KERBEROS_PRINCIPAL;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.HBASE_SECURITY_AUTH;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.KERBEROS;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.KERBEROS_KEYTAB_FILE;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.KERBEROS_PRINCIPAL;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.KERBEROS_PROXY_USER;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.KRB5_CONF_PATH;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.MASTER_SERVER_KERBEROS_PRINCIPAL;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.REGION_SERVER_KERBEROS_PRINCIPAL;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.SIMPLE;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.UNIQUE_KEY_DELIMITER;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.ZOOKEEPER_CLIENT_PORT;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.ZOOKEEPER_NODE_PARENT;
import static org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant.ZOOKEEPER_QUORUM;

public class HBaseConnectionManager {
  private static final Logger LOG = LoggerFactory.getLogger(HBaseConnectionManager.class);
  private final ConcurrentHashMap<String, Connection> connectionMap;
  private final ReentrantLock lock = new ReentrantLock();
  private static final AtomicBoolean kerberosEnvInit = new AtomicBoolean(false);
  private static final int KERBEROS_RE_LOGIN_MAX_RETRY = 5;
  private static final long KERBEROS_RE_LOGIN_INTERVAL = 30 * 60 * 1000L;
  private static volatile HBaseConnectionManager instance = null;

  private HBaseConnectionManager() {
    connectionMap = new ConcurrentHashMap<>();
  }

  public static HBaseConnectionManager getInstance() {
    if (instance == null) {
      synchronized (HBaseConnectionManager.class) {
        if (instance == null) {
          instance = new HBaseConnectionManager();
        }
      }
    }
    return instance;
  }

  public Configuration getConfiguration(Properties prop) {
    Map<String, String> propMap = getPropMap(prop);
    return buildConfiguration(propMap);
  }

  public Connection getConnection(Properties prop) {
    Map<String, String> propMap = getPropMap(prop);
    return getConnection(propMap);
  }

  public Connection getConnection(Configuration configuration) {
    String clusterConnUniqueKey = generateUniqueConnectionKey(configuration);
    LOG.info("Start to get connection for cluster {}.", clusterConnUniqueKey);
    if (connectionMap.containsKey(clusterConnUniqueKey)) {
      return connectionMap.get(clusterConnUniqueKey);
    }
    try {
      lock.lock();
      if (isKerberosAuthType(configuration) && kerberosEnvInit.compareAndSet(false, true)) {
        doKerberosLogin(configuration);
      }
      Connection connection;
      String proxyUser = getKerberosProxyUser(configuration);
      UserGroupInformation kerberosLoginUser = UserGroupInformation.getLoginUser();
      String kerberosLoginShortUserName = kerberosLoginUser.getShortUserName();
      if (StringUtils.isNotBlank(proxyUser) && !proxyUser.equals(kerberosLoginShortUserName)) {
        UserGroupInformation ugi =
            UserGroupInformation.createProxyUser(proxyUser, kerberosLoginUser);
        connection =
            ugi.doAs(
                (PrivilegedAction<Connection>)
                    () -> {
                      try {
                        return ConnectionFactory.createConnection(configuration);
                      } catch (IOException e) {
                        LOG.error(
                            HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorDesc(),
                            e);
                        throw new JobExecutorException(
                            HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorCode(),
                            HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorDesc());
                      }
                    });
        LOG.info("Successfully create a connection {} and proxy user {}", connection, proxyUser);
      } else {
        connection = ConnectionFactory.createConnection(configuration);
        LOG.info("Successfully create a connection {}.", connection);
      }
      connectionMap.put(clusterConnUniqueKey, connection);
      return connection;
    } catch (IOException e) {
      LOG.error(HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorDesc(), e);
      throw new JobExecutorException(
          HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorCode(),
          HBaseErrorCodeSummary.HBASE_CLIENT_CONN_CREATE_FAILED.getErrorDesc());
    } finally {
      lock.unlock();
    }
  }

  public Connection getConnection(Map<String, String> prop) {
    Configuration configuration = buildConfiguration(prop);
    return getConnection(configuration);
  }

  private void doKerberosLogin(Configuration configuration) {
    String principal = configuration.get(KERBEROS_PRINCIPAL);
    String keytab = configuration.get(KERBEROS_KEYTAB_FILE);
    File file = new File(keytab);
    if (!file.exists()) {
      kerberosEnvInit.set(false);
      throw new HBaseParamsIllegalException(
          HBaseErrorCodeSummary.KERBEROS_KEYTAB_FILE_NOT_EXISTS.getErrorCode(),
          HBaseErrorCodeSummary.KERBEROS_KEYTAB_FILE_NOT_EXISTS.getErrorDesc());
    }
    if (!file.isFile()) {
      kerberosEnvInit.set(false);
      throw new HBaseParamsIllegalException(
          HBaseErrorCodeSummary.KERBEROS_KEYTAB_NOT_FILE.getErrorCode(),
          HBaseErrorCodeSummary.KERBEROS_KEYTAB_NOT_FILE.getErrorDesc());
    }
    try {
      UserGroupInformation.setConfiguration(configuration);
      UserGroupInformation.loginUserFromKeytab(principal, keytab);
      LOG.info("Login successfully via keytab: {} and principal: {}", keytab, principal);
      doKerberosReLogin();
    } catch (IOException e) {
      kerberosEnvInit.set(false);
      throw new JobExecutorException(
          HBaseErrorCodeSummary.KERBEROS_AUTH_FAILED.getErrorCode(),
          HBaseErrorCodeSummary.KERBEROS_AUTH_FAILED.getErrorDesc());
    }
  }

  private boolean runKerberosLogin() {
    Configuration conf = new org.apache.hadoop.conf.Configuration();
    conf.set("hadoop.security.authentication", KERBEROS);
    UserGroupInformation.setConfiguration(conf);
    try {
      if (UserGroupInformation.isLoginKeytabBased()) {
        LOG.info("Trying re login from keytab.");
        UserGroupInformation.getLoginUser().reloginFromKeytab();
        return true;
      } else if (UserGroupInformation.isLoginTicketBased()) {
        LOG.info("Trying re login from ticket cache");
        UserGroupInformation.getLoginUser().reloginFromTicketCache();
        return true;
      }
    } catch (Exception e) {
      LOG.error("Unable to run kinit.", e);
    }
    return false;
  }

  private void doKerberosReLogin() {
    if (!UserGroupInformation.isSecurityEnabled()) {
      return;
    }

    Thread reLoginThread =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                while (true) {
                  int times = 0;

                  while (times < KERBEROS_RE_LOGIN_MAX_RETRY) {
                    if (runKerberosLogin()) {
                      LOG.info("Run kerberos re login command successfully.");
                      break;
                    } else {
                      times++;
                      LOG.info("Run kerberos re login failed for {} time(s).", times);
                    }
                  }
                  try {
                    Thread.sleep(KERBEROS_RE_LOGIN_INTERVAL);
                  } catch (InterruptedException e) {
                    LOG.warn("Ignore error", e);
                  }
                }
              }
            });
    reLoginThread.setName("KerberosReLoginThread");
    reLoginThread.setDaemon(true);
    reLoginThread.start();
  }

  private Configuration buildConfiguration(Map<String, String> prop) {
    Configuration configuration = HBaseConfiguration.create();
    if (prop.isEmpty()) {
      return configuration;
    }
    String zkQuorum =
        HBasePropertiesParser.getString(prop, ZOOKEEPER_QUORUM, DEFAULT_ZOOKEEPER_QUORUM);
    configuration.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
    int zkClientPort =
        HBasePropertiesParser.getInt(prop, ZOOKEEPER_CLIENT_PORT, DEFAULT_ZOOKEEPER_CLIENT_PORT);
    configuration.set(HConstants.ZOOKEEPER_CLIENT_PORT, String.valueOf(zkClientPort));
    String zNodeParent =
        HBasePropertiesParser.getString(prop, ZOOKEEPER_NODE_PARENT, DEFAULT_ZOOKEEPER_NODE_PARENT);
    configuration.set(HConstants.ZOOKEEPER_ZNODE_PARENT, zNodeParent);
    String dfsRootDir =
        HBasePropertiesParser.getString(prop, HBASE_DFS_ROOT_DIR, DEFAULT_HBASE_DFS_ROOT_DIR);
    configuration.set(HConstants.HBASE_DIR, dfsRootDir);
    String securityAuth = HBasePropertiesParser.getString(prop, HBASE_SECURITY_AUTH, SIMPLE);
    configuration.set(HBASE_AUTH, securityAuth);
    if (isKerberosAuthType(configuration)) {
      configuration.set(HADOOP_SECURITY_AUTH, KERBEROS);
      String kerberosPrincipal = HBasePropertiesParser.getString(prop, KERBEROS_PRINCIPAL, "");
      if (StringUtils.isBlank(kerberosPrincipal)) {
        throw new HBaseParamsIllegalException(
            HBaseErrorCodeSummary.KERBEROS_PRINCIPAL_NOT_NULL.getErrorCode(),
            HBaseErrorCodeSummary.KERBEROS_PRINCIPAL_NOT_NULL.getErrorDesc());
      }
      configuration.set(KERBEROS_PRINCIPAL, kerberosPrincipal);
      String keytabFile = HBasePropertiesParser.getString(prop, KERBEROS_KEYTAB_FILE, "");
      if (StringUtils.isBlank(keytabFile)) {
        throw new HBaseParamsIllegalException(
            HBaseErrorCodeSummary.KERBEROS_KEYTAB_NOT_NULL.getErrorCode(),
            HBaseErrorCodeSummary.KERBEROS_KEYTAB_NOT_NULL.getErrorDesc());
      }
      configuration.set(KERBEROS_KEYTAB_FILE, keytabFile);
      String proxyUser = HBasePropertiesParser.getString(prop, KERBEROS_PROXY_USER, "");
      configuration.set(KERBEROS_PROXY_USER, proxyUser);
      String regionServerPrincipal =
          HBasePropertiesParser.getString(prop, HBASE_REGION_SERVER_KERBEROS_PRINCIPAL, "");
      if (StringUtils.isBlank(regionServerPrincipal)) {
        throw new HBaseParamsIllegalException(
            HBaseErrorCodeSummary.REGION_SERVER_KERBEROS_PRINCIPAL_NOT_NULL.getErrorCode(),
            HBaseErrorCodeSummary.REGION_SERVER_KERBEROS_PRINCIPAL_NOT_NULL.getErrorDesc());
      }
      configuration.set(REGION_SERVER_KERBEROS_PRINCIPAL, regionServerPrincipal);
      String masterPrincipal =
          HBasePropertiesParser.getString(prop, HBASE_MASTER_KERBEROS_PRINCIPAL, "");
      if (StringUtils.isBlank(masterPrincipal)) {
        throw new HBaseParamsIllegalException(
            HBaseErrorCodeSummary.MASTER_KERBEROS_PRINCIPAL_NOT_NULL.getErrorCode(),
            HBaseErrorCodeSummary.MASTER_KERBEROS_PRINCIPAL_NOT_NULL.getErrorDesc());
      }
      configuration.set(MASTER_SERVER_KERBEROS_PRINCIPAL, masterPrincipal);
      String krb5Conf =
          HBasePropertiesParser.getString(prop, KRB5_CONF_PATH, DEFAULT_KRB5_CONF_PATH);
      System.setProperty(KRB5_CONF_PATH, krb5Conf);
    }
    return configuration;
  }

  private boolean isKerberosAuthType(Configuration configuration) {
    String authType = configuration.get(HBASE_AUTH, SIMPLE);
    return KERBEROS.equalsIgnoreCase(authType.trim());
  }

  public String generateUniqueConnectionKey(Map<String, String> prop) {
    Configuration configuration = buildConfiguration(prop);
    return generateUniqueConnectionKey(configuration);
  }

  private String generateUniqueConnectionKey(Configuration configuration) {
    String zkQuorum = configuration.get(HConstants.ZOOKEEPER_QUORUM);
    String zkClientPort = configuration.get(HConstants.ZOOKEEPER_CLIENT_PORT);
    StringBuilder sb = new StringBuilder(zkQuorum);
    sb.append(UNIQUE_KEY_DELIMITER);
    sb.append(zkClientPort);
    if (supportKerberosProxyUser(configuration)) {
      sb.append(UNIQUE_KEY_DELIMITER);
      sb.append(getKerberosProxyUser(configuration));
    }
    return sb.toString();
  }

  private boolean supportKerberosProxyUser(Configuration configuration) {
    if (!isKerberosAuthType(configuration)) {
      return false;
    }
    String proxyUser = getKerberosProxyUser(configuration);
    return StringUtils.isNotBlank(proxyUser);
  }

  private String getKerberosProxyUser(Configuration configuration) {
    return configuration.get(KERBEROS_PROXY_USER, "");
  }

  public void destroy() {
    try {
      for (Connection connection : connectionMap.values()) {
        connection.close();
      }
      connectionMap.clear();
    } catch (IOException e) {
      LOG.warn("An exception occurred while destroy resources.", e);
    }
  }

  public Map<String, String> getPropMap(Properties prop) {
    Map<String, String> propMap = new HashMap<>();
    if (prop == null) {
      return propMap;
    }
    for (String key : prop.stringPropertyNames()) {
      propMap.put(key, prop.getProperty(key));
    }
    return propMap;
  }
}
