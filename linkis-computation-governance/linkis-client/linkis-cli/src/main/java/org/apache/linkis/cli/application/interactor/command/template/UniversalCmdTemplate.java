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

package org.apache.linkis.cli.application.interactor.command.template;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.ValidateException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.CliCmdType;
import org.apache.linkis.cli.application.interactor.command.template.option.*;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;

public class UniversalCmdTemplate extends AbstractCmdTemplate implements Cloneable {

  protected StdOption<String> gatewayUrl =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_COMMON_GATEWAY_URL,
          new String[] {"--gatewayUrl"},
          "specify linkis gateway url",
          true,
          "");
  protected StdOption<String> authenticatationStrategy =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY,
          new String[] {"--authStg"},
          "specify linkis authentication strategy",
          true,
          "");
  protected StdOption<String> authKey =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_COMMON_TOKEN_KEY,
          new String[] {"--authKey"},
          "specify linkis authentication key(tokenKey)",
          true,
          "");
  protected StdOption<String> authValue =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_COMMON_TOKEN_VALUE,
          new String[] {"--authVal"},
          "specify linkis authentication value(tokenValue)",
          true,
          "");
  protected StdOption<String> userConfigPath =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_USER_CONFIG,
          new String[] {"--userConf"},
          "specify user configuration file path(absolute)",
          true,
          "");
  protected StdOption<String> killOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_KILL_OPT,
          new String[] {"--kill"},
          "specify linkis taskId for job to be killed",
          true,
          "");
  protected StdOption<String> logOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_LOG_OPT,
          new String[] {"--log"},
          "specify linkis taskId for querying job status",
          true,
          "");
  protected StdOption<String> resultOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_RESULT_OPT,
          new String[] {"--result"},
          "specify linkis taskId for querying job status",
          true,
          "");
  protected StdOption<String> statusOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_STATUS_OPT,
          new String[] {"--status"},
          "specify linkis taskId for querying job status",
          true,
          "");
  protected StdOption<Boolean> asyncOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_ASYNC_OPT,
          new String[] {"--async"},
          "specify linkis taskId for querying job status",
          true,
          false);
  protected StdOption<String> modeOpt =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_MODE_OPT,
          new String[] {"--mode"},
          "specify linkis execution mode: "
              + CliConstants.UJES_MODE
              + "/"
              + CliConstants.ONCE_MODE
              + ".",
          true,
          CliConstants.UJES_MODE);
  protected Flag helpOpt =
      flag(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_HELP_OPT,
          new String[] {"--help"},
          "specify linkis taskId for querying job status",
          true,
          false);

  protected StdOption<String> clusterOP =
      option(
          CliKeys.JOB_LABEL,
          CliKeys.JOB_LABEL_CLUSTER,
          new String[] {"-yarnCluster", "-k8sCluster"},
          "specify linkis yarn cluster for this job",
          true,
          "");

  protected StdOption<String> engineTypeOP =
      option(
          CliKeys.JOB_LABEL,
          CliKeys.JOB_LABEL_ENGINE_TYPE,
          new String[] {"-engineType"},
          "specify linkis engineType for this job",
          true,
          "");

  protected StdOption<String> codeTypeOp =
      option(
          CliKeys.JOB_LABEL,
          CliKeys.JOB_LABEL_CODE_TYPE,
          new String[] {"-codeType"},
          "specify linkis runType for this job",
          true,
          "");
  protected StdOption<String> codePathOp =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.JOB_COMMON_CODE_PATH,
          new String[] {"-codePath"},
          "specify file path that contains code you want to execute",
          true,
          "");

  protected StdOption<String> codeOp =
      option(
          CliKeys.JOB_EXEC,
          CliKeys.JOB_EXEC_CODE,
          new String[] {"-code"},
          "specify code that you want to execute",
          true,
          "");

  protected StdOption<String> scriptPathOp =
      option(
          CliKeys.JOB_SOURCE,
          CliKeys.JOB_SOURCE_SCRIPT_PATH,
          new String[] {"-scriptPath"},
          "specify remote path for your uploaded script",
          true,
          "");

  protected StdOption<String> submitUser =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.JOB_COMMON_SUBMIT_USER,
          new String[] {"-submitUser"},
          "specify submit user for this job",
          true,
          "");

  protected StdOption<String> proxyUser =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.JOB_COMMON_PROXY_USER,
          new String[] {"-proxyUser"},
          "specify proxy user who executes your code in Linkis server-side",
          true,
          "");

  protected StdOption<String> creatorOp =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.JOB_COMMON_CREATOR,
          new String[] {"-creator"},
          "specify creator for this job",
          true,
          "");

  protected StdOption<String> outPathOp =
      option(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH,
          new String[] {"-outPath"},
          "specify output path for resultSet. If not specified, then output reset to screen(stdout)",
          true,
          "");

  protected MapOption confMapOp =
      mapOption(
          CliKeys.JOB_PARAM_CONF,
          CliKeys.JOB_PARAM_CONF,
          new String[] {"-confMap"},
          "specify configurationMap(startupMap) for your job. You can put any start-up parameters into this Map(e.g. spark.executor.instances). Input format: -confMap key1=value1 -confMap key2=value2",
          true);

  protected MapOption runtimeMapOp =
      mapOption(
          CliKeys.JOB_PARAM_RUNTIME,
          CliKeys.JOB_PARAM_RUNTIME,
          new String[] {"-runtimeMap"},
          "specify runtimeMap for your job. You can put any start-up parameters into this Map(e.g. spark.executor.instances). Input format: -runtimeMap key1=value1 -runtimeMap key2=value2",
          true);

  protected SpecialMapOption varMapOp =
      speciaMapOption(
          CliKeys.JOB_PARAM_VAR,
          CliKeys.JOB_PARAM_VAR,
          new String[] {"-varMap"},
          "specify variables map. Variables is for key-word substitution. Use \'${key}\' to specify key-word. Input substitution rule as follow: -varMap key1=value1 -varMap key2=value2",
          true);

  protected MapOption labelMapOp =
      mapOption(
          CliKeys.JOB_LABEL,
          CliKeys.JOB_LABEL,
          new String[] {"-labelMap"},
          "specify label map. You can put any Linkis into this Map. Input format: -labelMap labelName1=labelValue1 -labelMap labelName2=labelValue2",
          true);

  protected MapOption sourceMapOp =
      mapOption(
          CliKeys.JOB_SOURCE,
          CliKeys.JOB_SOURCE,
          new String[] {"-sourceMap"},
          "specify source map. Input format: -sourceMap key1=value1 -sourceMap key2=value2",
          true);

  protected MapOption jobContentMapOp =
      mapOption(
          CliKeys.JOB_CONTENT,
          CliKeys.JOB_CONTENT,
          new String[] {"-jobContentMap"},
          "specify jobContent map. Input format: -jobContentMap key1=value1 -jobContentMap key2=value2",
          true);

  protected Flag versionFlag =
      flag(
          CliKeys.VERSION,
          CliKeys.VERSION,
          new String[] {"--version"},
          "show version",
          true,
          false);

  protected Parameter<String[]> argumentsParas =
      parameter(
          CliKeys.LINKIS_CLIENT_COMMON,
          CliKeys.JOB_EXTRA_ARGUMENTS,
          "arguments",
          "specify arguments if exist any",
          true,
          new String[] {""});

  public UniversalCmdTemplate() {
    super(CliCmdType.UNIVERSAL);
  }

  @Override
  public void checkParams() throws CommandException {
    if (versionFlag.hasVal()) {
      return;
    }
    int cnt = 0;
    if (statusOpt.hasVal()) {
      cnt++;
    }
    if (killOpt.hasVal()) {
      cnt++;
    }
    if (logOpt.hasVal()) {
      cnt++;
    }
    if (resultOpt.hasVal()) {
      cnt++;
    }
    if (helpOpt.hasVal()) {
      cnt++;
    }
    if (cnt > 1) {
      throw new ValidateException(
          "VLD0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ValidationErr,
          "Can only specify 1 of: "
              + statusOpt.getParamName()
              + "/"
              + killOpt.getParamName()
              + "/"
              + helpOpt.getParamName()
              + "/");
    } else if (cnt == 0) {
      int cnt2 = 0;
      if (argumentsParas.hasVal()) {
        if (!(argumentsParas.getValue() instanceof String[])
            || argumentsParas.getValue().length == 0) {
          throw new ValidateException(
              "VLD0001",
              ErrorLevel.ERROR,
              CommonErrMsg.ValidationErr,
              argumentsParas.getParamName()
                  + "has raw-value but failed to convert it into String-array. Raw-value: "
                  + argumentsParas.getRawVal());
        }
        String firstPara = argumentsParas.getValue()[0];
        if (StringUtils.startsWith(firstPara, "-")) {
          throw new CommandException(
              "CMD0011",
              ErrorLevel.ERROR,
              CommonErrMsg.ValidationErr,
              this.cmdType,
              "Illegal argument: " + Arrays.toString(argumentsParas.getValue()));
        }
        File file = new File(firstPara);
        if (!file.exists() || !file.isFile()) {
          throw new ValidateException(
              "VLD0001",
              ErrorLevel.ERROR,
              CommonErrMsg.ValidationErr,
              "Argument: \'"
                  + firstPara
                  + "\' is not a linkis-cli option. Assume it's script file, but no file named \'"
                  + firstPara
                  + "\' is found");
        }
        cnt2++;
      }
      if (codeOp.hasVal()) {
        cnt2++;
      }
      if (codePathOp.hasVal()) {
        cnt2++;
      }
      if (!modeOpt.hasVal()
          || StringUtils.equalsIgnoreCase(modeOpt.getValue(), CliConstants.UJES_MODE)) {
        if (cnt2 > 1) {
          throw new ValidateException(
              "VLD0001",
              ErrorLevel.ERROR,
              CommonErrMsg.ValidationErr,
              "Can only specify at most one of linkis-cli option: \''"
                  + codeOp.getParamName()
                  + "\' or \'"
                  + codePathOp.getParamName()
                  + "\' or \'script-path and script-arguments\'");
        }
        if (cnt2 == 0) {
          throw new ValidateException(
              "VLD0001",
              ErrorLevel.ERROR,
              CommonErrMsg.ValidationErr,
              "Need to specify at least one of linkis-cli option: \'"
                  + codeOp.getParamName()
                  + "\' or \'"
                  + codePathOp.getParamName()
                  + "\' or \'script-path and script-arguments\'.");
        }
      }
    }
  }
}
