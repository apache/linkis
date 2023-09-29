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

package org.apache.linkis.cli.core.interactor.var;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.core.constants.TestConstants;
import org.apache.linkis.cli.core.interactor.command.TestCmdType;
import org.apache.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import org.apache.linkis.cli.core.interactor.command.parser.Parser;
import org.apache.linkis.cli.core.interactor.command.parser.SingleCmdParser;
import org.apache.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.core.interactor.command.template.TestSparkCmdTemplate;
import org.apache.linkis.cli.core.interactor.properties.PropertiesLoader;
import org.apache.linkis.cli.core.interactor.properties.PropsFilesScanner;
import org.apache.linkis.cli.core.interactor.properties.StdPropsLoader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropertiesReader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropsFileReader;
import org.apache.linkis.cli.core.interactor.validate.ParsedTplValidator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StdVarAccessTest {
  String[] cmdStr;
  VarAccess stdVarAccess;

  @BeforeEach
  public void before() throws Exception {
    cmdStr =
        new String[] {
          "-u",
          "hadoop",
          "-pwd",
          "1234",
          "-c",
          "src/test/resources/conf/user.properties",
          "--cmd",
          "show tables",
          "--split",
          "\',\'",
          "--queue",
          "q05",
          "--name",
          "testApp",
          //      "--hiveconf", "/path/...",
          "--num-executors",
          "4",
          "--executor-cores",
          "4",
          "--executor-memory",
          "4G",
          "--shuffle-partitions",
          "200",
          "--other",
          "--other-spark-config=none",
        };

    TestSparkCmdTemplate template = new TestSparkCmdTemplate();
    Map<String, CmdTemplate> templateMap = new HashMap<>();
    templateMap.put(template.getCmdType().getName(), template);

    Parser parser =
        new SingleCmdParser()
            .setMapper(null)
            .setTemplate(templateMap.get(TestCmdType.SPARK.getName()))
            .setFitter(new SingleTplFitter());

    ParseResult result = parser.parse(cmdStr);

    ParsedTplValidator parsedTplValidator = new ParsedTplValidator();
    parsedTplValidator.doValidation(result.getParsedTemplate());

    System.setProperty("conf.root", "src/test/resources/conf/");
    System.setProperty("conf.file", "linkis-cli.properties");
    String configPath = System.getProperty("conf.root");
    String defaultConfFileName = System.getProperty("conf.file");
    /*
     default config, -Dconf.root & -Dconf.file specifies config path
    */
    List<PropertiesReader> readersList =
        new PropsFilesScanner().getPropsReaders(configPath); // +1 user config
    /*
     user defined config
    */
    String userConfPath =
        (String) result.getParams().getParamItemMap().get(TestConstants.PARAM_USR_CONF).getValue();
    if (StringUtils.isNotBlank(userConfPath)) {
      PropertiesReader reader = new PropsFileReader();
      reader.setPropsId("user.conf");
      reader.setPropsPath(userConfPath);
      readersList.add(reader);
    } else {
    }
    // load all config files
    PropertiesLoader loader =
        new StdPropsLoader()
            .addPropertiesReaders(readersList.toArray(new PropertiesReader[readersList.size()]));
    ClientProperties[] loaderResult = loader.loadProperties();
    Map<String, ClientProperties> propertiesMap = new HashMap<>();
    for (ClientProperties properties : loaderResult) {
      propertiesMap.put(properties.getPropsId(), properties);
    }

    stdVarAccess =
        new StdVarAccess()
            .setCmdParams(result.getParams())
            .setUserConf(propertiesMap.get("user.conf"))
            .setDefaultConf(propertiesMap.get(defaultConfFileName))
            .init();
  }

  @AfterEach
  public void after() throws Exception {}

  /** Method: setPrimaryParam(Params primaryParam) */
  @Test
  public void testSetPrimaryParam() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getPrimaryParam(String identifier) */
  @Test
  public void testGetPrimaryParam() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: setCmdParams(Params subParam) */
  @Test
  public void testSetSubParam() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getSubParam(String identifier) */
  @Test
  public void testGetSubParam() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: setUserConf(ClientProperties userConf) */
  @Test
  public void testSetUserConf() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getUserConf(String identifier) */
  @Test
  public void testGetUserConf() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: setDefaultConf(ClientProperties defaultConf) */
  @Test
  public void testSetDefaultConf() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getDefaultConf(String identifier) */
  @Test
  public void testGetDefaultConf() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: checkInit() */
  @Test
  public void testCheckInit() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getVar(Class<T> clazz, String key) */
  @Test
  public void testGetVar() throws Exception {
    System.out.println(stdVarAccess.getVar(String.class, TestConstants.PARAM_COMMON_CMD));
    assertEquals(stdVarAccess.getVar(String.class, TestConstants.PARAM_COMMON_CMD), "show tables");
    System.out.println(stdVarAccess.getVar(String.class, "user.props"));
    assertEquals(stdVarAccess.getVar(String.class, "wds.linkis.client.not.exist"), null);
    System.out.println(
        stdVarAccess.getVar(
            Integer.class, TestConstants.PARAM_SPARK_EXECUTOR_CORES)); // see if priority works
    assertEquals(
        (long) stdVarAccess.getVar(Integer.class, TestConstants.PARAM_SPARK_EXECUTOR_CORES), 4);
    assertEquals((long) stdVarAccess.getVar(Integer.class, "conf.prop.integer"), 9);
    assertEquals(stdVarAccess.getVar(String.class, "conf.prop.string"), "str");
    assertEquals(
        stdVarAccess.getVar(String.class, "wds.linkis.client.param.conf.spark.executor.memory"),
        "11111G");

    System.out.println(stdVarAccess.getAllVarKeys().length);
    System.out.println(Arrays.toString(stdVarAccess.getAllVarKeys()));
    assertTrue(stdVarAccess.getAllVarKeys().length != 0);
  }

  /** Method: getVarOrDefault(Class<T> clazz, String key, T defaultValue) */
  @Test
  public void testGetVarOrDefault() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getAllVarKeys() */
  @Test
  public void testGetAllVarKeys() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getVarFromParam(Class<T> clazz, String key, Params param) */
  @Test
  public void testGetVarFromParam() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = StdVarAccess.getClass().getMethod("getVarFromParam", Class<T>.class, String.class, Params.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: getDefaultVarFromParam(Class<T> clazz, String key, Params param) */
  @Test
  public void testGetDefaultVarFromParam() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = StdVarAccess.getClass().getMethod("getDefaultVarFromParam", Class<T>.class, String.class, Params.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: getVarFromCfg(Class<T> clazz, String key, ClientProperties conf) */
  @Test
  public void testGetVarFromCfg() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = StdVarAccess.getClass().getMethod("getVarFromCfg", Class<T>.class, String.class, ClientProperties.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /**
   * Method: getVarFromCfgGivenConverter(String key, ClientProperties conf,
   * AbstractStringConverter<T> converter)
   */
  @Test
  public void testGetVarFromCfgGivenConverter() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = StdVarAccess.getClass().getMethod("getVarFromCfgGivenConverter", String.class, ClientProperties.class, AbstractStringConverter<T>.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }
}
