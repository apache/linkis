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
 
package org.apache.linkis.cli.application;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.application.data.FinishedData;
import org.apache.linkis.cli.application.data.PreparedData;
import org.apache.linkis.cli.application.data.ProcessedData;
import org.apache.linkis.cli.application.driver.LinkisClientDriver;
import org.apache.linkis.cli.application.driver.UjesClientDriverBuilder;
import org.apache.linkis.cli.application.driver.transformer.DriverTransformer;
import org.apache.linkis.cli.application.driver.transformer.UjesClientDriverTransformer;
import org.apache.linkis.cli.application.interactor.command.LinkisCmdType;
import org.apache.linkis.cli.application.interactor.command.template.UniversalCmdTemplate;
import org.apache.linkis.cli.application.interactor.result.PresentResultHandler;
import org.apache.linkis.cli.application.interactor.validate.LinkisJobValidator;
import org.apache.linkis.cli.application.presenter.LinkisJobLogPresenter;
import org.apache.linkis.cli.application.presenter.QueryBasedPresenter;
import org.apache.linkis.cli.application.presenter.converter.LinkisLogModelConverter;
import org.apache.linkis.cli.application.suite.ExecutionSuite;
import org.apache.linkis.cli.application.suite.ExecutionSuiteFactory;
import org.apache.linkis.cli.application.suite.SuiteFactoryImpl;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.constants.CommonConstants;
import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.command.Params;
import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.execution.ExecutionResult;
import org.apache.linkis.cli.common.entity.execution.executor.Executor;
import org.apache.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.common.exception.handler.ExceptionHandler;
import org.apache.linkis.cli.core.exception.CommandException;
import org.apache.linkis.cli.core.exception.PropsException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.exception.handler.CommandExceptionHandler;
import org.apache.linkis.cli.core.exception.handler.DefaultExceptionHandler;
import org.apache.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import org.apache.linkis.cli.core.interactor.command.parser.Parser;
import org.apache.linkis.cli.core.interactor.command.parser.SingleCmdParser;
import org.apache.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.core.interactor.execution.ExecutionResultImpl;
import org.apache.linkis.cli.core.interactor.execution.SyncSubmission;
import org.apache.linkis.cli.core.interactor.execution.observer.event.TriggerEvent;
import org.apache.linkis.cli.core.interactor.execution.observer.listener.IncLogEventListener;
import org.apache.linkis.cli.core.interactor.properties.PropertiesLoader;
import org.apache.linkis.cli.core.interactor.properties.PropsFilesScanner;
import org.apache.linkis.cli.core.interactor.properties.StdPropsLoader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropertiesReader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropsFileReader;
import org.apache.linkis.cli.core.interactor.properties.reader.SysEnvReader;
import org.apache.linkis.cli.core.interactor.properties.reader.SysPropsReader;
import org.apache.linkis.cli.core.interactor.result.DefaultResultHandler;
import org.apache.linkis.cli.core.interactor.validate.ParsedTplValidator;
import org.apache.linkis.cli.core.interactor.validate.Validator;
import org.apache.linkis.cli.core.interactor.var.StdVarAccess;
import org.apache.linkis.cli.core.interactor.var.SysVarAccess;
import org.apache.linkis.cli.core.interactor.var.VarAccess;
import org.apache.linkis.cli.core.presenter.Presenter;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: Main Enterance
 */
public class LinkisClientApplication {
    private static Logger logger = LoggerFactory.getLogger(LinkisClientApplication.class);

    /**
     * generate Templates
     * load env variables
     * TODO: load version info
     *
     * @return PreparedData
     */
    private static PreparedData prepare() throws LinkisClientRuntimeException {
    /*
      generate template
     */
        CmdTemplate template = new UniversalCmdTemplate();
        Map<String, CmdTemplate> templateMap = new HashedMap();
        templateMap.put(template.getCmdType().getName(), template);
    /*
      load env variables
     */
        Map<String, ClientProperties> propertiesMap = new HashMap<>();
        PropertiesLoader loader = new StdPropsLoader()
                .addPropertiesReader(new SysPropsReader())
                .addPropertiesReader(new SysEnvReader());
        for (ClientProperties properties : loader.loadProperties()) {
            propertiesMap.put(properties.getPropsId(), properties);
        }

        return new PreparedData(templateMap, propertiesMap);
    }

