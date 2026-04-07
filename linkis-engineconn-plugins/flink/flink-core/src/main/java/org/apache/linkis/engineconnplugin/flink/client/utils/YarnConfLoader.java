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

package org.apache.linkis.engineconnplugin.flink.client.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnConfLoader {
  private static final Logger LOG = LoggerFactory.getLogger(YarnConfLoader.class);

  public static YarnConfiguration getYarnConf(String yarnConfDir) {
    long startTime = System.currentTimeMillis();
    LOG.info("Loading Yarn configuration - yarnConfDir: {}", yarnConfDir);

    YarnConfiguration yarnConf = new YarnConfiguration();
    try {
      File dir = new File(yarnConfDir);
      if (dir.exists() && dir.isDirectory()) {
        File[] xmlFileList =
            new File(yarnConfDir)
                .listFiles(
                    (dir1, name) -> {
                      if (name.endsWith(".xml")) {
                        return true;
                      }
                      return false;
                    });
        if (xmlFileList != null) {
          LOG.info("Found {} XML configuration files in {}", xmlFileList.length, yarnConfDir);
          for (File xmlFile : xmlFileList) {
            LOG.debug("Adding Yarn configuration file: {}", xmlFile.getName());
            yarnConf.addResource(xmlFile.toURI().toURL());
          }
        } else {
          LOG.warn("No XML configuration files found in {}", yarnConfDir);
        }
      } else {
        LOG.warn(
            "Yarn configuration directory does not exist or is not a directory: {}", yarnConfDir);
      }
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOG.error(
          "Failed to load Yarn configuration - yarnConfDir: {}, duration: {}",
          yarnConfDir,
          org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration),
          e);
      throw new RuntimeException(e);
    }

    haYarnConf(yarnConf);

    long duration = System.currentTimeMillis() - startTime;
    LOG.info(
        "Yarn configuration loaded successfully - yarnConfDir: {}, duration: {}",
        yarnConfDir,
        org.apache.linkis.common.utils.ByteTimeUtils.msDurationToString(duration));
    return yarnConf;
  }

  private static Configuration haYarnConf(Configuration yarnConf) {
    Iterator<Map.Entry<String, String>> iterator = yarnConf.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String key = entry.getKey();
      String value = entry.getValue();
      if (key.startsWith("yarn.resourcemanager.hostname.")) {
        String rm = key.substring("yarn.resourcemanager.hostname.".length());
        String addressKey = "yarn.resourcemanager.address." + rm;
        if (yarnConf.get(addressKey) == null) {
          yarnConf.set(addressKey, value + ":" + YarnConfiguration.DEFAULT_RM_PORT);
        }
      }
    }
    return yarnConf;
  }
}
