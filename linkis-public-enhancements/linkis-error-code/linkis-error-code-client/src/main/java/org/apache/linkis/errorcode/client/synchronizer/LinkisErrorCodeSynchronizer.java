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

package org.apache.linkis.errorcode.client.synchronizer;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.errorcode.client.ErrorCodeClientBuilder;
import org.apache.linkis.errorcode.client.LinkisErrorCodeClient;
import org.apache.linkis.errorcode.common.LinkisErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisErrorCodeSynchronizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeSynchronizer.class);

  private LinkisErrorCode errorCode =
      new LinkisErrorCode(
          "60001", "会话创建失败，%s队列不存在，请检查队列设置是否正确", "queue (\\S+) does not exist in YARN", 0);

  private List<LinkisErrorCode> linkisErrorCodeList = Arrays.asList(errorCode);

  private final Object lock = new Object();

  private static final long PERIOD =
      CommonVars.apply("wds.linkis.errorcode.period.time", 1L).getValue();

  private static LinkisErrorCodeSynchronizer linkisErrorCodeSynchronizer;

  /** 一个同步器用一个client就行,不用进行关闭 */
  private final LinkisErrorCodeClient errorCodeClient =
      new ErrorCodeClientBuilder().setVersion("v1").build();

  private LinkisErrorCodeSynchronizer() {
    init();
  }

  private void init() {
    LOGGER.info("start to get errorcodes from linkis server");
    Utils.defaultScheduler()
        .scheduleAtFixedRate(
            () -> {
              LOGGER.info("start to get errorcodes from linkis server");
              synchronized (lock) {
                List<LinkisErrorCode> copyErrorCodes = new ArrayList<>(linkisErrorCodeList);
                try {
                  List<LinkisErrorCode> tmpList = errorCodeClient.getErrorCodesFromServer();
                  if (null != tmpList && !tmpList.isEmpty()) {
                    linkisErrorCodeList = tmpList;
                  } else {
                    LOGGER.warn("Got empty errorCodeList.");
                  }
                } catch (Throwable t) {
                  LOGGER.error("Failed to get ErrorCodes from linkis server", t);
                  linkisErrorCodeList = copyErrorCodes;
                }
              }
            },
            0L,
            1,
            TimeUnit.HOURS);
  }

  public static LinkisErrorCodeSynchronizer getInstance() {
    if (linkisErrorCodeSynchronizer == null) {
      synchronized (LinkisErrorCodeSynchronizer.class) {
        if (linkisErrorCodeSynchronizer == null) {
          linkisErrorCodeSynchronizer = new LinkisErrorCodeSynchronizer();
        }
      }
    }
    return linkisErrorCodeSynchronizer;
  }

  public List<LinkisErrorCode> synchronizeErrorCodes() {
    return this.linkisErrorCodeList;
  }
}
