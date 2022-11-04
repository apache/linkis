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

package org.apache.linkis.cli.application;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.data.FinishedData;
import org.apache.linkis.cli.application.data.PreparedData;
import org.apache.linkis.cli.application.data.ProcessedData;
import org.apache.linkis.cli.application.interactor.command.LinkisCmdType;
import org.apache.linkis.cli.application.interactor.command.template.UniversalCmdTemplate;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperatorBuilder;
import org.apache.linkis.cli.application.suite.ExecutionSuite;
import org.apache.linkis.cli.application.suite.ExecutionSuiteFactory;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.command.Params;
import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.common.entity.result.ExecutionResult;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.common.exception.handler.ExceptionHandler;
import org.apache.linkis.cli.core.constants.CommonConstants;
import org.apache.linkis.cli.core.exception.CommandException;
import org.apache.linkis.cli.core.exception.PropsException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.exception.handler.CommandExceptionHandler;
import org.apache.linkis.cli.core.exception.handler.DefaultExceptionHandler;
import org.apache.linkis.cli.core.interactor.command.CmdTemplateFactory;
import org.apache.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import org.apache.linkis.cli.core.interactor.command.parser.Parser;
import org.apache.linkis.cli.core.interactor.command.parser.SingleCmdParser;
import org.apache.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.core.interactor.properties.PropertiesLoader;
import org.apache.linkis.cli.core.interactor.properties.PropsFilesScanner;
import org.apache.linkis.cli.core.interactor.properties.StdPropsLoader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropertiesReader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropsFileReader;
import org.apache.linkis.cli.core.interactor.properties.reader.SysEnvReader;
import org.apache.linkis.cli.core.interactor.properties.reader.SysPropsReader;
import org.apache.linkis.cli.core.interactor.result.DefaultResultHandler;
import org.apache.linkis.cli.core.interactor.result.ExecutionResultImpl;
import org.apache.linkis.cli.core.interactor.result.ExecutionStatusEnum;
import org.apache.linkis.cli.core.interactor.validate.ParsedTplValidator;
import org.apache.linkis.cli.core.interactor.var.StdVarAccess;
import org.apache.linkis.cli.core.interactor.var.SysVarAccess;
import org.apache.linkis.cli.core.operator.JobOperatorBuilder;
import org.apache.linkis.cli.core.operator.JobOperatorFactory;
import org.apache.linkis.cli.core.present.PresentModeImpl;
import org.apache.linkis.cli.core.present.display.DisplayOperFactory;
import org.apache.linkis.cli.core.present.display.PlainTextFileWriter;
import org.apache.linkis.cli.core.present.display.StdOutWriter;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.linkis.cli.core.utils.SchedulerUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisClientApplication {
  private static Logger logger = LoggerFactory.getLogger(LinkisClientApplication.class);

  /**
   * generate Templates load env variables TODO: load version info
   *
   * @return PreparedData
   */
  private static PreparedData prepare() throws LinkisClientRuntimeException {
    /*
     generate template
    */
    CmdTemplate template = new UniversalCmdTemplate();
    CmdTemplateFactory.register(template);
    /*
     load env variables
    */
    Map<String, ClientProperties> propertiesMap = new HashMap<>();
    PropertiesLoader loader =
        new StdPropsLoader()
            .addPropertiesReader(new SysPropsReader())
            .addPropertiesReader(new SysEnvReader());
    for (ClientProperties properties : loader.loadProperties()) {
      propertiesMap.put(properties.getPropsId(), properties);
    }

    return new PreparedData(propertiesMap);
  }

  /**
   * parse user input load user config load default config check if all inputs are ok
   *
   * @param args user input arguments
   * @return ProcessedData
   */
  private static ProcessedData processInput(String[] args, PreparedData preparedData)
      throws Exception {

    if (preparedData == null) {
      return null;
    }

    /*
     user input
    */
    CmdTemplate template = CmdTemplateFactory.getTemplateCopy(LinkisCmdType.UNIVERSAL);
    Parser parser =
        new SingleCmdParser()
            .setMapper(null)
            .setTemplate(template)
            .setFitter(new SingleTplFitter());

    ParseResult result = parser.parse(args);

    ParsedTplValidator parsedTplValidator = new ParsedTplValidator();
    parsedTplValidator.doValidation(result.getParsedTemplate());

    Params params = result.getParams();
    logger.debug("==========params============\n" + Utils.GSON.toJson(params));

    /*
     VarAccess for sys_prop, sys_env
    */
    Map<String, ClientProperties> propertiesMap = preparedData.getPropertiesMap();
    VarAccess sysVarAccess =
        new SysVarAccess()
            .setSysProp(propertiesMap.get(CommonConstants.SYSTEM_PROPERTIES_IDENTIFIER))
            .setSysEnv(propertiesMap.get(CommonConstants.SYSTEM_ENV_IDENTIFIER));
    logger.debug("==========sys_var============\n" + Utils.GSON.toJson(sysVarAccess));

    LogUtils.getInformationLogger()
        .info(
            "LogFile path: "
                + sysVarAccess.getVar(String.class, AppKeys.LOG_PATH_KEY)
                + "/"
                + sysVarAccess.getVar(String.class, AppKeys.LOG_FILE_KEY));
    /*
     default config, -Dconf.root & -Dconf.file specifies config path
    */
    // scan config files given root path
    String configPath = sysVarAccess.getVar(String.class, AppKeys.CLIENT_CONFIG_ROOT_KEY);
    String defaultConfFileName =
        sysVarAccess.getVarOrDefault(
            String.class, AppKeys.DEFAULT_CONFIG_FILE_NAME_KEY, AppConstants.DEFAULT_CONFIG_NAME);
    if (StringUtils.isBlank(configPath)) {
      throw new PropsException(
          "PRP0007",
          ErrorLevel.ERROR,
          CommonErrMsg.PropsLoaderErr,
          "configuration root path specified by env variable: "
              + AppKeys.CLIENT_CONFIG_ROOT_KEY
              + " is empty.");
    }

    List<PropertiesReader> readersList =
        new PropsFilesScanner().getPropsReaders(configPath); // +1 user config
    /*
     user defined config
    */
    String userConfPath = null;
    if (params.containsParam(AppKeys.LINKIS_CLIENT_USER_CONFIG)) {
      userConfPath =
          (String) params.getParamItemMap().get(AppKeys.LINKIS_CLIENT_USER_CONFIG).getValue();
    }
    if (StringUtils.isNotBlank(userConfPath)) {
      PropertiesReader reader =
          new PropsFileReader()
              .setPropsId(AppKeys.LINKIS_CLIENT_USER_CONFIG)
              .setPropsPath(userConfPath);
      readersList.add(reader);
    } else {
      LogUtils.getInformationLogger()
          .info("User does not provide usr-configuration file. Will use default config");
    }
    /*
    load properties
    */
    PropertiesLoader loader =
        new StdPropsLoader()
            .addPropertiesReaders(readersList.toArray(new PropertiesReader[readersList.size()]));
    ClientProperties[] loaderResult = loader.loadProperties();
    for (ClientProperties properties : loaderResult) {
      if (StringUtils.equals(properties.getPropsId(), AppKeys.LINKIS_CLIENT_USER_CONFIG)) {
        for (Map.Entry prop : properties.entrySet()) {
          if (StringUtils.startsWith(
              (String) prop.getKey(), AppKeys.LINKIS_CLIENT_NONCUSTOMIZABLE)) {
            throw new PropsException(
                "PRP0007",
                ErrorLevel.ERROR,
                CommonErrMsg.PropsLoaderErr,
                "User cannot specify non-customizable configuration: " + prop.getKey());
          }
        }
      }
      propertiesMap.put(properties.getPropsId(), properties);
    }

    /*
     VarAccess for cmd, config
    */
    VarAccess stdVarAccess =
        new StdVarAccess()
            .setCmdParams(params)
            .setUserConf(propertiesMap.get(AppKeys.LINKIS_CLIENT_USER_CONFIG))
            .setDefaultConf(propertiesMap.get(defaultConfFileName))
            .init();
    logger.info("==========std_var============\n" + Utils.GSON.toJson(stdVarAccess));

    /*
     Prepare operator for accessing linkis
    */
    JobOperatorBuilder builder =
        new LinkisOperatorBuilder().setStdVarAccess(stdVarAccess).setSysVarAccess(sysVarAccess);

    JobOperatorFactory.register(AppKeys.REUSABLE_UJES_CLIENT, builder);
    /*
    Prepare DisplayOperator
    */
    DisplayOperFactory.register(PresentModeImpl.STDOUT, new StdOutWriter());
    DisplayOperFactory.register(PresentModeImpl.TEXT_FILE, new PlainTextFileWriter());

    return new ProcessedData(
        AppConstants.DUMMY_CID, params.getCmdType(), stdVarAccess, sysVarAccess);
  }

  /**
   * submit job display result
   *
   * @return FinishedData
   */
  private static FinishedData exec(ProcessedData data) throws Exception {
    if (data == null) {
      return null;
    }

    ExecutionSuite suite =
        ExecutionSuiteFactory.getSuite(
            data.getCmdType(), data.getStdVarAccess(), data.getSysVarAccess());

    /*
    Get everything
     */
    Map<String, Job> jobs = suite.getJobs();
    ResultHandler[] resultHandlers = suite.getResultHandlers();
    Execution execution = suite.getExecution();

    /*
    execute
     */
    final Map<String, Job> jobsToKill = jobs;
    Thread hook = new Thread(() -> execution.terminate(jobsToKill));
    if (jobsToKill != null && jobsToKill.size() != 0) {
      Runtime.getRuntime().addShutdownHook(hook);
    }
    ExecutionResult result = execution.execute(jobs);

    Runtime.getRuntime().removeShutdownHook(hook);

    return new FinishedData(result, resultHandlers);
  }

  public static void main(String[] args) {

    ExceptionHandler handler = new DefaultExceptionHandler();
    ProcessedData processedData = null;
    FinishedData finishedData = null;
    ExecutionResult executionResult = new ExecutionResultImpl(null, ExecutionStatusEnum.UNDEFINED);
    PreparedData preparedData = null;

    try {
      preparedData = prepare();
    } catch (Exception e) {
      handler.handle(e);
      executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
    }

    try {
      processedData = processInput(args, preparedData);
    } catch (CommandException ce) {
      new CommandExceptionHandler().handle(ce);
      executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
    } catch (Exception e) {
      handler.handle(e);
      executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
    }

    try {
      finishedData = exec(processedData);
    } catch (Exception e) {
      handler.handle(e);
      executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
    }

    if (finishedData != null) {
      executionResult = finishedData.getExecutionResult();
      if (executionResult == null) {
        executionResult = new ExecutionResultImpl(null, ExecutionStatusEnum.UNDEFINED);
      }
      if (executionResult.getException() != null) {
        handler.handle(executionResult.getException());
        new DefaultResultHandler().process(executionResult);
      } else {
        if (finishedData.getResultHandlers() != null) {
          for (ResultHandler resultHandler : finishedData.getResultHandlers()) {
            if (resultHandler != null) {
              resultHandler.process(executionResult);
            }
          }
        }
      }
    } else {
      executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
      new DefaultResultHandler().process(executionResult);
    }

    SchedulerUtils.shutDown();
  }
}
