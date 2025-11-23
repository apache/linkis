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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisClientApplicationTest {
  private static final Logger logger = LoggerFactory.getLogger(LinkisClientApplicationTest.class);

  String[] cmdStr;
  String[] cmdStr2;

  @BeforeEach
  public void before() {
    System.setProperty("conf.root", "src/test/resources/conf/");
    System.setProperty("user.name", "hadoop");
    cmdStr2 =
        new String[] {
          "--gatewayUrl",
          "http://127.0.0.1:9001",
          "--status",
          //                    "--log",
          //          "--kill",
          //          "--result",
          "5773107",
          "-submitUser",
          "hadoop",
          "-proxyUser",
          "hadoop",
          "-varMap",
          "name=\"tables\"",
          "-varMap",
          "name2=\"databases\""
        };
    cmdStr =
        new String[] {
          "--gatewayUrl",
          "http://127.0.0.1:9001",
          //                "--help",
          //                "--kill", "8249",
          //                "--status", "379",

          //                "--userConf", "src/test/resources/linkis-cli.properties",

          "-creator",
          "LINKISCLI",
          //                "-code", "show \${test};",
          //                "-codePath", "src/test/resources/test",
          //                "--kill", "6795",
          "-submitUser",
          "hadoop",
          "-proxyUser",
          "hadoop",
          //                "-sourceMap", "scriptPath=1234",
          //                "-outPath", "./data/bdp-job/test/",
          //                "-labelMap", "codeType=sql",
          //                "-confMap", "wds.linkis.yarnqueue=q02",
          //                "-confMap", "wds.linkis.yarnqueue=q02",
          //                "-confMap", "spark.num.executor=3",
          //                "-varMap", "wds.linkis.yarnqueue=q02",
          //                "-varMap", "name=\"databases\"",

          /* Test different task type */

          //                "-engineType", "spark-2.4.3",
          //                "-codeType", "sql",
          //                "-code", "show tables;show tables;show tables",

          //
          //        "-engineType", "hive-1.2.1",
          //        "-codeType", "sql",
          //        "-code", "show tables;",

          "-engineType",
          "shell-1",
          "-codeType",
          "shell",
          "-code",
          //          "exit -1",
          "whoami",

          //        "-engineType", "spark-2.4.3",
          //        "-codeType", "py",
          //        "-code", "print ('hello')",

          //        "-engineType", "spark-2.4.3",
          //        "-codeType", "scala",
          //        "-codePath", "src/test/resources/testScala.scala",

          /* Failed */
          //        "-engineType", "jdbc-1",
          //        "-codeType", "jdbc",
          //        "-code", "show tables",

          //        "-engineType", "python-python2",
          //        "-codeType", "python",
          ////        "-code", "print(\'hello\')\nprint(\'hello\')\nprint(\'hello\') ",

        };
  }

  @AfterEach
  public void after() {}

  /** Method: main(String[] args) */
  @Test
  public void testMain() {
    // TODO: Test goes here...
  }

  /** Method: prepare() */
  @Test
  public void testPrepare() {
    // TODO: Test goes here...
    /*
    try {
       Method method = LinkisClientApplication.getClass().getMethod("prepare");
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: processInput(String[] args, PreparedData preparedData) */
  @Test
  public void testProcessInput() {
    // TODO: Test goes here...
    /*
    try {
       Method method = LinkisClientApplication.getClass().getMethod("processInput", String[].class, PreparedData.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>); c
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }

  /** Method: exec(ProcessedData data) */
  @Test
  public void testExec() {
    //    LinkisClientApplication.main(cmdStr);
    //    LinkisClientApplication.main(cmdStr);
    //    LinkisClientApplication.main(cmdStr2);
    /*
    try {
       Method method = LinkisClientApplication.getClass().getMethod("exec", ProcessedData.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }
}
