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

package com.webank.wedatasphere.linkis.engine.flink.client.utils;

import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;

import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;


public class EnvironmentUtil {
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentUtil.class);

	public static Environment readEnvironment(URL envUrl) {
		if (envUrl == null) {
			LOG.info("No session environment specified.");
			return new Environment();
		}
		LOG.info("Using configuration file: {}", envUrl);
		try {
			return Environment.parse(envUrl);
		} catch (IOException e) {
			throw new SqlGatewayException("Could not read configuration file at: " + envUrl, e);
		}
	}
}
