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

package com.webank.wedatasphere.linkis.engine.flink.client.config.entries;

import org.apache.flink.table.client.config.ConfigUtil;
import org.apache.flink.table.client.config.entries.*;
import org.apache.flink.table.descriptors.DescriptorProperties;
import org.apache.flink.util.TimeUtils;

import java.util.HashMap;
import java.util.Map;

import static com.webank.wedatasphere.linkis.engine.flink.client.config.Environment.SESSION_ENTRY;


public class SessionEntry extends ConfigEntry {

	public static final SessionEntry DEFAULT_INSTANCE = new SessionEntry(new DescriptorProperties(true));

	private static final String SESSION_IDLE_TIMEOUT = "idle-timeout";

	private static final String SESSION_CHECK_INTERVAL = "check-interval";

	private static final String SESSION_MAX_COUNT = "max-count";

	private SessionEntry(DescriptorProperties properties) {
		super(properties);
	}

	@Override
	protected void validate(DescriptorProperties properties) {
		properties.validateDuration(SESSION_IDLE_TIMEOUT, true, 1);
		properties.validateDuration(SESSION_CHECK_INTERVAL, true, 1);
		properties.validateLong(SESSION_MAX_COUNT, true, 0);
	}

	public static SessionEntry create(Map<String, Object> config) {
		return new SessionEntry(ConfigUtil.normalizeYaml(config));
	}

	public Map<String, String> asTopLevelMap() {
		return properties.asPrefixedMap(SESSION_ENTRY + '.');
	}

	/**
	 * Merges two session entries. The properties of the first execution entry might be
	 * overwritten by the second one.
	 */
	public static SessionEntry merge(SessionEntry session1, SessionEntry session2) {
		final Map<String, String> mergedProperties = new HashMap<>(session1.asTopLevelMap());
		mergedProperties.putAll(session2.asTopLevelMap());

		final DescriptorProperties properties = new DescriptorProperties(true);
		properties.putProperties(mergedProperties);

		return new SessionEntry(properties);
	}

	public long getIdleTimeout() {
		String timeout = properties.getOptionalString(SESSION_IDLE_TIMEOUT).orElse("1d");
		return TimeUtils.parseDuration(timeout).toMillis();
	}

	public long getCheckInterval() {
		String interval = properties.getOptionalString(SESSION_CHECK_INTERVAL).orElse("1h");
		return TimeUtils.parseDuration(interval).toMillis();
	}

	public long getMaxCount() {
		return properties.getOptionalLong(SESSION_MAX_COUNT).orElse(1000000L);
	}
}
