/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadata.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils;
import com.webank.wedatasphere.linkis.hadoop.common.conf.HadoopConf;
import com.webank.wedatasphere.linkis.hadoop.common.utils.HDFSUtils;
import com.webank.wedatasphere.linkis.metadata.dao.DataSourceDao;
import com.webank.wedatasphere.linkis.metadata.domain.DataSource;
import com.webank.wedatasphere.linkis.metadata.hive.dao.HiveMetaDao;
import com.webank.wedatasphere.linkis.metadata.service.DataSourceService;
import com.webank.wedatasphere.linkis.metadata.util.DWSConfig;
import com.webank.wedatasphere.linkis.metadata.util.HiveUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Created by shanhuang on 9/13/18.
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    Logger logger = Logger.getLogger(HiveUtils.class);

    private static Hive hiveDB = null;
    private static FileSystem rootHdfs = null;

    @Autowired(required = false)
    DataSourceDao dataSourceDao;

    @Autowired
    HiveMetaDao hiveMetaDao;

    HttpClient client = new HttpClient();

    ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void create(DataSource dataSource, String userName) throws Exception {
        dataSource.setInfoOnCreate(userName);
        dataSourceDao.insert(dataSource);

        DataSource left = dataSource;
        DataSource right = dataSource.getRight();
        while(right != null){
            right.setViewId(left.getViewId());
            right.setInfoOnCreate(userName);
            dataSourceDao.insert(right);
            left.setRightId(right.getId());
            dataSourceDao.update(left);
            left = right;
            right = left.getRight();
        }
    }

    @Override
    public void update(DataSource dataSource, String userName) throws Exception {
        dataSource.setInfoOnUpdate(userName);
        dataSourceDao.update(dataSource);
    }

    @Override
    public void delete(Integer id) throws Exception {
        dataSourceDao.deleteById(id);
    }

    @Override
    public String analyseDataSourceSql(DataSource dataSource) throws Exception {
        List<DataSource> flatDataSources = Lists.newArrayList();
        DataSource toAnalyse = dataSource;
        while(toAnalyse != null){
            toAnalyse.setSql(getSqlByType(toAnalyse));
            flatDataSources.add(toAnalyse);
            toAnalyse = toAnalyse.getRight();
        }
        // TODO velocity

        return null;
    }

    private String getSqlByType(DataSource dataSource){


        return null;
    }

    @Override
    public JsonNode getDirContent(String path, String userName) throws Exception {
        PostMethod post = new PostMethod(DWSConfig.IDE_URL.getValue() + "api/fileSystem/fileManageBrowser");
        post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        post.setParameter("user", userName);
        if(StringUtils.isNotBlank(path)) post.setParameter("path", path);
        post.setParameter("type", "script");
        JsonNode response;
        try {
            client.executeMethod(post);
            response = jsonMapper.readTree(post.getResponseBodyAsStream());
        } catch (Exception e) {
            throw new Exception("Failed to get file directory(获取文件目录失败)。", e);
        } finally {
            post.releaseConnection();
        }
        return response;
    }

    @Override
    public JsonNode getDbs(String userName) throws Exception {
        List<String> dbs = hiveMetaDao.getDbsByUser(userName);
        ArrayNode dbsNode = jsonMapper.createArrayNode();
        for(String db : dbs){
            ObjectNode dbNode = jsonMapper.createObjectNode();
            dbNode.put("dbName", db);
            dbsNode.add(dbNode);
        }
        return dbsNode;
    }

    @Override
    public JsonNode getDbsWithTables(String userName) throws Exception {
        ArrayNode dbNodes = jsonMapper.createArrayNode();
        List<String> dbs = hiveMetaDao.getDbsByUser(userName);
        for(String db : dbs){
            ObjectNode dbNode = jsonMapper.createObjectNode();
            dbNode.put("databaseName", db);
            dbNode.put("tables", queryTables(db, userName));
            dbNodes.add(dbNode);
        }
        return dbNodes;
    }

    @Override
    public JsonNode queryTables(String database, String userName) {
        List<Map<String, Object>> listTables = Lists.newArrayList();
        try {
            Map<String, String> map = Maps.newHashMap();
            map.put("dbName", database);
            map.put("userName", userName);
            listTables =hiveMetaDao.getTablesByDbNameAndUser(map);
        } catch (Throwable e) {
            hiveDB = null;
            logger.error("Failed to list Tables:", e);
            throw new RuntimeException(e);
        }

        ArrayNode tables = jsonMapper.createArrayNode();
        for(Map<String, Object> table : listTables){
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

    @Override
    public JsonNode queryTableMeta(String dbName, String tableName, String userName) {
        logger.info("getTable:" + userName);
        Map<String, String> param = Maps.newHashMap();
        param.put("dbName", dbName);
        param.put("tableName", tableName);
        List<Map<String, Object>> columns = hiveMetaDao.getColumns(param);
        ArrayNode columnsNode = jsonMapper.createArrayNode();
        for(Map<String, Object> column : columns){
            ObjectNode fieldNode = jsonMapper.createObjectNode();
            fieldNode.put("columnName", (String) column.get("COLUMN_NAME"));
            fieldNode.put("columnType", (String) column.get("TYPE_NAME"));
            fieldNode.put("columnComment", (String) column.get("COMMENT"));
            fieldNode.put("partitioned", false);
            columnsNode.add(fieldNode);
        }
        List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(param);
        for(Map<String, Object> partitionKey : partitionKeys){
            ObjectNode fieldNode = jsonMapper.createObjectNode();
            fieldNode.put("columnName", (String) partitionKey.get("PKEY_NAME"));
            fieldNode.put("columnType", (String) partitionKey.get("PKEY_TYPE"));
            fieldNode.put("columnComment", (String) partitionKey.get("PKEY_COMMENT"));
            fieldNode.put("partitioned", true);
            columnsNode.add(fieldNode);
        }
        return columnsNode;
    }

    @Override
    public JsonNode getTableSize(String dbName, String tableName, String userName) {
        logger.info("getTable:" + userName);
        Table table = null;
        try {
            table = getHive().getTable(dbName, tableName, true);
        } catch (HiveException e) {
            logger.error("getTable error:", e);
        }

        String tableSize = "";
        if(table != null){
            try{
                FileStatus tableFile = getRootHdfs().getFileStatus(table.getDataLocation());
                if(tableFile.isDirectory()){
                    tableSize = ByteTimeUtils.bytesToString(getRootHdfs().getContentSummary(tableFile.getPath()).getLength());
                } else {
                    tableSize = ByteTimeUtils.bytesToString(tableFile.getLen());
                }
            } catch (IOException e){
                logger.error("getTableSize error:", e);
            }
        }

        ObjectNode sizeJson = jsonMapper.createObjectNode();
        sizeJson.put("size", tableSize);
        sizeJson.put("tableName", dbName + "." + tableName);
        return sizeJson;
    }

    @Override
    public JsonNode getPartitionSize(String dbName, String tableName, String partitionName, String userName){
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", dbName);
        map.put("tableName", tableName);
        map.put("partitionName", partitionName);
        map.put("userName", userName);
        Long partitionSize =hiveMetaDao.getPartitionSize(map);
        if(partitionSize == null) partitionSize = 0L;
        ObjectNode sizeJson = jsonMapper.createObjectNode();
        sizeJson.put("size", ByteTimeUtils.bytesToString(partitionSize));
        sizeJson.put("tableName", dbName + "." + tableName);
        sizeJson.put("partitionName", partitionName);
        return sizeJson;
    }

    @Override
    public JsonNode getPartitions(String dbName, String tableName, String userName){
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", dbName);
        map.put("tableName", tableName);
        List<String> partitions = hiveMetaDao.getPartitions(map);
        Collections.sort(partitions);
        Collections.reverse(partitions);

        ObjectNode partitionJson = jsonMapper.createObjectNode();
        partitionJson.put("tableName", dbName + "." + tableName);
        if(CollectionUtils.isEmpty(partitions)){
            partitionJson.put("isPartition", false);
        } else {
            partitionJson.put("isPartition", true);
            partitionJson.put("partitions", jsonMapper.createArrayNode());
            int depth = StringUtils.countMatches(partitions.get(0), "/");
            Map<String, JsonNode> nameToNode = Maps.newHashMap();
            for(String partition: partitions){
                String[] lables = StringUtils.split(partition, "/");
                for(int i = 0; i <= depth; i++){
                    if(i == 0){
                        if(!nameToNode.containsKey(lables[i])){
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
                        if(!nameToNode.containsKey(currentPath)){
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
        if(rootHdfs == null){
            synchronized (this) {
                if(rootHdfs == null){
                    rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
                }
            }
        }
        return rootHdfs;
    }

    private Hive getHive() throws HiveException {
        if (hiveDB == null) {
            synchronized (this) {
                if (hiveDB == null) {
                    hiveDB = Hive.get(HiveUtils.getDefaultConf(HadoopConf.HADOOP_ROOT_USER().getValue()));
                }
            }
        }
        return hiveDB;
    }
}
