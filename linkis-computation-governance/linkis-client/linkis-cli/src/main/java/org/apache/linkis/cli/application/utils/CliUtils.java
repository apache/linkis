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

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.application.entity.var.VarAccess;
import org.apache.linkis.cli.application.exception.BuilderException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CliUtils {
  public static final Gson GSON =
      new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public static String getSubmitUser(VarAccess stdVarAccess, String osUser, Set<String> adminSet) {

    String enableSpecifyUserStr =
        stdVarAccess.getVar(
            String.class, CliKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_USER_SPECIFICATION);
    Boolean enableSpecifyUser =
        Boolean.parseBoolean(enableSpecifyUserStr) || adminSet.contains(osUser);
    String authenticationStrategy =
        stdVarAccess.getVarOrDefault(
            String.class,
            CliKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY,
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
        if (stdVarAccess.hasVar(CliKeys.JOB_COMMON_SUBMIT_USER)) {
          submitUsr = stdVarAccess.getVar(String.class, CliKeys.JOB_COMMON_SUBMIT_USER);
          if (!adminSet.contains(osUser) && adminSet.contains(submitUsr)) {
            throw new BuilderException(
                "BLD0010",
                ErrorLevel.ERROR,
                CommonErrMsg.BuilderBuildErr,
                "Cannot specify admin-user as submit-user");
          }
        } else {
          submitUsr = osUser;
          LoggerManager.getInformationLogger()
              .info(
                  "user does not specify submit-user, will use current Linux user \""
                      + osUser
                      + "\" by default.");
        }
      } else if (stdVarAccess.hasVar(CliKeys.JOB_COMMON_SUBMIT_USER)) {
        submitUsr = stdVarAccess.getVar(String.class, CliKeys.JOB_COMMON_SUBMIT_USER);
        if (!StringUtils.equals(submitUsr, osUser)) {
          throw new BuilderException(
              "BLD0010",
              ErrorLevel.ERROR,
              CommonErrMsg.BuilderBuildErr,
              "Cannot specify submit-user when user-specification switch is off");
        }
      } else {
        submitUsr = osUser;
        LoggerManager.getInformationLogger()
            .info(
                "user does not specify submit-user, will use current Linux user \""
                    + osUser
                    + "\" by default.");
      }
    } else if (StringUtils.equalsIgnoreCase(
        authenticationStrategy, LinkisConstants.AUTH_STRATEGY_STATIC)) {
      String authKey = stdVarAccess.getVar(String.class, CliKeys.LINKIS_COMMON_TOKEN_KEY);
      String submitUsrInput =
          stdVarAccess.getVarOrDefault(String.class, CliKeys.JOB_COMMON_SUBMIT_USER, authKey);
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
        stdVarAccess.getVar(String.class, CliKeys.LINKIS_CLIENT_NONCUSTOMIZABLE_ENABLE_PROXY_USER);
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
      if (stdVarAccess.hasVar(CliKeys.JOB_COMMON_PROXY_USER)) {
        proxyUsr = stdVarAccess.getVar(String.class, CliKeys.JOB_COMMON_PROXY_USER);
        if (!adminSet.contains(submitUsr) && adminSet.contains(proxyUsr)) {
          throw new BuilderException(
              "BLD0010",
              ErrorLevel.ERROR,
              CommonErrMsg.BuilderBuildErr,
              "Cannot specify admin-user as proxy-user");
        }
      } else {
        proxyUsr = submitUsr;
        LoggerManager.getInformationLogger()
            .info(
                "user does not specify proxy-user, will use current submit-user \""
                    + submitUsr
                    + "\" by default.");
      }
    } else if (stdVarAccess.hasVar(CliKeys.JOB_COMMON_PROXY_USER)) {
      proxyUsr = stdVarAccess.getVar(String.class, CliKeys.JOB_COMMON_PROXY_USER);
      if (!StringUtils.equals(proxyUsr, submitUsr)) {
        throw new BuilderException(
            "BLD0010",
            ErrorLevel.ERROR,
            CommonErrMsg.BuilderBuildErr,
            "Cannot specify proxy-user when proxy-user-specification switch is off");
      }
    } else {
      proxyUsr = submitUsr;
      LoggerManager.getInformationLogger()
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

  public static <T> T castStringToAny(Class<T> clazz, String val) {
    if (StringUtils.isBlank(val)) {
      return null;
    }
    T ret = null;
    if (clazz == Object.class) {
      ret = clazz.cast(val);
    } else if (clazz == String.class) {
      ret = clazz.cast(val);
    } else if (clazz == Integer.class) {
      ret = clazz.cast(Integer.parseInt(val));
    } else if (clazz == Double.class) {
      ret = clazz.cast(Double.parseDouble(val));
    } else if (clazz == Float.class) {
      ret = clazz.cast(Float.parseFloat(val));
    } else if (clazz == Long.class) {
      ret = clazz.cast(Long.parseLong(val));
    } else if (clazz == Boolean.class) {
      ret = clazz.cast(Boolean.parseBoolean(val));
    }
    return ret;
  }

  public static Map<String, String> parseKVStringToMap(String kvStr, String separator) {
    if (StringUtils.isBlank(separator)) {
      separator = ",";
    }
    if (StringUtils.isBlank(kvStr)) {
      return null;
    }
    Map<String, String> argsProps = new HashMap<>();
    String[] args = StringUtils.splitByWholeSeparator(kvStr, separator);
    for (String arg : args) {
      int index = arg.indexOf("=");
      if (index != -1) {
        argsProps.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
      }
    }

    return argsProps;
  }

  public static boolean isValidExecId(String execId) {
    boolean ret = false;
    if (StringUtils.isNotBlank(execId)) {
      ret = true;
    }
    return ret;
  }

  public static String progressInPercentage(float progress) {
    return String.valueOf(progress * 100) + "%";
  }

  public static void doSleepQuietly(Long sleepMills) {
    try {
      Thread.sleep(sleepMills);
    } catch (Exception ignore) {
      // ignored
    }
  }
}
