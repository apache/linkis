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

package org.apache.linkis.engineplugin.elasticsearch.conf;

import org.apache.linkis.common.conf.ByteType;
import org.apache.linkis.common.conf.CommonVars;

public class ElasticSearchConfiguration {

  // es client
  public static final CommonVars<String> ES_CLUSTER =
      CommonVars.apply("linkis.es.cluster", "127.0.0.1:9200");
  public static final CommonVars<String> ES_DATASOURCE_NAME =
      CommonVars.apply("linkis.es.datasource", "default_datasource");
  public static final CommonVars<Boolean> ES_AUTH_CACHE =
      CommonVars.apply("linkis.es.auth.cache", false);
  public static final CommonVars<String> ES_USERNAME = CommonVars.apply("linkis.es.username", "");
  public static final CommonVars<String> ES_PASSWORD = CommonVars.apply("linkis.es.password", "");
  public static final CommonVars<Boolean> ES_SNIFFER_ENABLE =
      CommonVars.apply("linkis.es.sniffer.enable", false);
  public static final CommonVars<String> ES_HTTP_METHOD =
      CommonVars.apply("linkis.es.http.method", "GET");
  public static final CommonVars<String> ES_HTTP_ENDPOINT =
      CommonVars.apply("linkis.es.http.endpoint", "/_search");
  public static final CommonVars<String> ES_HTTP_SQL_ENDPOINT =
      CommonVars.apply("linkis.es.sql.endpoint", "/_sql");
  public static final CommonVars<String> ES_SQL_FORMAT =
      CommonVars.apply("linkis.es.sql.format", "{\"query\": \"%s\"}");
  public static final String ES_HTTP_HEADER_PREFIX = "linkis.es.headers.";

  // entrance resource
  public static final CommonVars<Integer> ENTRANCE_MAX_JOB_INSTANCE =
      CommonVars.apply("linkis.es.max.job.instance", 100);
  public static final CommonVars<Integer> ENTRANCE_PROTECTED_JOB_INSTANCE =
      CommonVars.apply("linkis.es.protected.job.instance", 20);
  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("linkis.es.default.limit", 5000);

  // resultSet
  public static final CommonVars<ByteType> ENGINE_RESULT_SET_MAX_CACHE =
      CommonVars.apply("linkis.resultSet.cache.max", new ByteType("512k"));

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.concurrent.limit", 100);

  public static final CommonVars<String> DEFAULT_VERSION =
      CommonVars.apply("linkis.engineconn.io.version", "7.6.2");
}
