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

package org.apache.linkis.instance.label.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.exception.InstanceErrorException;
import org.apache.linkis.instance.label.service.impl.DefaultInsLabelService;
import org.apache.linkis.instance.label.utils.EntityParser;
import org.apache.linkis.instance.label.vo.InstanceInfoVo;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import static org.apache.linkis.instance.label.errorcode.LinkisInstanceLabelErrorCodeSummary.INCLUDE_REPEAT;
import static org.apache.linkis.instance.label.errorcode.LinkisInstanceLabelErrorCodeSummary.ONLY_ADMIN_CAN_MODIFY;
import static org.apache.linkis.instance.label.errorcode.LinkisInstanceLabelErrorCodeSummary.ONLY_ADMIN_CAN_VIEW;

@Api(tags = "instance restful")
@RestController
@RequestMapping(path = "/microservice")
public class InstanceRestful {

  private static final Log logger = LogFactory.getLog(InstanceRestful.class);

  private LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Autowired private DefaultInsLabelService insLabelService;

  @ApiOperation(
      value = "listAllInstanceWithLabel",
      notes = "list all instance with label",
      response = Message.class)
  @RequestMapping(path = "/allInstance", method = RequestMethod.GET)
  public Message listAllInstanceWithLabel(HttpServletRequest req) throws Exception {
    String userName = ModuleUserUtils.getOperationUser(req, "listAllInstanceWithLabel");
    if (Configuration.isNotAdmin(userName)) {
      throw new InstanceErrorException(
          String.format(
              ONLY_ADMIN_CAN_VIEW.getErrorDesc() + "The user [%s] is not admin.", userName));
    }

    logger.info("start to get all instance informations.....");
    List<InstanceInfo> instances = insLabelService.listAllInstanceWithLabel();
    insLabelService.markInstanceLabel(instances);
    List<InstanceInfoVo> instanceVos = EntityParser.parseToInstanceVo(instances);
    logger.info("Done, all instance:" + instances);
    return Message.ok().data("instances", instanceVos);
  }

  @ApiOperation(
      value = "upDateInstanceLabel",
      notes = "up date instance label",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "applicationName", dataType = "String"),
    @ApiImplicitParam(name = "instance", required = false, dataType = "String", value = "instance"),
    @ApiImplicitParam(name = "labels", required = false, dataType = "List", value = "labels"),
    @ApiImplicitParam(name = "labelKey", dataType = "String"),
    @ApiImplicitParam(name = "stringValue", dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/instanceLabel", method = RequestMethod.PUT)
  public Message upDateInstanceLabel(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws Exception {
    String userName = ModuleUserUtils.getOperationUser(req, "upDateInstanceLabel");
    if (Configuration.isNotAdmin(userName)) {
      throw new InstanceErrorException(
          String.format(
              ONLY_ADMIN_CAN_MODIFY.getErrorDesc() + " The user [%s] is not admin", userName));
    }
    String instanceName = jsonNode.get("instance").asText();
    String instanceType = jsonNode.get("applicationName").asText();
    if (StringUtils.isEmpty(instanceName)) {
      return Message.error("instance cannot be empty(实例名不能为空");
    }
    if (StringUtils.isEmpty(instanceType)) {
      return Message.error("instance cannot be empty(实例类型不能为空");
    }
    JsonNode labelsNode = jsonNode.get("labels");
    Iterator<JsonNode> labelKeyIterator = labelsNode.iterator();
    ServiceInstance instance = ServiceInstance.apply(instanceType, instanceName);
    List<Label<? extends Label<?>>> labels = new ArrayList<>();
    Set<String> keyList = LabelUtils.listAllUserModifiableLabel();
    Set<String> labelKeySet = new HashSet<>();
    // Traverse all labelKeys and take out labelValue.
    while (labelKeyIterator.hasNext()) {
      JsonNode label = labelKeyIterator.next();
      String labelKey = label.get("labelKey").asText();
      String labelStringValue = label.get("stringValue").asText();
      if (labelStringValue != null && keyList.contains(labelKey)) {
        Label realLabel = labelBuilderFactory.createLabel(labelKey, labelStringValue);
        if (realLabel instanceof UserModifiable) {
          ((UserModifiable) realLabel).valueCheck(labelStringValue);
        }
        labelKeySet.add(labelKey);
        labels.add(realLabel);
      }
    }
    if (labelKeySet.size() != labels.size()) {
      throw new InstanceErrorException(INCLUDE_REPEAT.getErrorDesc());
    }
    insLabelService.refreshLabelsToInstance(labels, instance);
    InstanceInfo instanceInfo = insLabelService.getInstanceInfoByServiceInstance(instance);
    instanceInfo.setUpdateTime(new Date());
    insLabelService.updateInstance(instanceInfo);
    return Message.ok("success").data("labels", labels);
  }

  @ApiOperation(
      value = "listAllModifiableLabelKey",
      notes = "list all modifiable label key",
      response = Message.class)
  @RequestMapping(path = "/modifiableLabelKey", method = RequestMethod.GET)
  public Message listAllModifiableLabelKey(HttpServletRequest req) {
    ModuleUserUtils.getOperationUser(req, "upDateInstanceLabel");
    Set<String> keyList = LabelUtils.listAllUserModifiableLabel();
    return Message.ok().data("keyList", keyList);
  }

  @ApiOperation(value = "getServiceRegistryURL", response = Message.class)
  @RequestMapping(path = "/serviceRegistryURL", method = RequestMethod.GET)
  public Message getServiceRegistryURL(HttpServletRequest request) throws Exception {
    String serviceRegistryURL = insLabelService.getServiceRegistryURL();
    ModuleUserUtils.getOperationUser(request, "getServiceRegistryURL");
    return Message.ok().data("url", serviceRegistryURL);
  }
}
