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

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.constants.TestConstants;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.interactor.command.template.option.StdOption;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @description: CommandTemplate for Spark Jobs */
public class TestSparkCmdTemplate extends AbstractCmdTemplate {
  protected StdOption<String> databaseOp =
      option(
          null,
          TestConstants.PARAM_DB,
          new String[] {"-d", "--database"},
          "specify database",
          true,
          "");
  protected StdOption<String> proxyOp =
      option(
          null,
          TestConstants.PARAM_PROXY,
          new String[] {"-x", "--proxy"},
          "specify proxy url",
          true,
          "");
  protected StdOption<String> userOp =
      option(
          null,
          CliKeys.JOB_COMMON_SUBMIT_USER,
          new String[] {"-u", "--user"},
          "specify user",
          true,
          "");
  protected StdOption<String> confOp =
      option(
          null,
          TestConstants.PARAM_USR_CONF,
          new String[] {"-c", "--conf"},
          "specify configuration from property file",
          true,
          "");
  private Logger logger = LoggerFactory.getLogger(TestSparkCmdTemplate.class);
  private StdOption<String> passwordOp =
      option(
          null,
          CliKeys.JOB_COMMON_SUBMIT_PASSWORD,
          new String[] {"-pwd", "--passwd"},
          "specify user password",
          true,
          "");
  private StdOption<String> syncOp =
      option(
          null,
          TestConstants.PARAM_SYNC_KEY,
          new String[] {"-sk", "--synckey"},
          "specify sync key",
          true,
          "");
  private StdOption<String> proxyUserOp =
      option(
          null,
          TestConstants.PARAM_PROXY_USER,
          new String[] {"-pu", "--proxy-user"},
          "specify proxy user",
          true,
          "");

  private StdOption<String> helpOp =
      option(null, TestConstants.PARAM_HELP, new String[] {"-h", "--help"}, "help info", true, "");

  private StdOption<Map<String, String>> confMap =
      option(
          null,
          CliKeys.JOB_PARAM_CONF,
          new String[] {"-confMap"},
          "confMap",
          true,
          new HashMap<>());

  private StdOption<String> filePara =
      option(
          null,
          TestConstants.PARAM_COMMON_FILE,
          new String[] {"--file", "-f"},
          "Spark SQL File to Execute!",
          true,
          "");

  private StdOption<String> commandPara =
      option(
          null,
          TestConstants.PARAM_COMMON_CMD,
          new String[] {"--cmd"},
          "Spark SQL Command to Execute!",
          true,
          "");

  private StdOption<String> argsPara =
      option(
          null,
          TestConstants.PARAM_COMMON_ARGS,
          new String[] {"--args", "-a"},
          "Set command args, k-v pairs delimited by comma, e.g. key1=value1,key2=value2,...",
          true,
          "");

  private StdOption<String> splitPara =
      option(
          null,
          TestConstants.PARAM_COMMON_SPLIT,
          new String[] {"--split", "-s"},
          "specify the split character string",
          true,
          ",");

  private StdOption<String> queuePara =
      option(
          null,
          TestConstants.PARAM_YARN_QUEUE,
          new String[] {"--queue", "-q"},
          "specify the queue",
          true,
          "default");

  private StdOption<String> namePara =
      option(
          null,
          TestConstants.PARAM_SPARK_NAME,
          new String[] {"--name", "-n"},
          "specify the application name. WARNING:this option is deprecated. Linkis does not support this variable",
          true,
          "");

  private StdOption<Map<String, String>> hiveconfPara =
      option(
          null,
          TestConstants.PARAM_SPARK_HIVECONF,
          new String[] {"--hiveconf", "-hc"},
          "specify the hiveconf setting,e.g. hive.cli.print.header=false",
          true,
          new HashMap<>());

  private StdOption<Integer> nePara =
      option(
          null,
          TestConstants.PARAM_SPARK_NUM_EXECUTORS,
          new String[] {"--num-executors", "-ne"},
          "specify the spark application container",
          true,
          3);

  private StdOption<Integer> ecPara =
      option(
          null,
          TestConstants.PARAM_SPARK_EXECUTOR_CORES,
          new String[] {"--executor-cores", "-ec"},
          "specify the spark application container vcores(less than queue's max vcores)",
          true,
          2);

  private StdOption<String> emPara =
      option(
          null,
          TestConstants.PARAM_SPARK_EXECUTOR_MEMORY,
          new String[] {"--executor-memory", "-em"},
          "specify the spark application executor's memory, 1.5G-2G/vcore",
          true,
          "4G");

  private StdOption<Integer> spPara =
      option(
          null,
          TestConstants.PARAM_SPARK_SHUFFLE_PARTITIONS,
          new String[] {"--shuffle-partitions", "-sp"},
          "specify the spark.sql.shuffle.partitions",
          true,
          200);

  private StdOption<Map<String, String>> otherPara =
      option(
          null,
          TestConstants.PARAM_COMMON_OTHER_KV,
          new String[] {"--other"},
          "specify the other parameters",
          true,
          new HashMap<>());

  //  private CmdOption<String> runTypePara = option(TestConstants.PARAM_SPARK_RUNTYPE, new
  // String[]{"--runtype"},
  //      "specify the runtype parameters: sql pyspark scala", true, "sql");

  public TestSparkCmdTemplate() {
    super(TestCmdType.SPARK);
  }

  @Override
  public void checkParams() throws CommandException {}

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public TestSparkCmdTemplate getCopy() {
    return (TestSparkCmdTemplate) super.getCopy();
  }
}
