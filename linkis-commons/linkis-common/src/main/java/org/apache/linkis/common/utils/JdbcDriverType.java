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

package org.apache.linkis.common.utils;

/**
 * Identifies the JDBC driver family for security-policy dispatch.
 *
 * <p>Used by {@link SecurityUtils#checkJdbcConnParams(JdbcDriverType, String, Integer, String,
 * String, String, java.util.Map)} and {@link SecurityUtils#buildSecureProperties(JdbcDriverType,
 * String, String, java.util.Map)} so that each driver family gets its own sensitive-parameter
 * denylist and force-set security defaults.
 */
public enum JdbcDriverType {
  MYSQL,
  POSTGRESQL,
  GREENPLUM,
  KINGBASE,
  ORACLE,
  SQLSERVER,
  DB2,
  CLICKHOUSE,
  DM,
  STARROCKS
}
