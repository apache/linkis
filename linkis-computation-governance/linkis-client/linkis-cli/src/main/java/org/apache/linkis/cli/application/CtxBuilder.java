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

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.Params;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.var.VarAccess;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.PropsException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.CliCmdType;
import org.apache.linkis.cli.application.interactor.command.CmdTemplateFactory;
import org.apache.linkis.cli.application.interactor.command.fitter.SingleTplFitter;
import org.apache.linkis.cli.application.interactor.command.parser.Parser;
import org.apache.linkis.cli.application.interactor.command.parser.SingleCmdParser;
import org.apache.linkis.cli.application.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.application.interactor.context.CliCtxImpl;
import org.apache.linkis.cli.application.interactor.properties.ClientProperties;
import org.apache.linkis.cli.application.interactor.properties.PropertiesLoader;
import org.apache.linkis.cli.application.interactor.properties.PropsFilesScanner;
import org.apache.linkis.cli.application.interactor.properties.reader.PropertiesReader;
import org.apache.linkis.cli.application.interactor.properties.reader.PropsFileReader;
import org.apache.linkis.cli.application.interactor.validate.ParsedTplValidator;
import org.apache.linkis.cli.application.interactor.var.VarAccessImpl;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CtxBuilder {
  private static Logger logger = LoggerFactory.getLogger(CtxBuilder.class);

  /** generate Templates load env variables TODO: load version info */
  public static CliCtx buildCtx(String[] args) throws LinkisClientRuntimeException {
    /*
     user input
    */
    CmdTemplate template = CmdTemplateFactory.getTemplateCopy(CliCmdType.UNIVERSAL);
    Parser parser =
        new SingleCmdParser()
            .setMapper(null)
            .setTemplate(template)
            .setFitter(new SingleTplFitter());

    ParseResult result = parser.parse(args);

    ParsedTplValidator parsedTplValidator = new ParsedTplValidator();

    parsedTplValidator.doValidation(result.getParsedTemplate());

    Params params = result.getParams();
    logger.debug("==========params============\n" + CliUtils.GSON.toJson(params));

    /*
     VarAccess for sys_prop, sys_env
    */

    Map<String, ClientProperties> propertiesMap = new HashMap<>();

    LoggerManager.getInformationLogger()
        .info(
            "LogFile path: "
                + System.getProperty(CliKeys.LOG_PATH_KEY)
                + "/"
                + System.getProperty(CliKeys.LOG_FILE_KEY));
    /*
     default config, -Dconf.root & -Dconf.file specifies config path
    */
    // scan config files given root path
    String configPath = System.getProperty(CliKeys.CLIENT_CONFIG_ROOT_KEY);
    String defaultConfFileName =
        System.getProperty(CliKeys.DEFAULT_CONFIG_FILE_NAME_KEY, CliConstants.DEFAULT_CONFIG_NAME);

    if (StringUtils.isBlank(configPath)) {
      throw new PropsException(
          "PRP0007",
          ErrorLevel.ERROR,
          CommonErrMsg.PropsLoaderErr,
          "configuration root path specified by env variable: "
              + CliKeys.CLIENT_CONFIG_ROOT_KEY
              + " is empty.");
    }

    List<PropertiesReader> readersList =
        new PropsFilesScanner().getPropsReaders(configPath); // +1 user config
    /*
     user defined config
    */
    String userConfPath = null;
    if (params.containsParam(CliKeys.LINKIS_CLIENT_USER_CONFIG)) {
      userConfPath =
          (String) params.getParamItemMap().get(CliKeys.LINKIS_CLIENT_USER_CONFIG).getValue();
    }
    if (StringUtils.isNotBlank(userConfPath)) {
      PropertiesReader reader =
          new PropsFileReader()
              .setPropsId(CliKeys.LINKIS_CLIENT_USER_CONFIG)
              .setPropsPath(userConfPath);
      readersList.add(reader);
    } else {
      LoggerManager.getInformationLogger()
          .info("User does not provide usr-configuration file. Will use default config");
    }
    /*
    load properties
    */
    PropertiesLoader loader =
        new PropertiesLoader()
            .addPropertiesReaders(readersList.toArray(new PropertiesReader[readersList.size()]));
    ClientProperties[] loaderResult = loader.loadProperties();
    for (ClientProperties properties : loaderResult) {
      if (StringUtils.equals(properties.getPropsId(), CliKeys.LINKIS_CLIENT_USER_CONFIG)) {
        for (Map.Entry prop : properties.entrySet()) {
          if (StringUtils.startsWith(
              (String) prop.getKey(), CliKeys.LINKIS_CLIENT_NONCUSTOMIZABLE)) {
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
    VarAccess varAccess =
        new VarAccessImpl()
            .setCmdParams(params)
            .setUserConf(propertiesMap.get(CliKeys.LINKIS_CLIENT_USER_CONFIG))
            .setDefaultConf(propertiesMap.get(defaultConfFileName))
            .init();
    logger.info("==========std_var============\n" + CliUtils.GSON.toJson(varAccess));

    Properties props = new Properties();
    try (InputStream inputStream =
        CtxBuilder.class.getClassLoader().getResourceAsStream("version.properties")) {
      try (InputStreamReader reader = new InputStreamReader(inputStream)) {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
          props.load(bufferedReader);
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to load version info", e);
    }

    String verion = props.getProperty(CliKeys.VERSION);

    Map<String, Object> extraMap = new HashMap<>();
    extraMap.put(CliKeys.VERSION, verion);

    return new CliCtxImpl(params.getCmdType(), template, varAccess, extraMap);
  }
}
