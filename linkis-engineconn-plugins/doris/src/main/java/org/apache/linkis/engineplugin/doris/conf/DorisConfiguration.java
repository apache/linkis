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

package org.apache.linkis.engineplugin.doris.conf;

import org.apache.linkis.common.conf.CommonVars;

public class DorisConfiguration {

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.concurrent.limit", 100);

  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("linkis.ec.doris.default.limit", 5000);

  public static final CommonVars<String> DORIS_COLUMN_SEPARATOR =
      CommonVars.apply("linkis.ec.doris.column.separator", ",");

  public static final CommonVars<String> DORIS_LINE_DELIMITER =
      CommonVars.apply("linkis.ec.doris.line.delimiter", "\\n");

  public static final CommonVars<String> DORIS_STREAM_LOAD_FILE_PATH =
      CommonVars.apply(
          "linkis.ec.doris.stream.load.file.path",
          "",
          "A file path, for example: /test/test.csv, currently supports csv、json、parquet、orc format");

  public static final CommonVars<String> DORIS_COLUMNS =
      CommonVars.apply("linkis.ec.doris.columns", "");

  public static final CommonVars<String> DORIS_LABEL =
      CommonVars.apply("linkis.ec.doris.label", "");

  public static final CommonVars<String> DORIS_CONF =
      CommonVars.apply(
          "linkis.ec.doris.conf",
          "",
          "The doris parameter, separated by commas, for example: timeout:600,label:123");

  public static final CommonVars<String> DORIS_HOST =
      CommonVars.apply("linkis.ec.doris.host", "127.0.0.1");

  public static final CommonVars<Integer> DORIS_HTTP_PORT =
      CommonVars.apply("linkis.ec.doris.http.port", 8030);

  public static final CommonVars<Integer> DORIS_JDBC_PORT =
      CommonVars.apply("linkis.ec.doris.jdcb.port", 9030);

  public static final CommonVars<String> DORIS_DATABASE =
      CommonVars.apply("linkis.ec.doris.database", "");

  public static final CommonVars<String> DORIS_TABLE =
      CommonVars.apply("linkis.ec.doris.table", "");

  public static final CommonVars<String> DORIS_USER_NAME =
      CommonVars.apply("linkis.ec.doris.username", "root");

  public static final CommonVars<String> DORIS_PASSWORD =
      CommonVars.apply("linkis.ec.doris.password", "");

  public static final CommonVars<Boolean> DORIS_RECONNECT_ENABLED =
      CommonVars.apply("linkis.ec.doris.2pc.enabled", false, "two phase commit Whether to enable");

  public static final CommonVars<Boolean> DORIS_STRIP_OUTER_ARRAY =
      CommonVars.apply(
          "linkis.ec.doris.strip.outer.array",
          true,
          "true indicates that the json data starts with an array object and flattens the array object, the default value is true, Refer to doris for strip_outer_array");
}
