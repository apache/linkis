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

package org.apache.linkis.manager.rm.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.common.utils.ResourceUtils;
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.external.domain.ExternalAppInfo;
import org.apache.linkis.manager.rm.external.service.ExternalResourceService;
import org.apache.linkis.manager.rm.external.yarn.YarnAppInfo;
import org.apache.linkis.manager.rm.external.yarn.YarnResourceIdentifier;
import org.apache.linkis.manager.rm.restful.vo.UserCreatorEngineType;
import org.apache.linkis.manager.rm.restful.vo.UserResourceVo;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.manager.rm.service.impl.UserResourceService;
import org.apache.linkis.manager.rm.utils.RMUtils;
import org.apache.linkis.manager.rm.utils.UserConfiguration;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import scala.Option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.ONLY_ADMIN_READ;
import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.ONLY_ADMIN_RESET;

@Api(tags = {"resource management"})
@RestController
@RequestMapping(path = {"/linkisManager/rm"})
public class RMMonitorRest {

  private static final Logger logger = LoggerFactory.getLogger(RMMonitorRest.class);

  private final ObjectMapper mapper = new ObjectMapper();
  private final ThreadLocal<SimpleDateFormat> dateFormatLocal =
      new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        }
      };
  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();
  private final CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();
  private final com.google.gson.Gson gson = BDPJettyServerHelper.gson();

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private ResourceManagerPersistence resourceManagerPersistence;

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired private ExternalResourceService externalResourceService;

  @Autowired private LabelResourceService labelResourceService;

  @Autowired private ResourceManager resourceManager;

  @Autowired private UserResourceService userResourceService;

  private String COMBINED_USERCREATOR_ENGINETYPE;

  public RMMonitorRest() {
    dateFormatLocal.get().setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  Message appendMessageData(Message message, String key, Object value) {
    try {
      message = message.data(key, mapper.readTree(ResourceUtils.toJSONString(value)));
    } catch (JsonProcessingException e) {
      //
    }
    return message;
  }

  @ApiOperation(value = "getApplicationList", notes = "get applicationList")
  @RequestMapping(path = "applicationlist", method = RequestMethod.POST)
  public Message getApplicationList(
      HttpServletRequest request, @RequestBody Map<String, Object> param) {
    Message message = Message.ok("");
    String userName = ModuleUserUtils.getOperationUser(request, "applicationlist");
    String userCreator =
        param.get("userCreator") == null ? null : (String) param.get("userCreator");
    String engineType = param.get("engineType") == null ? null : (String) param.get("engineType");
    EngineNode[] nodes = getEngineNodes(userName, true);
    Map<String, Map<String, Object>> creatorToApplicationList = new HashMap<>();
    Arrays.stream(nodes)
        .forEach(
            node -> {
              UserCreatorLabel userCreatorLabel =
                  (UserCreatorLabel)
                      node.getLabels().stream()
                          .filter(label -> label instanceof UserCreatorLabel)
                          .findFirst()
                          .get();
              EngineTypeLabel engineTypeLabel =
                  (EngineTypeLabel)
                      node.getLabels().stream()
                          .filter(label -> label instanceof EngineTypeLabel)
                          .findFirst()
                          .get();

              if (getUserCreator(userCreatorLabel).equals(userCreator)) {
                if (engineType == null || getEngineType(engineTypeLabel).equals(engineType)) {
                  if (!creatorToApplicationList.containsKey(userCreatorLabel.getCreator())) {
                    Map<String, Object> applicationList = new HashMap<>();
                    applicationList.put("engineInstances", new ArrayList<>());
                    applicationList.put(
                        "usedResource", Resource.initResource(ResourceType.LoadInstance));
                    applicationList.put(
                        "maxResource", Resource.initResource(ResourceType.LoadInstance));
                    applicationList.put(
                        "minResource", Resource.initResource(ResourceType.LoadInstance));
                    applicationList.put(
                        "lockedResource", Resource.initResource(ResourceType.LoadInstance));
                    creatorToApplicationList.put(userCreatorLabel.getCreator(), applicationList);
                  }

                  Map<String, Object> applicationList =
                      creatorToApplicationList.get(userCreatorLabel.getCreator());

                  applicationList.put(
                      "usedResource",
                      ((applicationList.get("usedResource") == null)
                              ? Resource.initResource(ResourceType.LoadInstance)
                              : (Resource) applicationList.get("usedResource"))
                          .add(node.getNodeResource().getUsedResource()));
                  applicationList.put(
                      "maxResource",
                      ((applicationList.get("maxResource") == null)
                              ? Resource.initResource(ResourceType.LoadInstance)
                              : (Resource) applicationList.get("maxResource"))
                          .add(node.getNodeResource().getMaxResource()));
                  applicationList.put(
                      "minResource",
                      ((applicationList.get("minResource") == null)
                              ? Resource.initResource(ResourceType.LoadInstance)
                              : (Resource) applicationList.get("minResource"))
                          .add(node.getNodeResource().getMinResource()));
                  applicationList.put(
                      "lockedResource",
                      ((applicationList.get("lockedResource") == null)
                              ? Resource.initResource(ResourceType.LoadInstance)
                              : (Resource) applicationList.get("lockedResource"))
                          .add(node.getNodeResource().getLockedResource()));
                  Map<String, Object> engineInstance = new HashMap<>();
                  engineInstance.put("creator", userCreatorLabel.getCreator());
                  engineInstance.put("engineType", engineTypeLabel.getEngineType());
                  engineInstance.put("instance", node.getServiceInstance().getInstance());
                  engineInstance.put("label", engineTypeLabel.getStringValue());
                  node.setNodeResource(
                      ResourceUtils.convertTo(node.getNodeResource(), ResourceType.LoadInstance));
                  engineInstance.put("resource", node.getNodeResource());
                  engineInstance.put(
                      "status",
                      (node.getNodeStatus() == null) ? "Busy" : node.getNodeStatus().toString());
                  engineInstance.put(
                      "startTime", dateFormatLocal.get().format(node.getStartTime()));
                  engineInstance.put("owner", node.getOwner());
                  ((List) applicationList.get("engineInstances")).add(engineInstance);
                }
              }
            });
    List<Map<String, Object>> applications =
        creatorToApplicationList.entrySet().stream()
            .map(
                creatorEntry -> {
                  Map<String, Object> application = new HashMap<>();
                  application.put("creator", creatorEntry.getKey());
                  application.put("applicationList", creatorEntry.getValue());
                  return application;
                })
            .collect(Collectors.toList());
    appendMessageData(message, "applications", applications);
    return message;
  }

  @ApiOperation(value = "resetUserResource", notes = "reset user resource")
  @RequestMapping(path = "resetResource", method = RequestMethod.DELETE)
  public Message resetUserResource(
      HttpServletRequest request,
      @RequestParam(value = "resourceId", required = false) Integer resourceId) {
    Option<String> queryUser = SecurityFilter.getLoginUser(request);
    if (!Configuration.isAdmin(queryUser.get())) {
      throw new RMWarnException(ONLY_ADMIN_RESET.getErrorCode(), ONLY_ADMIN_RESET.getErrorDesc());
    }

    if (resourceId == null || resourceId <= 0) {
      userResourceService.resetAllUserResource(COMBINED_USERCREATOR_ENGINETYPE);
    } else {
      userResourceService.resetUserResource(resourceId);
    }
    return Message.ok("success");
  }

  @ApiOperation(value = "listAllEngineType", notes = "list all engineType")
  @RequestMapping(path = "engineType", method = RequestMethod.GET)
  public Message listAllEngineType(HttpServletRequest request) {
    String engineTypeString = RMUtils.ENGINE_TYPE.getValue();
    String[] engineTypeList = engineTypeString.split(",");
    return Message.ok().data("engineType", engineTypeList);
  }

  @ApiOperation(value = "getAllUserResource", notes = "get all user resource")
  @RequestMapping(path = "allUserResource", method = RequestMethod.GET)
  public Message getAllUserResource(
      HttpServletRequest request,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "page", required = false, defaultValue = "1") int page,
      @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
    Option<String> queryUser = SecurityFilter.getLoginUser(request);
    if (!Configuration.isAdmin(queryUser.get())) {
      throw new RMWarnException(ONLY_ADMIN_READ.getErrorCode(), ONLY_ADMIN_READ.getErrorDesc());
    }
    // 1. Construct a string for SQL LIKE query, query the label_value of the label table
    String searchUsername = username != null ? username : "";
    String searchCreator = creator != null ? creator : "";
    String searchEngineType = engineType != null ? engineType : "";
    // label value in db as
    // :{"creator":"nodeexecution","user":"hadoop","engineType":"appconn","version":"1"}
    String labelValuePattern =
        MessageFormat.format(
            "%{0}%,%{1}%,%{2}%,%", searchCreator, searchUsername, searchEngineType);

    if (COMBINED_USERCREATOR_ENGINETYPE == null) {
      UserCreatorLabel userCreatorLabel = labelFactory.createLabel(UserCreatorLabel.class);
      EngineTypeLabel engineTypeLabel = labelFactory.createLabel(EngineTypeLabel.class);
      Label<?> combinedLabel =
          combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel));
      COMBINED_USERCREATOR_ENGINETYPE = combinedLabel.getLabelKey();
    }

    // 2. The resource label of all users, including the associated resourceId
    Page<Object> resultPage = PageHelper.startPage(page, size);
    List<PersistenceLabelRel> userLabels =
        labelManagerPersistence.getLabelByPattern(
            labelValuePattern, COMBINED_USERCREATOR_ENGINETYPE, page, size);

    // 3. All user resources, including resourceId
    List<PersistenceResource> resources =
        resourceManagerPersistence.getResourceByLabels(userLabels);

    // 4. Store users and resources in Vo
    List<UserResourceVo> userResources = new ArrayList<>();
    resources.forEach(
        resource -> {
          UserResource userResource = ResourceUtils.fromPersistenceResourceAndUser(resource);
          Label userLabel =
              userLabels.stream()
                  .filter(label -> label.getResourceId().equals(resource.getId()))
                  .findFirst()
                  .orElse(null);
          if (userLabel != null) {
            UserCreatorEngineType userCreatorEngineType =
                gson.fromJson(userLabel.getStringValue(), UserCreatorEngineType.class);
            if (userCreatorEngineType != null) {
              userResource.setUsername(userCreatorEngineType.getUser());
              userResource.setCreator(userCreatorEngineType.getCreator());
              userResource.setEngineType(userCreatorEngineType.getEngineType());
              userResource.setVersion(userCreatorEngineType.getVersion());
            }
          }
          userResources.add(RMUtils.toUserResourceVo(userResource));
        });
    return Message.ok().data("resources", userResources).data("total", resultPage.getTotal());
  }

  @ApiOperation(value = "getUserResource", notes = "get user resource")
  @RequestMapping(
      path = {"userresources"},
      method = RequestMethod.POST)
  public Message getUserResource(
      HttpServletRequest request, @RequestBody(required = false) Map<String, Object> param) {
    Message message = Message.ok("");
    String userName = ModuleUserUtils.getOperationUser(request, "get userresources");
    EngineNode[] nodes = getEngineNodes(userName, true);
    if (nodes == null) {
      nodes = new EngineNode[0];
    } else {
      nodes =
          Arrays.stream(nodes)
              .filter(
                  node ->
                      node.getNodeResource() != null
                          && !node.getLabels().isEmpty()
                          && node.getLabels().stream()
                                  .filter(label -> label instanceof UserCreatorLabel)
                                  .findFirst()
                                  .orElse(null)
                              != null
                          && node.getLabels().stream()
                                  .filter(label -> label instanceof EngineTypeLabel)
                                  .findFirst()
                                  .orElse(null)
                              != null)
              .toArray(EngineNode[]::new);
    }
    Map<String, Map<String, NodeResource>> userCreatorEngineTypeResourceMap = new HashMap<>();
    for (EngineNode node : nodes) {
      UserCreatorLabel userCreatorLabel =
          (UserCreatorLabel)
              node.getLabels().stream()
                  .filter(label -> label instanceof UserCreatorLabel)
                  .findFirst()
                  .get();
      EngineTypeLabel engineTypeLabel =
          (EngineTypeLabel)
              node.getLabels().stream()
                  .filter(label -> label instanceof EngineTypeLabel)
                  .findFirst()
                  .get();

      String userCreator = getUserCreator(userCreatorLabel);
      if (!userCreatorEngineTypeResourceMap.containsKey(userCreator)) {
        userCreatorEngineTypeResourceMap.put(userCreator, new HashMap<>());
      }

      Map<String, NodeResource> engineTypeResourceMap =
          userCreatorEngineTypeResourceMap.get(userCreator);
      String engineType = getEngineType(engineTypeLabel);
      if (!engineTypeResourceMap.containsKey(engineType)) {
        NodeResource nodeResource = CommonNodeResource.initNodeResource(ResourceType.LoadInstance);
        engineTypeResourceMap.put(engineType, nodeResource);
      }
      NodeResource resource = engineTypeResourceMap.get(engineType);
      resource.setUsedResource(
          node.getNodeResource().getUsedResource().add(resource.getUsedResource()));

      // combined label
      Label<?> combinedLabel =
          combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel));
      NodeResource labelResource = labelResourceService.getLabelResource(combinedLabel);
      if (labelResource == null) {
        resource.setLeftResource(
            node.getNodeResource().getMaxResource().minus(resource.getUsedResource()));
      } else {
        labelResource = ResourceUtils.convertTo(labelResource, ResourceType.LoadInstance);
        resource.setUsedResource(labelResource.getUsedResource());
        resource.setLockedResource(labelResource.getLockedResource());
        resource.setLeftResource(labelResource.getLeftResource());
        resource.setMaxResource(labelResource.getMaxResource());
      }
      if (resource.getLeftResource() instanceof DriverAndYarnResource) {
        resource.setLeftResource(((DriverAndYarnResource) resource).getLoadInstanceResource());
      }
    }

    //        Map<String, Object> userCreatorEngineTypeResources = new HashMap<>();
    List<Map<String, Object>> userCreatorEngineTypeResources =
        userCreatorEngineTypeResourceMap.entrySet().stream()
            .map(
                userCreatorEntry -> {
                  String userCreator = userCreatorEntry.getKey();
                  Map<String, Object> userCreatorEngineTypeResource = new HashMap<>();
                  userCreatorEngineTypeResource.put("userCreator", userCreator);
                  long totalUsedMemory = 0L;
                  int totalUsedCores = 0;
                  int totalUsedInstances = 0;
                  long totalLockedMemory = 0L;
                  int totalLockedCores = 0;
                  int totalLockedInstances = 0;
                  long totalMaxMemory = 0L;
                  int totalMaxCores = 0;
                  int totalMaxInstances = 0;
                  List<Map<String, Object>> engineTypeResources = new ArrayList<>();

                  for (Map.Entry<String, NodeResource> engineTypeEntry :
                      userCreatorEntry.getValue().entrySet()) {
                    String engineType = engineTypeEntry.getKey();
                    NodeResource engineResource = engineTypeEntry.getValue();
                    Map<String, Object> engineTypeResource = new HashMap<>();
                    engineTypeResource.put("engineType", engineType);
                    LoadInstanceResource usedResource =
                        (LoadInstanceResource) engineResource.getUsedResource();
                    LoadInstanceResource lockedResource =
                        (LoadInstanceResource) engineResource.getLockedResource();
                    LoadInstanceResource maxResource =
                        (LoadInstanceResource) engineResource.getMaxResource();
                    long usedMemory = usedResource.getMemory();
                    int usedCores = usedResource.getCores();
                    int usedInstances = usedResource.getInstances();
                    totalUsedMemory += usedMemory;
                    totalUsedCores += usedCores;
                    totalUsedInstances += usedInstances;
                    long lockedMemory = lockedResource.getMemory();
                    int lockedCores = lockedResource.getCores();
                    int lockedInstances = lockedResource.getInstances();
                    totalLockedMemory += lockedMemory;
                    totalLockedCores += lockedCores;
                    totalLockedInstances += lockedInstances;
                    long maxMemory = maxResource.getMemory();
                    int maxCores = maxResource.getCores();
                    int maxInstances = maxResource.getInstances();
                    totalMaxMemory += maxMemory;
                    totalMaxCores += maxCores;
                    totalMaxInstances += maxInstances;
                    double memoryPercent =
                        maxMemory > 0 ? (usedMemory + lockedMemory) / (double) maxMemory : 0;
                    double coresPercent =
                        maxCores > 0 ? (usedCores + lockedCores) / (double) maxCores : 0;
                    double instancePercent =
                        maxInstances > 0
                            ? (usedInstances + lockedInstances) / (double) maxInstances
                            : 0;
                    double maxPercent =
                        Math.max(Math.max(memoryPercent, coresPercent), instancePercent);
                    engineTypeResource.put("percent", String.format("%.2f", maxPercent));
                    engineTypeResources.add(engineTypeResource);
                  }

                  double totalMemoryPercent =
                      totalMaxMemory > 0
                          ? (totalUsedMemory + totalLockedMemory) / (double) totalMaxMemory
                          : 0;
                  double totalCoresPercent =
                      totalMaxCores > 0
                          ? (totalUsedCores + totalLockedCores) / (double) totalMaxCores
                          : 0;
                  double totalInstancePercent =
                      totalMaxInstances > 0
                          ? (totalUsedInstances + totalLockedInstances) / (double) totalMaxInstances
                          : 0;
                  double totalPercent =
                      Math.max(
                          Math.max(totalMemoryPercent, totalCoresPercent), totalInstancePercent);
                  userCreatorEngineTypeResource.put("engineTypes", engineTypeResources);
                  userCreatorEngineTypeResource.put("percent", String.format("%.2f", totalPercent));
                  return userCreatorEngineTypeResource;
                })
            .collect(Collectors.toList());

    appendMessageData(message, "userResources", userCreatorEngineTypeResources);
    return message;
  }

  @ApiOperation(value = "getEngines", notes = "get engines")
  @RequestMapping(path = "engines", method = RequestMethod.POST)
  public Message getEngines(
      HttpServletRequest request, @RequestBody(required = false) Map<String, Object> param) {
    Message message = Message.ok("");
    String userName = ModuleUserUtils.getOperationUser(request, "get engines");
    EngineNode[] nodes = getEngineNodes(userName, true);
    if (nodes == null || nodes.length == 0) {
      return message;
    }
    List<Map<String, Object>> engines = new ArrayList<>();
    Arrays.stream(nodes)
        .forEach(
            node -> {
              UserCreatorLabel userCreatorLabel =
                  (UserCreatorLabel)
                      node.getLabels().stream()
                          .filter(label -> label instanceof UserCreatorLabel)
                          .findFirst()
                          .get();
              EngineTypeLabel engineTypeLabel =
                  (EngineTypeLabel)
                      node.getLabels().stream()
                          .filter(label -> label instanceof EngineTypeLabel)
                          .findFirst()
                          .get();
              Map<String, Object> record = new HashMap<>();
              if (node.getServiceInstance() != null) {
                record.put("applicationName", node.getServiceInstance().getApplicationName());
                record.put("engineInstance", node.getServiceInstance().getInstance());
              }

              record.put("creator", userCreatorLabel.getCreator());
              record.put("engineType", engineTypeLabel.getEngineType());
              if (node.getNodeResource() != null) {
                if (node.getNodeResource().getLockedResource() != null) {
                  record.put("preUsedResource", node.getNodeResource().getLockedResource());
                }
                if (node.getNodeResource().getUsedResource() != null) {
                  record.put("usedResource", node.getNodeResource().getUsedResource());
                }
              }
              if (node.getNodeStatus() == null) {
                record.put("engineStatus", "Busy");
              } else {
                record.put("engineStatus", node.getNodeStatus().toString());
              }
              engines.add(record);
            });
    appendMessageData(message, "engines", engines);
    return message;
  }

  @ApiOperation(value = "getQueueResource", notes = "get queue resource")
  @RequestMapping(path = "queueresources", method = RequestMethod.POST)
  public Message getQueueResource(
      HttpServletRequest request, @RequestBody Map<String, Object> param) {
    Message message = Message.ok("");
    YarnResourceIdentifier yarnIdentifier =
        new YarnResourceIdentifier((String) param.get("queuename"));
    ClusterLabel clusterLabel = labelFactory.createLabel(ClusterLabel.class);
    clusterLabel.setClusterName((String) param.get("clustername"));
    clusterLabel.setClusterType((String) param.get("clustertype"));
    RMLabelContainer labelContainer = new RMLabelContainer(Collections.singletonList(clusterLabel));
    NodeResource providedYarnResource =
        externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier);

    double usedMemoryPercentage = 0.0;
    double usedCPUPercentage = 0.0;
    if (providedYarnResource != null) {
      YarnResource maxResource = (YarnResource) providedYarnResource.getMaxResource();
      YarnResource usedResource = (YarnResource) providedYarnResource.getUsedResource();
      Map<String, Object> queueInfo = new HashMap<>();
      queueInfo.put("queuename", maxResource);
      queueInfo.put(
          "maxResources",
          ImmutableMap.of(
              "memory", maxResource.getQueueMemory(), "cores", maxResource.getQueueCores()));
      queueInfo.put(
          "usedResources",
          ImmutableMap.of(
              "memory", usedResource.getQueueMemory(), "cores", usedResource.getQueueCores()));
      usedMemoryPercentage = usedResource.getQueueMemory() / (double) maxResource.getQueueMemory();
      usedCPUPercentage = usedResource.getQueueCores() / (double) maxResource.getQueueCores();
      queueInfo.put(
          "usedPercentage",
          ImmutableMap.of("memory", usedMemoryPercentage, "cores", usedCPUPercentage));
      appendMessageData(message, "queueInfo", queueInfo);
    } else {
      message = Message.error("Failed to get queue resource");
    }

    List<Map<String, Object>> userResourceRecords = new ArrayList<>();
    List<ExternalAppInfo> yarnAppsInfo =
        externalResourceService.getAppInfo(ResourceType.Yarn, labelContainer, yarnIdentifier);

    Map<String, List<ExternalAppInfo>> user2YarnAppsInfos =
        yarnAppsInfo.stream().collect(Collectors.groupingBy(app -> ((YarnAppInfo) app).getUser()));
    List<String> userList = new ArrayList<>(user2YarnAppsInfos.keySet());
    try {
      Map<String, List<EngineNode>> nodesList = getEngineNodesByUserList(userList, true);
      for (Map.Entry<String, List<ExternalAppInfo>> stringListEntry :
          user2YarnAppsInfos.entrySet()) {
        String user = stringListEntry.getKey();
        List<ExternalAppInfo> userAppInfo = stringListEntry.getValue();

        YarnResource busyResource = (YarnResource) Resource.initResource(ResourceType.Yarn);
        YarnResource idleResource = (YarnResource) Resource.initResource(ResourceType.Yarn);
        Map<String, EngineNode> appIdToEngineNode = new HashMap<>();

        List<EngineNode> nodesplus = nodesList.get(user);
        if (nodesplus != null) {
          nodesplus.forEach(
              node -> {
                if (node.getNodeResource() != null
                    && node.getNodeResource().getUsedResource() != null) {
                  if (node.getNodeResource().getUsedResource() instanceof DriverAndYarnResource) {
                    DriverAndYarnResource driverAndYarnResource =
                        (DriverAndYarnResource) node.getNodeResource().getUsedResource();
                    if (driverAndYarnResource
                        .getYarnResource()
                        .getQueueName()
                        .equals(yarnIdentifier.getQueueName())) {
                      appIdToEngineNode.put(
                          driverAndYarnResource.getYarnResource().getApplicationId(), node);
                    }

                  } else if (node.getNodeResource().getUsedResource() instanceof YarnResource) {
                    YarnResource yarnResource =
                        (YarnResource) node.getNodeResource().getUsedResource();
                    if (yarnResource.getQueueName().equals(yarnIdentifier.getQueueName())) {
                      appIdToEngineNode.put(yarnResource.getApplicationId(), node);
                    }
                  }
                }
              });
        }

        for (ExternalAppInfo appInfo : userAppInfo) {
          String appId = ((YarnAppInfo) appInfo).getId();
          EngineNode node = appIdToEngineNode.get(appId);
          if (node != null) {
            if (node.getNodeStatus() == NodeStatus.Busy) {
              busyResource = busyResource.add(((YarnAppInfo) appInfo).getUsedResource());
            } else {
              idleResource = idleResource.add(((YarnAppInfo) appInfo).getUsedResource());
            }
          } else {
            busyResource = busyResource.add(((YarnAppInfo) appInfo).getUsedResource());
          }
        }
        YarnResource totalResource = busyResource.add(idleResource);
        if (totalResource.moreThan(Resource.getZeroResource(totalResource))) {
          Map<String, Object> userResource = new HashMap<>();
          userResource.put("username", user);
          YarnResource queueResource = (YarnResource) providedYarnResource.getMaxResource();
          if (usedMemoryPercentage > usedCPUPercentage) {
            userResource.put(
                "busyPercentage",
                busyResource.getQueueMemory() / (double) queueResource.getQueueMemory());
            userResource.put(
                "idlePercentage",
                idleResource.getQueueMemory() / (double) queueResource.getQueueMemory());
            userResource.put(
                "totalPercentage",
                totalResource.getQueueMemory() / (double) queueResource.getQueueMemory());
          } else {
            userResource.put(
                "busyPercentage",
                busyResource.getQueueCores() / (double) queueResource.getQueueCores());
            userResource.put(
                "idlePercentage",
                idleResource.getQueueCores() / (double) queueResource.getQueueCores());
            userResource.put(
                "totalPercentage",
                totalResource.getQueueCores() / (double) queueResource.getQueueCores());
          }
          userResourceRecords.add(userResource);
        }
      }
    } catch (Exception exception) {
      logger.error("queresource search failed!", exception);
    }
    userResourceRecords.sort(
        (Map<String, Object> o1, Map<String, Object> o2) ->
            Double.compare(
                (double) o2.getOrDefault("totalPercentage", 0.0),
                (double) o1.getOrDefault("totalPercentage", 0.0)));
    appendMessageData(message, "userResources", userResourceRecords);

    return message;
  }

  @ApiOperation(value = "getQueues", notes = "get queues")
  @RequestMapping(path = "queues", method = RequestMethod.POST)
  public Message getQueues(
      HttpServletRequest request, @RequestBody(required = false) Map<String, Object> param) {
    Message message = Message.ok();
    String userName = ModuleUserUtils.getOperationUser(request, "get queues");
    List clusters = new ArrayList<>();
    Map<String, Object> clusterInfo = new HashMap<>();
    Set queues = new LinkedHashSet<>();
    Map<String, String> userConfiguration = UserConfiguration.getGlobalConfig(userName);
    String clusterName = RMConfiguration.USER_AVAILABLE_CLUSTER_NAME.getValue(userConfiguration);
    clusterInfo.put("clustername", clusterName);
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration));
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue());
    clusterInfo.put("queues", queues);
    clusters.add(clusterInfo);
    appendMessageData(message, "queues", clusters);
    return message;
  }

  private String getUserCreator(UserCreatorLabel userCreatorLabel) {
    return "(" + userCreatorLabel.getUser() + "," + userCreatorLabel.getCreator() + ")";
  }

  private String getEngineType(EngineTypeLabel engineTypeLabel) {
    return "(" + engineTypeLabel.getEngineType() + "," + engineTypeLabel.getVersion() + ")";
  }

  private EngineNode[] getEngineNodes(String user, boolean withResource) {
    List<ServiceInstance> serviceInstanceList =
        nodeManagerPersistence.getNodes(user).stream()
            .map(Node::getServiceInstance)
            .collect(Collectors.toList());
    List<EngineNode> nodes =
        nodeManagerPersistence.getEngineNodeByServiceInstance(serviceInstanceList);

    List<NodeMetrics> nodeMetrics = nodeMetricManagerPersistence.getNodeMetrics(nodes);
    Map<String, NodeMetrics> metrics =
        nodeMetrics.stream()
            .collect(
                Collectors.toMap(
                    m -> m.getServiceInstance().toString(),
                    m -> m,
                    (existingValue, newValue) -> newValue));

    Map<String, Resource> configurationMap = new HashMap<>();

    Map<String, List<Label<?>>> labelsMap =
        nodeLabelService.getNodeLabelsByInstanceList(
            nodes.stream().map(Node::getServiceInstance).collect(Collectors.toList()));
    //        nodeLabelsByInstanceList.entrySet().stream().collect(Collectors.groupingBy(label ->
    // label.getInstance().toString()));

    return nodes.stream()
        .map(
            node -> {
              node.setLabels(labelsMap.get(node.getServiceInstance().toString()));
              if (!node.getLabels().stream().anyMatch(label -> label instanceof UserCreatorLabel)) {
                return null;
              } else {
                Optional.ofNullable(metrics.get(node.getServiceInstance().toString()))
                    .ifPresent(
                        metricsData -> metricsConverter.fillMetricsToNode(node, metricsData));
                if (withResource) {
                  Optional userCreatorLabelOption =
                      node.getLabels().stream()
                          .filter(label -> label instanceof UserCreatorLabel)
                          .findFirst();
                  Optional engineTypeLabelOption =
                      node.getLabels().stream()
                          .filter(label -> label instanceof EngineTypeLabel)
                          .findFirst();
                  Optional engineInstanceOption =
                      node.getLabels().stream()
                          .filter(label -> label instanceof EngineInstanceLabel)
                          .findFirst();

                  if (userCreatorLabelOption.isPresent()
                      && engineTypeLabelOption.isPresent()
                      && engineInstanceOption.isPresent()) {
                    UserCreatorLabel userCreatorLabel =
                        (UserCreatorLabel) userCreatorLabelOption.get();
                    EngineTypeLabel engineTypeLabel = (EngineTypeLabel) engineTypeLabelOption.get();
                    EngineInstanceLabel engineInstanceLabel =
                        (EngineInstanceLabel) engineInstanceOption.get();
                    engineInstanceLabel.setServiceName(
                        node.getServiceInstance().getApplicationName());
                    engineInstanceLabel.setInstance(node.getServiceInstance().getInstance());
                    NodeResource nodeResource =
                        labelResourceService.getLabelResource(engineInstanceLabel);
                    String configurationKey =
                        getUserCreator(userCreatorLabel) + getEngineType(engineTypeLabel);
                    Resource configuredResource = configurationMap.get(configurationKey);
                    if (configuredResource == null) {
                      if (nodeResource != null) {
                        configuredResource =
                            UserConfiguration.getUserConfiguredResource(
                                nodeResource.getResourceType(), userCreatorLabel, engineTypeLabel);
                        configurationMap.put(configurationKey, configuredResource);
                      }
                    }
                    if (nodeResource != null) {
                      nodeResource.setMaxResource(configuredResource);
                      if (nodeResource.getUsedResource() == null) {
                        nodeResource.setUsedResource(nodeResource.getLockedResource());
                      }
                      if (nodeResource.getMinResource() == null) {
                        nodeResource.setMinResource(
                            Resource.initResource(nodeResource.getResourceType()));
                      }
                      node.setNodeResource(nodeResource);
                    }
                  }
                }
                return node;
              }
            })
        .filter(Objects::nonNull)
        .toArray(EngineNode[]::new);
  }

  public Map<String, List<EngineNode>> getEngineNodesByUserList(
      List<String> userList, boolean withResource) {
    List<Node> nodesByOwnerList = nodeManagerPersistence.getNodesByOwnerList(userList);
    List<ServiceInstance> serviceInstance =
        nodesByOwnerList.stream().map(Node::getServiceInstance).collect(Collectors.toList());

    List<EngineNode> engineNodesList =
        nodeManagerPersistence.getEngineNodeByServiceInstance(serviceInstance);

    Map<String, NodeMetrics> metrics =
        nodeMetricManagerPersistence.getNodeMetrics(engineNodesList).stream()
            .collect(
                Collectors.toMap(
                    m -> m.getServiceInstance().toString(),
                    Function.identity(),
                    (existingValue, newValue) -> newValue));

    Map<String, List<Label<?>>> labelsMap =
        nodeLabelService.getNodeLabelsByInstanceList(
            engineNodesList.stream()
                .map(EngineNode::getServiceInstance)
                .collect(Collectors.toList()));

    return engineNodesList.stream()
        .map(
            nodeInfo -> {
              nodeInfo.setLabels(labelsMap.get(nodeInfo.getServiceInstance().toString()));
              if (nodeInfo.getLabels().stream().anyMatch(l -> l instanceof UserCreatorLabel)) {
                Optional.ofNullable(metrics.get(nodeInfo.getServiceInstance().toString()))
                    .ifPresent(m -> metricsConverter.fillMetricsToNode(nodeInfo, m));
                if (withResource) {
                  Optional<EngineInstanceLabel> engineInstanceOption =
                      nodeInfo.getLabels().stream()
                          .filter(l -> l instanceof EngineInstanceLabel)
                          .map(label -> (EngineInstanceLabel) label)
                          .findFirst();
                  engineInstanceOption.ifPresent(
                      engineInstanceLabel -> {
                        engineInstanceLabel.setServiceName(
                            nodeInfo.getServiceInstance().getApplicationName());
                        engineInstanceLabel.setInstance(
                            nodeInfo.getServiceInstance().getInstance());

                        NodeResource nodeResource =
                            labelResourceService.getLabelResource(engineInstanceLabel);
                        if (nodeResource != null) {
                          if (nodeResource.getUsedResource() == null) {
                            nodeResource.setUsedResource(nodeResource.getLockedResource());
                          }
                          nodeInfo.setNodeResource(nodeResource);
                        }
                      });
                }
              }
              return nodeInfo;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(EngineNode::getOwner));
  }
}