    /**
     * parse user input
     * load user config
     * load default config
     * check if all inputs are ok
     *
     * @param args user input arguments
     * @return ProcessedData
     */
    private static ProcessedData processInput(String[] args, PreparedData preparedData) throws Exception {

        if (preparedData == null) {
            return null;
        }

    /*
      user input
     */
        CmdTemplate template = preparedData.getTemplateMap().get(LinkisCmdType.UNIVERSAL.getName());
        Parser parser = new SingleCmdParser()
                .setMapper(null)
                .setTemplate(template)
                .setFitter(new SingleTplFitter());

        ParseResult result = parser.parse(args);

        ParsedTplValidator parsedTplValidator = new ParsedTplValidator();
        parsedTplValidator.doValidation(result.getParsedTemplateCopy());

        Params params = result.getParams();
        logger.debug("==========params============\n" + Utils.GSON.toJson(params));

    /*
      VarAccess for sys_prop, sys_env
     */
        Map<String, ClientProperties> propertiesMap = preparedData.getPropertiesMap();
        VarAccess sysVarAccess = new SysVarAccess()
                .setSysProp(propertiesMap.get(CommonConstants.SYSTEM_PROPERTIES_IDENTIFIER))
                .setSysEnv(propertiesMap.get(CommonConstants.SYSTEM_ENV_IDENTIFIER));
        logger.debug("==========sys_var============\n" + Utils.GSON.toJson(sysVarAccess));

        LogUtils.getInformationLogger().info("LogFile path: " +
                sysVarAccess.getVar(String.class, LinkisClientKeys.LOG_PATH_KEY) + "/" +
                sysVarAccess.getVar(String.class, LinkisClientKeys.LOG_FILE_KEY)
        );
    /*
      default config, -Dconf.root & -Dconf.file specifies config path
     */
        //scan config files given root path
        String configPath = sysVarAccess.getVar(String.class, LinkisClientKeys.CLIENT_CONFIG_ROOT_KEY);
        String defaultConfFileName = sysVarAccess.getVarOrDefault(
                String.class, LinkisClientKeys.DEFAULT_CONFIG_FILE_NAME_KEY, AppConstants.DEFAULT_CONFIG_NAME);
        if (StringUtils.isBlank(configPath)) {
            throw new PropsException(
                    "PRP0007", ErrorLevel.ERROR, CommonErrMsg.PropsLoaderErr,
                    "configuration root path specified by env variable: " +
                            LinkisClientKeys.CLIENT_CONFIG_ROOT_KEY + " is empty.");
        }

        List<PropertiesReader> readersList = new PropsFilesScanner().getPropsReaders(configPath); //+1 user config
    /*
      user defined config
     */
        String userConfPath = null;
        if (params.containsParam(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)) {
            userConfPath = (String) params
                    .getParamItemMap()
                    .get(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)
                    .getValue();
        }
        if (StringUtils.isNotBlank(userConfPath)) {
            PropertiesReader reader = new PropsFileReader()
                    .setPropsId(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)
                    .setPropsPath(userConfPath);
            readersList.add(reader);
        } else {
            LogUtils.getInformationLogger().info("User does not provide usr-configuration file. Will use default config");
        }
    /*
     load properties
     */
        PropertiesLoader loader = new StdPropsLoader()
                .addPropertiesReaders(
                        readersList.toArray(
                                new PropertiesReader[readersList.size()]
                        )
                );
        ClientProperties[] loaderResult = loader.loadProperties();
        for (ClientProperties properties : loaderResult) {
            if (StringUtils.equals(properties.getPropsId(), LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)) {
                for (Map.Entry prop : properties.entrySet()) {
                    if (StringUtils.startsWith((String) prop.getKey(), LinkisClientKeys.LINKIS_CLIENT_NONCUSTOMIZABLE)) {
                        throw new PropsException("PRP0007", ErrorLevel.ERROR, CommonErrMsg.PropsLoaderErr, "User cannot specify non-customizable configuration: " + prop.getKey());
                    }
                }

            }
            propertiesMap.put(properties.getPropsId(), properties);
        }

    /*
      VarAccess for cmd, config
     */
        VarAccess stdVarAccess = new StdVarAccess()
                .setCmdParams(params)
                .setUserConf(propertiesMap.get(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG))
                .setDefaultConf(propertiesMap.get(defaultConfFileName))
                .init();
        logger.info("==========std_var============\n" + Utils.GSON.toJson(stdVarAccess));


        return new ProcessedData(null,
                params.getCmdType(),
                stdVarAccess,
                sysVarAccess
        );
    }

