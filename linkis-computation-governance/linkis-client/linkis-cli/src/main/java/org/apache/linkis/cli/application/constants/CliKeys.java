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

package org.apache.linkis.cli.application.constants;

public class CliKeys {

  /** User Not configurable */
  public static final String ADMIN_USERS = "hadoop,root,shangda";

  public static final String LINKIS_CLIENT_NONCUSTOMIZABLE = "wds.linkis.client.noncustomizable";
  public static final String LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_USER_SPECIFICATION =
      LINKIS_CLIENT_NONCUSTOMIZABLE
          + ".enable.user.specification"; // allow user to specify submit user
  public static final String LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_PROXY_USER =
      LINKIS_CLIENT_NONCUSTOMIZABLE + ".enable.proxy.user"; // allow user to specify proxy user

  /** In env */
  public static final String LOG_PATH_KEY = "log.path";

  public static final String LOG_FILE_KEY = "log.file";

  public static final String CLIENT_CONFIG_ROOT_KEY = "conf.root";
  public static final String DEFAULT_CONFIG_FILE_NAME_KEY = "conf.file";
  public static final String LINUX_USER_KEY = "user.name";

  /** Configurable */
  /*
  execution type
   */
  public static final String JOB_EXEC = "wds.linkis.client.exec";

  public static final String JOB_EXEC_CODE = JOB_EXEC + ".code";

  /*
  jobContent type
   */
  public static final String JOB_CONTENT = "wds.linkis.client.jobContent";

  /*
  source
   */
  public static final String JOB_SOURCE = "wds.linkis.client.source";
  public static final String JOB_SOURCE_SCRIPT_PATH =
      JOB_SOURCE + "." + LinkisKeys.KEY_SCRIPT_PATH; // corresponds to server api.

  /*
  params
   */
  public static final String JOB_PARAM_CONF = "wds.linkis.client.param.conf";
  public static final String JOB_PARAM_RUNTIME = "wds.linkis.client.param.runtime";
  public static final String JOB_PARAM_VAR = "wds.linkis.client.param.var";

  /*
  labels
   */
  public static final String JOB_LABEL = "wds.linkis.client.label";
  public static final String JOB_LABEL_ENGINE_TYPE =
      JOB_LABEL + "." + LinkisKeys.KEY_ENGINETYPE; // corresponds to server api.
  public static final String JOB_LABEL_CODE_TYPE =
      JOB_LABEL + "." + LinkisKeys.KEY_CODETYPE; // corresponds to server api.
  public static final String JOB_LABEL_EXECUTEONCE =
      JOB_LABEL + "." + LinkisKeys.KEY_EXECUTEONCE; // corresponds to server api.
  public static final String JOB_LABEL_CLUSTER =
      JOB_LABEL + "." + LinkisKeys.KEY_CLUSTER; // corresponds to server api.

  /*
  Job command
   */
  public static final String LINKIS_CLIENT_JOB = "wds.linkis.client.job";
  public static final String LINKIS_CLIENT_JOB_TYPE = "wds.linkis.client.job.type";
  public static final String LINKIS_CLIENT_JOB_ID = "wds.linkis.client.job.id";

