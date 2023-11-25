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

package org.apache.linkis.entrance.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.scheduler.queue.ConsumerManager;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "entrance lable manager")
@RestController
@RequestMapping(path = "/entrance/operation/consumer")
public class EntranceConsumerRestfulApi {

  private EntranceServer entranceServer;

  private static final Logger logger = LoggerFactory.getLogger(EntranceConsumerRestfulApi.class);

  @Autowired
  public void setEntranceServer(EntranceServer entranceServer) {
    this.entranceServer = entranceServer;
  }

  @ApiOperation(value = "kill-consumer", notes = "kill consumer", response = Message.class)
  @RequestMapping(path = "/kill", method = RequestMethod.GET)
  public Message killConsumer(
      HttpServletRequest req, @RequestParam(value = "groupName") String groupName) {
    String operationUser = ModuleUserUtils.getOperationUser(req, "kill consumer");
    if (Configuration.isNotAdmin(operationUser)) {
      return Message.error("only admin can do this");
    }
    logger.info("user {} to kill consumer {}", operationUser, groupName);
    ConsumerManager consumerManager =
        entranceServer
            .getEntranceContext()
            .getOrCreateScheduler()
            .getSchedulerContext()
            .getOrCreateConsumerManager();
    consumerManager.destroyConsumer(groupName);
    logger.info("user {} finished to kill consumer {}", operationUser, groupName);
    return Message.ok();
  }

  @ApiOperation(value = "consumer-info", notes = "list consumers info", response = Message.class)
  @RequestMapping(path = "/info", method = RequestMethod.GET)
  public Message countConsumer(HttpServletRequest req) {
    String operationUser = ModuleUserUtils.getOperationUser(req, "kill consumer");
    if (Configuration.isNotAdmin(operationUser)) {
      return Message.error("only admin can do this");
    }
    ConsumerManager consumerManager =
        entranceServer
            .getEntranceContext()
            .getOrCreateScheduler()
            .getSchedulerContext()
            .getOrCreateConsumerManager();
    return Message.ok().data("consumerNum", consumerManager.listConsumers().length);
  }
}
