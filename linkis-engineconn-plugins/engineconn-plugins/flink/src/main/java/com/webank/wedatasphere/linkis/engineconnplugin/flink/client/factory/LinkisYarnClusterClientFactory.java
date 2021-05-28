package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.factory;

import static org.apache.flink.configuration.ConfigOptions.key;
import static org.apache.flink.util.Preconditions.checkNotNull;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.utils.YarnConfLoader;
import java.io.Closeable;
import java.io.IOException;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptionsInternal;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnLogConfigUtil;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: linkis
 */
public class LinkisYarnClusterClientFactory extends YarnClusterClientFactory implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(LinkisYarnClusterClientFactory.class);

    public static final ConfigOption<String> YARN_CONFIG_DIR =
            key("$internal.yarn.config-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("**DO NOT USE** The location of the log config file, e.g. the path to your log4j.properties for log4j.");

    private YarnConfiguration yarnConfiguration;
    private YarnClient yarnClient;

    private void initYarnClient(Configuration configuration) {
        String yarnConfDir = configuration.getString(YARN_CONFIG_DIR);
        yarnConfiguration = YarnConfLoader.getYarnConf(yarnConfDir);
        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(yarnConfiguration);
        yarnClient.start();
    }

    @Override
    public YarnClusterDescriptor createClusterDescriptor(Configuration configuration) {
        checkNotNull(configuration);
        final String configurationDirectory =
                configuration.get(DeploymentOptionsInternal.CONF_DIR);
        YarnLogConfigUtil.setLogConfigFileInConfig(configuration, configurationDirectory);
        if(yarnClient == null) {
            synchronized (this) {
                if(yarnClient == null) {
                    initYarnClient(configuration);
                }
            }
        }
        return new YarnClusterDescriptor(
            configuration,
            yarnConfiguration,
            yarnClient,
            YarnClientYarnClusterInformationRetriever.create(yarnClient),
            true);
    }

    @Override
    public void close() throws IOException {
        yarnClient.close();
    }
}
