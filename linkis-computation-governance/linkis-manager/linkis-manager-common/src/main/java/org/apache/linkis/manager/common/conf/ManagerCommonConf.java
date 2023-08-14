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

package org.apache.linkis.manager.common.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;

public class ManagerCommonConf {

  public static final CommonVars<String> DEFAULT_ENGINE_TYPE =
      CommonVars.apply("wds.linkis.default.engine.type", "spark");

  public static final CommonVars<String> DEFAULT_ENGINE_VERSION =
      CommonVars.apply(
          "wds.linkis.default.engine.version",
          LabelCommonConfig.SPARK_ENGINE_VERSION.defaultValue());

  public static final CommonVars<String> DEFAULT_ADMIN =
      CommonVars.apply("wds.linkis.manager.admin", "hadoop");
}
