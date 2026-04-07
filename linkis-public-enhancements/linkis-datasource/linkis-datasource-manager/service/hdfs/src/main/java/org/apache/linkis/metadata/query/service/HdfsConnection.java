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

import org.apache.linkis.hadoop.common.utils.HDFSUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Hdfs connection */
public class HdfsConnection implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(HdfsConnection.class);

  /** Hadoop configuration */
  private final Configuration hadoopConf;

  /** File system */
  private final FileSystem fs;

  public HdfsConnection(String scheme, String operator, String clusterLabel, boolean cache)
      throws IOException {
    long startTime = System.currentTimeMillis();
    LOG.info(
        "Creating HdfsConnection - scheme: {}, operator: {}, clusterLabel: {}, cache: {}",
        scheme,
        operator,
        clusterLabel,
        cache);

    try {
      // TODO fix the problem of connecting multiple cluster in FSFactory.getFSByLabelAndUser
      //        Fs fileSystem = FSFactory.getFSByLabelAndUser(scheme, operator, clusterLabel);
      hadoopConf = HDFSUtils.getConfigurationByLabel(operator, clusterLabel);
      LOG.info(
          "Hadoop configuration loaded for HdfsConnection - operator: {}, clusterLabel: {}",
          operator,
          clusterLabel);
      fs = createFileSystem(operator, this.hadoopConf, cache);

      long duration = System.currentTimeMillis() - startTime;
      LOG.info(
          "HdfsConnection created successfully - operator: {}, clusterLabel: {}, duration: {}",
          operator,
          clusterLabel,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration));
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOG.error(
          "Failed to create HdfsConnection - operator: {}, clusterLabel: {}, duration: {}",
          operator,
          clusterLabel,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration),
          e);
      throw e;
    }
  }

  public HdfsConnection(
      String scheme, String operator, Map<String, String> configuration, boolean cache) {
    long startTime = System.currentTimeMillis();
    LOG.info(
        "Creating HdfsConnection with custom config - scheme: {}, operator: {}, cache: {}, configSize: {}",
        scheme,
        operator,
        cache,
        configuration != null ? configuration.size() : 0);

    try {
      if (Objects.nonNull(configuration)) {
        hadoopConf = new Configuration();
        configuration.forEach(hadoopConf::set);
      } else {
        hadoopConf = HDFSUtils.getConfiguration(operator);
      }
      fs = createFileSystem(operator, this.hadoopConf, cache);

      long duration = System.currentTimeMillis() - startTime;
      LOG.info(
          "HdfsConnection with custom config created successfully - operator: {}, duration: {}",
          operator,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration));
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOG.error(
          "Failed to create HdfsConnection with custom config - operator: {}, duration: {}",
          operator,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration),
          e);
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    long startTime = System.currentTimeMillis();
    LOG.info("Closing HdfsConnection");

    try {
      this.fs.close();

      long duration = System.currentTimeMillis() - startTime;
      LOG.info(
          "HdfsConnection closed successfully - duration: {}",
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration));
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOG.error(
          "Failed to close HdfsConnection - duration: {}",
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration),
          e);
      throw e;
    }
  }

  /**
   * Get schema value
   *
   * @return schema
   */
  public String getSchema() {
    return fs.getScheme();
  }

  /**
   * Get hadoop configuration
   *
   * @return configuration
   */
  public Configuration getConfiguration() {
    return this.hadoopConf;
  }

  /**
   * Get file system
   *
   * @return file system
   */
  public FileSystem getFileSystem() {
    return this.fs;
  }
  /**
   * Create file system
   *
   * @param operator operator
   * @param hadoopConf hadoop conf
   * @param cache cache
   * @return file system
   */
  private FileSystem createFileSystem(String operator, Configuration hadoopConf, boolean cache) {
    long startTime = System.currentTimeMillis();
    LOG.info("Creating FileSystem for HdfsConnection - operator: {}, cache: {}", operator, cache);

    try {
      if (!cache) {
        hadoopConf.set("fs.hdfs.impl.disable.cache", "true");
      }
      FileSystem fs = HDFSUtils.createFileSystem(operator, hadoopConf);

      long duration = System.currentTimeMillis() - startTime;
      LOG.info(
          "FileSystem created for HdfsConnection - operator: {}, duration: {}",
          operator,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration));
      return fs;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOG.error(
          "Failed to create FileSystem for HdfsConnection - operator: {}, duration: {}",
          operator,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration),
          e);
      throw e;
    }
  }
}