  /*
  common
   */
  public static final String LINKIS_CLIENT_COMMON = "wds.linkis.client.common";
  public static final String LINKIS_CLIENT_KILL_OPT = LINKIS_CLIENT_COMMON + ".kill";
  public static final String LINKIS_CLIENT_STATUS_OPT = LINKIS_CLIENT_COMMON + ".status";
  public static final String LINKIS_CLIENT_ASYNC_OPT = LINKIS_CLIENT_COMMON + ".async.submit";
  public static final String LINKIS_CLIENT_HELP_OPT = LINKIS_CLIENT_COMMON + ".help";
  public static final String LINKIS_CLIENT_DESC_OPT = LINKIS_CLIENT_COMMON + ".desc";
  public static final String LINKIS_CLIENT_LOG_OPT = LINKIS_CLIENT_COMMON + ".log";
  public static final String LINKIS_CLIENT_RESULT_OPT = LINKIS_CLIENT_COMMON + ".result";
  public static final String LINKIS_CLIENT_LIST_OPT = LINKIS_CLIENT_COMMON + ".list";
  public static final String LINKIS_CLIENT_MODE_OPT = LINKIS_CLIENT_COMMON + ".mode";
  public static final String LINKIS_CLIENT_USER_CONFIG = LINKIS_CLIENT_COMMON + ".user.conf";
  public static final String LINKIS_CLIENT_DEFAULT_CONFIG = LINKIS_CLIENT_COMMON + ".default.conf";
  public static final String LINKIS_COMMON_GATEWAY_URL = LINKIS_CLIENT_COMMON + ".gatewayUrl";
  public static final String LINKIS_COMMON_DIAPLAY_META_LOGO =
      LINKIS_CLIENT_COMMON + ".display.meta.log";
  public static final String LINKIS_COMMON_LOG_FROMLINE = LINKIS_CLIENT_COMMON + ".fromline";
  public static final String LINKIS_COMMON_RESULT_FROMPAGE = LINKIS_CLIENT_COMMON + ".frompage";
  public static final String LINKIS_COMMON_RESULT_FROMIDX = LINKIS_CLIENT_COMMON + ".fromidx";
  public static final String LINKIS_COMMON_RESULTPATHS = LINKIS_CLIENT_COMMON + ".resultpaths";
  public static final String JOB_EXTRA_ARGUMENTS =
      LINKIS_CLIENT_COMMON + "." + LinkisKeys.EXTRA_ARGUMENTS;
  public static final String JOB_COMMON_CODE_PATH = LINKIS_CLIENT_COMMON + ".code.path";

  // all static token , default static
  public static final String LINKIS_CLIENT_COMMON_OUTPUT_PATH =
      LINKIS_CLIENT_COMMON + ".output.path";

  public static final String LINKIS_COMMON_AUTHENTICATION_STRATEGY =
      LINKIS_CLIENT_COMMON + ".authStrategy";
  public static final String LINKIS_COMMON_TOKEN_KEY = LINKIS_CLIENT_COMMON + ".tokenKey";
  public static final String LINKIS_COMMON_TOKEN_VALUE = LINKIS_CLIENT_COMMON + ".tokenValue";

  public static final String JOB_COMMON_SUBMIT_USER = LINKIS_CLIENT_COMMON + ".submitUser";
  public static final String JOB_COMMON_SUBMIT_PASSWORD = LINKIS_CLIENT_COMMON + ".submitPassword";
  public static final String JOB_COMMON_PROXY_USER = LINKIS_CLIENT_COMMON + ".proxyUser";
  public static final String JOB_COMMON_CREATOR = LINKIS_CLIENT_COMMON + ".creator";

  public static final String UJESCLIENT_COMMON_CONNECTT_TIMEOUT =
      LINKIS_CLIENT_COMMON + ".connectionTimeout";
  public static final String UJESCLIENT_COMMON_DISCOVERY_ENABLED =
      LINKIS_CLIENT_COMMON + ".discoveryEnabled";
  public static final String UJESCLIENT_COMMON_LOADBALANCER_ENABLED =
      LINKIS_CLIENT_COMMON + ".loadbalancerEnabled";
  public static final String UJESCLIENT_COMMON_MAX_CONNECTION_SIZE =
      LINKIS_CLIENT_COMMON + ".maxConnectionSize";
  public static final String UJESCLIENT_COMMON_RETRY_ENABLED =
      LINKIS_CLIENT_COMMON + ".retryEnabled";
  public static final String UJESCLIENT_COMMON_READTIMEOUT = LINKIS_CLIENT_COMMON + ".readTimeout";
  public static final String UJESCLIENT_COMMON_DWS_VERSION = LINKIS_CLIENT_COMMON + ".dwsVersion";

  public static final String LINKIS_CLIENT_COMMON_RESULT_SET_PAGE_SIZE =
      LINKIS_CLIENT_COMMON + ".resultset.page.size";

  public static final String Linkis_OPER = "linkis.oper";
  public static final String LINKIS_ONCE = "linkis.once";
}
