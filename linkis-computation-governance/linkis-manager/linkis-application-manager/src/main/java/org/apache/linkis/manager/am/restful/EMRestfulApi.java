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

package org.apache.linkis.manager.am.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.governance.common.protocol.conf.TenantRequest;
import org.apache.linkis.governance.common.protocol.conf.TenantResponse;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.converter.DefaultMetricsConverter;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.manager.EngineNodeManager;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.service.em.ECMOperateService;
import org.apache.linkis.manager.am.service.em.EMInfoService;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.ConfigVo;
import org.apache.linkis.manager.am.vo.EMNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.protocol.OperateRequest$;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest$;
import org.apache.linkis.manager.common.protocol.em.ECMOperateResponse;
import org.apache.linkis.manager.exception.PersistenceErrorException;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;
import org.apache.linkis.manager.rm.restful.vo.UserResourceVo;
import org.apache.linkis.manager.rm.utils.RMUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "ECM(engineconnmanager) operation")
@RequestMapping(
    path = "/linkisManager",
    produces = {"application/json"})
@RestController
public class EMRestfulApi {

  public static final String KEY_TENANT = "tenant";

  @Autowired private EMInfoService emInfoService;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private DefaultMetricsConverter defaultMetricsConverter;

  @Autowired private EngineNodeManager engineNodeManager;

  @Autowired private ECMOperateService ecmOperateService;

  @Autowired private ECResourceInfoService ecResourceInfoService;

  @Autowired private ResourceManagerPersistence resourceManagerPersistence;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  private LabelBuilderFactory stdLabelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

  private String[] adminOperations = AMConfiguration.ECM_ADMIN_OPERATIONS().getValue().split(",");

  private void checkAdmin(String userName) throws AMErrorException {
    if (Configuration.isNotAdmin(userName)) {
      throw new AMErrorException(210003, "Only admin can modify ECMs(只有管理员才能修改ECM).");
    }
  }

