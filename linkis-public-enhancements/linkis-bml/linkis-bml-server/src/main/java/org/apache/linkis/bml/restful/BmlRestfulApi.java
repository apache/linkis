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

package org.apache.linkis.bml.restful;

import org.apache.linkis.bml.common.*;
import org.apache.linkis.bml.common.Constant;
import org.apache.linkis.bml.entity.DownloadModel;
import org.apache.linkis.bml.entity.Resource;
import org.apache.linkis.bml.entity.ResourceTask;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.entity.Version;
import org.apache.linkis.bml.service.BmlService;
import org.apache.linkis.bml.service.DownloadService;
import org.apache.linkis.bml.service.ResourceService;
import org.apache.linkis.bml.service.TaskService;
import org.apache.linkis.bml.service.VersionService;
import org.apache.linkis.bml.threading.TaskState;
import org.apache.linkis.bml.util.HttpRequestHelper;
import org.apache.linkis.bml.vo.ResourceBasicVO;
import org.apache.linkis.bml.vo.ResourceVO;
import org.apache.linkis.bml.vo.ResourceVersionsVO;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.bml.errorcode.BmlServerErrorCodeSummary.*;

@Api(tags = "bml(bigdata material library) opreation")
@RequestMapping(path = "/bml")
@RestController
public class BmlRestfulApi {

  @Autowired private BmlService bmlService;

  @Autowired private VersionService versionService;

  @Autowired private ResourceService resourceService;

  @Autowired private DownloadService downloadService;

  @Autowired private TaskService taskService;

  private Logger logger = LoggerFactory.getLogger(getClass());

  public static final String URL_PREFIX = "/bml/";