    /**
     * submit job
     * display result
     *
     * @return FinishedData
     */
    private static FinishedData exec(ProcessedData data) throws Exception {
        if (data == null) {
            return null;
        }

    /*
    decide jobBuilder and executorBuilder
     */
        ExecutionSuiteFactory suiteFactory = new SuiteFactoryImpl();
        ExecutionSuite suite = suiteFactory.getSuite(data.getCmdType(), data.getStdVarAccess());

    /*
    build job
     */
        Executor executor = null;
        Job job = null;
        ResultHandler[] resultHandlers = null;
        Execution execution = suite.getExecution();
        if (suite.getJobBuilder() != null) {
            job = suite.getJobBuilder()
                    .setStdVarAccess(data.getStdVarAccess())
                    .setSysVarAccess(data.getSysVarAccess())
                    .build();


            logger.info("==========job============\n" + Utils.GSON.toJson(job));


            Validator jobValidator = new LinkisJobValidator();
            jobValidator.doValidation(job);
        }


    /*
    prepare executor
     */
        if (suite.getExecutorBuilder() != null) {
            executor = suite.getExecutorBuilder()
                    .setStdVarAccess(data.getStdVarAccess())
                    .setSysVarAccess(data.getSysVarAccess())
                    .build();
        }

    /*
    Execution
     */

        LinkisClientDriver driver = new UjesClientDriverBuilder()
                .setStdVarAccess(data.getStdVarAccess())
                .setSysVarAccess(data.getSysVarAccess())
                .build();
        // TODO: use executor rather than driver in presenter

        DriverTransformer driverTransformer = new UjesClientDriverTransformer();
        if (execution instanceof SyncSubmission) {
            // TODO: use executor rather than driver in presenter
            //TODO: let suiteFactory do this, but don't want to new an Executor
            LinkisJobLogPresenter inclogPresenter = new LinkisJobLogPresenter();
            inclogPresenter.setClientDriver(driver);
            inclogPresenter.setTransformer(driverTransformer);

            /*
            inform incLogPresenter to listen to incLogEvent
             */
            IncLogEventListener incLogEventListener = new IncLogEventListener();
            incLogEventListener.setPresenter(inclogPresenter);
            incLogEventListener.setConverter(new LinkisLogModelConverter());
            ((SyncSubmission) execution).registerIncLogEventListener(incLogEventListener);

            /*
            when incLogPresenter finished it will trigger IncLogFinObserver so that
            SyncSubmission ends
             */
            TriggerEvent logFinEvent = new TriggerEvent();
            inclogPresenter.setLogFinEvent(logFinEvent);
            ((SyncSubmission) execution).getIncLogFinObserverRegistered(logFinEvent);
        }

    /*
    ResultHandler
     */
        if (suite.getResultHandlers() != null) {
            resultHandlers = suite.getResultHandlers();
            for (ResultHandler handler : resultHandlers) {
                if (handler instanceof PresentResultHandler) {
                    Presenter presenter = ((PresentResultHandler) handler).getPresenter();
                    if (presenter instanceof QueryBasedPresenter) {
                        // TODO: use executor rather than driver in presenter
                        // TODO: let suiteFactory do this, but don't want to new an Executor
                        ((QueryBasedPresenter) presenter).setClientDriver(driver);
                        ((QueryBasedPresenter) presenter).setTransformer(driverTransformer);
                    }
                }
            }
        }

    /*
    execute
     */
        final Executor executorKill = executor;
        final Job jobKill = job;
        Thread hook = new Thread(() -> execution.terminate(executorKill, jobKill)); //add ShutdownHook so that job can be killed if ctrl + c
        if (executorKill != null && jobKill != null) {
            Runtime.getRuntime().addShutdownHook(hook);
        }
        ExecutionResult result = execution.execute(executor, job);
        Runtime.getRuntime().removeShutdownHook(hook); //execution complete, no need ShutdownHook anymore
        return new FinishedData(result, resultHandlers);
    }


    public static void main(String[] args) {

        ExceptionHandler handler = new DefaultExceptionHandler();
        ProcessedData processedData = null;
        FinishedData finishedData = null;
        ExecutionResult executionResult = new ExecutionResultImpl();
        PreparedData preparedData = null;

        try {
            preparedData = prepare();
        } catch (Exception e) {
            handler.handle(e);
            executionResult.setExecutionStatus(ExecutionStatus.FAILED);
        }

        try {
            processedData = processInput(args, preparedData);
        } catch (CommandException ce) {
            new CommandExceptionHandler().handle(ce);
            executionResult.setExecutionStatus(ExecutionStatus.FAILED);
        } catch (Exception e) {
            handler.handle(e);
            executionResult.setExecutionStatus(ExecutionStatus.FAILED);
        }

        try {
            finishedData = exec(processedData);
        } catch (Exception e) {
            handler.handle(e);
            executionResult.setExecutionStatus(ExecutionStatus.FAILED);
        }

        if (finishedData != null) {
            executionResult = finishedData.getExecutionResult();
            if (executionResult == null) {
                executionResult = new ExecutionResultImpl();
            }
            if (finishedData.getResultHandlers() != null) {
                for (ResultHandler processor : finishedData.getResultHandlers()) {
                    processor.process(executionResult);
                }
            }
        } else {
            executionResult.setExecutionStatus(ExecutionStatus.FAILED);
            new DefaultResultHandler().process(executionResult);
        }

    }


}
