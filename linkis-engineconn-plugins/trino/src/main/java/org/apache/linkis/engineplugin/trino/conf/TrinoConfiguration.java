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

package org.apache.linkis.engineplugin.trino.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.storage.utils.StorageConfiguration;

public class TrinoConfiguration {

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.concurrent.limit", 100);

  public static final CommonVars<Integer> DEFAULT_LIMIT =
      CommonVars.apply("linkis.trino.default.limit", 5000);

  public static final CommonVars<Long> TRINO_HTTP_CONNECT_TIME_OUT =
      CommonVars.apply("linkis.trino.http.connectTimeout.seconds", 60L);

  public static final CommonVars<Long> TRINO_HTTP_READ_TIME_OUT =
      CommonVars.apply("linkis.trino.http.readTimeout.seconds", 60L);

  public static final CommonVars<String> TRINO_URL =
      CommonVars.apply("linkis.trino.url", "http://127.0.0.1:8080");

  public static final CommonVars<String> TRINO_PASSWORD =
      CommonVars.apply("linkis.trino.password", null);

  public static final CommonVars<String> TRINO_PASSWORD_CMD =
      CommonVars.apply("linkis.trino.password.cmd", null);

  public static final CommonVars<String> TRINO_CATALOG =
      CommonVars.apply("linkis.trino.catalog", "system");

  public static final CommonVars<String> TRINO_SCHEMA = CommonVars.apply("linkis.trino.schema", "");

  public static final CommonVars<String> TRINO_SOURCE =
      CommonVars.apply("linkis.trino.source", "global");

  public static final CommonVars<Boolean> TRINO_SSL_INSECURED =
      CommonVars.apply("linkis.trino.ssl.insecured", true);

  public static final CommonVars<String> TRINO_SSL_KEYSTORE =
      CommonVars.apply("linkis.trino.ssl.keystore", null);

  public static final CommonVars<String> TRINO_SSL_KEYSTORE_TYPE =
      CommonVars.apply("linkis.trino.ssl.keystore.type", null);

  public static final CommonVars<String> TRINO_SSL_KEYSTORE_PASSWORD =
      CommonVars.apply("linkis.trino.ssl.keystore.password", null);

  public static final CommonVars<String> TRINO_SSL_TRUSTSTORE =
      CommonVars.apply("linkis.trino.ssl.truststore", null);

  public static final CommonVars<String> TRINO_SSL_TRUSTSTORE_TYPE =
      CommonVars.apply("linkis.trino.ssl.truststore.type", null);

  public static final CommonVars<String> TRINO_SSL_TRUSTSTORE_PASSWORD =
      CommonVars.apply("linkis.trino.ssl.truststore.password", null);

  public static final CommonVars<Boolean> TRINO_FORBID_GRANT =
      CommonVars.apply("linkis.trino.forbid.grant", true);

  public static final CommonVars<Boolean> TRINO_FORBID_MODIFY_SCHEMA =
      CommonVars.apply("linkis.trino.forbid.modifySchema", true);

  public static final CommonVars<Boolean> TRINO_USER_ISOLATION_MODE =
      CommonVars.apply("linkis.trino.user.isolation.mode", false);

  public static final CommonVars<String> TRINO_DEFAULT_USER =
      CommonVars.apply(
          "linkis.trino.default.start.user", StorageConfiguration.HDFS_ROOT_USER.getValue());

  public static final CommonVars<Boolean> TRINO_SQL_HOOK_ENABLED =
      CommonVars.apply("linkis.trino.sql.hook.enabled", true, "trino sql hoook");
}
