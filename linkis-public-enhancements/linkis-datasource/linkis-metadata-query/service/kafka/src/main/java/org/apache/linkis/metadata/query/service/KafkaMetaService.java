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
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractDbMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMetaService extends AbstractDbMetaService<KafkaConnection> {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaMetaService.class);
  private static final CommonVars<String> TMP_FILE_STORE_LOCATION =
      CommonVars.apply("wds.linkis.server.mdm.service.temp.location", "/tmp/keytab");
  private BmlClient client;

  public KafkaMetaService() {
    client = BmlClientFactory.createBmlClient();
  }

  @Override
  public MetadataConnection<KafkaConnection> getConnection(
      String operator, Map<String, Object> params) throws Exception {
    FileUtils.forceMkdir(new File(TMP_FILE_STORE_LOCATION.getValue()));
    String brokers =
        String.valueOf(params.getOrDefault(KafkaParamsMapper.PARAM_KAFKA_BROKERS.getValue(), ""));
    String principle =
        String.valueOf(params.getOrDefault(KafkaParamsMapper.PARAM_KAFKA_PRINCIPLE.getValue(), ""));
    KafkaConnection conn;

    if (StringUtils.isNotBlank(principle)) {
      LOG.info("Try to connect Kafka MetaStore in kerberos with principle:[" + principle + "]");
      String keytabResourceId =
          String.valueOf(params.getOrDefault(KafkaParamsMapper.PARAM_KAFKA_KEYTAB.getValue(), ""));
      if (StringUtils.isNotBlank(keytabResourceId)) {
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
        conn = new KafkaConnection(brokers, principle, keytabFilePath);
      } else {
        throw new MetaRuntimeException("Cannot find the keytab file in connect parameters", null);
      }
    } else {
      conn = new KafkaConnection(brokers);
    }

    // because KafkaAdminClient.create does not do a real connection, we use listTopics here for
    // testing connection
    conn.getClient().listTopics().names().get();

    return new MetadataConnection<>(conn, true);
  }

  @Override
  public List<String> queryDatabases(KafkaConnection connection) {
    return Arrays.asList("default");
  }

  @Override
  public List<String> queryTables(KafkaConnection connection, String database) {
    try {
      return connection.getClient().listTopics().names().get().stream()
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Fail to get Kafka topics(获取topic列表失败)", e);
    }
  }

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
    BmlDownloadResponse downloadResponse = client.downloadResource(user, resourceId, absolutePath);
    if (downloadResponse.isSuccess()) {
      IOUtils.copy(downloadResponse.inputStream(), new FileOutputStream(absolutePath));
      return true;
    }
    return false;
  }
}
