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

package org.apache.linkis.cli.core.interactor.command.fitter;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.core.interactor.command.template.TestSparkCmdTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SingleTplFitterTest {
  Fitter fitter;
  CmdTemplate template;
  String[] cmdStr, cmdStr2;

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
    cmdStr2 =
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
          "-P",
          "key1=value1, key2=value2,  key5=\"key3=value3,key4=value4\" "
        };
    template = new TestSparkCmdTemplate();
    fitter = new SingleTplFitter();
  }

  @AfterEach
  public void after() throws Exception {}

  /** Method: fit(TemplateFitterInput[] inputs) */
  @Test
  public void testParseAndFit() throws Exception {
    FitterResult[] results = new FitterResult[2];
    results[0] = fitter.fit(cmdStr, template);
    results[1] = fitter.fit(cmdStr2, new TestSparkCmdTemplate());

    assertTrue(results[0].getParsedTemplate() instanceof TestSparkCmdTemplate);
    assertEquals(
        results[0].getParsedTemplate().getOptionsMap().get("--cmd").getValue(), "show tables");
    assertNotEquals(results[0].getParsedTemplate(), template.getCopy());
    assertNotEquals(results[0].getParsedTemplate().getOptions(), template.getCopy().getOptions());
    assertNotEquals(
        results[0].getParsedTemplate().getOptions().get(1), template.getCopy().getOptions().get(1));
    assertEquals(
        results[0].getParsedTemplate().getOptions().get(1).getValue(),
        template.getCopy().getOptions().get(1).getValue());
  }
}
