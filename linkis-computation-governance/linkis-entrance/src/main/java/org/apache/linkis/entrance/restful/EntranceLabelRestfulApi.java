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
import org.apache.linkis.instance.label.client.InstanceLabelClient;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.constant.LabelValueConstant;
import org.apache.linkis.protocol.label.InsLabelRefreshRequest;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "entrance lable manager")
@RestController
@RequestMapping(path = "/entrance/operation/label")
public class EntranceLabelRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(EntranceLabelRestfulApi.class);

  @ApiOperation(value = "update", notes = "update route label", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/update", method = RequestMethod.POST)
  public Message updateRouteLabel(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
    String userName = ModuleUserUtils.getOperationUser(req, "updateRouteLabel");
    if (Configuration.isNotAdmin(userName)) {
      return Message.error("Non-administrators cannot update Route Label");
    }
    String routeLabel = jsonNode.get("routeLabel").textValue();
    Map<String, Object> labels = new HashMap<String, Object>();
    logger.info("Prepare to update entrance label {}", routeLabel);
    labels.put(LabelKeyConstant.ROUTE_KEY, routeLabel);
    InsLabelRefreshRequest insLabelRefreshRequest = new InsLabelRefreshRequest();
    insLabelRefreshRequest.setLabels(labels);
    insLabelRefreshRequest.setServiceInstance(Sender.getThisServiceInstance());
    InstanceLabelClient.getInstance().refreshLabelsToInstance(insLabelRefreshRequest);
    logger.info("Finished to update entrance label {}", routeLabel);
    return Message.ok();
  }

  @ApiOperation(value = "markoffline", notes = "add offline label", response = Message.class)
  @RequestMapping(path = "/markoffline", method = RequestMethod.GET)
  public Message updateRouteLabel(HttpServletRequest req) {
    ModuleUserUtils.getOperationUser(req, "markoffline");
    Map<String, Object> labels = new HashMap<String, Object>();
    logger.info("Prepare to modify the routelabel of entry to offline");
    labels.put(LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE);
    InsLabelRefreshRequest insLabelRefreshRequest = new InsLabelRefreshRequest();
    insLabelRefreshRequest.setLabels(labels);
    insLabelRefreshRequest.setServiceInstance(Sender.getThisServiceInstance());
    InstanceLabelClient.getInstance().refreshLabelsToInstance(insLabelRefreshRequest);
    logger.info("Finished to modify the routelabel of entry to offline");
    return Message.ok();
  }
}
