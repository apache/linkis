package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation;

import org.apache.flink.api.common.JobID;

/**
 * Description:
 */
public interface JobInfo {
    JobID getJobId();

    String getApplicationId();

    String getWebInterfaceUrl();
}
