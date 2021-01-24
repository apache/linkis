package com.webank.wedatasphere.linkis.engine.flink.client.cluster;

import com.webank.wedatasphere.linkis.engine.flink.client.utils.YarnConfLoader;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptionsInternal;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptionsInternal;
import org.apache.flink.yarn.configuration.YarnLogConfigUtil;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.flink.configuration.ConfigOptions.key;
import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * @program: linkis
 * @description: 主要解决多yarn环境的加载问题
 * @author: hui zhu
 * @create: 2020-09-21 12:04
 */
public class MultipleYarnClusterClientFactory<ClusterID> extends YarnClusterClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleYarnClusterClientFactory.class);

    public static final ConfigOption<String> YARN_CONFIG_DIR =
            key("$internal.yarn.config-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("**DO NOT USE** The location of the log config file, e.g. the path to your log4j.properties for log4j.");


    @Override
    public YarnClusterDescriptor createClusterDescriptor(Configuration configuration) {
        checkNotNull(configuration);
        final String configurationDirectory =
                configuration.get(DeploymentOptionsInternal.CONF_DIR);
        YarnLogConfigUtil.setLogConfigFileInConfig(configuration, configurationDirectory);
        return getClusterDescriptorByYarn(configuration);
    }

    private YarnClusterDescriptor getClusterDescriptorByYarn(Configuration configuration) {
        String yarnConfDir = configuration.getString(YARN_CONFIG_DIR);
        final YarnConfiguration yarnConfiguration = YarnConfLoader.getYarnConf(yarnConfDir);;
        final YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(yarnConfiguration);
        yarnClient.start();
        return new YarnClusterDescriptor(
                configuration,
                yarnConfiguration,
                yarnClient,
                YarnClientYarnClusterInformationRetriever.create(yarnClient),
                false);
    }

}