  @ApiOperation(value = "listAllEMs", notes = "get all ECM service list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "instance", dataType = "String", value = "Instance"),
    @ApiImplicitParam(
        name = "nodeHealthy",
        dataType = "String",
        value = "node  healthy status",
        example = "Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable"),
    @ApiImplicitParam(name = "owner", dataType = "String", value = "Owner"),
    @ApiImplicitParam(name = "tenantLabel", dataType = "String", value = "tenantLabel like")
  })
  // todo add healthInfo
  @RequestMapping(path = "/listAllEMs", method = RequestMethod.GET)
  public Message listAllEMs(
      HttpServletRequest req,
      @RequestParam(value = "instance", required = false) String instance,
      @RequestParam(value = "nodeHealthy", required = false) String nodeHealthy,
      @RequestParam(value = "owner", required = false) String owner,
      @RequestParam(value = "tenantLabel", required = false) String tenantLabel)
      throws AMErrorException {
    checkAdmin(ModuleUserUtils.getOperationUser(req, "listAllEMs"));
    List<EMNodeVo> emNodeVos = AMUtils.copyToEMVo(emInfoService.getAllEM());
    if (CollectionUtils.isNotEmpty(emNodeVos)) {
      Stream<EMNodeVo> stream = emNodeVos.stream();
      if (StringUtils.isNotBlank(instance)) {
        stream = stream.filter(em -> em.getInstance().contains(instance));
      }
      if (StringUtils.isNotBlank(nodeHealthy)) {
        stream =
            stream.filter(
                em ->
                    em.getNodeHealthy() == null
                        || em.getNodeHealthy().equals(NodeHealthy.valueOf(nodeHealthy)));
      }
      if (StringUtils.isNotBlank(owner)) {
        stream = stream.filter(em -> em.getOwner().equalsIgnoreCase(owner));
      }

      if (StringUtils.isNotBlank(tenantLabel)) {
        stream =
            stream.filter(
                em -> {
                  List<Label> labels = em.getLabels();
                  labels =
                      labels.stream()
                          .filter(
                              label ->
                                  KEY_TENANT.equals(label.getLabelKey())
                                      && label.getStringValue().contains(tenantLabel))
                          .collect(Collectors.toList());
                  return labels.size() > 0 ? true : false;
                });
      }

      emNodeVos = stream.collect(Collectors.toList());

      // sort
      if (StringUtils.isNotBlank(tenantLabel)) {
        Collections.sort(
            emNodeVos,
            new Comparator<EMNodeVo>() {
              @Override
              public int compare(EMNodeVo a, EMNodeVo b) {
                String aLabelStr =
                    a.getLabels().stream()
                        .filter(label -> KEY_TENANT.equals(label.getLabelKey()))
                        .collect(Collectors.toList())
                        .get(0)
                        .getStringValue();
                String bLabelStr =
                    b.getLabels().stream()
                        .filter(label -> KEY_TENANT.equals(label.getLabelKey()))
                        .collect(Collectors.toList())
                        .get(0)
                        .getStringValue();
                return aLabelStr.compareTo(bLabelStr);
              }
            });
      } else {
        Collections.sort(emNodeVos, Comparator.comparing(EMNodeVo::getInstance));
      }
    }
    return Message.ok().data("EMs", emNodeVos);
  }

  @ApiOperation(
      value = "listAllECMHealthyStatus",
      notes = "get all ECM healthy status",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "onlyEditable", dataType = "Boolean", value = "only editable")
  })
  @RequestMapping(path = "/listAllECMHealthyStatus", method = RequestMethod.GET)
  public Message listAllNodeHealthyStatus(
      HttpServletRequest req,
      @RequestParam(value = "onlyEditable", required = false) Boolean onlyEditable) {
    NodeHealthy[] nodeHealthy = NodeHealthy.values();
    if (onlyEditable) {
      nodeHealthy =
          new NodeHealthy[] {
            NodeHealthy.Healthy,
            NodeHealthy.UnHealthy,
            NodeHealthy.WARN,
            NodeHealthy.StockAvailable,
            NodeHealthy.StockUnavailable
          };
    }
    return Message.ok().data("nodeHealthy", nodeHealthy);
  }

  @ApiOperation(value = "modifyEMInfo", notes = "modify ECM info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconnmanager"),
    @ApiImplicitParam(
        name = "emStatus",
        dataType = "String",
        example = "Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable"),
    @ApiImplicitParam(name = "instance", dataType = "String", example = "bdp110:9102"),
    @ApiImplicitParam(name = "labels", dataType = "List", value = "Labels"),
    @ApiImplicitParam(name = "labelKey", dataType = "String", example = "emInstance"),
    @ApiImplicitParam(
        name = "stringValue",
        dataType = "String",
        example = "linkis-cg-engineconn-bdp110:12295")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/modifyEMInfo", method = RequestMethod.PUT)
  @Transactional(rollbackFor = Exception.class)
  public Message modifyEMInfo(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException, LabelErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "modifyEMInfo");
    checkAdmin(username);
    String applicationName = jsonNode.get("applicationName").asText();
    String instance = jsonNode.get("instance").asText();
    if (StringUtils.isEmpty(applicationName)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getErrorCode(),
          "applicationName cannot be null(请求参数applicationName不能为空)");
    }
    if (StringUtils.isEmpty(instance)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getErrorCode(), "instance cannot be null(请求参数instance不能为空)");
    }
    ServiceInstance serviceInstance = ServiceInstance.apply(applicationName, instance);
    if (serviceInstance == null) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getErrorCode(),
          "serviceInstance:" + applicationName + " non-existent(" + applicationName + ")");
    }
    String healthyStatus = jsonNode.get("emStatus").asText();
    if (healthyStatus != null) {
      NodeHealthyInfo nodeHealthyInfo = new NodeHealthyInfo();
      nodeHealthyInfo.setNodeHealthy(NodeHealthy.valueOf(healthyStatus));
      emInfoService.updateEMInfo(serviceInstance, nodeHealthyInfo);
    }
    JsonNode labels = jsonNode.get("labels");
    Set<String> labelKeySet = new HashSet<>();
    if (labels != null) {
      ArrayList<Label<?>> newLabelList = new ArrayList<>();
      Iterator<JsonNode> iterator = labels.iterator();
      while (iterator.hasNext()) {
        JsonNode label = iterator.next();
        String labelKey = label.get("labelKey").asText();
        String stringValue = label.get("stringValue").asText();
        Label newLabel = stdLabelBuilderFactory.createLabel(labelKey, stringValue);
        if (newLabel instanceof UserModifiable) {
          ((UserModifiable) newLabel).valueCheck(stringValue);
        }
        labelKeySet.add(labelKey);
        newLabelList.add(newLabel);
      }
      if (labelKeySet.size() != newLabelList.size()) {
        throw new AMErrorException(
            210003, "Failed to update label, include repeat labels(更新label失败，包含重复label)");
      }
      nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
      logger.info("success to update label of instance: " + serviceInstance.getInstance());
    }
    return Message.ok("success");
  }

  @ApiOperation(
      value = "executeECMOperationByEC",
      notes = "EC execute ECM operation",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/executeECMOperationByEC", method = RequestMethod.POST)
  public Message executeECMOperationByEC(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {

    ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
    String userName = ModuleUserUtils.getOperationUser(req, "executeECMOperationByEC");
    logger.info(
        "User {} try to execute ECM Operation by EngineConn {}.", userName, serviceInstance);
    EngineNode engineNode = engineNodeManager.getEngineNode(serviceInstance);
    Map<String, Object> parameters;
    try {
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    }
    parameters.put(ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY(), serviceInstance.getInstance());
    if (!userName.equals(engineNode.getOwner()) && Configuration.isNotAdmin(userName)) {
      return Message.error(
          "You have no permission to execute ECM Operation by this EngineConn " + serviceInstance);
    }
    return executeECMOperation(
        engineNode.getEMNode(),
        engineNode.getServiceInstance().getInstance(),
        new ECMOperateRequest(userName, parameters));
  }

  @ApiOperation(
      value = "executeECMOperation",
      notes = "execute ECM operation",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/executeECMOperation", method = RequestMethod.POST)
  public Message executeECMOperation(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "executeECMOperation");
    ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
    logger.info("User {} try to execute ECM Operation with {}.", userName, serviceInstance);
    EMNode ecmNode = this.emInfoService.getEM(serviceInstance);
    Map<String, Object> parameters;
    try {
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    }
    if (parameters.containsKey("engineConnInstance")) {
      return executeECMOperation(
          ecmNode,
          parameters.get("engineConnInstance").toString(),
          new ECMOperateRequest(userName, parameters));
    } else {
      return executeECMOperation(ecmNode, "", new ECMOperateRequest(userName, parameters));
    }
  }

  @ApiOperation(value = "openEngineLog", notes = "open Engine log", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconn"),
    @ApiImplicitParam(name = "emInstance", dataType = "String", example = "bdp110:9100"),
    @ApiImplicitParam(name = "instance", dataType = "String", example = "bdp110:21976"),
    @ApiImplicitParam(name = "parameters", dataType = "Map", value = "Parameters"),
    @ApiImplicitParam(name = "logType", dataType = "String", example = "stdout"),
    @ApiImplicitParam(name = "fromLine", dataType = "String", example = "0"),
    @ApiImplicitParam(name = "pageSize", dataType = "String", defaultValue = "1000"),
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/openEngineLog", method = RequestMethod.POST)
  public Message openEngineLog(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "openEngineLog");
    EMNode ecmNode;
    Map<String, Object> parameters;
    String engineInstance;
    try {
      String emInstance = jsonNode.get("emInstance").asText();
      engineInstance = jsonNode.get("instance").asText();
      ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
      logger.info("User {} try to open engine: {} log.", userName, serviceInstance);
      ecmNode =
          this.emInfoService.getEM(
              ServiceInstance.apply("linkis-cg-engineconnmanager", emInstance));
      logger.info("ecm node info:{}", ecmNode);
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
      String logType = (String) parameters.get("logType");
      if (!logType.equals("stdout")
          && !logType.equals("stderr")
          && !logType.equals("gc")
          && !logType.equals("yarnApp")) {
        throw new AMErrorException(
            AMErrorCode.PARAM_ERROR.getErrorCode(), AMErrorCode.PARAM_ERROR.getErrorDesc());
      }
      parameters.put(OperateRequest$.MODULE$.OPERATOR_NAME_KEY(), "engineConnLog");
      parameters.put(ECMOperateRequest$.MODULE$.ENGINE_CONN_INSTANCE_KEY(), engineInstance);
      if (!parameters.containsKey("enableTail")) {
        parameters.put("enableTail", true);
      }
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    } catch (Exception e) {
      logger.error("Failed to open engine log, error:", e);
      return Message.error(e.getMessage());
    }
    return executeECMOperation(
        ecmNode, engineInstance, new ECMOperateRequest(userName, parameters));
  }

  private Message executeECMOperation(
      EMNode ecmNode, String engineInstance, ECMOperateRequest ecmOperateRequest) {
    if (Objects.isNull(ecmNode)) {
      return Message.error(
          MessageFormat.format(
              "ECM node :[{0}]  does not exist, Unable to open engine log(ECM节点:[{1}] 异常，无法打开日志，可能是该节点服务重启或者服务异常导致)",
              engineInstance, engineInstance));
    }
    String operationName = OperateRequest$.MODULE$.getOperationName(ecmOperateRequest.parameters());
    String userName = ecmOperateRequest.user();
    if (ArrayUtils.contains(adminOperations, operationName) && Configuration.isNotAdmin(userName)) {
      logger.warn(
          "User {} has no permission to execute {} admin Operation in ECM {}.",
          userName,
          operationName,
          ecmNode.getServiceInstance());
      return Message.error(
          "You have no permission to execute "
              + operationName
              + " admin Operation in ECM "
              + ecmNode.getServiceInstance());
    }

    // fill in logDirSuffix
    if (StringUtils.isNotBlank(engineInstance)
        && Objects.isNull(ecmOperateRequest.parameters().get("logDirSuffix"))) {
      ECResourceInfoRecord ecResourceInfoRecord =
          ecResourceInfoService.getECResourceInfoRecordByInstance(engineInstance);
      if (Objects.isNull(ecResourceInfoRecord)) {
        return Message.error("EC instance: " + engineInstance + " not exist ");
      }
      // eg logDirSuffix -> root/20230705/io_file/6d48068a-0e1e-44b5-8eb2-835034db5b30/logs
      String logDirSuffix = ecResourceInfoRecord.getLogDirSuffix();
      if (!userName.equals(ecResourceInfoRecord.getCreateUser())
          && Configuration.isNotJobHistoryAdmin(userName)) {
        logger.warn(
            "User {} has no permission to get log with path: {} in ECM:{}.",
            userName,
            logDirSuffix,
            ecmNode.getServiceInstance());
        return Message.error(
            "You have no permission to get log with path:"
                + logDirSuffix
                + " in ECM:"
                + ecmNode.getServiceInstance());
      }
      ecmOperateRequest.parameters().put("logDirSuffix", logDirSuffix);
    }

    ECMOperateResponse engineOperateResponse =
        ecmOperateService.executeOperation(ecmNode, ecmOperateRequest);

    return Message.ok()
        .data("result", engineOperateResponse.getResult())
        .data("errorMsg", engineOperateResponse.errorMsg())
        .data("isError", engineOperateResponse.isError());
  }

  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/taskprediction", method = RequestMethod.GET)
  public Message taskprediction(
      HttpServletRequest req,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "queueName", required = false) String queueName,
      @RequestParam(value = "tenant", required = false) String tenant)
      throws PersistenceErrorException {
    //    String userName = ModuleUserUtils.getOperationUser(req, "taskprediction");
    String tokenName = "";
    if (StringUtils.isBlank(username)) {
      username = tokenName;
    }
    if (StringUtils.isBlank(engineType)) {
      Message.error("parameters:engineType can't be null (请求参数【engineType】不能为空)");
    }
    if (StringUtils.isBlank(creator)) {
      Message.error("parameters:creator can't be null (请求参数【creator】不能为空)");
    }
    // 获取yarn资源数据和用户资源数据
    String labelValuePattern =
        MessageFormat.format("%{0}%,%{1}%,%{2}%,%", creator, username, engineType);
    List<PersistenceLabelRel> userLabels =
        labelManagerPersistence.getLabelByPattern(
            labelValuePattern, RMUtils.getCombinedLabel(), 0, 0);
    List<PersistenceResource> resources =
        resourceManagerPersistence.getResourceByLabels(userLabels);
    ArrayList<UserResourceVo> userResources = RMUtils.getUserResources(userLabels, resources);

    // 获取租户标签数据
    if (StringUtils.isBlank(tenant)) {
      Sender sender =
          Sender.getSender(
              Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());
      TenantResponse response = (TenantResponse) sender.ask(new TenantRequest(username, creator));
      if (StringUtils.isBlank(response.tenant())) {
        response = (TenantResponse) sender.ask(new TenantRequest(username, "*"));
        if (StringUtils.isBlank(response.tenant())) {
          response = (TenantResponse) sender.ask(new TenantRequest("*", creator));
        }
      }
      tenant = response.tenant();
    }

    // 获取ecm列表数据
    List<EMNodeVo> emNodeVos = AMUtils.copyToEMVo(emInfoService.getAllEM());
    String finalTenant = tenant;
    List<EMNodeVo> collect =
        emNodeVos.stream()
            .filter(
                emNodeVo -> {
                  Stream<Label> labelStream = emNodeVo.getLabels().stream();
                  if (StringUtils.isNotBlank(finalTenant)) {
                    return labelStream.anyMatch(
                        label ->
                            KEY_TENANT.equals(label.getLabelKey())
                                && label.getStringValue().contains(finalTenant));
                  } else {
                    return labelStream.noneMatch(label -> KEY_TENANT.equals(label.getLabelKey()));
                  }
                })
            .collect(Collectors.toList());

    // 获取配置值
    String responseStr = "";
    List<ConfigVo> configlist = new ArrayList<>();
    try {
      HttpClient httpClient = HttpClients.createDefault();
      String url =
          MessageFormat.format(
              "/api/rest_j/v1/configuration/getFullTreesByAppName?creator={0}&engineType={1}",
              creator, engineType);
      HttpGet httpGet = new HttpGet(Configuration.getGateWayURL() + url);
      httpGet.addHeader("Token-User", username);
      httpGet.addHeader("Token-Code", "BML-AUTH");
      responseStr = EntityUtils.toString(httpClient.execute(httpGet).getEntity());
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode fullTree = objectMapper.readTree(responseStr).get("data").get("fullTree");
      for (JsonNode node : fullTree) {
        JsonNode settingsList = node.get("settings");
        for (JsonNode key : settingsList) {
          configlist.add(
              new ConfigVo(
                  key.get("key").asText(),
                  key.get("configValue").asText(),
                  key.get("defaultValue").asText()));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Message.ok()
        .data("test", userResources)
        .data("tenant", tenant)
        .data("test3", collect)
        .data("test4", configlist);
  }
}
