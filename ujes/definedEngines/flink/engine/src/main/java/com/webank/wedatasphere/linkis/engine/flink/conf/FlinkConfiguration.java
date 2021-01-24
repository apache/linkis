package com.webank.wedatasphere.linkis.engine.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 17:15
 */
public class FlinkConfiguration {
    public static final CommonVars FLINK_ENGINE_SPRING_APPLICATION_NAME  = CommonVars$.MODULE$.apply("wds.linkis.engine.application.name", "flinkEngine");
    public static final CommonVars STREAMMANAGER_SPRING_APPLICATION_NAME = CommonVars$.MODULE$.apply("wds.linkis.streammanager.application.name", "cloud-streammanager");

    //system default conf
    public static CommonVars<String> HADOOP_CONF_DIR           = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.hadoop.conf.dir", "/etc/hadoop/conf");
    public static CommonVars<String> FLINK_CONF_DIR            = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.conf.dir", "/opt/flink-1.11.1/conf");
    public static CommonVars<String> FLINK_HOME                = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.home", "/opt/flink");
    public static CommonVars<String> FLINK_DIST_JAR_PATH       = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.dist.jar.path", "hdfs:///stream/flink/flink-1.11.1/lib/flink-dist_2.11-1.11.1.jar");
    public static CommonVars<String> FLINK_LIB_REMOTE_PATH            = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.lib.path", "hdfs:///stream/flink/flink-1.11.1/lib");
    public static CommonVars<String> FLINK_OPT_LOCAL_PATH            = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.local.opt.path", "/opt/flink/opt");
    public static CommonVars<String> FLINK_LIB_LOCAL_PATH            = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.local.lib.path", "/opt/flink/lib");
    public static CommonVars<String> FLINK_SHIP_DIRECTORIES       = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.yarn.ship-directories", "/opt/flink/lib");

    public static CommonVars<String> FLINK_SYSTEM_CONF_CONTEXT = CommonVars$.MODULE$.apply("wds.linkis.engine.flink.system.configuration", "{\"yarn.application.queue\":\"root.default\"}");

    //application default conf
    public static CommonVars<String> FLINK_JOB_TYPE                              = CommonVars$.MODULE$.apply("flink.app.type", "datastream");
    public static CommonVars<String> FLINK_ENV_TYPE                              = CommonVars$.MODULE$.apply("flink.env.type", "dev");
    public static CommonVars<String> FLINK_SQL_EXECUTIONTYPE                            = CommonVars$.MODULE$.apply("flink.sql.executionType", "batch");
    public static CommonVars<String> FLINK_APP_DEFAULT_QUEUE                     = CommonVars$.MODULE$.apply("flink.app.queue", "root.default");
    public static CommonVars<String> FLINK_APP_DEFAULT_PARALLELISM               = CommonVars$.MODULE$.apply("flink.app.parallelism", "1");
    public static CommonVars<String> FLINK_APP_DEFAULT_JM_MEMORY                 = CommonVars$.MODULE$.apply("flink.app.jobmanagerMemory", "1G");
    public static CommonVars<String> FLINK_APP_DEFAULT_TM_MEMORY                 = CommonVars$.MODULE$.apply("flink.app.taskmanagerMemory", "1G");
    public static CommonVars<String> FLINK_APP_DEFAULT_TASK_SLOT_NUMBER          = CommonVars$.MODULE$.apply("flink.app.taskmanager.slot.number", "1");
    public static CommonVars<String> FLINK_APP_DEFAULT_ALLOW_NON_RESTORED_STATUS = CommonVars$.MODULE$.apply("flink.app.allowNonRestoredStatus", "false");


}
