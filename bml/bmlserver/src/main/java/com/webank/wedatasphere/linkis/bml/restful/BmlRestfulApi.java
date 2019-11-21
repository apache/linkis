/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.restful;

import com.webank.wedatasphere.linkis.bml.Entity.DownloadModel;
import com.webank.wedatasphere.linkis.bml.Entity.Resource;
import com.webank.wedatasphere.linkis.bml.Entity.ResourceTask;
import com.webank.wedatasphere.linkis.bml.Entity.ResourceVersion;
import com.webank.wedatasphere.linkis.bml.Entity.Version;
import com.webank.wedatasphere.linkis.bml.common.Constant;
import com.webank.wedatasphere.linkis.bml.service.BmlService;
import com.webank.wedatasphere.linkis.bml.service.DownloadService;
import com.webank.wedatasphere.linkis.bml.service.ResourceService;
import com.webank.wedatasphere.linkis.bml.service.TaskService;
import com.webank.wedatasphere.linkis.bml.service.VersionService;
import com.webank.wedatasphere.linkis.bml.threading.TaskState;
import com.webank.wedatasphere.linkis.bml.util.HttpRequestHelper;
import com.webank.wedatasphere.linkis.bml.vo.ResourceBasicVO;
import com.webank.wedatasphere.linkis.bml.vo.ResourceVO;
import com.webank.wedatasphere.linkis.bml.vo.ResourceVersionsVO;
import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.bml.common.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * created by cooperyang on 2019/5/14
 * Description:
 */
@Path("bml")
@Component
public class BmlRestfulApi {

    @Autowired
    private BmlService bmlService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private TaskService taskService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String URL_PREFIX = "/bml/";

