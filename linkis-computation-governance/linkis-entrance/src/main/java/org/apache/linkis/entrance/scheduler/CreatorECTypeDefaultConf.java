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

package org.apache.linkis.entrance.scheduler;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.utils.EntranceUtils;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfig;
import org.apache.linkis.governance.common.protocol.conf.ResponseQueryConfig;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.rpc.Sender;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import scala.Tuple2;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatorECTypeDefaultConf {

  private static final Logger logger = LoggerFactory.getLogger(CreatorECTypeDefaultConf.class);

  public static Sender confSender =
      Sender.getSender(
          Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());

  private static LoadingCache<String, Integer> confCache =
      CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(
              (long) EntranceConfiguration.ENTRANCE_CREATOR_JOB_LIMIT_CONF_CACHE().getValue(),
              TimeUnit.MINUTES)
          .build(
              new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) throws Exception {
                  Tuple2<UserCreatorLabel, EngineTypeLabel> tuple2 =
                      EntranceUtils.fromKeyGetLabels(key);
                  RequestQueryEngineConfig requestQueryEngineConfig =
                      new RequestQueryEngineConfig(tuple2._1, tuple2._2(), null);
                  int jobLimit =
                      (int) EntranceConfiguration.ENTRANCE_CREATOR_JOB_LIMIT().getValue();
                  try {
                    Object response = confSender.ask(requestQueryEngineConfig);
                    if (response instanceof ResponseQueryConfig) {
                      jobLimit =
                          (int)
                              EntranceConfiguration.ENTRANCE_CREATOR_JOB_LIMIT()
                                  .getValue(((ResponseQueryConfig) response).getKeyAndValue());
                    }
                  } catch (Exception e) {
                    logger.warn("Failed to get key {} from conf", key, e);
                  }
                  return jobLimit;
                }
              });

  public static int getCreatorECTypeMaxRunningJobs(String creator, String ecType) {
    int jobLimit = (int) EntranceConfiguration.ENTRANCE_CREATOR_JOB_LIMIT().getValue();
    if (StringUtils.isNoneBlank(creator, ecType)) {
      try {
        String key = EntranceUtils.getDefaultCreatorECTypeKey(creator, ecType);
        jobLimit = confCache.get(key);
      } catch (Exception e) {
        logger.warn("Failed to get key creator {} ecType {} from cache", creator, ecType, e);
      }
    }
    int entranceNumber = EntranceUtils.getRunningEntranceNumber();
    return jobLimit / entranceNumber;
  }
}
