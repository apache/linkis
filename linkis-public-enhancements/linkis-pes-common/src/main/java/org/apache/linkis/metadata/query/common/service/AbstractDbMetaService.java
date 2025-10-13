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

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.metadata.query.common.domain.GenerateSqlInfo;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Database meta service
 *
 * @param <C>
 */
public abstract class AbstractDbMetaService<C extends Closeable> extends AbstractCacheMetaService<C>
    implements MetadataDbService {

  @Override
  public List<String> getDatabases(String operator, Map<String, Object> params) {
    return this.getConnAndRun(operator, params, this::queryDatabases);
  }

  @Override
  public String getSqlConnectUrl(String operator, Map<String, Object> params) {
    return this.getConnAndRun(operator, params, this::querySqlConnectUrl);
  }

  @Override
  public GenerateSqlInfo getJdbcSql(
      String operator, Map<String, Object> params, String database, String table) {
    return this.getConnAndRun(operator, params, conn -> this.queryJdbcSql(conn, database, table));
  }

  @Override
  public List<String> getTables(String operator, Map<String, Object> params, String database) {
    return this.getConnAndRun(operator, params, conn -> this.queryTables(conn, database));
  }

  @Override
  public Map<String, String> getTableProps(
      String operator, Map<String, Object> params, String database, String table) {
    return this.getConnAndRun(
        operator, params, conn -> this.queryTableProps(conn, database, table));
  }

  @Override
  public MetaPartitionInfo getPartitions(
      String operator,
      Map<String, Object> params,
      String database,
      String table,
      boolean traverse) {
    return this.getConnAndRun(
        operator, params, conn -> this.queryPartitions(conn, database, table, traverse));
  }

  @Override
  public List<MetaColumnInfo> getColumns(
      String operator, Map<String, Object> params, String database, String table) {
    return this.getConnAndRun(operator, params, conn -> this.queryColumns(conn, database, table));
  }

  @Override
  public Map<String, String> getPartitionProps(
      String operator,
      Map<String, Object> params,
      String database,
      String table,
      String partition) {
    return this.getConnAndRun(
        operator, params, conn -> this.queryPartitionProps(conn, database, table, partition));
  }

  /**
   * Get database list by connection
   *
   * @param connection metadata connection
   * @return
   */
  public List<String> queryDatabases(C connection) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get sql connect url
   *
   * @param connection metadata connection
   * @return
   */
  public String querySqlConnectUrl(C connection) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get jdbc sql
   *
   * @param connection metadata connection
   * @param database database
   * @param table table
   * @return
   */
  public GenerateSqlInfo queryJdbcSql(C connection, String database, String table) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get table list by connection and database
   *
   * @param connection metadata connection
   * @param database database
   * @return
   */
  public List<String> queryTables(C connection, String database) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get partitions by connection, database and table
   *
   * @param connection metadata connection
   * @param database database
   * @param table table
   * @return
   */
  public MetaPartitionInfo queryPartitions(
      C connection, String database, String table, boolean traverse) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get columns by connection, database and table
   *
   * @param connection metadata connection
   * @param database database
   * @param table table
   * @return
   */
  public List<MetaColumnInfo> queryColumns(C connection, String database, String table) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get the properties of partition
   *
   * @param connection
   * @param database
   * @param table
   * @param partition
   * @return
   */
  public Map<String, String> queryPartitionProps(
      C connection, String database, String table, String partition) {
    throw new WarnException(-1, "This method is no supported");
  }

  /**
   * Get table properties
   *
   * @param connection metadata connection
   * @param database database
   * @param table table
   * @return
   */
  public Map<String, String> queryTableProps(C connection, String database, String table) {
    throw new WarnException(-1, "This method is no supported");
  }
}
