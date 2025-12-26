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

package org.apache.linkis.engineplugin.openlookeng.conf;

import org.apache.linkis.common.conf.CommonVars;

public class OpenLooKengConfiguration {

  public static final CommonVars<Integer> OPENLOOKENG_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.openlookeng.engineconn.concurrent.limit", 100);
  public static final CommonVars<Long> OPENLOOKENG_HTTP_CONNECT_TIME_OUT =
      CommonVars.apply("linkis.openlookeng.http.connectTimeout", 60L);
  public static final CommonVars<Long> OPENLOOKENG_HTTP_READ_TIME_OUT =
      CommonVars.apply("linkis.openlookeng.http.readTimeout", 60L);
  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("linkis.openlookeng.default.limit", 5000);
  public static final CommonVars<String> OPENLOOKENG_URL =
      CommonVars.apply("linkis.openlookeng.url", "http://127.0.0.1:8080");
  public static final CommonVars<String> OPENLOOKENG_RESOURCE_CONFIG_PATH =
      CommonVars.apply("linkis.openlookeng.resource.config", "");
  public static final CommonVars<String> OPENLOOKENG_USER_NAME =
      CommonVars.apply("linkis.openlookeng.username", "default");
  public static final CommonVars<String> OPENLOOKENG_PASSWORD =
      CommonVars.apply("linkis.openlookeng.password", "");
  public static final CommonVars<String> OPENLOOKENG_CATALOG =
      CommonVars.apply("linkis.openlookeng.catalog", "system");
  public static final CommonVars<String> OPENLOOKENG_SCHEMA =
      CommonVars.apply("linkis.openlookeng.schema", "");
  public static final CommonVars<String> OPENLOOKENG_SOURCE =
      CommonVars.apply("linkis.openlookeng.source", "global");
  public static final CommonVars<String> OPENLOOKENG_REQUEST_MEMORY =
      CommonVars.apply("openlookeng.session.query_max_total_memory", "8GB");
}
