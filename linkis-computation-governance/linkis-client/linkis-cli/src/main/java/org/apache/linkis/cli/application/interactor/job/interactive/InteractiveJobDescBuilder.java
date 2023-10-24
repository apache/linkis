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

package org.apache.linkis.cli.application.interactor.job.interactive;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.var.VarAccess;
import org.apache.linkis.cli.application.interactor.job.common.KeyParser;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.operator.ujes.UJESClientFactory;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class InteractiveJobDescBuilder {

  public static InteractiveJobDesc build(CliCtx ctx) {
    InteractiveJobDesc desc = new InteractiveJobDesc();

    VarAccess stdVarAccess = ctx.getVarAccess();

    Map<String, Object> confMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_PARAM_CONF);
    Map<String, Object> runtimeMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_PARAM_RUNTIME);
    Map<String, Object> varMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_PARAM_VAR);
    Map<String, Object> labelMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_LABEL);
    Map<String, Object> sourceMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_SOURCE);
    Map<String, Object> executionMap = stdVarAccess.getVar(Map.class, CliKeys.JOB_EXEC);

    confMap = confMap == null ? new HashMap<>() : confMap;
    runtimeMap = runtimeMap == null ? new HashMap<>() : runtimeMap;
    varMap = varMap == null ? new HashMap<>() : varMap;
    labelMap = labelMap == null ? new HashMap<>() : labelMap;
    sourceMap = sourceMap == null ? new HashMap<>() : sourceMap;
    executionMap = executionMap == null ? new HashMap<>() : executionMap;

    /** remove key prefix of all keys in map type params. e.g. kv in confMap, labelMap etc. */
    confMap = KeyParser.removePrefixForKeysInMap(confMap);
    runtimeMap = KeyParser.removePrefixForKeysInMap(runtimeMap);
    labelMap = KeyParser.removePrefixForKeysInMap(labelMap);
    sourceMap = KeyParser.removePrefixForKeysInMap(sourceMap);
    executionMap = KeyParser.removePrefixForKeysInMap(executionMap);

    /** remove key prefix of non-map type params */
    for (String key : stdVarAccess.getAllVarKeys()) {
      Object val = stdVarAccess.getVar(Object.class, key);
      if (!(val instanceof Map) && val != null) {
        // note that we allow it to overwrite existing values in map
        if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_PARAM_CONF)) {
          KeyParser.removePrefixAndPutValToMap(confMap, key, val, CliKeys.JOB_PARAM_CONF);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_PARAM_VAR)) {
          KeyParser.removePrefixAndPutValToMap(varMap, key, val, CliKeys.JOB_PARAM_VAR);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_PARAM_RUNTIME)) {
          KeyParser.removePrefixAndPutValToMap(runtimeMap, key, val, CliKeys.JOB_PARAM_RUNTIME);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_EXEC)) {
          KeyParser.removePrefixAndPutValToMap(executionMap, key, val, CliKeys.JOB_EXEC);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_LABEL)) {
          KeyParser.removePrefixAndPutValToMap(labelMap, key, val, CliKeys.JOB_LABEL);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_SOURCE)) {
          KeyParser.removePrefixAndPutValToMap(sourceMap, key, val, CliKeys.JOB_SOURCE);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.LINKIS_CLIENT_COMMON)) {
          // do nothing
        } else {
          //        confMap.put(key, stdVarAccess.getVar(Object.class, key));
        }
      }
    }

    Boolean asyncSubmission =
        stdVarAccess.getVarOrDefault(Boolean.class, CliKeys.LINKIS_CLIENT_ASYNC_OPT, false);

    String creator;
    if (!asyncSubmission) {
      creator =
          stdVarAccess.getVarOrDefault(
              String.class, CliKeys.JOB_COMMON_CREATOR, CliConstants.JOB_CREATOR_DEFAULT);
    } else {
      creator =
          stdVarAccess.getVarOrDefault(
              String.class, CliKeys.JOB_COMMON_CREATOR, CliConstants.JOB_CREATOR_ASYNC_DEFAULT);
    }
    String code = stdVarAccess.getVar(String.class, CliKeys.JOB_EXEC_CODE);
    String engineType = stdVarAccess.getVar(String.class, CliKeys.JOB_LABEL_ENGINE_TYPE);
    String runType = stdVarAccess.getVar(String.class, CliKeys.JOB_LABEL_CODE_TYPE);
    String scriptPath =
        stdVarAccess.getVarOrDefault(String.class, CliKeys.JOB_SOURCE_SCRIPT_PATH, "LinkisCli");

    String osUser = System.getProperty(CliKeys.LINUX_USER_KEY);
    String[] adminUsers = StringUtils.split(CliKeys.ADMIN_USERS, ',');
    Set<String> adminSet = new HashSet<>();
    for (String admin : adminUsers) {
      adminSet.add(admin);
    }
    String submitUsr = CliUtils.getSubmitUser(stdVarAccess, osUser, adminSet);
    String proxyUsr = CliUtils.getProxyUser(stdVarAccess, submitUsr, adminSet);

    String enableExecuteOnce =
        stdVarAccess.getVarOrDefault(String.class, CliKeys.JOB_LABEL_EXECUTEONCE, "true");
    // default executeOnce-mode
    if (Boolean.parseBoolean(enableExecuteOnce)) {
      labelMap.put(LinkisKeys.KEY_EXECUTEONCE, "");
    } else {
      labelMap.remove(LinkisKeys.KEY_EXECUTEONCE);
    }
    String codePath = stdVarAccess.getVar(String.class, CliKeys.JOB_COMMON_CODE_PATH);
    Object extraArgsObj = stdVarAccess.getVar(Object.class, CliKeys.JOB_EXTRA_ARGUMENTS);
    if (extraArgsObj != null
        && extraArgsObj instanceof String[]
        && StringUtils.isBlank(code)
        && StringUtils.isBlank(codePath)) {
      String[] extraArgs = (String[]) extraArgsObj;
      codePath = extraArgs[0];
      if (extraArgs.length > 1) {
        runtimeMap.put(
            LinkisKeys.EXTRA_ARGUMENTS, Arrays.copyOfRange(extraArgs, 1, extraArgs.length));
      }
    }

    if (StringUtils.isBlank(code) && StringUtils.isNotBlank(codePath)) {
      try {
        code = CliUtils.readFile(codePath);
      } catch (Exception e) {
        LoggerManager.getInformationLogger().error("Failed to read file", e);
        throw e;
      }
    }

    executionMap.put(LinkisKeys.KEY_CODE, code);
    labelMap.put(LinkisKeys.KEY_ENGINETYPE, engineType);
    labelMap.put(LinkisKeys.KEY_CODETYPE, runType);
    labelMap.put(LinkisKeys.KEY_USER_CREATOR, proxyUsr + "-" + creator);
    sourceMap.put(LinkisKeys.KEY_SCRIPT_PATH, scriptPath);
    if (ctx.getExtraMap().containsKey(CliKeys.VERSION)) {
      sourceMap.put(LinkisKeys.CLI_VERSION, ctx.getExtraMap().get(CliKeys.VERSION));
    }
    runtimeMap.put(LinkisKeys.KEY_HIVE_RESULT_DISPLAY_TBALE, true);

    desc.setCreator(creator);
    desc.setParamConfMap(confMap);
    desc.setParamRunTimeMap(runtimeMap);
    desc.setParamVarsMap(varMap);
    desc.setLabelMap(labelMap);
    desc.setSourceMap(sourceMap);
    desc.setExecutionMap(executionMap);
    desc.setSubmitUser(submitUsr);
    desc.setProxyUser(proxyUsr);

    return desc;
  }

  public static LinkisJobOper generateOperator(CliCtx ctx) {
    LinkisJobOper linkisJobOperator = new LinkisJobOper();
    linkisJobOperator.setUJESClient(UJESClientFactory.getReusable(ctx.getVarAccess()));
    linkisJobOperator.setServerUrl(
        ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_COMMON_GATEWAY_URL));
    return linkisJobOperator;
  }
}
