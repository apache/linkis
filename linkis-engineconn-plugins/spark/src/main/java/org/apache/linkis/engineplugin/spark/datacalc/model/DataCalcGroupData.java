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

package org.apache.linkis.engineplugin.spark.datacalc.model;

import org.apache.linkis.server.BDPJettyServerHelper;

import java.io.Serializable;

public class DataCalcGroupData extends DataCalcPluginConfig implements Serializable {

  private DataCalcDataConfig[] sources;
  private DataCalcDataConfig[] transformations;
  private DataCalcDataConfig[] sinks;

  public DataCalcDataConfig[] getSources() {
    return sources;
  }

  public void setSources(DataCalcDataConfig[] sources) {
    this.sources = sources;
  }

  public DataCalcDataConfig[] getTransformations() {
    return transformations;
  }

  public void setTransformations(DataCalcDataConfig[] transformations) {
    this.transformations = transformations;
  }

  public DataCalcDataConfig[] getSinks() {
    return sinks;
  }

  public void setSinks(DataCalcDataConfig[] sinks) {
    this.sinks = sinks;
  }

  public static DataCalcGroupData getData(String data) {
    return BDPJettyServerHelper.gson().fromJson(data, DataCalcGroupData.class);
  }
}
