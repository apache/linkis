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

package org.apache.linkis.cli.application.utils;

import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.BuilderException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Set;

public class ExecutionUtils {
  public static String getSubmitUser(VarAccess stdVarAccess, String osUser, Set<String> adminSet) {

    String enableSpecifyUserStr =
        stdVarAccess.getVar(
            String.class, AppKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_USER_SPECIFICATION);
    Boolean enableSpecifyUser =
        Boolean.parseBoolean(enableSpecifyUserStr) || adminSet.contains(osUser);
    String authenticationStrategy =
        stdVarAccess.getVarOrDefault(
            String.class,
            AppKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY,
            LinkisConstants.AUTH_STRATEGY_STATIC);

    String submitUsr;
    if (StringUtils.equalsIgnoreCase(authenticationStrategy, LinkisConstants.AUTH_STRATEGY_TOKEN)) {
      /*
         default -> use current os user
         enableSpecifyUser -> -submitUser
         enableSpecifyProxyUser -> -proxyUser
         ADMIN_USERS can do anything
      */
      if (enableSpecifyUser) {
        if (stdVarAccess.hasVar(AppKeys.JOB_COMMON_SUBMIT_USER)) {
          submitUsr = stdVarAccess.getVar(String.class, AppKeys.JOB_COMMON_SUBMIT_USER);
          if (!adminSet.contains(osUser) && adminSet.contains(submitUsr)) {
            throw new BuilderException(
                "BLD0010",
                ErrorLevel.ERROR,
                CommonErrMsg.BuilderBuildErr,
                "Cannot specify admin-user as submit-user");
          }
        } else {
          submitUsr = osUser;
          LogUtils.getInformationLogger()
              .info(
                  "user does not specify submit-user, will use current Linux user \""
                      + osUser
                      + "\" by default.");
        }
      } else if (stdVarAccess.hasVar(AppKeys.JOB_COMMON_SUBMIT_USER)) {
        submitUsr = stdVarAccess.getVar(String.class, AppKeys.JOB_COMMON_SUBMIT_USER);
        if (!StringUtils.equals(submitUsr, osUser)) {
          throw new BuilderException(
              "BLD0010",
              ErrorLevel.ERROR,
              CommonErrMsg.BuilderBuildErr,
              "Cannot specify submit-user when user-specification switch is off");
        }
      } else {
        submitUsr = osUser;
        LogUtils.getInformationLogger()
            .info(
                "user does not specify submit-user, will use current Linux user \""
                    + osUser
                    + "\" by default.");
      }
    } else if (StringUtils.equalsIgnoreCase(
        authenticationStrategy, LinkisConstants.AUTH_STRATEGY_STATIC)) {
      String authKey = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_TOKEN_KEY);
      String submitUsrInput =
          stdVarAccess.getVarOrDefault(String.class, AppKeys.JOB_COMMON_SUBMIT_USER, authKey);
      if (StringUtils.equalsIgnoreCase(submitUsrInput, authKey)) {
        submitUsr = authKey;
      } else {
        throw new BuilderException(
            "BLD0011",
            ErrorLevel.ERROR,
            CommonErrMsg.BuilderBuildErr,
            "Submit-User should be the same as Auth-Key under Static-Authentication-Strategy \'");
      }
    } else {
      throw new BuilderException(
          "BLD0011",
          ErrorLevel.ERROR,
          CommonErrMsg.BuilderBuildErr,
          "Authentication strategy \'" + authenticationStrategy + "\' is not supported");
    }

    return submitUsr;
  }

  public static String getProxyUser(
      VarAccess stdVarAccess, String submitUsr, Set<String> adminSet) {

    String enableSpecifyPRoxyUserStr =
        stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_PROXY_USER);
    Boolean enableSpecifyProxyUser =
        Boolean.parseBoolean(enableSpecifyPRoxyUserStr) || adminSet.contains(submitUsr);

    /*
       default -> use current -submitUser user
       enableSpecifyUser -> -submitUser
       enableSpecifyProxyUser -> -proxyUser
       ADMIN_USERS can do anything
    */
    String proxyUsr;

    if (enableSpecifyProxyUser) {
      if (stdVarAccess.hasVar(AppKeys.JOB_COMMON_PROXY_USER)) {
        proxyUsr = stdVarAccess.getVar(String.class, AppKeys.JOB_COMMON_PROXY_USER);
        if (!adminSet.contains(submitUsr) && adminSet.contains(proxyUsr)) {
          throw new BuilderException(
              "BLD0010",
              ErrorLevel.ERROR,
              CommonErrMsg.BuilderBuildErr,
              "Cannot specify admin-user as proxy-user");
        }
      } else {
        proxyUsr = submitUsr;
        LogUtils.getInformationLogger()
            .info(
                "user does not specify proxy-user, will use current submit-user \""
                    + submitUsr
                    + "\" by default.");
      }
    } else if (stdVarAccess.hasVar(AppKeys.JOB_COMMON_PROXY_USER)) {
      proxyUsr = stdVarAccess.getVar(String.class, AppKeys.JOB_COMMON_PROXY_USER);
      if (!StringUtils.equals(proxyUsr, submitUsr)) {
        throw new BuilderException(
            "BLD0010",
            ErrorLevel.ERROR,
            CommonErrMsg.BuilderBuildErr,
            "Cannot specify proxy-user when proxy-user-specification switch is off");
      }
    } else {
      proxyUsr = submitUsr;
      LogUtils.getInformationLogger()
          .info(
              "user does not specify proxy-user, will use current submit-user \""
                  + proxyUsr
                  + "\" by default.");
    }
    return proxyUsr;
  }

  public static String readFile(String path) {
    try {
      File inputFile = new File(path);

      InputStream inputStream = new FileInputStream(inputFile);
      InputStreamReader iReader = new InputStreamReader(inputStream);
      BufferedReader bufReader = new BufferedReader(iReader);

      StringBuilder sb = new StringBuilder();
      StringBuilder line;
      while (bufReader.ready()) {
        line = new StringBuilder(bufReader.readLine());
        sb.append(line).append(System.lineSeparator());
      }

      return sb.toString();

    } catch (FileNotFoundException fe) {
      throw new BuilderException(
          "BLD0005",
          ErrorLevel.ERROR,
          CommonErrMsg.BuilderBuildErr,
          "User specified script file does not exist: " + path,
          fe);
    } catch (Exception e) {
      throw new BuilderException(
          "BLD0006",
          ErrorLevel.ERROR,
          CommonErrMsg.BuilderBuildErr,
          "Cannot read user specified script file: " + path,
          e);
    }
  }
}
