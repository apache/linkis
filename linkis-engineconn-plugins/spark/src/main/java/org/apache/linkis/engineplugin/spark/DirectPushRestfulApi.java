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

package org.apache.linkis.engineplugin.spark;

import org.apache.linkis.engineplugin.spark.utils.DataFrameResponse;
import org.apache.linkis.engineplugin.spark.utils.DirectPushCache;
import org.apache.linkis.server.Message;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "DirectPush")
@RestController
@RequestMapping(path = "directpush")
public class DirectPushRestfulApi {
  private static final Logger logger = LoggerFactory.getLogger(DirectPushRestfulApi.class);

  @RequestMapping(path = "pull", method = RequestMethod.POST)
  public Message getDirectPushResult(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    Message message = null;
    try {
      String taskId = (String) json.getOrDefault("taskId", null);
      if (taskId == null) {
        message = Message.error("taskId is null");
        return message;
      }
      int fetchSize = (int) json.getOrDefault("fetchSize", 1000);

      DataFrameResponse response = DirectPushCache.fetchResultSetOfDataFrame(taskId, fetchSize);
      if (response.dataFrame() == null) {
        message = Message.error("No result found for taskId: " + taskId);
      } else {
        message =
            Message.ok()
                .data("data", response.dataFrame())
                .data("hasMoreData", response.hasMoreData());
      }
    } catch (Exception e) {
      logger.error("Failed to get direct push result", e);
      message = Message.error("Failed to get direct push result: " + e.getMessage());
    }
    return message;
  }
}
