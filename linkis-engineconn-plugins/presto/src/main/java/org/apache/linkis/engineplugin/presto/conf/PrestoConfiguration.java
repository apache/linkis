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

package org.apache.linkis.engineplugin.presto.conf;

import org.apache.linkis.common.conf.CommonVars;

public class PrestoConfiguration {

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("wds.linkis.engineconn.concurrent.limit", 100);

  // unit in seconds
  public static final CommonVars<Long> PRESTO_HTTP_CONNECT_TIME_OUT =
      CommonVars.apply("wds.linkis.presto.http.connectTimeout", 60L);

  public static final CommonVars<Long> PRESTO_HTTP_READ_TIME_OUT =
      CommonVars.apply("wds.linkis.presto.http.readTimeout", 60L);

  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("wds.linkis.presto.default.limit", 5000);

  public static final CommonVars<String> PRESTO_URL =
      CommonVars.apply("wds.linkis.presto.url", "http://127.0.0.1:8080");

  public static final CommonVars<String> PRESTO_RESOURCE_CONFIG_PATH =
      CommonVars.apply("wds.linkis.presto.resource.config", "");

  public static final CommonVars<String> PRESTO_USER_NAME =
      CommonVars.apply("wds.linkis.presto.username", "default");

  public static final CommonVars<String> PRESTO_PASSWORD =
      CommonVars.apply("wds.linkis.presto.password", "");

  public static final CommonVars<String> PRESTO_CATALOG =
      CommonVars.apply("wds.linkis.presto.catalog", "system");

  public static final CommonVars<String> PRESTO_SCHEMA =
      CommonVars.apply("wds.linkis.presto.schema", "");

  public static final CommonVars<String> PRESTO_SOURCE =
      CommonVars.apply("wds.linkis.presto.source", "global");

  public static final CommonVars<String> PRESTO_REQUEST_MEMORY =
      CommonVars.apply("presto.session.query_max_total_memory", "8GB");

  public static final CommonVars<Boolean> PRESTO_SQL_HOOK_ENABLED =
      CommonVars.apply("linkis.presto.sql.hook.enabled", true, "presto sql hook");
}
