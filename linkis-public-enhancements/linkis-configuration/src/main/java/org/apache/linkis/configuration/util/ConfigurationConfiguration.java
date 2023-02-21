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

package org.apache.linkis.configuration.util;

import org.apache.linkis.common.conf.CommonVars$;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import java.util.ArrayList;

public class ConfigurationConfiguration {

  public static final ArrayList<Label> PERMIT_LABEL_TYPE = new ArrayList<>();

  public static final String COPYKEYTOKEN =
      CommonVars$.MODULE$.apply("wds.linkis.configuration.copykey.token", "e8724-e").getValue();
  public static final String IPCHECK =
      CommonVars$.MODULE$
          .apply(
              "linkis.configuration.ipcheck.pattern",
              "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$")
          .getValue();

  static {
    PERMIT_LABEL_TYPE.add(new UserCreatorLabel());
    PERMIT_LABEL_TYPE.add(new EngineTypeLabel());
  }
}
