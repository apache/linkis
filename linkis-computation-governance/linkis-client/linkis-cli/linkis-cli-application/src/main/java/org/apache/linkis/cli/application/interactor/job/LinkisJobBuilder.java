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
 
package org.apache.linkis.cli.application.interactor.job;

import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.job.OutputWay;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.BuilderException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;
import org.apache.linkis.cli.core.interactor.var.VarAccess;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class LinkisJobBuilder extends JobBuilder {

    @Override
    public Job build() {
        checkInit();

        Map<String, Object> confMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_PARAM_CONF);
        Map<String, Object> runtimeMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_PARAM_RUNTIME);
        Map<String, Object> varMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_PARAM_VAR);
        Map<String, Object> labelMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_LABEL);
        Map<String, Object> sourceMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_SOURCE);
        Map<String, Object> executionMap = stdVarAccess.getVar(Map.class, LinkisClientKeys.JOB_EXEC);

        confMap = confMap == null ? new HashMap<>() : confMap;
        runtimeMap = runtimeMap == null ? new HashMap<>() : runtimeMap;
        varMap = varMap == null ? new HashMap<>() : varMap;
        labelMap = labelMap == null ? new HashMap<>() : labelMap;
        sourceMap = sourceMap == null ? new HashMap<>() : sourceMap;
        executionMap = executionMap == null ? new HashMap<>() : executionMap;

        confMap = removeKeyPrefixInMap(confMap);
        runtimeMap = removeKeyPrefixInMap(runtimeMap);
        labelMap = removeKeyPrefixInMap(labelMap);
        sourceMap = removeKeyPrefixInMap(sourceMap);
        executionMap = removeKeyPrefixInMap(executionMap);

        for (String key : stdVarAccess.getAllVarKeys()) {
            Object val = stdVarAccess.getVar(Object.class, key);
            if (!(val instanceof Map) && val != null) {
                // note that we allow it to overwrite existing values in map
                if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_PARAM_CONF)) {
                    removePrefixAndPutVal(confMap, key, val, LinkisClientKeys.JOB_PARAM_CONF);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_PARAM_VAR)) {
                    removePrefixAndPutVal(varMap, key, val, LinkisClientKeys.JOB_PARAM_VAR);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_PARAM_RUNTIME)) {
                    removePrefixAndPutVal(runtimeMap, key, val, LinkisClientKeys.JOB_PARAM_RUNTIME);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_EXEC)) {
                    removePrefixAndPutVal(executionMap, key, val, LinkisClientKeys.JOB_EXEC);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_LABEL)) {
                    removePrefixAndPutVal(labelMap, key, val, LinkisClientKeys.JOB_LABEL);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.JOB_SOURCE)) {
                    removePrefixAndPutVal(sourceMap, key, val, LinkisClientKeys.JOB_SOURCE);
                } else if (StringUtils.startsWithIgnoreCase(key, LinkisClientKeys.LINKIS_CLIENT_COMMON)) {
                    //do nothing
                } else {
//        confMap.put(key, stdVarAccess.getVar(Object.class, key));
                }
            }
        }


        String creator = stdVarAccess.getVarOrDefault(String.class, LinkisClientKeys.JOB_COMMON_CREATOR, LinkisConstants.JOB_CREATOR_DEFAULT);
        String code = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_EXEC_CODE);
        String engineType = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_LABEL_ENGINE_TYPE);
        String runType = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_LABEL_CODE_TYPE);
        String codePath = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_CODE_PATH);
        String scriptPath = stdVarAccess.getVarOrDefault(String.class, LinkisClientKeys.JOB_SOURCE_SCRIPT_PATH, "LinkisCli");

        String enableSpecifyUserStr = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_USER_SPECIFICATION);
        String enableSpecifyPRoxyUserStr = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_PROXY_USER);

        String osUser = sysVarAccess.getVar(String.class, LinkisClientKeys.LINUX_USER_KEY);
        String[] adminUsers = StringUtils.split(LinkisClientKeys.ADMIN_USERS, ',');
        Set<String> adminSet = new HashSet<>();
        for (String admin : adminUsers) {
            adminSet.add(admin);
        }
        Boolean enableSpecifyUser = Boolean.parseBoolean(enableSpecifyUserStr) || adminSet.contains(osUser);
        Boolean enableSpecifyProxyUser = Boolean.parseBoolean(enableSpecifyPRoxyUserStr) || adminSet.contains(osUser);

        /*
            default -> use current os user
            enableSpecifyUser -> -submitUser
            enableSpecifyProxyUser -> -proxyUser
            ADMIN_USERS can do anything
         */
        String submitUsr;
        String proxyUsr;

        if (enableSpecifyUser) {
            if (stdVarAccess.hasVar(LinkisClientKeys.JOB_COMMON_SUBMIT_USER)) {
                submitUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_SUBMIT_USER);
                if (!adminSet.contains(osUser) && adminSet.contains(submitUsr)) {
                    throw new BuilderException("BLD0010", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot specify admin-user as submit-user");
                }
            } else {
                submitUsr = osUser;
                LogUtils.getInformationLogger().info("user does not specify submit-user, will use current Linux user \"" + osUser + "\" by default.");
            }
        } else if (stdVarAccess.hasVar(LinkisClientKeys.JOB_COMMON_SUBMIT_USER)) {
            submitUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_SUBMIT_USER);
            if (!StringUtils.equals(submitUsr, osUser)) {
                throw new BuilderException("BLD0010", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot specify submit-user when user-specification switch is off");
            }
        } else {
            submitUsr = osUser;
            LogUtils.getInformationLogger().info("user does not specify submit-user, will use current Linux user \"" + osUser + "\" by default.");
        }

        if (enableSpecifyProxyUser) {
            if (stdVarAccess.hasVar(LinkisClientKeys.JOB_COMMON_PROXY_USER)) {
                proxyUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_PROXY_USER);
                if (!adminSet.contains(osUser) && adminSet.contains(proxyUsr)) {
                    throw new BuilderException("BLD0010", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot specify admin-user as proxy-user");
                }
            } else {
                proxyUsr = submitUsr;
                LogUtils.getInformationLogger().info("user does not specify proxy-user, will use current submit-user \"" + submitUsr + "\" by default.");
            }
        } else if (stdVarAccess.hasVar(LinkisClientKeys.JOB_COMMON_PROXY_USER)) {
            proxyUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_PROXY_USER);
            if (!StringUtils.equals(proxyUsr, submitUsr)) {
                throw new BuilderException("BLD0010", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot specify proxy-user when proxy-user-specification switch is off");
            }
        } else {
            proxyUsr = submitUsr;
            LogUtils.getInformationLogger().info("user does not specify proxy-user, will use current submit-user \"" + proxyUsr + "\" by default.");
        }

        if (StringUtils.isBlank(code) && StringUtils.isNotBlank(codePath)) {
            try {
                File inputFile = new File(codePath);

                InputStream inputStream = new FileInputStream(inputFile);
                InputStreamReader iReader = new InputStreamReader(inputStream);
                BufferedReader bufReader = new BufferedReader(iReader);

                StringBuilder sb = new StringBuilder();
                StringBuilder line;
                while (bufReader.ready()) {
                    line = new StringBuilder(bufReader.readLine());
                    sb.append(line).append(System.lineSeparator());
                }

                code = sb.toString();

            } catch (FileNotFoundException fe) {
                throw new BuilderException("BLD0005", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "User specified script file does not exist: " + codePath, fe);
            } catch (Exception e) {
                throw new BuilderException("BLD0006", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot read user specified script file: " + codePath, e);
            }
        }

        executionMap.put(LinkisKeys.KEY_CODE, code);
        labelMap.put(LinkisKeys.KEY_ENGINETYPE, engineType);
        labelMap.put(LinkisKeys.KEY_CODETYPE, runType);
        labelMap.put(LinkisKeys.KEY_USER_CREATOR, proxyUsr + "-" + creator);
        sourceMap.put(LinkisKeys.KEY_SCRIPT_PATH, scriptPath);

        String outputPath = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH);

        targetObj.setOutputPath(outputPath);
        targetObj.setOutputWay(OutputWay.STANDARD);
        if (StringUtils.isNotBlank(outputPath)) {
            targetObj.setOutputWay(OutputWay.TEXT_FILE);
        }
        ((LinkisJob) targetObj).setParamConfMap(confMap);
        ((LinkisJob) targetObj).setParamRunTimeMap(runtimeMap);
        ((LinkisJob) targetObj).setParamVarsMap(varMap);
        ((LinkisJob) targetObj).setLabelMap(labelMap);
        ((LinkisJob) targetObj).setSourceMap(sourceMap);
        ((LinkisJob) targetObj).setExecutionMap(executionMap);
        targetObj.setSubmitUser(submitUsr);
        targetObj.setProxyUser(proxyUsr);

        return super.build();
    }

    private Map<String, Object> removeKeyPrefixInMap(Map<String, Object> map) {
        final String[] PREFIX = new String[]{
                LinkisClientKeys.JOB_PARAM_CONF,
                LinkisClientKeys.JOB_PARAM_RUNTIME,
                LinkisClientKeys.JOB_PARAM_VAR,
                LinkisClientKeys.JOB_EXEC,
                LinkisClientKeys.JOB_SOURCE,
                LinkisClientKeys.JOB_LABEL
        };
        for (String prefix : PREFIX) {
            map = removeKeyPrefixInMap(map, prefix);
        }
        return map;
    }

    private Map<String, Object> removeKeyPrefixInMap(Map<String, Object> map, String prefix) {
        if (map == null) {
            return null;
        }
        Map<String, Object> newMap = new HashMap<>();
        for (String key : map.keySet()) {
            String realKey = getRealKey(key, prefix);
            if (StringUtils.isNotBlank(realKey)) {
                if (StringUtils.startsWith(key, prefix)) {
                    newMap.put(realKey, map.get(key));
                } else {
                    newMap.put(key, map.get(key));
                }
            }
        }
        return newMap;
    }

    private void removePrefixAndPutVal(Map<String, Object> map, String key, Object value, String prefix) {
        String realKey = getRealKey(key, prefix);
        if (StringUtils.isNotBlank(realKey) && !(value instanceof Map)) {
            map.put(realKey, value);
        }
    }


    private String getRealKey(String key, String prefix) {
        String realKey = key;
        if (StringUtils.startsWith(key, prefix)) {
            realKey = StringUtils.substring(key, prefix.length() + 1);
        }
        return realKey;
    }

    @Override
    public LinkisJobBuilder setStdVarAccess(VarAccess varAccess) {
        return (LinkisJobBuilder) super.setStdVarAccess(varAccess);
    }

    @Override
    public LinkisJobBuilder setSysVarAccess(VarAccess varAccess) {
        return (LinkisJobBuilder) super.setSysVarAccess(varAccess);
    }

    @Override
    public LinkisJob getTargetNewInstance() {
        return new LinkisJob();
    }

}