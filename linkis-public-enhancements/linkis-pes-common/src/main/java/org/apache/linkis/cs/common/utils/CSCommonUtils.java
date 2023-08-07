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

package org.apache.linkis.cs.common.utils;

import org.apache.linkis.common.conf.CommonVars;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CSCommonUtils {

  public static final String CONTEXT_ID_STR = "contextID";

  public static final String NODE_NAME_STR = "nodeName";

  public static final String NODE_ID = "nodeID";

  public static final String ID_NODE_NAME = "id_nodeName";

  public static final String FLOW_INFOS = "flow.infos";

  public static final String CONTEXT_ENV_DEV =
      CommonVars.apply("wds.linkis.dev.contextID.env", "BDP_DEV").getValue();

  public static final String CONTEXT_ENV_PROD =
      CommonVars.apply("wds.linkis.production.contextID.env", "BDAP_PROD").getValue();

  public static final String CS_TMP_TABLE_PREFIX = "cs_tmp_";

  public static Gson gson =
      new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();

  public static final String NODE_PREFIX = "node.";

  public static final String FLOW_PREFIX = "flow.";

  public static final String PROJECT_PREFIX = "project.";

  public static final String WORKSPACE_PREFIX = "workspace.";

  public static final String RESOURCE_PREFIX = "resource.";

  public static final String TABLE_PREFIX = "table.";

  public static final String DB_PREFIX = "db.";

  public static final String VARIABLE_PREFIX = "variable.";

  public static final String JOB_ID = ".jobID";

  public static final String FLOW_RESOURCE_PREFIX = FLOW_PREFIX + RESOURCE_PREFIX;

  public static final String PROJECT_RESOURCE_PREFIX = PROJECT_PREFIX + RESOURCE_PREFIX;

  public static final String WORKSPACE_RESOURCE_PREFIX = WORKSPACE_PREFIX + RESOURCE_PREFIX;

  public static final String FLOW_VARIABLE_PREFIX = FLOW_PREFIX + VARIABLE_PREFIX;

  public static final String WORKSPACE_VARIABLE_PREFIX = WORKSPACE_PREFIX + VARIABLE_PREFIX;

  public static final String PROJECT_VARIABLE_PREFIX = PROJECT_PREFIX + VARIABLE_PREFIX;

  public static final int CONTEXT_MAX_PAGE_SIZE = 5000;

  public static String getVariableKey(String nodeName, String varName) {

    return CSCommonUtils.NODE_PREFIX + nodeName + "." + CSCommonUtils.VARIABLE_PREFIX + varName;
  }

  public static String getTableKey(String nodeName, String tableName) {

    return CSCommonUtils.NODE_PREFIX + nodeName + "." + CSCommonUtils.TABLE_PREFIX + tableName;
  }

  public static Date localDatetimeToDate(LocalDateTime ldt) {
    if (null != ldt) {
      return new Date(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    } else {
      return null;
    }
  }

  public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
