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

package org.apache.linkis.ecm.scheduled;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EcmClearTask {

  private Logger logger = LoggerFactory.getLogger(EcmClearTask.class);

  public static final String shellPath = Configuration.getLinkisHome() + "/admin/";

  private class CleanExpiredThread implements Runnable {
    @Override
    public void run() {
      logger.info("Start to linkis-ec-clear shell");
      List<String> cmdlist = new ArrayList<>();
      cmdlist.add("sh");
      cmdlist.add(shellPath + "linkis-ec-clear.sh");
      try {
        Utils.exec(cmdlist.toArray(new String[0]), 3000L);
      } catch (Exception e) {
        logger.warn("Shell linkis-ec-clear.sh execution failed, msg:" + e.getMessage());
      }
      logger.info("End to linkis-ec-clear shell");
    }
  }

  @PostConstruct
  public void init() {
    logger.info("Schedule Task is init");
    CleanExpiredThread cleanExpiredThread = new CleanExpiredThread();
    // Once a day
    Utils.defaultScheduler()
        .scheduleAtFixedRate(cleanExpiredThread, 10, 24 * 60 * 60, TimeUnit.SECONDS);
  }
}
