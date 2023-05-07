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

package org.apache.linkis.cli.application.constants;

public class TestConstants {

  // for command name
  public static final String SPARK = "spark";
  public static final String HIVE = "hive";

  // for command description
  public static final String PRIMARY_COMMAND = "test.primary.command";
  public static final String SPARK_DESC = "Execute sql with spark";
  public static final String HIVE_DESC = "Execute hive sql";
  public static final String JOB_DESC = ""; // TODO

  // Common command params
  public static final String PARAM_COMMON_FILE = "param.common.file";
  public static final String PARAM_COMMON_CMD = "param.common.command";
  public static final String PARAM_COMMON_OUTPUT = "param.common.output";
  public static final String PARAM_COMMON_ARGS = "param.common.args";
  public static final String PARAM_COMMON_SPLIT = "param.common.split";
  public static final String PARAM_COMMON_OTHER_KV =
      "param.common.other.kv"; // for customize some parameters for some commands. Has to be
  // in the for of kv-pairs separated by ','
  public static final String PARAM_YARN_QUEUE = "param.yarn.queue";

  // for job command
  public static final String PARAM_JOB_TYPE = "param.job.type";
  public static final String PARAM_JOB_ID = "param.job.id";
  public static final String PARAM_FORCE_KILL = "param.force.kill";
  public static final String LINKIS_JOBID_PREFIX = "lks_";

  public static final String PARAM_MAPPER_KV_STRING =
      "param.mapper.kv.string"; // Rules for ParamKeyMapper. format:
  // key1=value1,key2=value2...
  public static final String YARN_QUEUE = "wds.linkis.yarnqueue";
  public static final String YARN_QUEUE_DEFAULT = "default";
  public static final String YARN_QUEUE_CORE_MAX = "wds.linkis.yarnqueue.cores.max";
  public static final int YARN_QUEUE_CORE_MAX_DEFAULT = 150;
  public static final String YARN_QUEUE_MEM_MAX = "wds.linkis.yarnqueue.memory.max";
  public static final String YARN_QUEUE_MEM_MAX_DEFAULT = "300G";
  public static final String PREHEATING_TIME = "wds.linkis.preheating.time";
  public static final String PREHEATING_TIME_DEFAULT = "9:00";
  public static final String TMPFILE_CLEAN_TIME = "wds.linkis.tmpfile.clean.time";
  public static final String TMPFILE_CLEAN_TIME_DEFAULT = "10:00";
  public static final String LINKIS_INSTANCE = "wds.linkis.instance";
  public static final int LINKIS_INSTANCE_DEFAULT = 10;
  public static final String LINKIS_CLIENT_MEMORY_MAX = "wds.linkis.client.memory.max";
  public static final String LINKIS_CLIENT_MEMORY_MAX_DEFAULT = "20G";

  // Common
  public static final String LINKIS_NULL_VALUE = "";

  public static final String SPARK_CMD = "spark";

  public static final String PARAM_SPARK_NAME = "param.spark.name";
  public static final String PARAM_SPARK_HIVECONF = "param.spark.hiveconf";
  public static final String PARAM_SPARK_NUM_EXECUTORS = "param.spark.num.executors";
  public static final String PARAM_SPARK_EXECUTOR_CORES = "param.spark.executor.cores";
  public static final String PARAM_SPARK_EXECUTOR_MEMORY = "param.spark.executor.memory";
  public static final String PARAM_SPARK_SHUFFLE_PARTITIONS = "param.spark.shuffle.partitions";
  public static final String PARAM_SPARK_RUNTYPE = "param.spark.runtype";

  public static final String LINKIS_SPARK_NUM_EXECUTORS =
      "wds.linkis.client.param.conf.spark.executor.instances";
  public static final String LINKIS_SPARK_EXECUTOR_CORES =
      "wds.linkis.client.param.conf.spark.executor.cores";
  public static final String LINKIS_SPARK_EXECUTOR_MEMORY =
      "wds.linkis.client.param.conf.spark.executor.memory";
  public static final String LINKIS_SPARK_SHUFFLE_PARTITIONS =
      "wds.linkis.client.param.conf.spark.sql.shuffle.partitions";

  public static final String PARAM_DB = "test.param.primary.database";
  public static final String PARAM_PROXY = "test.param.primary.proxy";
  public static final String PARAM_USER = "test.param.primary.user";
  public static final String PARAM_USR_CONF = "test.param.primary.user.conf";
  public static final String PARAM_PASSWORD = "test.param.primary.password";
  public static final String PARAM_SYNC_KEY = "test.param.primary.synckey";
  public static final String PARAM_PROXY_USER = "test.param.primary.proxyUser";
  public static final String PARAM_HELP = "test.param.help";
  public static final String PARAM_REAL_NAME = "test.param.primary.realName";
  public static final String PARAM_PIN_TOKEN = "test.param.primary.pinToken";

  public static final String PARAM_PROPERTIES = "params.properties";
}
