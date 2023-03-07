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

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractDbMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.Partition;
import org.apache.hadoop.hive.ql.metadata.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveMetaService extends AbstractDbMetaService<HiveConnection> {

  private static final Logger LOG = LoggerFactory.getLogger(HiveMetaService.class);
  private static final CommonVars<String> TMP_FILE_STORE_LOCATION =
      CommonVars.apply("wds.linkis.server.mdm.service.temp.location", "/tmp/keytab");

  private BmlClient client;

  private static final String PARTITION_PART_SEPARATOR = ",";
  private static final String PARTITION_KV_SEPARATOR = "=";
  private static final String PARTITION_CV_SEPARATOR = "/";

  public HiveMetaService() {
    client = BmlClientFactory.createBmlClient();
  }

  @Override
  public MetadataConnection<HiveConnection> getConnection(
      String operator, Map<String, Object> params) throws Exception {

    String uris =
        String.valueOf(params.getOrDefault(HiveParamsMapper.PARAM_HIVE_URIS.getValue(), ""));
    String principle =
        String.valueOf(params.getOrDefault(HiveParamsMapper.PARAM_HIVE_PRINCIPLE.getValue(), ""));
    HiveConnection conn = null;
    if (StringUtils.isNotBlank(principle)) {
      LOG.info("Try to connect Hive MetaStore in kerberos with principle:[" + principle + "]");
      String keytabResourceId =
          String.valueOf(params.getOrDefault(HiveParamsMapper.PARAM_HIVE_KEYTAB.getValue(), ""));
      if (StringUtils.isNotBlank(keytabResourceId)) {
        FileUtils.forceMkdir(new File(TMP_FILE_STORE_LOCATION.getValue()));
        LOG.info("Start to download resource id:[" + keytabResourceId + "]");
        String keytabFilePath =
            TMP_FILE_STORE_LOCATION.getValue()
                + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + ".keytab";

        if (!downloadResource(keytabResourceId, operator, keytabFilePath)) {
          throw new MetaRuntimeException(
              "Fail to download resource i:[" + keytabResourceId + "]", null);
        }
        conn = new HiveConnection(uris, principle, keytabFilePath, getExtraHadoopConf(params));
      } else {
        throw new MetaRuntimeException("Cannot find the keytab file in connect parameters", null);
      }
    } else {
      conn = new HiveConnection(uris, getExtraHadoopConf(params));
    }
    return new MetadataConnection<>(conn, true);
  }

  /**
   * Download resource to path by id
   *
   * @param resourceId resource id
   * @param user user
   * @param absolutePath absolute path
   * @return
   * @throws IOException
   */
  private boolean downloadResource(String resourceId, String user, String absolutePath)
      throws IOException {
    LOG.info(
        "Try to download resource resourceId:["
            + resourceId
            + "]"
            + ",user=["
            + user
            + "], will store in path:"
            + absolutePath);
    BmlDownloadResponse downloadResponse = client.downloadResource(user, resourceId);
    if (downloadResponse.isSuccess()) {
      IOUtils.copy(downloadResponse.inputStream(), new FileOutputStream(absolutePath));
      return true;
    }
    return false;
  }

  @Override
  public List<String> queryDatabases(HiveConnection connection) {
    try {
      return connection.getClient().getAllDatabases();
    } catch (HiveException e) {
      throw new RuntimeException("Fail to get Hive databases(获取数据库列表失败)", e);
    }
  }

  @Override
  public List<String> queryTables(HiveConnection connection, String database) {
    try {
      return connection.getClient().getAllTables(database);
    } catch (HiveException e) {
      throw new RuntimeException("Fail to get Hive tables(获取表列表失败)", e);
    }
  }

  @Override
  public MetaPartitionInfo queryPartitions(
      HiveConnection connection, String database, String table, boolean traverse) {
    List<Partition> partitions;
    Table rawTable;
    try {
      rawTable = connection.getClient().getTable(database, table);
      partitions =
          traverse ? connection.getClient().getPartitions(rawTable) : Collections.emptyList();
    } catch (HiveException e) {
      throw new RuntimeException("Fail to get Hive partitions(获取分区信息失败)", e);
    }
    MetaPartitionInfo info = new MetaPartitionInfo();
    List<FieldSchema> partitionKeys = rawTable.getPartitionKeys();
    List<String> partKeys = new ArrayList<>();
    partitionKeys.forEach(e -> partKeys.add(e.getName()));
    info.setPartKeys(partKeys);
    if (traverse) {
      // Static partitions
      Map<String, MetaPartitionInfo.PartitionNode> pMap = new HashMap<>(20);
      MetaPartitionInfo.PartitionNode root = new MetaPartitionInfo.PartitionNode();
      info.setRoot(root);
      long t = System.currentTimeMillis();
      for (Partition p : partitions) {
        try {
          List<String> values = p.getValues();
          if (!partitionKeys.isEmpty()) {
            String parentNameValue = "";
            for (int i = 0; i < values.size(); i++) {
              FieldSchema fieldSchema = partitionKeys.get(i);
              String name = fieldSchema.getName();
              String value = values.get(i);
              String nameValue = name + "=" + value;
              MetaPartitionInfo.PartitionNode node = new MetaPartitionInfo.PartitionNode();
              if (i > 0) {
                MetaPartitionInfo.PartitionNode parent = pMap.get(parentNameValue);
                parent.setName(name);
                parent.getPartitions().putIfAbsent(value, node);
              } else {
                root.setName(name);
                root.getPartitions().putIfAbsent(value, node);
              }
              parentNameValue += "/" + nameValue;
              pMap.putIfAbsent(parentNameValue, node);
            }
          }
        } catch (Exception e) {
          LOG.warn(e.getMessage(), e);
        }
      }
    }
    return info;
  }

  @Override
  public List<MetaColumnInfo> queryColumns(
      HiveConnection connection, String database, String table) {
    List<MetaColumnInfo> columns = new ArrayList<>();
    Table tb;
    try {
      tb = connection.getClient().getTable(database, table);
    } catch (HiveException e) {
      throw new RuntimeException("Fail to get Hive columns(获得表字段信息失败)", e);
    }
    tb.getFields()
        .forEach(
            field -> {
              MetaColumnInfo metaColumnInfo = new MetaColumnInfo();
              metaColumnInfo.setIndex(field.getFieldID());
              metaColumnInfo.setName(field.getFieldName());
              metaColumnInfo.setType(field.getFieldObjectInspector().getTypeName());
              columns.add(metaColumnInfo);
            });
    return columns;
  }

  @Override
  public Map<String, String> queryTableProps(
      HiveConnection connection, String database, String table) {
    try {
      Table rawTable = connection.getClient().getTable(database, table);
      return new HashMap<>((Map) rawTable.getMetadata());
    } catch (Exception e) {
      throw new RuntimeException("Fail to get Hive table properties(获取表参数信息失败)", e);
    }
  }

  @Override
  public Map<String, String> queryPartitionProps(
      HiveConnection connection, String database, String table, String partition) {
    Map<String, String> properties = new HashMap<>();
    Hive client = connection.getClient();
    if (Objects.nonNull(client) && StringUtils.isNotBlank(partition)) {
      try {
        // Convert to pairs of (partition_key: partition_value)
        Map<String, String> partitionParts =
            Arrays.stream(
                    partition.split(
                        partition.contains(PARTITION_CV_SEPARATOR)
                            ? PARTITION_CV_SEPARATOR
                            : PARTITION_PART_SEPARATOR))
                .map(part -> part.split(PARTITION_KV_SEPARATOR))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv.length > 1 ? kv[1] : ""));
        Table rawTable = client.getTable(database, table);
        Partition rawPartition = client.getPartition(rawTable, partitionParts, false);
        if (Objects.nonNull(rawPartition)) {
          Properties metadataProps = rawPartition.getMetadataFromPartitionSchema();
          Enumeration<?> propertyNames = metadataProps.propertyNames();
          while (propertyNames.hasMoreElements()) {
            String propName = String.valueOf(propertyNames.nextElement());
            Optional.ofNullable(metadataProps.getProperty(propName, null))
                .ifPresent(value -> properties.put(propName, value));
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Fail to get partition's properties(获取分区参数信息失败)", e);
      }
    }
    return properties;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getExtraHadoopConf(Map<String, Object> connectParams) {
    Map<String, String> extraHadoopConf = new HashMap<>();
    Object extraHadoopConfObj = connectParams.get(HiveParamsMapper.PARAM_HADOOP_CONF.getValue());
    if (Objects.nonNull(extraHadoopConfObj)) {
      try {
        if (!(extraHadoopConfObj instanceof Map)) {
          extraHadoopConf =
              Json.fromJson(
                  String.valueOf(extraHadoopConfObj), Map.class, String.class, String.class);
        } else {
          extraHadoopConf = (Map<String, String>) extraHadoopConfObj;
        }
      } catch (Exception e) {
        throw new MetaRuntimeException(
            "Cannot parse the param:[" + HiveParamsMapper.PARAM_HADOOP_CONF.getValue() + "]", e);
      }
    }
    return extraHadoopConf;
  }
}
