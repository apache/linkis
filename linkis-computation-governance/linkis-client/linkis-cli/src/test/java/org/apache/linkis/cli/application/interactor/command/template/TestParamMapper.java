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
import org.apache.linkis.cli.application.interactor.command.parser.transformer.ParamKeyMapper;

public class TestParamMapper extends ParamKeyMapper {
  @Override
  public void initMapperRules() {
    super.updateMapping(TestConstants.PARAM_COMMON_CMD, CliKeys.JOB_EXEC_CODE);
    super.updateMapping(TestConstants.PARAM_PROXY, CliKeys.LINKIS_COMMON_GATEWAY_URL);
    super.updateMapping(TestConstants.PARAM_USER, CliKeys.LINKIS_COMMON_TOKEN_KEY);
    super.updateMapping(TestConstants.PARAM_USR_CONF, CliKeys.LINKIS_CLIENT_USER_CONFIG);
    super.updateMapping(TestConstants.PARAM_PASSWORD, CliKeys.LINKIS_COMMON_TOKEN_VALUE);
    super.updateMapping(TestConstants.PARAM_PROXY_USER, CliKeys.JOB_COMMON_PROXY_USER);

    updateMapping(
        TestConstants.PARAM_SPARK_EXECUTOR_CORES, TestConstants.LINKIS_SPARK_EXECUTOR_CORES);
    updateMapping(
        TestConstants.PARAM_SPARK_EXECUTOR_MEMORY, TestConstants.LINKIS_SPARK_EXECUTOR_MEMORY);
    updateMapping(
        TestConstants.PARAM_SPARK_NUM_EXECUTORS, TestConstants.LINKIS_SPARK_NUM_EXECUTORS);
    updateMapping("spark.executor.instances", TestConstants.LINKIS_SPARK_NUM_EXECUTORS);
    //    updateMapping(SparkCommandConstants.PARAM_SPARK_NAME,
    // SparkCommandConstants.LINKIS_SPARK_NAME);
    updateMapping(
        TestConstants.PARAM_SPARK_SHUFFLE_PARTITIONS,
        TestConstants.LINKIS_SPARK_SHUFFLE_PARTITIONS);
    updateMapping(TestConstants.PARAM_SPARK_RUNTYPE, CliKeys.JOB_LABEL_CODE_TYPE);
    updateMapping(TestConstants.PARAM_YARN_QUEUE, TestConstants.YARN_QUEUE);
  }

  //    super.updateMapping("key1", "spark.cmd"); //should throw exception
  //    super.updateMapping("TestConstants.PARAM_SPARK_CMD", "spark.cmd");
  //    super.updateMapping("TestConstants.PARAM_SPARK_CMD", "spark.cmd");

}
