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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation.result;

/** Constant column names. */
public class ConstantNames {

  // for statement execution
  public static final String JOB_ID = "job_id";
  // for results with SUCCESS result kind
  public static final String RESULT = "result";
  public static final String OK = "OK";

  public static final String SHOW_MODULES_RESULT = "modules";

  public static final String SHOW_CURRENT_CATALOG_RESULT = "catalog";

  public static final String SHOW_CATALOGS_RESULT = "catalogs";

  public static final String SHOW_CURRENT_DATABASE_RESULT = "database";

  public static final String SHOW_DATABASES_RESULT = "databases";

  public static final String SHOW_FUNCTIONS_RESULT = "functions";

  public static final String EXPLAIN_RESULT = "explanation";

  public static final String DESCRIBE_NAME = "name";
  public static final String DESCRIBE_TYPE = "type";
  public static final String DESCRIBE_NULL = "null";
  public static final String DESCRIBE_KEY = "key";
  public static final String DESCRIBE_COMPUTED_COLUMN = "computed_column";
  public static final String DESCRIBE_WATERMARK = "watermark";

  public static final String SHOW_TABLES_RESULT = "tables";

  public static final String SHOW_VIEWS_RESULT = "views";

  public static final String SET_KEY = "key";
  public static final String SET_VALUE = "value";
}
