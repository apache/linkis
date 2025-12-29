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

package org.apache.linkis.bml.common;

import org.apache.linkis.bml.conf.BmlServerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceHelperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelperFactory.class);

  private static final String FILESYSTEM_TYPE =
      BmlServerConfiguration.BML_FILESYSTEM_TYPE().getValue();

  private static final ResourceHelper HDFS_RESOURCE_HELPER = new HdfsResourceHelper();

  private static final ResourceHelper LOCAL_RESOURCE_HELPER = new LocalResourceHelper();

  private static final ResourceHelper S3_RESOURCE_HELPER = new S3ResourceHelper();

  public static ResourceHelper getResourceHelper() {
    if (FILESYSTEM_TYPE.equals("hdfs")) {
      LOGGER.info("will store resource in hdfs");
      return HDFS_RESOURCE_HELPER;
    } else if (FILESYSTEM_TYPE.equals("s3")) {
      LOGGER.info("will store resource in s3");
      return S3_RESOURCE_HELPER;
    } else {
      LOGGER.info("will store resource in local");
      return LOCAL_RESOURCE_HELPER;
    }
  }
}
