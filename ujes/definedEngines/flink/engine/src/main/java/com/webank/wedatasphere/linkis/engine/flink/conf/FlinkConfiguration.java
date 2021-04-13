/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;

/**
 * Created by liangqilang on 01 20, 2021
 */
public class FlinkConfiguration {
    public static final CommonVars FLINK_ENGINE_SPRING_APPLICATION_NAME  = CommonVars$.MODULE$.apply("wds.linkis.engine.application.name", "flinkEngine");
    public static final CommonVars STREAMMANAGER_SPRING_APPLICATION_NAME = CommonVars$.MODULE$.apply("wds.linkis.streammanager.application.name", "cloud-streammanager");

    //system default conf
    public static CommonVars<String> HADOOP_CONF_DIR        = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.hadoop.conf.dir", "/etc/hadoop/conf");
    public static CommonVars<String> FLINK_CONF_DIR         = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.conf.dir", "/opt/flink-1.11.1/conf");
    public static CommonVars<String> FLINK_HOME             = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.home", "/opt/flink");
    public static CommonVars<String> FLINK_DIST_JAR_PATH    = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.dist.jar.path", "hdfs:///stream/flink/flink-1.11.1/lib/flink-dist_2.11-1.11.1.jar");
    public static CommonVars<String> FLINK_LIB_REMOTE_PATH  = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.lib.path", "hdfs:///stream/flink/flink-1.11.1/lib");
    public static CommonVars<String> FLINK_OPT_LOCAL_PATH   = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.local.opt.path", "/data/flink/flink-1.11.1/opt");
    public static CommonVars<String> FLINK_LIB_LOCAL_PATH   = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.local.lib.path", "/data/flink/flink-1.11.1/lib");
    public static CommonVars<String> FLINK_SHIP_DIRECTORIES = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.yarn.ship-directories", "/data/flink/flink-1.11.1/lib");

    public static CommonVars<String> FLINK_SYSTEM_CONF_CONTEXT = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.system.configuration", "{\"yarn.application.queue\":\"root.default\"}");

    //application default conf
    public static CommonVars<String> FLINK_JOB_TYPE                              = CommonVars$.MODULE$.apply("flink.app.type", "datastream");
    public static CommonVars<String> FLINK_ENV_TYPE                              = CommonVars$.MODULE$.apply("flink.env.type", "dev");
    public static CommonVars<String> FLINK_SQL_EXECUTIONTYPE                     = CommonVars$.MODULE$.apply("flink.sql.executionType", "batch");
    public static CommonVars<String> FLINK_APP_DEFAULT_QUEUE                     = CommonVars$.MODULE$.apply("flink.app.queue", "root.default");
    public static CommonVars<String> FLINK_APP_DEFAULT_PARALLELISM               = CommonVars$.MODULE$.apply("flink.app.parallelism", "1");
    public static CommonVars<String> FLINK_APP_DEFAULT_JM_MEMORY                 = CommonVars$.MODULE$.apply("flink.app.jobmanagerMemory", "1G");
    public static CommonVars<String> FLINK_APP_DEFAULT_TM_MEMORY                 = CommonVars$.MODULE$.apply("flink.app.taskmanagerMemory", "1G");
    public static CommonVars<String> FLINK_APP_DEFAULT_TASK_SLOT_NUMBER          = CommonVars$.MODULE$.apply("flink.app.taskmanager.slot.number", "1");
    public static CommonVars<String> FLINK_APP_DEFAULT_ALLOW_NON_RESTORED_STATUS = CommonVars$.MODULE$.apply("flink.app.allowNonRestoredStatus", "false");
    public static CommonVars<String> FLINK_APP_CHECKPOINT_INTERVAL               = CommonVars$.MODULE$.apply("flink.app.checkpoint.interval", "0");
    public static CommonVars<String> FLINK_APP_MIN_STATE_RETENTION_MINUTES       = CommonVars$.MODULE$.apply("flink.app.minIdleStateRetentionMinutes", "0");
    public static CommonVars<String> FLINK_APP_MAX_STATE_RETENTION_MINUTES       = CommonVars$.MODULE$.apply("flink.app.maxIdleStateRetentionMinutes", "0");
    public static CommonVars<String> FLINK_ENGINE_STREAM_HADOOP_CONF             = CommonVars$.MODULE$.apply("engine.flink.stream.hadoop.conf", "/data/hadoop/stream-hadoop/etc/hadoop/conf");
}
