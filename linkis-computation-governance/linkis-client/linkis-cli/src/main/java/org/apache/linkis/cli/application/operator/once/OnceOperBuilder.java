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

package org.apache.linkis.cli.application.operator.once;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.operator.JobOper;
import org.apache.linkis.cli.application.interactor.job.common.KeyParser;
import org.apache.linkis.cli.application.operator.JobOperBuilder;
import org.apache.linkis.cli.application.utils.CliUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class OnceOperBuilder implements JobOperBuilder {
  @Override
  public JobOper build(CliCtx ctx) {

    OnceJobDesc desc = new OnceJobDesc();

    Map<String, Object> confMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_PARAM_CONF);
    Map<String, Object> runtimeMap =
        ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_PARAM_RUNTIME);
    Map<String, Object> varMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_PARAM_VAR);
    Map<String, Object> labelMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_LABEL);
    Map<String, Object> sourceMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_SOURCE);
    Map<String, Object> executionMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_EXEC);
    Map<String, Object> jobContentMap = ctx.getVarAccess().getVar(Map.class, CliKeys.JOB_CONTENT);

    confMap = confMap == null ? new HashMap<>() : confMap;
    runtimeMap = runtimeMap == null ? new HashMap<>() : runtimeMap;
    varMap = varMap == null ? new HashMap<>() : varMap;
    labelMap = labelMap == null ? new HashMap<>() : labelMap;
    sourceMap = sourceMap == null ? new HashMap<>() : sourceMap;
    executionMap = executionMap == null ? new HashMap<>() : executionMap;
    jobContentMap = jobContentMap == null ? new HashMap<>() : jobContentMap;

    confMap = KeyParser.removePrefixForKeysInMap(confMap);
    runtimeMap = KeyParser.removePrefixForKeysInMap(runtimeMap);
    labelMap = KeyParser.removePrefixForKeysInMap(labelMap);
    sourceMap = KeyParser.removePrefixForKeysInMap(sourceMap);
    executionMap = KeyParser.removePrefixForKeysInMap(executionMap);
    jobContentMap = KeyParser.removePrefixForKeysInMap(jobContentMap);

    for (String key : ctx.getVarAccess().getAllVarKeys()) {
      Object val = ctx.getVarAccess().getVar(Object.class, key);
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
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.JOB_CONTENT)) {
          KeyParser.removePrefixAndPutValToMap(jobContentMap, key, val, CliKeys.JOB_CONTENT);
        } else if (StringUtils.startsWithIgnoreCase(key, CliKeys.LINKIS_CLIENT_COMMON)) {
          // do nothing
        } else {
          //        confMap.put(key, stdVarAccess.getVar(Object.class, key));
        }
      }
    }

    Boolean isAsync =
        ctx.getVarAccess().getVarOrDefault(Boolean.class, CliKeys.LINKIS_CLIENT_ASYNC_OPT, false);

    String creator;
    if (!isAsync) {
      creator =
          ctx.getVarAccess()
              .getVarOrDefault(
                  String.class, CliKeys.JOB_COMMON_CREATOR, CliConstants.JOB_CREATOR_DEFAULT);
    } else {
      creator =
          ctx.getVarAccess()
              .getVarOrDefault(
                  String.class, CliKeys.JOB_COMMON_CREATOR, CliConstants.JOB_CREATOR_ASYNC_DEFAULT);
    }
    String code = ctx.getVarAccess().getVar(String.class, CliKeys.JOB_EXEC_CODE);
    String engineType = ctx.getVarAccess().getVar(String.class, CliKeys.JOB_LABEL_ENGINE_TYPE);
    String runType = ctx.getVarAccess().getVar(String.class, CliKeys.JOB_LABEL_CODE_TYPE);
    String scriptPath =
        ctx.getVarAccess()
            .getVarOrDefault(String.class, CliKeys.JOB_SOURCE_SCRIPT_PATH, "LinkisCli");

    String osUser = System.getProperty(CliKeys.LINUX_USER_KEY);
    String[] adminUsers = StringUtils.split(CliKeys.ADMIN_USERS, ',');
    Set<String> adminSet = new HashSet<>();
    for (String admin : adminUsers) {
      adminSet.add(admin);
    }
    String submitUsr = CliUtils.getSubmitUser(ctx.getVarAccess(), osUser, adminSet);
    String proxyUsr = CliUtils.getProxyUser(ctx.getVarAccess(), submitUsr, adminSet);

    String enableExecuteOnce =
        ctx.getVarAccess().getVarOrDefault(String.class, CliKeys.JOB_LABEL_EXECUTEONCE, "true");
    // default executeOnce-mode
    if (Boolean.parseBoolean(enableExecuteOnce)) {
      labelMap.put(LinkisKeys.KEY_EXECUTEONCE, "");
    } else {
      labelMap.remove(LinkisKeys.KEY_EXECUTEONCE);
    }
    String codePath = ctx.getVarAccess().getVar(String.class, CliKeys.JOB_COMMON_CODE_PATH);
    Object extraArgsObj = ctx.getVarAccess().getVar(Object.class, CliKeys.JOB_EXTRA_ARGUMENTS);
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
      code = CliUtils.readFile(codePath);
    }

    executionMap.put(LinkisKeys.KEY_CODE, code);
    labelMap.put(LinkisKeys.KEY_ENGINETYPE, engineType);
    labelMap.put(LinkisKeys.KEY_CODETYPE, runType);
    labelMap.put(LinkisKeys.KEY_USER_CREATOR, proxyUsr + "-" + creator);
    sourceMap.put(LinkisKeys.KEY_SCRIPT_PATH, scriptPath);
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
    desc.setJobContentMap(jobContentMap);

    OnceJobOper onceJobOper = new OnceJobOper();
    onceJobOper.init(desc);

    return onceJobOper;
  }
}
