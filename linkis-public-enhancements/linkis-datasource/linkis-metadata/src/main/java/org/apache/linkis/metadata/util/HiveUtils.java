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

package org.apache.linkis.metadata.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveUtils {

  static Logger logger = LoggerFactory.getLogger(HiveUtils.class);

  public static Configuration getDefaultConf(String userName) {
    Configuration conf = new Configuration();
    String hiveConfPath = DWSConfig.HIVE_CONF_DIR.getValue();
    if (StringUtils.isNotEmpty(hiveConfPath)) {
      logger.info("Load hive configuration from " + hiveConfPath);
      conf.addResource(new Path(hiveConfPath + File.separator + "hive-site.xml"));
    } else {
      conf.addResource("hive-site.xml");
    }
    return conf;
  }

  public static String decode(String str) {
    Base64.Decoder decoder = Base64.getMimeDecoder();
    String res = str;
    try {
      res = new String(decoder.decode(str));
    } catch (Throwable e) {
      logger.error(str + " decode failed", e);
    }
    return res;
  }
}
