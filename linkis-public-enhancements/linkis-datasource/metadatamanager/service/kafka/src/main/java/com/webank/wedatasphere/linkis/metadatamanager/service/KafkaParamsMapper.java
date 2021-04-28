package com.webank.wedatasphere.linkis.metadatamanager.service;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

public class KafkaParamsMapper {
    public static final CommonVars<String> PARAM_KAFKA_PRINCIPLE =
            CommonVars.apply("wds.linkis.server.mdm.service.kafka.principle", "principle");

    public static final CommonVars<String> PARAM_KAFKA_KEYTAB =
            CommonVars.apply("wds.linkis.server.mdm.service.kafka.keytab", "keytab");

    public static final CommonVars<String> PARAM_KAFKA_BROKERS =
            CommonVars.apply("wds.linkis.server.mdm.service.kafka.brokers", "brokers");
}
