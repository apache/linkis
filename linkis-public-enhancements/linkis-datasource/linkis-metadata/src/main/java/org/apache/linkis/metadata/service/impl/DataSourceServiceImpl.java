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
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.util.DWSConfig;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

  @Autowired HiveMetaWithPermissionService hiveMetaWithPermissionService;

  ObjectMapper jsonMapper = new ObjectMapper();

  private static String dbKeyword = DWSConfig.DB_FILTER_KEYWORDS.getValue();

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getDbs(String userName) throws Exception {
    List<String> dbs = hiveMetaWithPermissionService.getDbsOptionalUserName(userName);
    ArrayNode dbsNode = jsonMapper.createArrayNode();
    for (String db : dbs) {
      ObjectNode dbNode = jsonMapper.createObjectNode();
      dbNode.put("dbName", db);
      dbsNode.add(dbNode);
    }
    return dbsNode;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getDbsWithTables(String userName) {
    ArrayNode dbNodes = jsonMapper.createArrayNode();
    List<String> dbs = hiveMetaWithPermissionService.getDbsOptionalUserName(userName);
    MetadataQueryParam queryParam = MetadataQueryParam.of(userName);
    for (String db : dbs) {
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
  public JsonNode queryTables(MetadataQueryParam queryParam) {
    List<Map<String, Object>> listTables;
    try {
      listTables = hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(queryParam);
    } catch (Throwable e) {
      logger.error("Failed to list Tables:", e);
      throw new RuntimeException(e);
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
    return tables;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode queryTableMeta(MetadataQueryParam queryParam) {
    logger.info("getTable:" + queryParam.getTableName());
    List<Map<String, Object>> columns = hiveMetaDao.getColumns(queryParam);
    List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
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
    List<Map<String, Object>> columns = hiveMetaDao.getColumnsByStorageDescriptionID(queryParam);
    List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    return getJsonNodesFromColumnMap(columns, partitionKeys);
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  public String getTableLocation(MetadataQueryParam queryParam) {
    String tableLocation = hiveMetaDao.getLocationByDbAndTable(queryParam);
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

    Long partitionSize = hiveMetaDao.getPartitionSize(queryParam);
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
    List<String> partitions = hiveMetaDao.getPartitions(queryParam);
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
    List<String> partitions = hiveMetaDao.getPartitions(queryParam);
    boolean res = Boolean.FALSE;
    if (CollectionUtils.isNotEmpty(partitions)
        && partitions.contains(queryParam.getPartitionName())) {
      res = Boolean.TRUE;
    }
    return res;
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
    if (HadoopConf.HDFS_ENABLE_CACHE()) {
      HDFSUtils.closeHDFSFIleSystem(
          HDFSUtils.getHDFSRootUserFileSystem(), HadoopConf.HADOOP_ROOT_USER().getValue(), true);
      return;
    }
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
    if (HadoopConf.HDFS_ENABLE_CACHE()) {
      return HDFSUtils.getHDFSRootUserFileSystem();
    }
    if (rootHdfs == null) {
      synchronized (this) {
        if (rootHdfs == null) {
          rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
        }
      }
    }
    return rootHdfs;
  }
}
