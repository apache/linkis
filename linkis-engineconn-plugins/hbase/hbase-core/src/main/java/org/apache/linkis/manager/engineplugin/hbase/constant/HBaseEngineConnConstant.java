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

package org.apache.linkis.manager.engineplugin.hbase.constant;

import org.apache.hadoop.hbase.HConstants;

public class HBaseEngineConnConstant {
  private HBaseEngineConnConstant() {}

  public static final String LINKIS_PREFIX = "linkis.";

  public static final String ZOOKEEPER_QUORUM = LINKIS_PREFIX + HConstants.ZOOKEEPER_QUORUM;
  public static final String DEFAULT_ZOOKEEPER_QUORUM = "localhost";
  public static final String ZOOKEEPER_CLIENT_PORT =
      LINKIS_PREFIX + HConstants.ZOOKEEPER_CLIENT_PORT;
  public static final int DEFAULT_ZOOKEEPER_CLIENT_PORT = 2181;
  public static final String ZOOKEEPER_NODE_PARENT =
      LINKIS_PREFIX + HConstants.ZOOKEEPER_ZNODE_PARENT;
  public static final String DEFAULT_ZOOKEEPER_NODE_PARENT = "/hbase";
  public static final String HBASE_DFS_ROOT_DIR = LINKIS_PREFIX + HConstants.HBASE_DIR;
  public static final String DEFAULT_HBASE_DFS_ROOT_DIR = "/hbase";
  public static final String HBASE_AUTH = "hbase.security.authentication";
  public static final String HADOOP_SECURITY_AUTH = "hadoop.security.authentication";
  public static final String HBASE_SECURITY_AUTH = LINKIS_PREFIX + HBASE_AUTH;
  public static final String KERBEROS = "kerberos";
  public static final String SIMPLE = "simple";
  public static final String KERBEROS_PRINCIPAL = LINKIS_PREFIX + "hbase.kerberos.principal";
  public static final String KERBEROS_KEYTAB_FILE = LINKIS_PREFIX + "hbase.keytab.file";
  public static final String KERBEROS_PROXY_USER = LINKIS_PREFIX + "hbase.kerberos.proxy.user";
  public static final String REGION_SERVER_KERBEROS_PRINCIPAL =
      "hbase.regionserver.kerberos.principal";
  public static final String HBASE_REGION_SERVER_KERBEROS_PRINCIPAL =
      LINKIS_PREFIX + REGION_SERVER_KERBEROS_PRINCIPAL;
  public static final String MASTER_SERVER_KERBEROS_PRINCIPAL = "hbase.master.kerberos.principal";
  public static final String HBASE_MASTER_KERBEROS_PRINCIPAL =
      LINKIS_PREFIX + MASTER_SERVER_KERBEROS_PRINCIPAL;
  public static final String KRB5_CONF_PATH = "java.security.krb5.conf";
  public static final String DEFAULT_KRB5_CONF_PATH = "/etc/krb5.conf";
  public static final String UNIQUE_KEY_DELIMITER = "#";

  public static final String HBASE_SHELL_SESSION_INIT_TIMEOUT_MS =
      LINKIS_PREFIX + "hbase.shell.session.init.timeout.ms";
  public static final long DEFAULT_SHELL_SESSION_INIT_TIMEOUT_MS = 2 * 60 * 1000L;

  public static final String HBASE_SHELL_SESSION_INIT_MAX_TIMES =
      LINKIS_PREFIX + "hbase.shell.session.init.max.times";
  public static final int DEFAULT_SHELL_SESSION_INIT_MAX_TIMES = 10;

  public static final String HBASE_SHELL_SESSION_INIT_RETRY_INTERVAL_MS =
      LINKIS_PREFIX + "hbase.shell.session.init.retry.interval";
  public static final long DEFAULT_SHELL_SESSION_INIT_RETRY_INTERVAL_MS = 500L;

  public static final String HBASE_SHELL_SESSION_IDLE_MS =
      LINKIS_PREFIX + "hbase.shell.session.idle";
  public static final long DEFAULT_SHELL_SESSION_IDLE_MS = 2 * 60 * 60 * 1000L;

  public static final String HBASE_SHELL_DEBUG_LOG =
      LINKIS_PREFIX + "hbase.shell.session.debug.log";
  public static final boolean DEFAULT_SHELL_DEBUG_LOG = false;
}
