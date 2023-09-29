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

package org.apache.linkis.cli.core.interactor.command.parser;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.command.ParamItem;
import org.apache.linkis.cli.core.interactor.command.TestCmdType;
import org.apache.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import org.apache.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.core.interactor.command.template.TestParamMapper;
import org.apache.linkis.cli.core.interactor.command.template.TestSparkCmdTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SingleCmdParserTest {
  String[] cmdStr;
  Map<String, CmdTemplate> templateMap;

  @BeforeEach
  public void before() throws Exception {
    cmdStr =
        new String[] {
          "-u",
          "hadoop",
          "-pwd",
          "1234",
          "-c",
          "/path/to/user/config",
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
    templateMap = new HashMap<>();
    templateMap.put(template.getCmdType().getName(), template);
  }

  @AfterEach
  public void after() throws Exception {}

  /** Method: parse(String[] input) */
  @Test
  public void testParse() {

    Parser parser =
        new SingleCmdParser()
            .setMapper(null)
            .setTemplate(templateMap.get(TestCmdType.SPARK.getName()))
            .setFitter(new SingleTplFitter())
            .setMapper(new TestParamMapper());

    ParseResult result = parser.parse(cmdStr);

    assertEquals(result.getParams().getCmdType(), TestCmdType.SPARK);

    Map<String, ParamItem> params = result.getParams().getParamItemMap();

    assertEquals(params.size(), 21);

    List<String> sortedKeys = params.keySet().stream().sorted().collect(Collectors.toList());
    assertEquals(
        sortedKeys,
        Arrays.asList(
            "converted.args",
            "converted.split",
            "param.common.command",
            "param.common.file",
            "param.common.other.kv",
            "param.help",
            "param.primary.database",
            "param.primary.password",
            "param.primary.proxy",
            "param.primary.proxyUser",
            "param.primary.synckey",
            "param.primary.user",
            "param.primary.user.conf",
            "param.spark.executor.cores",
            "param.spark.executor.memory",
            "param.spark.hiveconf",
            "param.spark.name",
            "param.spark.num.executors",
            "param.spark.runtype",
            "param.spark.shuffle.partitions",
            "param.yarn.queue"));
    assertEquals(result.getRemains().length, 0);
  }

  /** Method: parsePrimary(String[] input) */
  @Test
  public void testParsePrimary() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = SingleCmdParser.getClass().getMethod("parsePrimary", String[].class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: parseSingleSub(String[] remains) */
  @Test
  public void testParseSingleSub() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = SingleCmdParser.getClass().getMethod("parseSingleSub", String[].class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: standardParse(String identifier, String[] args, CmdTemplate templateOri) */
  @Test
  public void testStandardParse() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = SingleCmdParser.getClass().getMethod("standardParse", String.class, String[].class, CmdTemplate.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }
}
