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

package org.apache.linkis.engineconnplugin.flink.client.factory;

import org.apache.linkis.engineconnplugin.flink.client.utils.YarnConfLoader;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptionsInternal;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnLogConfigUtil;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.flink.configuration.ConfigOptions.key;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class LinkisYarnClusterClientFactory extends YarnClusterClientFactory implements Closeable {

  public static final ConfigOption<String> YARN_CONFIG_DIR =
      key("$internal.yarn.config-dir")
          .stringType()
          .noDefaultValue()
          .withDescription(
              "**DO NOT USE** The location of the log config file, e.g. the path to your log4j.properties for log4j.");

  private YarnConfiguration yarnConfiguration;
  private YarnClient yarnClient;

  private Configuration configuration;

  private static final Logger LOG = LoggerFactory.getLogger(LinkisYarnClusterClientFactory.class);

  private void initYarnClient(Configuration configuration) {
    checkNotNull(configuration);
    String configurationDirectory = configuration.get(DeploymentOptionsInternal.CONF_DIR);
    YarnLogConfigUtil.setLogConfigFileInConfig(configuration, configurationDirectory);
    String yarnConfDir = configuration.getString(YARN_CONFIG_DIR);
    this.configuration = configuration;
    yarnConfiguration = YarnConfLoader.getYarnConf(yarnConfDir);
    yarnClient = YarnClient.createYarnClient();
    yarnClient.init(yarnConfiguration);
    yarnClient.start();
  }

  public YarnConfiguration getYarnConfiguration(Configuration configuration) {
    if (yarnClient == null) {
      synchronized (this) {
        if (yarnClient == null) {
          initYarnClient(configuration);
        }
      }
    }
    return yarnConfiguration;
  }

  @Override
  public YarnClusterDescriptor createClusterDescriptor(Configuration configuration) {
    if (yarnClient == null) {
      synchronized (this) {
        if (yarnClient == null) {
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
    if (yarnClient != null) {
      ApplicationId applicationId = getClusterId(configuration);
      if (applicationId != null) {
        LOG.info("Begin to kill application {}", applicationId);
        try {
          yarnClient.killApplication(applicationId);
        } catch (YarnException e) {
          LOG.error("Failed to kill application {}.", applicationId, e);
        }
      }
      yarnClient.close();
      LOG.info("End to kill application {},", applicationId);
    } else {
      LOG.warn("yarnClient is null, this is not able to kill application");
    }
  }
}
