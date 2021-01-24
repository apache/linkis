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


import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.descriptors.DescriptorProperties;

import java.util.Map;
import java.util.Objects;


public abstract class ConfigEntry {

	protected final DescriptorProperties properties;

	protected ConfigEntry(DescriptorProperties properties) {
		try {
			validate(properties);
		} catch (ValidationException e) {
			throw new SqlGatewayException("Invalid configuration entry.", e);
		}

		this.properties = properties;
	}

	/**
	 * Performs syntactic validation.
	 */
	protected abstract void validate(DescriptorProperties properties);

	public Map<String, String> asMap() {
		return properties.asMap();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConfigEntry that = (ConfigEntry) o;
		return Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(properties);
	}
}
