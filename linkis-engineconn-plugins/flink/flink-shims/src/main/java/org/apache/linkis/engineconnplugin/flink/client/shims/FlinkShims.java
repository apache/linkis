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

package org.apache.linkis.engineconnplugin.flink.client.shims;

import java.lang.reflect.Constructor;

/**
 * This is abstract class for anything that is api incompatible between different flink versions. It
 * will load the correct version of FlinkShims based on the version of flink.
 */
public abstract class FlinkShims {

  private static FlinkShims flinkShims;

  protected String flinkVersion;

  public FlinkShims(String flinkVersion) {
    this.flinkVersion = flinkVersion;
  }

  private static FlinkShims loadShims(String flinkVersion) throws Exception {
    Class<?> flinkShimsClass;
    if (flinkVersion.equals("1.12.2")) {
      flinkShimsClass =
          Class.forName("org.apache.linkis.engineconnplugin.flink.client.shims.Flink1122Shims");
    } else if (flinkVersion.equals("1.16.2")) {
      flinkShimsClass =
          Class.forName("org.apache.linkis.engineconnplugin.flink.client.shims.Flink1162Shims");
    } else {
      throw new Exception("Flink version: '" + flinkVersion + "' is not supported yet");
    }

    Constructor c = flinkShimsClass.getConstructor(String.class);
    return (FlinkShims) c.newInstance(flinkVersion);
  }

  /**
   * @param flinkVersion
   * @param properties
   * @return
   */
  public static FlinkShims getInstance(String flinkVersion) throws Exception {
    if (flinkShims == null) {
      flinkShims = loadShims(flinkVersion);
    }
    return flinkShims;
  }

  //  public abstract Object createScalaBlinkStreamTableEnvironment(
  //      Object environmentSettingsObj,
  //      Object senvObj,
  //      Object tableConfigObj,
  //      Object moduleManagerObj,
  //      Object functionCatalogObj,
  //      Object catalogManagerObj,
  //      List<URL> jars,
  //      ClassLoader classLoader);
  //
  //  public abstract Object createJavaBlinkStreamTableEnvironment(
  //      Object environmentSettingsObj,
  //      Object senvObj,
  //      Object tableConfigObj,
  //      Object moduleManagerObj,
  //      Object functionCatalogObj,
  //      Object catalogManagerObj,
  //      List<URL> jars,
  //      ClassLoader classLoader);

  public Object createTableEnvironment(
      Object flinkConfig, Object streamExecEnv, Object sessionState, ClassLoader classLoader) {
    return null;
  }
}
