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

package org.apache.linkis.metadata.query.common.service;

public class GenerateSqlTemplate {

  public static final String ES_DDL_SQL_TEMPLATE =
      "CREATE TEMPORARY TABLE %s "
          + "USING org.elasticsearch.spark.sql  "
          + "OPTIONS ("
          + "  'es.nodes' '%s',"
          + "  'es.port' '%s',"
          + "  'es.resource' '%s/_doc'"
          + ")";

  public static final String JDBC_DDL_SQL_TEMPLATE =
      "CREATE TEMPORARY TABLE %s "
          + "USING org.apache.spark.sql.jdbc "
          + "OPTIONS ("
          + "  url '%s',"
          + "  dbtable '%s',"
          + "  user '%s',"
          + "  password '%s'"
          + ")";

  public static final String KAFKA_DDL_SQL_TEMPLATE =
      "CREATE TEMPORARY TABLE %s "
          + "USING kafka "
          + "OPTIONS ("
          + "  'kafka.bootstrap.servers' '%s',"
          + "  'subscribe' '%s'"
          + ")";

  public static final String MONGO_DDL_SQL_TEMPLATE =
      "CREATE TEMPORARY TABLE %s "
          + "USING mongo "
          + "OPTIONS ("
          + "  'spark.mongodb.input.uri' '%s',"
          + "  'spark.mongodb.input.database' '%s',"
          + "  'spark.mongodb.input.collection' '%s'"
          + ")";
  public static final String DML_SQL_TEMPLATE = "INSERT INTO %s SELECT * FROM ${resultTable}";

  public static final String DQL_SQL_TEMPLATE = "SELECT %s FROM %s";

  public static String generateDqlSql(String columns, String table) {
    return String.format(GenerateSqlTemplate.DQL_SQL_TEMPLATE, columns, table);
  }

  public static String generateDmlSql(String table) {
    return String.format(GenerateSqlTemplate.DML_SQL_TEMPLATE, table);
  }
}
