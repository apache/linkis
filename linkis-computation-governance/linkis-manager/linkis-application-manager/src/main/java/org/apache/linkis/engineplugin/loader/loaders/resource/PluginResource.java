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

package org.apache.linkis.engineplugin.loader.loaders.resource;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class PluginResource {
  private String id;

  private String version;

  private long updateTime;

  private URL[] urls = new URL[0];

  PluginResource() {}

  PluginResource(String id, String version, long updateTime, URL[] urls) {
    this.id = id;
    this.version = version;
    this.updateTime = updateTime;
    this.urls = urls;
  }

  public void merge(PluginResource pluginResource) {
    if (StringUtils.isNotBlank(pluginResource.id)) {
      this.id = pluginResource.id;
    }
    if (StringUtils.isNotBlank(pluginResource.version)) {
      this.version = pluginResource.version;
    }
    if (pluginResource.updateTime > 0) {
      this.updateTime = pluginResource.updateTime;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  public URL[] getUrls() {
    return urls;
  }

  public void setUrls(URL[] urls) {
    this.urls = urls;
  }
}