    @GET
    @Path("getVersions")
    public Response getVersions(@QueryParam("resourceId") String resourceId,
                                @QueryParam("currentPage") String  currentPage,
                                @QueryParam("pageSize") String pageSize,
                                @Context HttpServletRequest request)throws ErrorException{

        String user = RestfulUtils.getUserName(request);
        if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)){
            logger.error("用户{} 提供的resourceId {} is illegal", user, resourceId );
            throw new BmlServerParaErrorException("您提交的resourceId无效");
        }


        Integer current = 0;
        Integer size = 0;
        if (StringUtils.isEmpty(currentPage)  || !StringUtils.isNumeric(currentPage)){
            current = 1;
        }else{
            current = Integer.valueOf(currentPage);
        }
        if ( StringUtils.isEmpty(pageSize )  || !StringUtils.isNumeric(pageSize) ){
            size = 10;
        }else{
            size = Integer.valueOf(pageSize);
        }


        Message message = null;

        try{
            logger.info("用户 {} 开始获取 {} 的所有版本信息", user, resourceId);
            List<Version> versionList = versionService.selectVersionByPage(current, size, resourceId);
            if (versionList.size() > 0){
                message = Message.ok("成功获取版本信息");
                message.setMethod(URL_PREFIX + "getVersions");
                message.setStatus(0);
                ResourceVersionsVO resourceVersionsVO = new ResourceVersionsVO();
                resourceVersionsVO.setVersions(versionList);
                resourceVersionsVO.setResourceId(resourceId);
                resourceVersionsVO.setUser(user);
                message.data("ResourceVersions", resourceVersionsVO);
            }else{
                logger.warn("user {} 获取资源未报错，但是获取到的version长度为0", user, resourceId);
                message = Message.error("未能正确获取到版本信息");
                message.setMethod(URL_PREFIX + "getVersions");
                message.setStatus(2);
            }
            logger.info("用户 {} 结束获取 {} 的所有版本信息", user, resourceId);
        }catch(final Exception e){
            logger.error("user {} 获取resourceId {} 资源的版本信息失败", user, resourceId, e);
            throw new BmlQueryFailException("抱歉，查询版本信息失败");
        }

        return Message.messageToResponse(message);
    }

    @GET
    @Path("getResources")
    public Response getResources(@QueryParam("system") String system,
                                 @QueryParam("currentPage") String  currentPage,
                                 @QueryParam("pageSize") String pageSize,
                                 @Context HttpServletRequest request,
                                 @Context HttpServletResponse response)throws ErrorException {

        String user = RestfulUtils.getUserName(request);

        if (StringUtils.isEmpty(system)) {
            //默认系统是wtss
            system = Constant.DEFAULT_SYSTEM;
        }

        Integer current = 0;
        Integer size = 0;
        if (StringUtils.isEmpty(currentPage)  || !StringUtils.isNumeric(currentPage)){
            current = 1;
        }else{
            current = Integer.valueOf(currentPage);
        }
        if ( StringUtils.isEmpty(pageSize )  || !StringUtils.isNumeric(pageSize) ){
            size = 10;
        }else{
            size = Integer.valueOf(pageSize);
        }
        Message message = null;
        try{
            logger.info("用户 {} 开始获取系统 {} 的所有资源", user, system);
            List<ResourceVersion> resourceVersionPageInfoList  = versionService.selectResourcesViaSystemByPage(current, size, system, user);
            if (resourceVersionPageInfoList.size() > 0){
                message = Message.ok("获取您在系统" + system + "中所有资源成功");
                message.setStatus(0);
                message.setMethod(URL_PREFIX + "getResources");
                List<ResourceVO> resourceVOList = new ArrayList<>();
                for(ResourceVersion resourceVersion : resourceVersionPageInfoList){
                    ResourceVO resourceVO = new ResourceVO();
                    resourceVO.setResource(resourceVersion.getResource());
                    resourceVO.setUser(user);
                    resourceVO.setResourceId(resourceVersion.getResourceId());
                    resourceVO.setVersion(resourceVersion.getVersion());
                    resourceVOList.add(resourceVO);
                }
                message.data("Resources", resourceVOList);
            }else{
                logger.warn("用户 {} 获取系统 {} 资源的size为0", user, system);
                message = Message.error("未能成功获取到所有资源信息");
                message.setStatus(2);
                message.setMethod(URL_PREFIX + "getResources");
            }
            logger.info("用户 {} 结束获取系统 {} 的所有资源", user, system);
        }catch(final Exception e){
            logger.error("用户 {} 获取系统 {} 所有资源失败.",user, system, e);
            throw new BmlQueryFailException("获取系统所有资源信息失败");
        }

        return Message.messageToResponse(message);
    }



    @POST
    @Path("deleteVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVersion(JsonNode jsonNode,
                                  @Context HttpServletRequest request) throws IOException, ErrorException{


        String user = RestfulUtils.getUserName(request);

        if (null == jsonNode.get("resourceId") || null == jsonNode.get("version") ||
                StringUtils.isEmpty(jsonNode.get("resourceId").getTextValue()) || StringUtils.isEmpty(jsonNode.get("version").getTextValue())) {
            throw new BmlServerParaErrorException("删除指定版本，需要指定resourceId 和 version");
        }



        String resourceId = jsonNode.get("resourceId").getTextValue();
        String version = jsonNode.get("version").getTextValue();
        //检查资源和版本是否存在
        if (!resourceService.checkResourceId(resourceId) || !versionService.checkVersion(resourceId, version)
                || !versionService.canAccess(resourceId, version)){
            throw new BmlServerParaErrorException("传入的resourceId或version非法,或已删除");
        }
        Message message = null;
        ResourceTask resourceTask = taskService.createDeleteVersionTask(resourceId, version, user, HttpRequestHelper.getIp(request));
        try{
            logger.info("用户 {} 开始删除 resourceId: {} version: {} 的资源", resourceId, version);
            versionService.deleteResourceVersion(resourceId, version);
            message = Message.ok("删除版本成功");
            message.setMethod(URL_PREFIX + "deleteVersion");
            message.setStatus(0);
            logger.info("用户 {} 结束删除 resourceId: {} version: {} 的资源", resourceId, version);
            taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
            logger.info("删除版本成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.SUCCESS.getValue());
        }catch(final Exception e){
            logger.error("用户{}删除resource {}, version {} 失败", user, resourceId, version, e);
            taskService.updateState2Failed(resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
            logger.info("删除版本失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue());
            throw new BmlQueryFailException("删除资源版本失败");
        }
        return Message.messageToResponse(message);
    }

    @POST
    @Path("deleteResource")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteResource(JsonNode jsonNode,
                                   @Context HttpServletRequest request) throws IOException, ErrorException{


        String user = RestfulUtils.getUserName(request);

        if (null == jsonNode.get("resourceId")) {
            throw new BmlServerParaErrorException("您未传入有效的resourceId");
        }

        String resourceId = jsonNode.get("resourceId").getTextValue();
        if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
            logger.error("错误的resourceId  is {} ", resourceId);
            throw new BmlServerParaErrorException("resourceId:"+resourceId+"为空,非法或者已被删除!");
        }

        Message message = null;
        ResourceTask resourceTask = taskService.createDeleteResourceTask(resourceId, user, HttpRequestHelper.getIp(request));
        try{
            logger.info("用户 {}  开始删除 resourceId: {} 对应的所有资源", user, resourceId);
            resourceService.deleteResource(resourceId);
            message = Message.ok("删除资源成功");
            message.setMethod(URL_PREFIX + "deleteResource");
            message.setStatus(0);
            logger.info("用户 {}  结束删除 resourceId: {} 对应的所有资源", user, resourceId);
            taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
            logger.info("删除资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.SUCCESS.getValue());
        }catch(final Exception e){
            logger.error("用户 {} 删除资源 {} 失败", user, resourceId);
            taskService.updateState2Failed(resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
            logger.info("删除资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue());
            throw new BmlQueryFailException("删除资源操作失败");
        }

        return Message.messageToResponse(message);

    }

    @POST
    @Path("deleteResources")
    public Response deleteResources(JsonNode jsonNode,
                                    @Context HttpServletRequest request) throws IOException, ErrorException{
        String user = RestfulUtils.getUserName(request);
        List<String> resourceIds = new ArrayList<>();

        if (null == jsonNode.get("resourceIds")) {
            throw new BmlServerParaErrorException("批量删除未传入resourceIds参数");
        }

        Iterator<JsonNode> jsonNodeIter = jsonNode.get("resourceIds").getElements();
        while (jsonNodeIter.hasNext()) {
            resourceIds.add(jsonNodeIter.next().asText());
        }

        if (0 == resourceIds.size()) {
            throw new BmlServerParaErrorException("批量删除资源操作传入的resourceIds参数为空");
        }else{
            for (String resourceId:  resourceIds) {
                if (StringUtils.isBlank(resourceId) || !resourceService.checkResourceId(resourceId)) {
                    Message message = Message.error("resourceId:"+resourceId+"为空,非法或已被删除");
                    message.setMethod(URL_PREFIX + "deleteResources");
                    message.setStatus(1);
                    return Message.messageToResponse(message);
                }
            }
        }

        ResourceTask resourceTask = taskService.createDeleteResourcesTask(resourceIds, user, HttpRequestHelper.getIp(request));
        Message message = null;
        try{
            logger.info("用户 {} 开始批删除资源", user);
            resourceService.batchDeleteResources(resourceIds);
            message = Message.ok("批量删除资源成功");
            message.setMethod(URL_PREFIX + "deleteResources");
            message.setStatus(0);
            logger.info("用户 {} 结束批量删除资源", user);
            taskService.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
            logger.info("批量删除资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.SUCCESS.getValue());
        }catch(final Exception e){
            logger.error("用户 {} 批量删除资源失败", user, e);
            taskService.updateState2Failed(resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
            logger.info("批量删除资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue());
            throw new BmlQueryFailException("批量删除资源操作失败");
        }
        return Message.messageToResponse(message);
    }

    /**
     * 通过resourceId 和 version两个参数获取下载对应的资源
     * @param resourceId 资源Id
     * @param version 资源版本，如果不指定，默认为最新
     * @param resp httpServletResponse
     * @param request httpServletRequest
     * @return Response
     * @throws IOException
     * @throws ErrorException
     */
    @GET
    @Path("download")
    public Response download(@QueryParam("resourceId") String resourceId,
                             @QueryParam("version") String version,
                             @Context HttpServletResponse resp,
                             @Context HttpServletRequest request) throws IOException, ErrorException {
        String user = RestfulUtils.getUserName(request);

        if (StringUtils.isBlank(resourceId) || !resourceService.checkResourceId(resourceId)) {
            Message message = Message.error("resourceId:"+resourceId+"为空,非法或者已被删除!");
            message.setMethod(URL_PREFIX + "download");
            message.setStatus(1);
            return Message.messageToResponse(message);
        }

        if (!resourceService.checkAuthority(user, resourceId)){
            throw new BmlPermissionDeniedException("您没有权限下载此资源");
        }
        //判version空,返回最新版本
        if (StringUtils.isBlank(version)){
            version = versionService.getNewestVersion(resourceId);
        }
        //判version不存在或者非法
        if (!versionService.checkVersion(resourceId, version)) {
            Message message = Message.error("version:"+version+"不存在,非法或者已被删除");
            message.setMethod(URL_PREFIX + "download");
            message.setStatus(1);
            return Message.messageToResponse(message);
        }
        //判resourceId和version是否过期
        if (!resourceService.checkExpire(resourceId, version)){
            throw new BmlResourceExpiredException(resourceId);
        }

        Message message = null;
        resp.setContentType("application/x-msdownload");
        resp.setHeader("Content-Disposition", "attachment");
        String ip = HttpRequestHelper.getIp(request);
        DownloadModel downloadModel = new DownloadModel(resourceId, version,user, ip);
        try{
            logger.info("用户 {} 开始下载资源 resourceId is {}, version is {} ,ip is {} ", user, resourceId, version, ip);
            Map<String, Object> properties = new HashMap<>();
            boolean downloadResult = versionService.downloadResource(user, resourceId, version, resp.getOutputStream(), properties);
            downloadModel.setEndTime(new Date(System.currentTimeMillis()));
            downloadModel.setState(0);
            if (downloadResult){
                message = Message.ok("下载资源成功");
                message.setStatus(0);
                message.setMethod(URL_PREFIX + "download");
            }else{
                logger.warn("用户 {} 下载资源 resourceId: {}, version:{} 出现问题,复制的size小于0", user, resourceId, version);
                downloadModel.setState(1);
                message = Message.error("下载资源失败");
                message.setStatus(1);
                message.setMethod(URL_PREFIX + "download");
            }
            downloadService.addDownloadRecord(downloadModel);
            logger.info("用户 {} 结束下载资源 {} ", user, resourceId);
        }catch(IOException e){
            logger.error("用户 {} 下载资源 resourceId: {}, version:{} 出现IO异常", user, resourceId, version, e);
            downloadModel.setEndTime(new Date());
            downloadModel.setState(1);
            downloadService.addDownloadRecord(downloadModel);
            throw new ErrorException(73562, "抱歉,后台IO错误造成您本次下载资源失败");
        }catch(final Throwable t){
            logger.error("用户 {} 下载资源 resourceId: {}, version:{} 出现异常", user, resourceId, version, t);
            downloadModel.setEndTime(new Date());
            downloadModel.setState(1);
            downloadService.addDownloadRecord(downloadModel);
            throw new ErrorException(73561, "抱歉，后台服务出错导致您本次下载资源失败");
        }finally{
            IOUtils.closeQuietly(resp.getOutputStream());
        }
        logger.info("{} 下载资源 {} 成功", user, resourceId);
        return Message.messageToResponse(message);
    }

    @POST
    @Path("upload")
    public Response uploadResource(@Context HttpServletRequest req,
                                   @FormDataParam("system") String system,
                                   @FormDataParam("resourceHeader") String resourceHeader,
                                   @FormDataParam("isExpire") String isExpire,
                                   @FormDataParam("expireType") String expireType,
                                   @FormDataParam("expireTime") String expireTime,
                                   @FormDataParam("maxVersion") int maxVersion,
                                   FormDataMultiPart form) throws ErrorException {
        String user = RestfulUtils.getUserName(req);
        Message message;
        try{
            logger.info("用户 {} 开始上传资源", user);
            Map<String, Object> properties = new HashMap<>();
            properties.put("system", system);
            properties.put("resourceHeader", resourceHeader);
            properties.put("isExpire", isExpire);
            properties.put("expireType", expireType);
            properties.put("expireTime", expireTime);
            properties.put("maxVersion", maxVersion);
            String clientIp = HttpRequestHelper.getIp(req);
            properties.put("clientIp", clientIp);
            ResourceTask resourceTask = taskService.createUploadTask(form, user, properties);
            message = Message.ok("提交上传资源任务成功");
            message.setMethod(URL_PREFIX + "upload");
            message.setStatus(0);
            message.data("resourceId", resourceTask.getResourceId());
            message.data("version", resourceTask.getVersion());
            message.data("taskId", resourceTask.getId());
            logger.info("用户 {} 提交上传资源任务成功, resourceId is {}", user, resourceTask.getResourceId());
        } catch(final Exception e){
            logger.error("upload resource for user : {} failed, reason:", user, e);
            ErrorException exception = new ErrorException(50073, "提交上传资源任务失败:" + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
        return Message.messageToResponse(message);
    }

    /**
     * 用户通过http的方式更新资源文件
     * @param request httpServletRequest
     * @param resourceId 用户希望更新资源的resourceId
     * @param formDataMultiPart form表单内容
     * @return resourceId 以及 新的版本号
     */
    @POST
    @Path("updateVersion")
    public Response updateVersion(@Context HttpServletRequest request,
                                  @FormDataParam("resourceId") String resourceId,
                                  FormDataMultiPart formDataMultiPart)throws Exception{
        String user = RestfulUtils.getUserName(request);
        if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
            logger.error("错误的resourceId  is {} ", resourceId);
            throw new BmlServerParaErrorException("resourceId:"+resourceId+"为空,非法或者已被删除!");
        }
        if (StringUtils.isEmpty(versionService.getNewestVersion(resourceId))) {
            logger.error("resourceId:{} 之前未上传物料,或物料已被删除,请先调用上传接口.", resourceId);
            throw new BmlServerParaErrorException("resourceId:"+resourceId+" 之前未上传物料,或物料已被删除,请先调用上传接口.!");
        }
        Message message;
        try{
            logger.info("用户 {} 开始更新资源 {} ", user, resourceId);
            String clientIp = HttpRequestHelper.getIp(request);
            Map<String, Object> properties = new HashMap<>();
            properties.put("clientIp", clientIp);
            ResourceTask resourceTask = taskService.createUpdateTask(resourceId, user, formDataMultiPart, properties);
            message = Message.ok("提交更新资源任务成功");
            message.data("resourceId",resourceId).data("version", resourceTask.getVersion()).data("taskId", resourceTask.getId());
        }catch(final ErrorException e){
            logger.error("{} update resource failed, resourceId is {}, reason:", user, resourceId, e);
            throw e;
        } catch(final Exception e){
            logger.error("{} update resource failed, resourceId is {}, reason:", user, resourceId, e);
            ErrorException exception = new ErrorException(50073, "提交上传资源任务失败:" + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
        logger.info("用户 {} 结束更新资源 {} ", user, resourceId);
        return Message.messageToResponse(message);
    }


    @GET
    @Path("getBasic")
    public Response getBasic(@QueryParam("resourceId") String resourceId,
                             @Context HttpServletRequest request)throws ErrorException{
        String user = RestfulUtils.getUserName(request);

        if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)){
            throw new BmlServerParaErrorException("获取资源基本信息未传入resourceId参数或参数非法");
        }

        Message message = null;
        try{
            Resource resource = resourceService.getResource(resourceId);
            //int numberOfVersions = versionService.getNumOfVersions(resourceId);
            if (!resource.isEnableFlag()){
                logger.warn("用户 {} 想要查询的资源 {} 已经被删除", user, resourceId);
                message = Message.error("资源已经被删除");
            }else{
                logger.info("用户 {} 开始获取 {} 的基本信息", user, resourceId);
                ResourceBasicVO basic = new ResourceBasicVO();
                basic.setResourceId(resourceId);
                basic.setCreateTime(resource.getCreateTime());
                basic.setDownloadedFileName(resource.getDownloadedFileName());
                basic.setOwner(resource.getUser());
                //todo cooperyang 正确的版本信息
                basic.setNumberOfVerions(10);
                if (resource.isExpire()){
                    basic.setExpireTime(RestfulUtils.getExpireTime(resource.getCreateTime(),resource.getExpireType(), resource.getExpireTime()));
                }else{
                    basic.setExpireTime("资源不过期");
                }
                message = Message.ok("获取资源基本信息成功");
                message.setStatus(0);
                message.setMethod(URL_PREFIX + "getBasic");
                message.data("basic", basic);
                logger.info("用户 {} 结束获取 {} 的基本信息", user, resourceId);
            }
        }catch(final Exception e){
            logger.error("用户 {} 获取 {} 资源信息失败", user, resourceId, e);
            throw new BmlQueryFailException("获取资源基本信息失败");
        }

        return Message.messageToResponse(message);
    }


}
