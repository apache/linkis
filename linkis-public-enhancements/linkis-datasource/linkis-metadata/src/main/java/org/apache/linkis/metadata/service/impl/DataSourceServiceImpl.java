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

package org.apache.linkis.metadata.service.impl;

import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.hadoop.common.utils.KerberosUtils;
import org.apache.linkis.metadata.conf.MdqConfiguration;
import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.service.RangerPermissionService;
import org.apache.linkis.metadata.util.DWSConfig;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DataSourceServiceImpl implements DataSourceService {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

  private static FileSystem rootHdfs = null;

  @Autowired HiveMetaDao hiveMetaDao;

  @Autowired @Lazy HiveMetaWithPermissionService hiveMetaWithPermissionService;

  @Autowired @Lazy RangerPermissionService rangerPermissionService;

  @Autowired DataSourceService dataSourceService;

  ObjectMapper jsonMapper = new ObjectMapper();

  private static String dbKeyword = DWSConfig.DB_FILTER_KEYWORDS.getValue();

  @Override
  public JsonNode getDbs(String userName, String permission) throws Exception {
    Set<String> hiveDbs = dataSourceService.getHiveDbs(userName, permission);
    if (checkRangerConnectionConfig()) {
      if (StringUtils.isNotBlank(permission) && permission.equals("write")) {
        // ranger只允许配置查询权限，如果需要查询有写入权限的表，则不查ranger的数据
        logger.info("ranger only support query permission");
      } else {
        Set<String> rangerDbs = dataSourceService.getRangerDbs(userName);
        hiveDbs.addAll(rangerDbs);
      }
    }
    // 将hiveDbs根据String升序排序
    List<String> sortedDbs = hiveDbs.stream().sorted().collect(Collectors.toList());
    ArrayNode dbsNode = jsonMapper.createArrayNode();
    for (String db : sortedDbs) {
      ObjectNode dbNode = jsonMapper.createObjectNode();
      dbNode.put("dbName", db);
      dbsNode.add(dbNode);
    }
    return dbsNode;
  }

  @DataSource(name = DSEnum.THIRD_DATA_SOURCE)
  @Override
  public Set<String> getRangerDbs(String username) {
    try {
      return new HashSet<>(rangerPermissionService.getDbsByUsername(username));
    } catch (Exception e) {
      logger.error("Failed to list Ranger Dbs:", e);
      return new HashSet<>();
    }
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public Set<String> getHiveDbs(String userName, String permission) throws Exception {
    return new HashSet<>(
        hiveMetaWithPermissionService.getDbsOptionalUserName(userName, permission));
  }

  @Override
  public JsonNode getDbsWithTables(String userName) throws Exception {
    ArrayNode dbNodes = jsonMapper.createArrayNode();
    Set<String> dbs = dataSourceService.getHiveDbs(userName, null);
    Set<String> rangerDbs = dataSourceService.getRangerDbs(userName);
    dbs.addAll(rangerDbs);
    List<String> sortedDbs = dbs.stream().sorted().collect(Collectors.toList());
    MetadataQueryParam queryParam = MetadataQueryParam.of(userName);
    for (String db : sortedDbs) {
      if (StringUtils.isBlank(db) || db.contains(dbKeyword)) {
        logger.info("db  will be filter: " + db);
        continue;
      }
      queryParam.setDbName(db);
      ObjectNode dbNode = jsonMapper.createObjectNode();
      dbNode.put("databaseName", db);
      dbNode.put("tables", queryTables(queryParam));
      dbNodes.add(dbNode);
    }
    return dbNodes;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getDbsWithTablesAndLastAccessAt(String userName) {
    ArrayNode dbNodes = jsonMapper.createArrayNode();
    List<String> dbs = hiveMetaWithPermissionService.getDbsOptionalUserName(userName, null);
    MetadataQueryParam queryParam = MetadataQueryParam.of(userName);
    int count = 0;
    for (String db : dbs) {
      if (StringUtils.isBlank(db) || db.contains(dbKeyword)) {
        logger.info("db  will be filter: " + db);
        continue;
      }
      JsonNode jsonNode = queryTablesWithLastAccessAt(queryParam);
      count += jsonNode.size();
      if (count < 100000) {
        queryParam.setDbName(db);
        ObjectNode dbNode = jsonMapper.createObjectNode();
        dbNode.put("databaseName", db);
        dbNode.put("tables", jsonNode);
        dbNodes.add(dbNode);
      } else {
        break;
      }
    }
    return dbNodes;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public List<Map<String, Object>> queryHiveTables(MetadataQueryParam queryParam) {
    return hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(queryParam);
  }

  @DataSource(name = DSEnum.THIRD_DATA_SOURCE)
  @Override
  public List<String> queryRangerTables(MetadataQueryParam queryParam) {
    try {
      return rangerPermissionService.queryRangerTables(queryParam);
    } catch (Exception e) {
      logger.error("Failed to get Ranger Tables:", e);
      return new ArrayList<>();
    }
  }

  @DataSource(name = DSEnum.THIRD_DATA_SOURCE)
  @Override
  public List<String> getRangerColumns(MetadataQueryParam queryParam) {
    try {
      List<String> rangerColumns = rangerPermissionService.queryRangerColumns(queryParam);
      if (null != rangerColumns) {
        if (rangerColumns.contains("*")) {
          List<String> allColumns = new ArrayList<>();
          MetadataQueryParam queryAllParam =
              MetadataQueryParam.of(DWSConfig.HIVE_DB_ADMIN_USER.getValue())
                  .withDbName(queryParam.getDbName())
                  .withTableName(queryParam.getTableName());
          JsonNode allColumnsNodes =
              hiveMetaWithPermissionService.getColumnsByDbTableNameAndOptionalUserName(
                  queryAllParam);
          for (int i = 0; i < allColumnsNodes.size(); i++) {
            JsonNode node = allColumnsNodes.get(i);
            allColumns.add(node.get("columnName").asText());
          }
          return allColumns;
        }
      }
      return rangerColumns;
    } catch (Exception e) {
      logger.error("Failed to get Ranger Columns:", e);
      return null;
    }
  }

  @Override
  public JsonNode filterRangerColumns(JsonNode hiveColumns, List<String> rangerColumns) {
    try {
      ArrayNode filteredColumns = jsonMapper.createArrayNode();
      for (int i = 0; i < hiveColumns.size(); i++) {
        JsonNode column = hiveColumns.get(i);
        if (rangerColumns.contains(column.get("columnName").asText())) {
          filteredColumns.add(column);
        }
      }
      return filteredColumns;
    } catch (Exception e) {
      logger.error("Failed to filterRangerColumns:", e);
      return hiveColumns;
    }
  }

  @Override
  public JsonNode queryTables(MetadataQueryParam queryParam) {
    List<Map<String, Object>> listTables;
    try {
      listTables = dataSourceService.queryHiveTables(queryParam);
    } catch (Throwable e) {
      logger.error("Failed to list Tables:", e);
      throw new RuntimeException(e);
    }
    List<String> rangerTables = new ArrayList<>();
    try {
      if (checkRangerConnectionConfig()) {
        rangerTables = dataSourceService.queryRangerTables(queryParam);
        Set<String> tableNames =
            listTables.stream()
                .map(table -> (String) table.get("NAME"))
                .collect(Collectors.toSet());
        // 过滤掉ranger中有，hive中也有的表
        rangerTables =
            rangerTables.stream()
                .filter(rangerTable -> !tableNames.contains(rangerTable))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      logger.error("Failed to get Ranger Tables:", e);
    }

    ArrayNode tables = jsonMapper.createArrayNode();
    for (Map<String, Object> table : listTables) {
      ObjectNode tableNode = jsonMapper.createObjectNode();
      tableNode.put("tableName", (String) table.get("NAME"));
      tableNode.put("isView", table.get("TYPE").equals("VIRTUAL_VIEW"));
      tableNode.put("databaseName", queryParam.getDbName());
      tableNode.put("createdBy", (String) table.get("OWNER"));
      tableNode.put("createdAt", (Integer) table.get("CREATE_TIME"));
      tableNode.put("lastAccessAt", (Integer) table.get("LAST_ACCESS_TIME"));
      tables.add(tableNode);
    }
    for (String rangerTable : rangerTables) {
      ObjectNode tableNode = jsonMapper.createObjectNode();
      tableNode.put("tableName", rangerTable);
      tableNode.put("isView", false);
      tableNode.put("databaseName", queryParam.getDbName());
      tableNode.put("createdBy", "");
      tableNode.put("createdAt", 0);
      tableNode.put("lastAccessAt", 0);
      tables.add(tableNode);
    }
    // 将Arraynode根据tableName字段排序
    tables = sortArrayNode(tables);
    return tables;
  }

  private ArrayNode sortArrayNode(ArrayNode tables) {
    try {
      List<JsonNode> tableArrays =
          jsonMapper.readValue(tables.toString(), new TypeReference<List<JsonNode>>() {});
      tableArrays.sort(Comparator.comparing(node -> node.get("tableName").asText()));
      ArrayNode sortedTables = jsonMapper.createArrayNode();
      for (JsonNode table : tableArrays) {
        sortedTables.add(table);
      }
      return sortedTables;
    } catch (Exception e) {
      logger.error("Exception occured when sorting tables: " + e);
      return tables;
    }
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode queryTablesWithLastAccessAt(MetadataQueryParam queryParam) {
    List<Map<String, Object>> listTables;
    try {
      listTables = hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(queryParam);
    } catch (Throwable e) {
      logger.error("Failed to list Tables:", e);
      throw new RuntimeException(e);
    }
    long threeMonthsAgo = System.currentTimeMillis() - (3L * 30 * 24 * 60 * 60 * 1000);
    ArrayNode tables = jsonMapper.createArrayNode();
    listTables.stream()
        .filter(table -> ((Integer) table.get("LAST_ACCESS_TIME")).longValue() > threeMonthsAgo)
        .forEach(
            table -> {
              ObjectNode tableNode = jsonMapper.createObjectNode();
              tableNode.put("tableName", (String) table.get("NAME"));
              tableNode.put("isView", table.get("TYPE").equals("VIRTUAL_VIEW"));
              tableNode.put("databaseName", queryParam.getDbName());
              tableNode.put("createdBy", (String) table.get("OWNER"));
              tableNode.put("createdAt", (Integer) table.get("CREATE_TIME"));
              tableNode.put("lastAccessAt", (Integer) table.get("LAST_ACCESS_TIME"));
              tables.add(tableNode);
            });
    return tables;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode queryTableMeta(MetadataQueryParam queryParam) {
    logger.info("getTable:" + queryParam.getTableName());
    List<Map<String, Object>> columns;
    List<Map<String, Object>> partitionKeys;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      columns = hiveMetaDao.getColumns(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    } else {
      columns = hiveMetaDao.getColumnsSlave(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeysSlave(queryParam);
    }
    return getJsonNodesFromColumnMap(columns, partitionKeys);
  }

  private ArrayNode getJsonNodesFromColumnMap(
      List<Map<String, Object>> columns, List<Map<String, Object>> partitionKeys) {
    ArrayNode columnsNode = jsonMapper.createArrayNode();
    for (Map<String, Object> column : columns) {
      ObjectNode fieldNode = jsonMapper.createObjectNode();
      fieldNode.put("columnName", (String) column.get("COLUMN_NAME"));
      fieldNode.put("columnType", (String) column.get("TYPE_NAME"));
      fieldNode.put("columnComment", (String) column.get("COMMENT"));
      fieldNode.put("partitioned", false);
      columnsNode.add(fieldNode);
    }
    for (Map<String, Object> partitionKey : partitionKeys) {
      ObjectNode fieldNode = jsonMapper.createObjectNode();
      fieldNode.put("columnName", (String) partitionKey.get("PKEY_NAME"));
      fieldNode.put("columnType", (String) partitionKey.get("PKEY_TYPE"));
      fieldNode.put("columnComment", (String) partitionKey.get("PKEY_COMMENT"));
      fieldNode.put("partitioned", true);
      columnsNode.add(fieldNode);
    }
    return columnsNode;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode queryTableMetaBySDID(MetadataQueryParam queryParam) {
    logger.info("getTableMetabysdid : sdid = {}", queryParam.getSdId());
    List<Map<String, Object>> columns;
    List<Map<String, Object>> partitionKeys;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      columns = hiveMetaDao.getColumns(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    } else {
      columns = hiveMetaDao.getColumnsSlave(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeysSlave(queryParam);
    }
    return getJsonNodesFromColumnMap(columns, partitionKeys);
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  public String getTableLocation(MetadataQueryParam queryParam) {
    String tableLocation;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      tableLocation = hiveMetaDao.getLocationByDbAndTable(queryParam);
    } else {
      tableLocation = hiveMetaDao.getLocationByDbAndTableSlave(queryParam);
    }
    logger.info("tableLocation:" + tableLocation);
    return tableLocation;
  }

  @Override
  public JsonNode getTableSize(MetadataQueryParam queryParam) {
    logger.info("getTable:" + queryParam.getTableName());

    String tableSize = "";
    try {
      FileStatus tableFile = getFileStatus(this.getTableLocation(queryParam));
      if (tableFile.isDirectory()) {
        tableSize =
            ByteTimeUtils.bytesToString(
                getRootHdfs().getContentSummary(tableFile.getPath()).getLength());
      } else {
        tableSize = ByteTimeUtils.bytesToString(tableFile.getLen());
      }
    } catch (IOException e) {
      logger.error("getTableSize error:", e);
    }

    ObjectNode sizeJson = jsonMapper.createObjectNode();
    sizeJson.put("size", tableSize);
    sizeJson.put("tableName", queryParam.getDbName() + "." + queryParam.getTableName());
    return sizeJson;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getPartitionSize(MetadataQueryParam queryParam) {

    Long partitionSize;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      partitionSize = hiveMetaDao.getPartitionSize(queryParam);
    } else {
      partitionSize = hiveMetaDao.getPartitionSizeSlave(queryParam);
    }
    if (partitionSize == null) {
      partitionSize = 0L;
    }
    ObjectNode sizeJson = jsonMapper.createObjectNode();
    sizeJson.put("size", ByteTimeUtils.bytesToString(partitionSize));
    sizeJson.put("tableName", queryParam.getDbName() + "." + queryParam.getTableName());
    sizeJson.put("partitionName", queryParam.getPartitionName());
    return sizeJson;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getPartitions(MetadataQueryParam queryParam) {
    List<String> partitions;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      partitions = hiveMetaDao.getPartitions(queryParam);
    } else {
      partitions = hiveMetaDao.getPartitionsSlave(queryParam);
    }
    Collections.sort(partitions);
    Collections.reverse(partitions);

    ObjectNode partitionJson = jsonMapper.createObjectNode();
    partitionJson.put("tableName", queryParam.getDbName() + "." + queryParam.getTableName());
    if (CollectionUtils.isEmpty(partitions)) {
      partitionJson.put("isPartition", false);
    } else {
      partitionJson.put("isPartition", true);
      partitionJson.put("partitions", jsonMapper.createArrayNode());
      int depth = StringUtils.countMatches(partitions.get(0), "/");
      Map<String, JsonNode> nameToNode = Maps.newHashMap();
      for (String partition : partitions) {
        String[] lables = StringUtils.split(partition, "/");
        for (int i = 0; i <= depth; i++) {
          if (i == 0) {
            if (!nameToNode.containsKey(lables[i])) {
              ObjectNode childJson = jsonMapper.createObjectNode();
              childJson.put("label", lables[i]);
              childJson.put("path", lables[i]);
              childJson.put("children", jsonMapper.createArrayNode());
              nameToNode.put(lables[i], childJson);
              ((ArrayNode) partitionJson.get("partitions")).add(childJson);
            }
          } else {
            String parentPath = StringUtils.join(Arrays.copyOfRange(lables, 0, i), "/");
            String currentPath = StringUtils.join(Arrays.copyOfRange(lables, 0, i + 1), "/");
            if (!nameToNode.containsKey(currentPath)) {
              ObjectNode childJson = jsonMapper.createObjectNode();
              childJson.put("label", lables[i]);
              childJson.put("path", currentPath);
              childJson.put("children", jsonMapper.createArrayNode());
              nameToNode.put(currentPath, childJson);
              ((ArrayNode) nameToNode.get(parentPath).get("children")).add(childJson);
            }
          }
        }
      }
    }
    return partitionJson;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public boolean partitionExists(MetadataQueryParam queryParam) {
    List<String> partitions;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      partitions = hiveMetaDao.getPartitions(queryParam);
    } else {
      partitions = hiveMetaDao.getPartitionsSlave(queryParam);
    }
    boolean res = Boolean.FALSE;
    if (CollectionUtils.isNotEmpty(partitions)
        && partitions.contains(queryParam.getPartitionName())) {
      res = Boolean.TRUE;
    }
    return res;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public Map<String, Object> getStorageInfo(MetadataQueryParam queryParam) {
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      return hiveMetaDao.getStorageInfo(queryParam);
    } else {
      return hiveMetaDao.getStorageInfoSlave(queryParam);
    }
  }

  private FileStatus getFileStatus(String location) throws IOException {
    try {
      return getRootHdfs().getFileStatus(new Path(location));
    } catch (IOException e) {
      String message = e.getMessage();
      String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
      if ((message != null && message.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS))
          || (rootCauseMessage != null
              && rootCauseMessage.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS))) {
        logger.info("Failed to getFileStatus, retry", e);
        resetRootHdfs();
        return getFileStatus(location);
      } else {
        throw e;
      }
    }
  }

  private void resetRootHdfs() {
    if (rootHdfs != null) {
      synchronized (this) {
        if (rootHdfs != null) {
          IOUtils.closeQuietly(rootHdfs);
          logger.info("reset RootHdfs");
          rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
        }
      }
    }
  }

  private FileSystem getRootHdfs() {
    if (rootHdfs == null) { // NOSONAR
      synchronized (this) { // NOSONAR
        if (rootHdfs == null) { // NOSONAR
          rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
          KerberosUtils.startKerberosRefreshThread();
        }
      }
    }
    return rootHdfs;
  }

  @Override
  public Boolean checkRangerConnectionConfig() {
    if (DWSConfig.RANGER_DB_ENABLE.getValue()
        && StringUtils.isNotBlank(DWSConfig.RANGER_DB_URL.getValue())
        && StringUtils.isNotBlank(DWSConfig.RANGER_DB_USER.getValue())
        && StringUtils.isNotBlank(DWSConfig.RANGER_DB_PASSWORD.getValue())) {
      logger.debug("ranger db config exists, connection check success");
      return true;
    }
    return false;
  }
}
