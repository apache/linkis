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

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext;
import org.apache.linkis.instance.label.client.InstanceLabelClient;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.constant.LabelValueConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.label.InsLabelRefreshRequest;
import org.apache.linkis.protocol.label.InsLabelRemoveRequest;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.SchedulerContext;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
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
  private EntranceServer entranceServer;

  @Autowired
  public void setEntranceServer(EntranceServer entranceServer) {
    this.entranceServer = entranceServer;
  }

  private static Boolean offlineFlag = false;

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
    logger.info("Prepare to modify the routelabel of entrance to offline");
    labels.put(LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE);
    InsLabelRefreshRequest insLabelRefreshRequest = new InsLabelRefreshRequest();
    insLabelRefreshRequest.setLabels(labels);
    insLabelRefreshRequest.setServiceInstance(Sender.getThisServiceInstance());
    InstanceLabelClient.getInstance().refreshLabelsToInstance(insLabelRefreshRequest);
    synchronized (offlineFlag) {
      offlineFlag = true;
    }
    logger.info("Finished to modify the routelabel of entry to offline");

    logger.info("Prepare to update all not execution task instances to empty string");
    SchedulerContext schedulerContext =
        entranceServer.getEntranceContext().getOrCreateScheduler().getSchedulerContext();
    if (schedulerContext instanceof EntranceSchedulerContext) {
      ((EntranceSchedulerContext) schedulerContext).setOfflineFlag(true);
    }
    entranceServer.updateAllNotExecutionTaskInstances(true);
    logger.info("Finished to update all not execution task instances to empty string");

    return Message.ok();
  }

  @ApiOperation(
      value = "backonline",
      notes = "from offline status to recover",
      response = Message.class)
  @RequestMapping(path = "/backonline", method = RequestMethod.GET)
  public Message backOnline(HttpServletRequest req) {
    ModuleUserUtils.getOperationUser(req, "backonline");
    logger.info("Prepare to modify the routelabel of entrance to remove offline");
    InsLabelRemoveRequest insLabelRemoveRequest = new InsLabelRemoveRequest();
    insLabelRemoveRequest.setServiceInstance(Sender.getThisServiceInstance());
    InstanceLabelClient.getInstance().removeLabelsFromInstance(insLabelRemoveRequest);
    synchronized (offlineFlag) {
      offlineFlag = false;
    }
    logger.info("Finished to backonline");
    return Message.ok();
  }

  @ApiOperation(value = "isOnline", notes = "entrance isOnline", response = Message.class)
  @RequestMapping(path = "/isOnline", method = RequestMethod.GET)
  public Message isOnline(HttpServletRequest req) {
    String thisInstance = Sender.getThisInstance();
    ServiceInstance mainInstance = DataWorkCloudApplication.getServiceInstance();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setApplicationName(mainInstance.getApplicationName());
    serviceInstance.setInstance(thisInstance);
    List<Label<?>> labelFromInstance =
        InstanceLabelClient.getInstance().getLabelFromInstance(serviceInstance);
    boolean res = true;
    String offline = "offline";
    if (!CollectionUtils.isEmpty(labelFromInstance)) {
      for (Label label : labelFromInstance) {
        if (offline.equals(label.getValue())) {
          res = false;
        }
      }
    }
    logger.info("Whether Entrance is online: {}", res);
    return Message.ok().data("isOnline", res);
  }
}
