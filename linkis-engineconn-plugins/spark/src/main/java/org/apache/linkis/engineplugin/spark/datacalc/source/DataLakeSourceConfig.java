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

package org.apache.linkis.engineplugin.spark.datacalc.source;

import org.apache.linkis.engineplugin.spark.datacalc.model.SourceConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class DataLakeSourceConfig extends SourceConfig {

  @NotBlank
  @Pattern(
      regexp = "^(((file|hdfs)://)|/).*",
      message =
          "Invalid path URI, please set the following allowed schemas: 'file://' or 'hdfs://'(default).")
  private String path;

  @NotBlank
  @Pattern(
      regexp = "^(delta|hudi)$",
      message = "Unknown table format: {saveMode}. Accepted save modes are 'delta', 'hudi'.")
  private String tableFormat = "delta";

  public String getPath() {
    if (path.startsWith("/")) return "hdfs://" + path;
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTableFormat() {
    return tableFormat;
  }

  public void setTableFormat(String tableFormat) {
    this.tableFormat = tableFormat;
  }
}
