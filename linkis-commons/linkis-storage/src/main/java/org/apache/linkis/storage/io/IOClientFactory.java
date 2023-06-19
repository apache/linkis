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

package org.apache.linkis.storage.io;

import org.apache.linkis.storage.exception.StorageErrorException;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.MUST_REGISTER_TOC;

public class IOClientFactory {
  private static final Logger logger = LoggerFactory.getLogger(IOClientFactory.class);
  private static IOClient ioClient = null;

  private static final String SUCCESS = "SUCCESS";
  private static final String FAILED = "FAILED";

  public static IOClient getIOClient() throws StorageErrorException {
    if (ioClient == null) {
      throw new StorageErrorException(
          MUST_REGISTER_TOC.getErrorCode(), MUST_REGISTER_TOC.getErrorDesc());
    }
    return ioClient;
  }

  /**
   * This method is called when ioClient is initialized. ioClient初始化时会调用该方法
   *
   * @param client IOClient
   */
  public static void register(IOClient client) {
    ioClient = client;
    logger.debug("IOClient: {} registered", ioClient.toString());
  }

  public static String getFSId() {
    return UUID.randomUUID().toString();
  }
}