  @ApiOperation(
      value = "getVersions",
      notes = "get resource versions info list",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", dataType = "String"),
    @ApiImplicitParam(name = "currentPage", dataType = "String"),
    @ApiImplicitParam(name = "pageSize", required = false, dataType = "String", value = "page size")
  })
  @RequestMapping(path = "getVersions", method = RequestMethod.GET)
  public Message getVersions(
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "currentPage", required = false) String currentPage,
      @RequestParam(value = "pageSize", required = false) String pageSize,
      HttpServletRequest request)
      throws ErrorException {

    String user = RestfulUtils.getUserName(request);
    if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
      logger.error(
          "ResourceId {} provided by user {} is illegal (用户{} 提供的resourceId {} is illegal)",
          resourceId,
          user,
          user,
          resourceId);
      throw new BmlServerParaErrorException(SUBMITTED_INVALID.getErrorDesc());
    }

    Integer current = 0;
    Integer size = 0;
    if (StringUtils.isEmpty(currentPage) || !StringUtils.isNumeric(currentPage)) {
      current = 1;
    } else {
      current = Integer.valueOf(currentPage);
    }
    if (StringUtils.isEmpty(pageSize) || !StringUtils.isNumeric(pageSize)) {
      size = 10;
    } else {
      size = Integer.valueOf(pageSize);
    }

    Message message = null;

    try {
      logger.info(
          "User {} starts getting information about all versions of {} (用户 {} 开始获取 {} 的所有版本信息)",
          user,
          resourceId,
          user,
          resourceId);
      List<Version> versionList = versionService.selectVersionByPage(current, size, resourceId);
      if (versionList.size() > 0) {
        message = Message.ok("Version information obtained successfully (成功获取版本信息)");
        message.setMethod(URL_PREFIX + "getVersions");
        message.setStatus(0);
        ResourceVersionsVO resourceVersionsVO = new ResourceVersionsVO();
        resourceVersionsVO.setVersions(versionList);
        resourceVersionsVO.setResourceId(resourceId);
        resourceVersionsVO.setUser(user);
        message.data("ResourceVersions", resourceVersionsVO);
      } else {
        logger.warn(
            "User {} fetch resource {} no error, but fetch version length 0 (user {} 获取资源{}未报错，但是获取到的version长度为0)",
            user,
            resourceId,
            user,
            resourceId);
        message = Message.error("Failed to get version information correctly(未能正确获取到版本信息)");
        message.setMethod(URL_PREFIX + "getVersions");
        message.setStatus(2);
      }
      logger.info(
          "User {} ends getting all version information for {} (用户 {} 结束获取 {} 的所有版本信息)",
          user,
          resourceId,
          user,
          resourceId);
    } catch (final Exception e) {
      logger.error(
          "User {} Failed to get version information of the ResourceId {} resource(user {} 获取resourceId {} 资源的版本信息失败)",
          user,
          resourceId,
          user,
          resourceId,
          e);
      throw new BmlQueryFailException(QUERY_VERSION_FAILED.getErrorDesc());
    }

    return message;
  }

  @ApiOperation(value = "getResources", notes = "get resources info list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "system", required = false, dataType = "String", value = "system"),
    @ApiImplicitParam(name = "currentPage", dataType = "String"),
    @ApiImplicitParam(name = "pageSize", required = false, dataType = "String", value = "page size")
  })
  @RequestMapping(path = "getResources", method = RequestMethod.GET)
  public Message getResources(
      @RequestParam(value = "system", required = false) String system,
      @RequestParam(value = "currentPage", required = false) String currentPage,
      @RequestParam(value = "pageSize", required = false) String pageSize,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ErrorException {

    String user = RestfulUtils.getUserName(request);

    if (StringUtils.isEmpty(system)) {
      // 默认系统是wtss
      system = Constant.DEFAULT_SYSTEM;
    }

    Integer current = 0;
    Integer size = 0;
    if (StringUtils.isEmpty(currentPage) || !StringUtils.isNumeric(currentPage)) {
      current = 1;
    } else {
      current = Integer.valueOf(currentPage);
    }
    if (StringUtils.isEmpty(pageSize) || !StringUtils.isNumeric(pageSize)) {
      size = 10;
    } else {
      size = Integer.valueOf(pageSize);
    }
    Message message = null;
    try {
      logger.info(
          "User {} starts fetching all the resources of the system {}(用户 {} 开始获取系统 {} 的所有资源)",
          user,
          system,
          user,
          system);
      List<ResourceVersion> resourceVersionPageInfoList =
          versionService.selectResourcesViaSystemByPage(current, size, system, user);
      if (resourceVersionPageInfoList.size() > 0) {
        message =
            Message.ok(
                "Get all your resources in system "
                    + system
                    + " successfully(获取您在系统"
                    + system
                    + "中所有资源成功)");
        message.setStatus(0);
        message.setMethod(URL_PREFIX + "getResources");
        List<ResourceVO> resourceVOList = new ArrayList<>();
        for (ResourceVersion resourceVersion : resourceVersionPageInfoList) {
          ResourceVO resourceVO = new ResourceVO();
          resourceVO.setResource(resourceVersion.getResource());
          resourceVO.setUser(user);
          resourceVO.setResourceId(resourceVersion.getResourceId());
          resourceVO.setVersion(resourceVersion.getVersion());
          resourceVOList.add(resourceVO);
        }
        message.data("Resources", resourceVOList);
      } else {
        logger.warn(
            "User {} gets system {} resource size of 0(用户 {} 获取系统 {} 资源的size为0)",
            user,
            system,
            user,
            system);
        message = Message.error("Failed to obtain all resource information(未能成功获取到所有资源信息)");
        message.setStatus(2);
        message.setMethod(URL_PREFIX + "getResources");
      }
      logger.info(
          "User {} ends fetching all resources for system {}(用户 {} 结束获取系统 {} 的所有资源)",
          user,
          system,
          user,
          system);
    } catch (final Exception e) {
      logger.error(
          "User {} failed to obtain all resources of the system {}(用户 {} 获取系统 {} 所有资源失败).",
          user,
          system,
          user,
          system,
          e);
      throw new BmlQueryFailException(FAILED_ALL_INFORMATION.getErrorDesc());
    }

    return message;
  }

  @ApiOperation(value = "deleteVersion", notes = "delete version", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", value = "version")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "deleteVersion", method = RequestMethod.POST)
  public Message deleteVersion(HttpServletRequest request, @RequestBody JsonNode jsonNode)
      throws IOException, ErrorException {
    String user = RestfulUtils.getUserName(request);
    if (null == jsonNode.get("resourceId")
        || null == jsonNode.get("version")
        || StringUtils.isEmpty(jsonNode.get("resourceId").textValue())
        || StringUtils.isEmpty(jsonNode.get("version").textValue())) {
      throw new BmlServerParaErrorException(DELETE_SPECIFIED_VERSION.getErrorDesc());
    }

    String resourceId = jsonNode.get("resourceId").textValue();
    String version = jsonNode.get("version").textValue();
    // 检查资源和版本是否存在
    if (!resourceService.checkResourceId(resourceId)
        || !versionService.checkVersion(resourceId, version)
        || !versionService.canAccess(resourceId, version)) {
      throw new BmlServerParaErrorException(ILLEGAL_OR_DELETED.getErrorDesc());
    }
    Message message = null;
    ResourceTask resourceTask =
        taskService.createDeleteVersionTask(
            resourceId, version, user, HttpRequestHelper.getIp(request));
    try {
      logger.info(
          "User {} starts to delete resource of ResourceID: {} version: {}(用户 {} 开始删除 resourceId: {} version: {} 的资源)",
          user,
          resourceId,
          version,
          user,
          resourceId,
          version);
      versionService.deleteResourceVersion(resourceId, version);
      message = Message.ok("Deleted version successfully(删除版本成功)");
      message.setMethod(URL_PREFIX + "deleteVersion");
      message.setStatus(0);
      logger.info(
          "User {} ends deleting the resourceID: {} version: {} resource(用户 {} 结束删除 resourceId: {} version: {} 的资源)",
          user,
          resourceId,
          version,
          user,
          resourceId,
          version);
      taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
      logger.info(
          "Update task tasKid :{} -ResourceId :{} with {} state（删除版本成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态）.",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue());
    } catch (final Exception e) {
      logger.error(
          "User {} failed to delete resource {}, version {}(用户{}删除resource {}, version {} 失败)",
          user,
          resourceId,
          version,
          user,
          resourceId,
          version,
          e);
      taskService.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      logger.info(
          "Update task tasKid :{} -ResourceId :{} with {} state（删除版本成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态）.",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue());
      throw new BmlQueryFailException(DELETE_VERSION_FAILED.getErrorDesc());
    }
    return message;
  }

  @ApiOperation(value = "deleteResource", notes = "delete Resource", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "resourceId", required = true, dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "deleteResource", method = RequestMethod.POST)
  public Message deleteResource(HttpServletRequest request, @RequestBody JsonNode jsonNode)
      throws IOException, ErrorException {

    String user = RestfulUtils.getUserName(request);

    if (null == jsonNode.get("resourceId")) {
      throw new BmlServerParaErrorException(NOT_BALID_RESOURCEID.getErrorDesc());
    }

    String resourceId = jsonNode.get("resourceId").textValue();
    if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
      logger.error("the error resourceId  is {} ", resourceId);
      throw new BmlServerParaErrorException(
          "the resourceId"
              + resourceId
              + " is null ,Illegal or deleted (resourceId:"
              + resourceId
              + "为空,非法或者已被删除!)");
    }

    Message message = null;
    ResourceTask resourceTask =
        taskService.createDeleteResourceTask(resourceId, user, HttpRequestHelper.getIp(request));
    try {
      logger.info(
          "User {} starts to delete all resources corresponding to ResourceId: {}(用户 {}  开始删除 resourceId: {} 对应的所有资源)",
          user,
          resourceId,
          user,
          resourceId);
      resourceService.deleteResource(resourceId);
      message = Message.ok("Resource deleted successfully(删除资源成功)");
      message.setMethod(URL_PREFIX + "deleteResource");
      message.setStatus(0);
      logger.info(
          "User {} ends deleting all resources corresponding to ResourceId: {}(用户 {}  结束删除 resourceId: {} 对应的所有资源)",
          user,
          resourceId,
          user,
          resourceId);
      taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
      logger.info(
          "Resource deleted successfully. Update task tasKid :{} -ResourceId :{} with {} state (删除资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态).",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue());
    } catch (final Exception e) {
      logger.error(
          "User {} failed to delete resource {}(用户 {} 删除资源 {} 失败)",
          user,
          resourceId,
          user,
          resourceId);
      taskService.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      logger.info(
          "Failed to delete resource. Update task tasKid :{} -ResourceId :{} is in {} state(删除资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.)",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue());
      throw new BmlQueryFailException(DELETE_OPERATION_FAILED.getErrorDesc());
    }

    return message;
  }

  @ApiOperation(
      value = "deleteResources",
      notes = "batch delete resource",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "resourceIds", required = true, dataType = "List")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "deleteResources", method = RequestMethod.POST)
  public Message deleteResources(HttpServletRequest request, @RequestBody JsonNode jsonNode)
      throws IOException, ErrorException {
    String user = RestfulUtils.getUserName(request);
    List<String> resourceIds = new ArrayList<>();

    if (null == jsonNode.get("resourceIds")) {
      throw new BmlServerParaErrorException(BULK_DELETION_PARAMETERS.getErrorDesc());
    }

    Iterator<JsonNode> jsonNodeIter = jsonNode.get("resourceIds").elements();
    while (jsonNodeIter.hasNext()) {
      resourceIds.add(jsonNodeIter.next().asText());
    }

    if (0 == resourceIds.size()) {
      throw new BmlServerParaErrorException(PARAMETERS_IS_NULL.getErrorDesc());
    } else {
      for (String resourceId : resourceIds) {
        if (StringUtils.isBlank(resourceId) || !resourceService.checkResourceId(resourceId)) {
          Message message =
              Message.error(
                  MessageFormat.format(RESOURCEID_BEEN_DELETED.getErrorDesc(), resourceId));
          message.setMethod(URL_PREFIX + "deleteResources");
          message.setStatus(1);
          return message;
        }
      }
    }

    ResourceTask resourceTask =
        taskService.createDeleteResourcesTask(resourceIds, user, HttpRequestHelper.getIp(request));
    Message message = null;
    try {
      logger.info("User {} begins to batch delete resources (用户 {} 开始批删除资源) ", user, user);
      resourceService.batchDeleteResources(resourceIds);
      message = Message.ok("Batch deletion of resource was successful(批量删除资源成功)");
      message.setMethod(URL_PREFIX + "deleteResources");
      message.setStatus(0);
      logger.info("User {} ends the bulk deletion of resources (用户 {} 结束批量删除资源)", user, user);
      taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
      logger.info(
          "Batch deletion of resource was successful. Update task tasKid :{} -ResourceId :{} is in the {} state (批量删除资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.)",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue());
    } catch (final Exception e) {
      logger.error("\"User {} failed to delete resources in bulk (用户 {} 批量删除资源失败)", user, user, e);
      taskService.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      logger.info(
          "Failed to delete resources in bulk. Update task tasKid :{} -ResourceId :{} is in the {} state (批量删除资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.)",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue(),
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue());
      throw new BmlQueryFailException(BULK_DELETE_FAILED.getErrorDesc());
    }
    return message;
  }

  /**
   * 通过resourceId 和 version两个参数获取下载对应的资源
   *
   * @param resourceId 资源Id
   * @param version 资源版本，如果不指定，默认为最新
   * @param resp httpServletResponse
   * @param request httpServletRequest
   * @return void
   * @throws IOException
   * @throws ErrorException
   */
  @ApiOperation(value = "download", notes = "download resource", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", dataType = "String"),
    @ApiImplicitParam(name = "version", dataType = "String")
  })
  @RequestMapping(path = "download", method = RequestMethod.GET)
  public void download(
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "version", required = false) String version,
      HttpServletResponse resp,
      HttpServletRequest request)
      throws IOException, ErrorException {
    String user = RestfulUtils.getUserName(request);

    if (StringUtils.isBlank(resourceId) || !resourceService.checkResourceId(resourceId)) {
      throw new BmlQueryFailException(
          "ResourceID :"
              + resourceId
              + " is empty, illegal or has been deleted (resourceId:"
              + resourceId
              + "为空,非法或者已被删除!)");
    }

    if (!resourceService.checkAuthority(user, resourceId)) {
      throw new BmlPermissionDeniedException(NOT_HAVE_PERMISSION.getErrorDesc());
    }
    // version is null get NewestVersion
    if (StringUtils.isBlank(version)) {
      version = versionService.getNewestVersion(resourceId);
    }
    // check version
    if (!versionService.checkVersion(resourceId, version)) {
      throw new BmlQueryFailException(
          MessageFormat.format(VERSION_BEEN_DELETED.getErrorDesc(), version));
    }
    // checkExpire
    if (!resourceService.checkExpire(resourceId, version)) {
      throw new BmlResourceExpiredException(resourceId);
    }

    Message message = null;
    resp.setContentType("application/x-msdownload");
    resp.setHeader("Content-Disposition", "attachment");
    String ip = HttpRequestHelper.getIp(request);
    DownloadModel downloadModel = new DownloadModel(resourceId, version, user, ip);
    try {
      logger.info(
          "user {} downLoad resource  resourceId is {}, version is {} ,ip is {} ",
          user,
          resourceId,
          version,
          ip);
      Map<String, Object> properties = new HashMap<>();
      boolean downloadResult =
          versionService.downloadResource(
              user, resourceId, version, resp.getOutputStream(), properties);
      downloadModel.setEndTime(new Date(System.currentTimeMillis()));
      downloadModel.setState(0);
      if (!downloadResult) {
        logger.warn(
            "ResourceId :{}, version:{} has a problem when user {} downloads the resource. The copied size is less than 0(用户 {} 下载资源 resourceId: {}, version:{} 出现问题,复制的size小于0)",
            resourceId,
            version,
            user,
            user,
            resourceId,
            version);
        downloadModel.setState(1);
        throw new BmlQueryFailException(FAILED_DOWNLOAD_RESOURCE.getErrorDesc());
      }
      downloadService.addDownloadRecord(downloadModel);
      logger.info(
          "User {} ends downloading the resource {}(用户 {} 结束下载资源 {}) ",
          user,
          resourceId,
          user,
          resourceId);
    } catch (IOException e) {
      logger.error(
          "IO Exception: ResourceId :{}, version:{} (用户 {} 下载资源 resourceId: {}, version:{} 出现IO异常)",
          resourceId,
          version,
          user,
          resourceId,
          version,
          e);
      downloadModel.setEndTime(new Date());
      downloadModel.setState(1);
      downloadService.addDownloadRecord(downloadModel);
      throw new ErrorException(
          73562,
          "Sorry, the background IO error caused you to download the resources failed(抱歉,后台IO错误造成您本次下载资源失败)");
    } catch (final Throwable t) {
      logger.error(
          "ResourceId :{}, version:{} abnormal when user {} downloads resource (用户 {} 下载资源 resourceId: {}, version:{} 出现异常)",
          resourceId,
          version,
          user,
          user,
          resourceId,
          version,
          t);
      downloadModel.setEndTime(new Date());
      downloadModel.setState(1);
      downloadService.addDownloadRecord(downloadModel);
      throw new ErrorException(
          73561,
          "Sorry, the background service error caused you to download the resources failed(抱歉，后台服务出错导致您本次下载资源失败)");
    } finally {
      IOUtils.closeQuietly(resp.getOutputStream());
    }
    logger.info(
        "{} Download resource {} successfully {} 下载资源 {} 成功", user, resourceId, user, resourceId);
  }

  @ApiOperation(value = "upload", notes = "upload resource", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "system", required = false, dataType = "String", value = "system"),
    @ApiImplicitParam(name = "resourceHeader", dataType = "String"),
    @ApiImplicitParam(name = "isExpire", dataType = "String"),
    @ApiImplicitParam(name = "expireType", dataType = "String"),
    @ApiImplicitParam(name = "expireTime", dataType = "String"),
    @ApiImplicitParam(name = "maxVersion", dataType = "String"),
    @ApiImplicitParam(name = "file", required = true, dataType = "List<MultipartFile>")
  })
  @RequestMapping(path = "upload", method = RequestMethod.POST)
  public Message uploadResource(
      HttpServletRequest req,
      @RequestParam(name = "system", required = false) String system,
      @RequestParam(name = "resourceHeader", required = false) String resourceHeader,
      @RequestParam(name = "isExpire", required = false) String isExpire,
      @RequestParam(name = "expireType", required = false) String expireType,
      @RequestParam(name = "expireTime", required = false) String expireTime,
      @RequestParam(name = "maxVersion", required = false) Integer maxVersion,
      @RequestParam(name = "file") List<MultipartFile> files)
      throws ErrorException {
    String user = RestfulUtils.getUserName(req);
    Message message;
    try {
      logger.info("User {} starts uploading resources (用户 {} 开始上传资源)", user, user);
      Map<String, Object> properties = new HashMap<>();
      properties.put("system", system);
      properties.put("resourceHeader", resourceHeader);
      properties.put("isExpire", isExpire);
      properties.put("expireType", expireType);
      properties.put("expireTime", expireTime);
      properties.put("maxVersion", maxVersion);
      String clientIp = HttpRequestHelper.getIp(req);
      properties.put("clientIp", clientIp);
      ResourceTask resourceTask = taskService.createUploadTask(files, user, properties);
      message =
          Message.ok("The task of submitting and uploading resources was successful(提交上传资源任务成功)");
      message.setMethod(URL_PREFIX + "upload");
      message.setStatus(0);
      message.data("resourceId", resourceTask.getResourceId());
      message.data("version", resourceTask.getVersion());
      message.data("taskId", resourceTask.getId());
      logger.info(
          "User {} submitted upload resource task successfully(用户 {} 提交上传资源任务成功, resourceId is {})",
          user,
          user,
          resourceTask.getResourceId());
    } catch (final Exception e) {
      logger.error("upload resource for user : {} failed, reason:", user, e);
      ErrorException exception =
          new ErrorException(
              50073, "The commit upload resource task failed(提交上传资源任务失败):" + e.getMessage());
      exception.initCause(e);
      throw exception;
    }
    return message;
  }

  /**
   * 用户通过http的方式更新资源文件
   *
   * @param request httpServletRequest
   * @param resourceId 用户希望更新资源的resourceId
   * @param file file文件
   * @return resourceId 以及 新的版本号
   */
  @ApiOperation(
      value = "updateVersion",
      notes = "update resource version",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "file", required = true, dataType = "MultipartFile", value = "file")
  })
  @RequestMapping(path = "updateVersion", method = RequestMethod.POST)
  public Message updateVersion(
      HttpServletRequest request,
      @RequestParam("resourceId") String resourceId,
      @RequestParam("file") MultipartFile file)
      throws Exception {
    String user = RestfulUtils.getUserName(request);
    if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
      logger.error("error resourceId  is {} ", resourceId);
      throw new BmlServerParaErrorException(
          "resourceId: " + resourceId + " is Null, illegal, or deleted!");
    }
    if (StringUtils.isEmpty(versionService.getNewestVersion(resourceId))) {
      logger.error(
          "If the material has not been uploaded or has been deleted, please call the upload interface first .(resourceId:{} 之前未上传物料,或物料已被删除,请先调用上传接口.)",
          resourceId);
      throw new BmlServerParaErrorException(
          "If the material has not been uploaded or has been deleted, please call the upload interface first( resourceId: "
              + resourceId
              + " 之前未上传物料,或物料已被删除,请先调用上传接口.!)");
    }
    Message message;
    try {
      logger.info(
          "User {} starts updating resources {}(用户 {} 开始更新资源 {}) ",
          user,
          resourceId,
          user,
          resourceId);
      String clientIp = HttpRequestHelper.getIp(request);
      Map<String, Object> properties = new HashMap<>();
      properties.put("clientIp", clientIp);
      ResourceTask resourceTask = null;
      synchronized (resourceId.intern()) {
        resourceTask = taskService.createUpdateTask(resourceId, user, file, properties);
      }
      message = Message.ok("The update resource task was submitted successfully(提交更新资源任务成功)");
      message
          .data("resourceId", resourceId)
          .data("version", resourceTask.getVersion())
          .data("taskId", resourceTask.getId());
    } catch (final ErrorException e) {
      logger.error("{} update resource failed, resourceId is {}, reason:", user, resourceId, e);
      throw e;
    } catch (final Exception e) {
      logger.error("{} update resource failed, resourceId is {}, reason:", user, resourceId, e);
      ErrorException exception =
          new ErrorException(
              50073, "The commit upload resource task failed(提交上传资源任务失败):" + e.getMessage());
      exception.initCause(e);
      throw exception;
    }
    logger.info(
        "User {} ends updating resources {}(用户 {} 结束更新资源 {}) ", user, resourceId, user, resourceId);
    return message;
  }

  @ApiOperation(value = "getBasic", notes = "get resource basic info", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "resourceId", required = true, dataType = "String")})
  @RequestMapping(path = "getBasic", method = RequestMethod.GET)
  public Message getBasic(
      @RequestParam(value = "resourceId", required = false) String resourceId,
      HttpServletRequest request)
      throws ErrorException {
    String user = RestfulUtils.getUserName(request);

    if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
      throw new BmlServerParaErrorException(PARAMETER_IS_ILLEGAL.getErrorDesc());
    }

    Message message = null;
    try {
      Resource resource = resourceService.getResource(resourceId);
      // int numberOfVersions = versionService.getNumOfVersions(resourceId);
      if (!resource.isEnableFlag()) {
        logger.warn(
            "The resource {} that user {} wants to query has been deleted (用户 {} 想要查询的资源 {} 已经被删除)",
            user,
            resourceId,
            user,
            resourceId);
        message = Message.error("The resource has been deleted(资源已经被删除)");
      } else {
        logger.info(
            "User {} starts getting basic information about {}(用户 {} 开始获取 {} 的基本信息)",
            user,
            resourceId,
            user,
            resourceId);
        ResourceBasicVO basic = new ResourceBasicVO();
        basic.setResourceId(resourceId);
        basic.setCreateTime(resource.getCreateTime());
        basic.setDownloadedFileName(resource.getDownloadedFileName());
        basic.setOwner(resource.getUser());
        // todo 正确的版本信息
        basic.setNumberOfVerions(10);
        if (resource.isExpire()) {
          basic.setExpireTime(
              RestfulUtils.getExpireTime(
                  resource.getCreateTime(), resource.getExpireType(), resource.getExpireTime()));
        } else {
          basic.setExpireTime("Resource not expired(资源不过期)");
        }
        message = Message.ok("Acquisition of resource basic information successfully(获取资源基本信息成功)");
        message.setStatus(0);
        message.setMethod(URL_PREFIX + "getBasic");
        message.data("basic", basic);
        logger.info(
            "User {} ends fetching basic information for {}(用户 {} 结束获取 {} 的基本信息)",
            user,
            resourceId,
            user,
            resourceId);
      }
    } catch (final Exception e) {
      logger.error("用户 {} 获取 {} 资源信息失败", user, resourceId, e);
      throw new BmlQueryFailException(FAILED_RESOURCE_BASIC.getErrorDesc());
    }

    return message;
  }

  @ApiOperation(value = "getResourceInfo", notes = "get resource info", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "resourceId", required = true, dataType = "String")})
  @RequestMapping(path = "getResourceInfo", method = RequestMethod.GET)
  public Message getResourceInfo(
      HttpServletRequest request,
      @RequestParam(value = "resourceId", required = false) String resourceId) {
    return Message.ok("Obtained information successfully(获取信息成功)");
  }

  @ApiOperation(value = "changeOwner", notes = "change resource owner", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "oldOwner", required = true, dataType = "String", value = "old Owner"),
    @ApiImplicitParam(name = "newOwner", required = true, dataType = "String", value = "new Owner")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "changeOwner", method = RequestMethod.POST)
  public Message changeOwnerByResourceId(HttpServletRequest request, @RequestBody JsonNode jsonNode)
      throws ErrorException {
    String resourceId = jsonNode.get("resourceId").textValue();
    String oldOwner = jsonNode.get("oldOwner").textValue();
    String newOwner = jsonNode.get("newOwner").textValue();
    resourceService.changeOwnerByResourceId(resourceId, oldOwner, newOwner);
    return Message.ok("更新owner成功！");
  }

  @ApiOperation(
      value = "copyResourceToAnotherUser",
      notes = "copy resource to another user",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "anotherUser", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "copyResourceToAnotherUser", method = RequestMethod.POST)
  public Message copyResourceToAnotherUser(
      HttpServletRequest request, @RequestBody JsonNode jsonNode) {
    String username = ModuleUserUtils.getOperationUser(request, "copyResourceToAnotherUser");
    String resourceId = jsonNode.get("resourceId").textValue();
    String anotherUser = jsonNode.get("anotherUser").textValue();
    Message message = null;
    try {
      logger.info("用户 {} 开始 copy bml resource: {}", username, resourceId);
      String clientIp = HttpRequestHelper.getIp(request);
      Map<String, Object> properties = new HashMap<>();
      properties.put("clientIp", clientIp);
      properties.put("maxVersion", 0);
      properties.put("system", "dss");
      ResourceTask resourceTask = null;
      synchronized (resourceId.intern()) {
        resourceTask = taskService.createCopyResourceTask(resourceId, anotherUser, properties);
      }
      message = Message.ok();
      message.data("resourceId", resourceTask.getResourceId());
    } catch (Exception e) {
      logger.error("failed to copy bml resource:", e);
      message = Message.error(e.getMessage());
    }
    logger.info("用户 {} 结束 copy bml resource: {}", username, resourceId);
    return message;
  }

  @ApiOperation(
      value = "RollbackVersion",
      notes = "rollback resource version",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "resourceId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", value = "version")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "rollbackVersion", method = RequestMethod.POST)
  public Message rollbackVersion(HttpServletRequest request, @RequestBody JsonNode jsonNode) {
    String username = ModuleUserUtils.getOperationUser(request, "rollbackVersion");
    String resourceId = jsonNode.get("resourceId").textValue();
    String rollbackVersion = jsonNode.get("version").textValue();
    Message message = null;
    try {
      logger.info(
          "用户 {} 开始rollback bml resource: {}, version:{} ", username, resourceId, rollbackVersion);
      String clientIp = HttpRequestHelper.getIp(request);
      Map<String, Object> properties = new HashMap<>();
      properties.put("clientIp", clientIp);
      ResourceTask resourceTask = null;
      synchronized (resourceId.intern()) {
        resourceTask =
            taskService.createRollbackVersionTask(
                resourceId, rollbackVersion, username, properties);
      }
      message = Message.ok();
      message
          .data("resourceId", resourceTask.getResourceId())
          .data("version", resourceTask.getVersion());
    } catch (Exception e) {
      logger.error("failed to rollback version:", e);
      message = Message.error(e.getMessage());
    }
    logger.info(
        "用户 {} 结束rollback bml resource: {}, version:{} ", username, resourceId, rollbackVersion);
    return message;
  }
}
