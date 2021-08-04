package com.webank.wedatasphere.linkis.engineconnplugin.flink.reporter;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.metrics.MetricConfig;

/**
 * created by cooperyang on 2021/6/21
 * Description:
 */
public class IMSOptions {
    public static final ConfigOption<String> METRIC_URL = ConfigOptions
            .key("metricUrl")
            .stringType().noDefaultValue()
            .withDescription("MetricReport Url");

    public static final CommonVars<String> METRIC_URL_VALUE = CommonVars.apply("wds.linkis.flink.metric.url" , "http://10.107.118.38:10812/ims_data_access/batch_report_metric_by_json.do");

    public static final ConfigOption<String> CLUSTER_NAME = ConfigOptions
            .key("clusterName")
            .stringType().noDefaultValue()
            .withDescription("MetricReport Cluster Name");

    public static final CommonVars<String> CLUSTER_NAME_VALUE =
            CommonVars.apply("wds.linkis.flink.cluter.name", "TEST_CLUSTER");


    public static final ConfigOption<String> JOB_NAME = ConfigOptions
            .key("jobName")
            .stringType().noDefaultValue()
            .withDescription("Application Job Name");


    public static final ConfigOption<String> AUTH_KEY = ConfigOptions
            .key("key")
            .stringType().defaultValue("sit_test")
            .withDescription("IMS Key");

    public static final CommonVars<String> AUTH_KEY_VALUE = CommonVars.apply("wds.linkis.flink.auth.key", "sit_test");


    public static final ConfigOption<String> SUB_SYSTEM_ID = ConfigOptions
            .key("subsys")
            .stringType().noDefaultValue()
            .withDescription("Metric Report Subsystem Id");

    public static final CommonVars<String> SUB_SYSTEM_ID_VALUE =
            CommonVars.apply("wds.linkis.flink.sub.system.id", "5632");


    public static final ConfigOption<String> METRICS = ConfigOptions
            .key("metrics")
            .stringType().defaultValue("")
            .withDescription("Metric to Report, Separated by ','");

    public static final CommonVars<String> METRICS_VALUE =
            CommonVars.apply("wds.linkis.flink.metrics", "OP.SimpleKafkaGauge,OP.SimpleKafkaCounter");


    public static final ConfigOption<String> UD_METRICS = ConfigOptions
            .key("udMetrics")
            .stringType().noDefaultValue()
            .withDescription("需要上报的用户自定义指标，用 “,” 分割");

    protected static String getString(MetricConfig config, ConfigOption<String> key) {
        String value = config.getString(key.key(), key.defaultValue());
        if (value == null) {
            return "";
        }
        return value;
    }

}
