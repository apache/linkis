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

package org.apache.linkis.engineconnplugin.flink.client.shims.config.entries;

import org.apache.linkis.engineconnplugin.flink.client.shims.config.ConfigUtil;

import org.apache.flink.table.descriptors.DescriptorProperties;

import java.util.Collections;
import java.util.Map;

/** Describes a module configuration entry. */
public class ModuleEntry extends ConfigEntry {

  public static final String MODULE_NAME = "name";

  public static final String MODULE_TYPE = "type";

  private final String name;

  protected ModuleEntry(String name, DescriptorProperties properties) {
    super(properties);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  protected void validate(DescriptorProperties properties) {
    properties.validateString(MODULE_TYPE, false, 1);
  }

  public static ModuleEntry create(Map<String, Object> config) {
    return create(ConfigUtil.normalizeYaml(config));
  }

  private static ModuleEntry create(DescriptorProperties properties) {
    properties.validateString(MODULE_NAME, false, 1);

    final String name = properties.getString(MODULE_NAME);

    final DescriptorProperties cleanedProperties =
        properties.withoutKeys(Collections.singletonList(MODULE_NAME));

    return new ModuleEntry(name, cleanedProperties);
  }
}
