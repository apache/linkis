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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractFsMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;
import org.apache.linkis.metadata.query.service.conf.ConfigurationUtils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Hdfs meta service */
public class HdfsMetaService extends AbstractFsMetaService<HdfsConnection> {

  private static final Logger LOG = LoggerFactory.getLogger(HdfsMetaService.class);

  private static final String PARAM_FILTER_RULE = "filter";
  /** Filter rules */
  private static final CommonVars<String> DEFAULT_FILTER_RULES =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.hadoop.filter.rules",
          StringUtils.join(
              new String[] {
                "fs.defaultFS",
                "dfs.nameservices",
                "dfs.ha.namenodes.<service>",
                "dfs.namenode.rpc-address.<suffix>",
                "dfs.client.failover.proxy.provider.<suffix>"
              },
              ","));

  @Override
  public MetadataConnection<HdfsConnection> getConnection(
      String creator, Map<String, Object> params) throws Exception {
    Map<String, String> hadoopConf = toMap(params, HdfsParamsMapper.PARAM_HADOOP_CONF.getValue());
    if (Objects.nonNull(hadoopConf) && !hadoopConf.isEmpty()) {
      return new MetadataConnection<>(
          new HdfsConnection("", creator, hadoopConf, !useCache()), true);
    } else {
      String clusterLabel =
          Optional.ofNullable(toMap(params, "labels"))
              .orElse(Collections.emptyMap())
              .get(HdfsParamsMapper.PARAM_HADOOP_LABEL_CLUSTER.getValue());
      LOG.info("Use Hadoop root config directory: " + HadoopConf.hadoopConfDir());
      return new MetadataConnection<>(
          new HdfsConnection("", creator, clusterLabel, !useCache()), true);
    }
  }

  @Override
  public Map<String, String> queryConnectionInfo(
      HdfsConnection connection, Map<String, String> queryParams) {
    List<String> filterRules = new ArrayList<>();
    AtomicReference<URI> uriReference = new AtomicReference<>();
    LOG.info("query hdfs ConnectionInfo for uri: {}", queryParams.get("uri"));
    Optional.ofNullable(queryParams.get("uri"))
        .ifPresent(
            uri -> {
              try {
                uriReference.set(new URI(uri));
              } catch (URISyntaxException e) {
                LOG.warn("Unrecognized uri value: [" + uri + "]", e);
              }
            });
    Optional.ofNullable(queryParams.get(PARAM_FILTER_RULE))
        .ifPresent(
            rules -> {
              if (StringUtils.isNotBlank(rules)) {
                filterRules.addAll(Arrays.asList(rules.split(",")));
              }
            });
    if (filterRules.isEmpty()) {
      filterRules.addAll(Arrays.asList(DEFAULT_FILTER_RULES.getValue().split(",")));
    }
    return ConfigurationUtils.filterConfiguration(
        connection.getFileSystem(), filterRules, uriReference.get());
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> toMap(Map<String, Object> connectParams, String key) {
    Map<String, String> valueMap = new HashMap<>();
    Object mapObj = connectParams.get(key);
    if (Objects.nonNull(mapObj)) {
      try {
        if (!(mapObj instanceof Map)) {
          valueMap = Json.fromJson(String.valueOf(mapObj), Map.class, String.class, String.class);
        } else {
          valueMap = (Map<String, String>) mapObj;
        }
      } catch (Exception e) {
        throw new MetaRuntimeException("Cannot parse the param:[" + key + "]", null);
      }
    }
    return valueMap;
  }
}
