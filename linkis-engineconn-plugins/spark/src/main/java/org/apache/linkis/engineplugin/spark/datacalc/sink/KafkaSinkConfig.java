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

package org.apache.linkis.engineplugin.spark.datacalc.sink;

import org.apache.linkis.engineplugin.spark.datacalc.model.SinkConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class KafkaSinkConfig extends SinkConfig {

  @NotBlank private String servers;

  @NotBlank private String topic;

  private String checkpointLocation = "./ck";

  @NotBlank
  @Pattern(
      regexp = "^(batch|stream)$",
      message = "Unknown mode: {saveMode}. Accepted modes are 'batch', 'stream'.")
  private String mode = "stream";

  public String getCheckpointLocation() {
    return checkpointLocation;
  }

  public void setCheckpointLocation(String checkpointLocation) {
    this.checkpointLocation = checkpointLocation;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getServers() {
    return servers;
  }

  public void setServers(String servers) {
    this.servers = servers;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
