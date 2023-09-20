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

package org.apache.linkis.engineplugin.nebula.conf;

import org.apache.linkis.common.conf.CommonVars;

public class NebulaConfiguration {

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.concurrent.limit", 100);

  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("linkis.nebula.default.limit", 5000);

  public static final CommonVars<String> NEBULA_HOST =
      CommonVars.apply("linkis.nebula.host", "127.0.0.1");

  public static final CommonVars<Integer> NEBULA_PORT =
      CommonVars.apply("linkis.nebula.port", 9669);

  public static final CommonVars<Integer> NEBULA_MAX_CONN_SIZE =
      CommonVars.apply("linkis.nebula.max.conn.size", 100);

  public static final CommonVars<String> NEBULA_USER_NAME =
      CommonVars.apply("linkis.nebula.username", "root");

  public static final CommonVars<String> NEBULA_PASSWORD =
      CommonVars.apply("linkis.nebula.password", "nebula");

  public static final CommonVars<Boolean> NEBULA_RECONNECT_ENABLED =
      CommonVars.apply(
          "linkis.nebula.reconnect.enabled",
          false,
          "whether to retry after the connection is disconnected");
}
