package com.webank.wedatasphere.linkis.engine.flink.client.cluster;

import org.apache.flink.client.deployment.executors.AbstractJobClusterExecutor;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.yarn.api.records.ApplicationId;

/**
 * @program: linkis
 * @description: 主要解决分集群初始化yarn问题
 * @author: hui zhu
 * @create: 2020-09-21 12:04
 */
public class MultipleYarnJobClusterExecutor extends AbstractJobClusterExecutor<ApplicationId, YarnClusterClientFactory> {
    public static final String NAME;

    public MultipleYarnJobClusterExecutor() {
        super(new MultipleYarnClusterClientFactory());
    }

    static {
        NAME = YarnDeploymentTarget.PER_JOB.getName();
    }

}
