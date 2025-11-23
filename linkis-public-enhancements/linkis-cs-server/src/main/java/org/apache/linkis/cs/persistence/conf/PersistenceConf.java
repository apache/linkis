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

package org.apache.linkis.cs.persistence.conf;

import org.apache.linkis.common.conf.CommonVars;

public class PersistenceConf {

  public static final CommonVars<String> TUNING_CLASS =
      CommonVars.apply(
          "wds.linkis.cs.ha.class", "org.apache.linkis.cs.highavailable.DefaultContextHAManager");
  // public static final CommonVars<String> TUNING_CLASS =
  // CommonVars.apply("wds.linkis.cs.ha.class","org.apache.linkis.cs.persistence.ProxyMethodA");

  public static final CommonVars<String> TUNING_METHOD =
      CommonVars.apply("wds.linkis.cs.ha.proxymethod", "getContextHAProxy");

  public static final CommonVars<Boolean> ENABLE_CS_DESERIALIZE_REPLACE_PACKAGE_HEADER =
      CommonVars.apply("wds.linkis.cs.deserialize.replace_package_header.enable", true);

  public static final CommonVars<String> CS_DESERIALIZE_REPLACE_PACKAGE_HEADER =
      CommonVars.apply(
          "wds.linkis.cs.deserialize.replace_package.header", "com.webank.wedatasphere.linkis");

  public static final String CSID_PACKAGE_HEADER = "org.apache.linkis";
}
