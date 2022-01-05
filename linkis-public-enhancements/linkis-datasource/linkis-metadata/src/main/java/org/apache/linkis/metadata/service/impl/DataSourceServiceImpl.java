/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DataSourceServiceImpl implements DataSourceService {

    private static final Logger logger = Logger.getLogger(DataSourceServiceImpl.class);

    private static FileSystem rootHdfs = null;

    @Autowired
    HiveMetaDao hiveMetaDao;

    @Autowired
    HiveMetaWithPermissionService hiveMetaWithPermissionService;

    ObjectMapper jsonMapper = new ObjectMapper();

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
    public JsonNode getDbsWithTables(String userName) throws Exception {
        ArrayNode dbNodes = jsonMapper.createArrayNode();
        List<String> dbs =  hiveMetaWithPermissionService.getDbsOptionalUserName(userName);
        for (String db : dbs) {
            ObjectNode dbNode = jsonMapper.createObjectNode();
            dbNode.put("databaseName", db);
            dbNode.put("tables", queryTables(db, userName));
            dbNodes.add(dbNode);
        }
        return dbNodes;
    }

    @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
    @Override
    public JsonNode queryTables(String database, String userName) {
        List<Map<String, Object>> listTables = Lists.newArrayList();
        try {
            Map<String, String> map = Maps.newHashMap();
            map.put("dbName", database);
            map.put("userName", userName);
            listTables =hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(map);
        } catch (Throwable e) {
            logger.error("Failed to list Tables:", e);
            throw new RuntimeException(e);
        }

        ArrayNode tables = jsonMapper.createArrayNode();
        for (Map<String, Object> table : listTables) {
            ObjectNode tableNode = jsonMapper.createObjectNode();
            tableNode.put("tableName", (String) table.get("NAME"));
            tableNode.put("isView", table.get("TYPE").equals("VIRTUAL_VIEW"));
            tableNode.put("databaseName", database);
            tableNode.put("createdBy", (String) table.get("OWNER"));
            tableNode.put("createdAt", (Integer) table.get("CREATE_TIME"));
            tableNode.put("lastAccessAt", (Integer) table.get("LAST_ACCESS_TIME"));
            tables.add(tableNode);
        }
        return tables;
    }

    @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
    @Override
    public JsonNode queryTableMeta(String dbName, String tableName, String userName) {
        logger.info("getTable:" + userName);
        Map<String, String> param = Maps.newHashMap();
        param.put("dbName", dbName);
        param.put("tableName", tableName);
        List<Map<String, Object>> columns = hiveMetaDao.getColumns(param);
        ArrayNode columnsNode = jsonMapper.createArrayNode();
        for (Map<String, Object> column : columns) {
            ObjectNode fieldNode = jsonMapper.createObjectNode();
            fieldNode.put("columnName", (String) column.get("COLUMN_NAME"));
            fieldNode.put("columnType", (String) column.get("TYPE_NAME"));
            fieldNode.put("columnComment", (String) column.get("COMMENT"));
            fieldNode.put("partitioned", false);
            columnsNode.add(fieldNode);
        }
        List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(param);
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
    public String getTableLocation(String database, String tableName) {
        Map<String, String> param = Maps.newHashMap();
        param.put("dbName", database);
        param.put("tableName", tableName);
        String tableLocation = hiveMetaDao.getLocationByDbAndTable(param);
        logger.info("tableLocation:" + tableLocation);
        return tableLocation;
    }

    @Override
    public JsonNode getTableSize(String dbName, String tableName, String userName) {
        logger.info("getTable:" + userName);


        String tableSize = "";
        try {
            FileStatus tableFile = getRootHdfs().getFileStatus(new Path(this.getTableLocation(dbName, tableName)));
            if (tableFile.isDirectory()) {
                tableSize = ByteTimeUtils.bytesToString(getRootHdfs().getContentSummary(tableFile.getPath()).getLength());
            } else {
                tableSize = ByteTimeUtils.bytesToString(tableFile.getLen());
            }
        } catch (IOException e) {
            logger.error("getTableSize error:", e);
        }

        ObjectNode sizeJson = jsonMapper.createObjectNode();
        sizeJson.put("size", tableSize);
        sizeJson.put("tableName", dbName + "." + tableName);
        return sizeJson;
    }

    @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
    @Override
    public JsonNode getPartitionSize(String dbName, String tableName, String partitionName, String userName) {
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", dbName);
        map.put("tableName", tableName);
        map.put("partitionName", partitionName);
        map.put("userName", userName);
        Long partitionSize = hiveMetaDao.getPartitionSize(map);
        if (partitionSize == null) {
            partitionSize = 0L;
        }
        ObjectNode sizeJson = jsonMapper.createObjectNode();
        sizeJson.put("size", ByteTimeUtils.bytesToString(partitionSize));
        sizeJson.put("tableName", dbName + "." + tableName);
        sizeJson.put("partitionName", partitionName);
        return sizeJson;
    }

    @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
    @Override
    public JsonNode getPartitions(String dbName, String tableName, String userName) {
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", dbName);
        map.put("tableName", tableName);
        List<String> partitions = hiveMetaDao.getPartitions(map);
        Collections.sort(partitions);
        Collections.reverse(partitions);

        ObjectNode partitionJson = jsonMapper.createObjectNode();
        partitionJson.put("tableName", dbName + "." + tableName);
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

    private FileSystem getRootHdfs() {
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
