/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.flink.client.context;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.engine.flink.client.cluster.MultipleYarnClusterClientFactory;
import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.client.cli.CliFrontend;
import org.apache.flink.client.cli.CliFrontendParser;
import org.apache.flink.client.cli.CustomCommandLine;
import org.apache.flink.client.deployment.ClusterClientServiceLoader;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptionsInternal;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.plugin.PluginUtils;

import org.apache.commons.cli.Options;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnConfigOptionsInternal;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *  默认上下文
 */
public class DefaultContext {

	private final String yarnConfDir;
	private final String flinkConfDir;
	private final String flinkHome;
	private final String flinkLibRemotePath;

	public List<String> getProvidedLibDirs() {
		return providedLibDirs;
	}

	public List<String> getShipDirs() {
		return shipDirs;
	}

	private final Environment defaultEnv;
	private final List<URL> dependencies;
	private  List<String> providedLibDirs;
	private  List<String> shipDirs;

	private final Configuration flinkConfig;
	private final ClusterClientServiceLoader clusterClientServiceLoader;

	public DefaultContext(Environment defaultEnv,Configuration systemConfiguration,String yarnConfDir, String flinkConfDir, String flinkHome, String distJarPath, String flinkLibRemotePath,String[] providedLibDirsArray,String[] shipDirsArray, List<URL> dependencies) {
		this.yarnConfDir = yarnConfDir;
		this.flinkConfDir = flinkConfDir;
		this.flinkHome = flinkHome;
		this.flinkLibRemotePath = flinkLibRemotePath;
		this.defaultEnv = defaultEnv;
		//本地jar
		this.dependencies = dependencies;

		//远程资源目录
		this.providedLibDirs = Lists.newArrayList(providedLibDirsArray).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());

		//本地资源目录
		this.shipDirs = Lists.newArrayList(shipDirsArray).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());

		//加载系统级别配置
		this.flinkConfig = GlobalConfiguration.loadConfiguration(this.flinkConfDir);
		if(null!=systemConfiguration){
			this.flinkConfig.addAll(systemConfiguration);
		}

		//设置 flink conf目录
		this.flinkConfig.set(DeploymentOptionsInternal.CONF_DIR, this.flinkConfDir);
		//设置 yarn conf目录
		this.flinkConfig.set(MultipleYarnClusterClientFactory.YARN_CONFIG_DIR, this.yarnConfDir);
		//设置 flink dist jar
		this.flinkConfig.set(YarnConfigOptions.FLINK_DIST_JAR, distJarPath);

		clusterClientServiceLoader = new DefaultClusterClientServiceLoader();
	}

	public String getYarnConfDir() {
		return yarnConfDir;
	}

	public String getFlinkConfDir() {
		return flinkConfDir;
	}

	public String getFlinkHome() {
		return flinkHome;
	}

	public String getFlinkLibRemotePath() {
		return flinkLibRemotePath;
	}

	public Configuration getFlinkConfig() {
		return flinkConfig;
	}

	public Environment getDefaultEnv() {
		return defaultEnv;
	}

	public List<URL> getDependencies() {
		return dependencies;
	}


	public ClusterClientServiceLoader getClusterClientServiceLoader() {
		return clusterClientServiceLoader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultContext)) {
			return false;
		}
		DefaultContext context = (DefaultContext) o;
		return Objects.equals(defaultEnv, context.defaultEnv) &&
			Objects.equals(dependencies, context.dependencies) &&
			Objects.equals(flinkConfig, context.flinkConfig) &&
			Objects.equals(clusterClientServiceLoader, context.clusterClientServiceLoader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			defaultEnv, dependencies, flinkConfig, clusterClientServiceLoader);
	}
}
