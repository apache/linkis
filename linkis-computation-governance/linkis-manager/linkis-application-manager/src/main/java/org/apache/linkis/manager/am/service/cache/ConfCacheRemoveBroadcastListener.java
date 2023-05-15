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

package org.apache.linkis.manager.am.service.cache;

import org.apache.linkis.governance.common.protocol.conf.RemoveCacheConfRequest;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfig;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfigWithGlobalConfig;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryGlobalConfig;
import org.apache.linkis.protocol.BroadcastProtocol;
import org.apache.linkis.rpc.BroadcastListener;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.interceptor.common.CacheableRPCInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConfCacheRemoveBroadcastListener implements BroadcastListener {
  private static final Logger logger =
      LoggerFactory.getLogger(ConfCacheRemoveBroadcastListener.class);

  @Autowired private CacheableRPCInterceptor cacheableRPCInterceptor;

  @Override
  public void onBroadcastEvent(BroadcastProtocol protocol, Sender sender) {
    if (protocol instanceof RemoveCacheConfRequest) {
      RemoveCacheConfRequest removeCacheConfRequest = (RemoveCacheConfRequest) protocol;
      if (removeCacheConfRequest.userCreatorLabel() != null) {
        if (removeCacheConfRequest.engineTypeLabel() != null) {
          RequestQueryEngineConfig request =
              new RequestQueryEngineConfig(
                  removeCacheConfRequest.userCreatorLabel(),
                  removeCacheConfRequest.engineTypeLabel(),
                  null);
          RequestQueryEngineConfigWithGlobalConfig globalRequest =
              new RequestQueryEngineConfigWithGlobalConfig(
                  removeCacheConfRequest.userCreatorLabel(), request.engineTypeLabel(), null);
          cacheableRPCInterceptor.removeCache(request.toString());
          cacheableRPCInterceptor.removeCache(globalRequest.toString());
          logger.info(
              String.format(
                  "success to clear cache about configuration of %s-%s",
                  removeCacheConfRequest.engineTypeLabel().getStringValue(),
                  removeCacheConfRequest.userCreatorLabel().getStringValue()));
        } else {
          RequestQueryGlobalConfig request =
              new RequestQueryGlobalConfig(removeCacheConfRequest.userCreatorLabel().getUser());
          cacheableRPCInterceptor.removeCache(request.toString());
          logger.info(
              String.format(
                  "success to clear cache about global configuration of %s",
                  removeCacheConfRequest.userCreatorLabel().getUser()));
        }
      }
    }
  }
}
