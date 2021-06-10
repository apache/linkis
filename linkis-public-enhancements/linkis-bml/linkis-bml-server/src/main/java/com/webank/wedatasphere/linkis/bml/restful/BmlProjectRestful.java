/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import com.webank.wedatasphere.linkis.bml.Entity.ResourceTask;
import com.webank.wedatasphere.linkis.bml.common.BmlPermissionDeniedException;
import com.webank.wedatasphere.linkis.bml.common.BmlProjectNoEditException;
import com.webank.wedatasphere.linkis.bml.common.BmlResourceExpiredException;
import com.webank.wedatasphere.linkis.bml.common.BmlServerParaErrorException;
import com.webank.wedatasphere.linkis.bml.service.*;
import com.webank.wedatasphere.linkis.bml.util.HttpRequestHelper;
import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static com.webank.wedatasphere.linkis.bml.restful.BmlRestfulApi.URL_PREFIX;

@Path("bml")
@Component
public class BmlProjectRestful {

    private static final Logger LOGGER = LoggerFactory.getLogger(BmlProjectRestful.class);

    private static final String PROJECT_NAME_STR = "projectName";
    private static final String EDIT_USERS_STR = "editUsers";
    private static final String ACCESS_USERS_STR = "accessUsers";
    public static final String DEFAULT_PROXY_USER = "hadoop";

    @Autowired
    private BmlProjectService bmlProjectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DownloadService downloadService;

    @POST
    @Path("createBmlProject")
    public Response createBmlProject(@Context HttpServletRequest request, JsonNode jsonNode){
        String username = SecurityFilter.getLoginUsername(request);
        String projectName = jsonNode.get(PROJECT_NAME_STR).getTextValue();
        LOGGER.info("{} begins to create a project {} in bml", username, projectName);
        JsonNode editUserNode = jsonNode.get(EDIT_USERS_STR);
        JsonNode accessUserNode = jsonNode.get(ACCESS_USERS_STR);
        List<String> accessUsers = new ArrayList<>();
        List<String> editUsers = new ArrayList<>();
        if (editUserNode.isArray()){
            for (JsonNode node : editUserNode) {
                editUsers.add(node.getTextValue());
            }
        }
        if (accessUserNode.isArray()){
            for (JsonNode node : accessUserNode) {
                accessUsers.add(node.getTextValue());
            }
        }
        bmlProjectService.createBmlProject(projectName, username, editUsers, accessUsers);

        return Message.messageToResponse(Message.ok("success to create project(创建工程ok)"));
    }

    @POST
    @Path("uploadShareResource")
    public Response uploadShareResource(@Context HttpServletRequest request, @FormDataParam("system") String system,
                                        @FormDataParam("resourceHeader") String resourceHeader,
                                        @FormDataParam("isExpire") String isExpire,
                                        @FormDataParam("expireType") String expireType,
                                        @FormDataParam("expireTime") String expireTime,
                                        @FormDataParam("maxVersion") int maxVersion,
                                        @FormDataParam("projectName") String projectName,
                                        FormDataMultiPart form) throws ErrorException{
        String username = SecurityFilter.getLoginUsername(request);
        Message message;
        try{
            LOGGER.info("User {} starts uploading shared resources and is proxied as a Hadoop user for uploading(用户 {} 开始上传共享资源,会被代理成hadoop用户进行上传)", username,username);
            if (!bmlProjectService.checkEditPriv(projectName, username)){
                LOGGER.error("{} does not have edit permission on project {}. Upload resource failed ({} 对工程 {} 没有编辑权限, 上传资源失败)", username, projectName, username, projectName);
                throw new BmlProjectNoEditException(username + "does not have edit permission on project " + projectName + ". Upload resource failed"+username + " 对工程 { " + projectName + " }没有编辑权限,上传资源失败");
            }
            Map<String, Object> properties = new HashMap<>();
            properties.put("system", system);
            properties.put("resourceHeader", resourceHeader);
            properties.put("isExpire", isExpire);
            properties.put("expireType", expireType);
            properties.put("expireTime", expireTime);
            properties.put("maxVersion", maxVersion);
            String clientIp = HttpRequestHelper.getIp(request);
            properties.put("clientIp", clientIp);
            ResourceTask resourceTask = taskService.createUploadTask(form, DEFAULT_PROXY_USER, properties);
            bmlProjectService.addProjectResource(resourceTask.getResourceId(), projectName);
            message = Message.ok("The task of submitting and uploading resources was successful(提交上传资源任务成功)");
            message.data("resourceId", resourceTask.getResourceId());
            message.data("version", resourceTask.getVersion());
            message.data("taskId", resourceTask.getId());
            LOGGER.info("The task of submitting and uploading resources was successful (用户 {} 提交上传资源任务成功), resourceId is {}", username, resourceTask.getResourceId());
        } catch(final Exception e){
            LOGGER.error("upload resource for user : {} failed, reason:", username, e);
            ErrorException exception = new ErrorException(50073, "The commit upload resource task failed: (提交上传资源任务失败)" + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
        return Message.messageToResponse(message);

    }




    @POST
    @Path("updateShareResource")
    public Response updateShareResource(@Context HttpServletRequest request,
                                        @FormDataParam("resourceId") String resourceId,
                                        FormDataMultiPart form)throws ErrorException{
        String username = SecurityFilter.getLoginUsername(request);
        if (StringUtils.isEmpty(resourceId) || !resourceService.checkResourceId(resourceId)) {
            LOGGER.error("the error resourceId  is {} ", resourceId);
            throw new BmlServerParaErrorException("the resourceId: " + resourceId + "is Null, illegal, or deleted (resourceId: " + resourceId + " 为空,非法或者已被删除!)");
        }
        if (StringUtils.isEmpty(versionService.getNewestVersion(resourceId))) {
            LOGGER.error("If the material has not been uploaded or has been deleted, please call the upload interface first(resourceId:{} 之前未上传物料,或物料已被删除,请先调用上传接口.)", resourceId);
            throw new BmlServerParaErrorException("If the material has not been uploaded or has been deleted, please call the upload interface first (resourceId: " + resourceId + " 之前未上传物料,或物料已被删除,请先调用上传接口.!)");
        }
        Message message;
        try{
            String projectName = bmlProjectService.getProjectNameByResourceId(resourceId);
            if (!bmlProjectService.checkEditPriv(projectName, username)){
                LOGGER.error("{} does not have edit permission on project {}. Upload resource failed ({} 对工程 {} 没有编辑权限, 上传资源失败)", username, projectName, username, projectName);
                throw new BmlProjectNoEditException(username +"does not have edit permission on project: "+projectName+". Upload resource failed ("+username + " 对工程 { " + projectName + " }没有编辑权限,上传资源失败");
            }
            LOGGER.info("User {} starts updating resources {}, using proxy user Hadoop (用户 {} 开始更新资源 {},使用代理用户hadoop)", username, resourceId,username, resourceId);
            String clientIp = HttpRequestHelper.getIp(request);
            Map<String, Object> properties = new HashMap<>();
            properties.put("clientIp", clientIp);
            ResourceTask resourceTask = null;
            synchronized (resourceId.intern()){
                resourceTask = taskService.createUpdateTask(resourceId, DEFAULT_PROXY_USER, form, properties);
            }
            message = Message.ok("The update resource task was submitted successfully(提交更新资源任务成功)");
            message.data("resourceId",resourceId).data("version", resourceTask.getVersion()).data("taskId", resourceTask.getId());
        }catch(final ErrorException e){
            LOGGER.error("{} update resource failed, resourceId is {}, reason:", username, resourceId, e);
            throw e;
        } catch(final Exception e){
            LOGGER.error("{} update resource failed, resourceId is {}, reason:", username, resourceId, e);
            ErrorException exception = new ErrorException(50073, "The commit upload resource task failed(提交上传资源任务失败):" + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
        LOGGER.info("User {} ends updating resources {} (用户 {} 结束更新资源 {} )", username, resourceId, username, resourceId);
        return Message.messageToResponse(message);
    }

    @GET
    @Path("downloadShareResource")
    public Response downloadShareResource(@QueryParam("resourceId") String resourceId,
                             @QueryParam("version") String version,
                             @Context HttpServletResponse resp,
                             @Context HttpServletRequest request) throws IOException, ErrorException {
        String user = RestfulUtils.getUserName(request);
        Message message = null;
        resp.setContentType("application/x-msdownload");
        resp.setHeader("Content-Disposition", "attachment");
        String ip = HttpRequestHelper.getIp(request);
        DownloadModel downloadModel = new DownloadModel(resourceId, version,user, ip);
        try{
            String projectName = bmlProjectService.getProjectNameByResourceId(resourceId);
            if (!bmlProjectService.checkAccessPriv(projectName, user)){
                LOGGER.error("{} does not have view privileges on project {}. Download resource failed({} 对工程 {} 没有查看权限, 下载资源失败)", user, projectName, user, projectName);
                throw new BmlProjectNoEditException(user+" does not have view privileges on project "+projectName+". Download resource failed"+ user + " 对工程 { " + projectName + " }没有编辑权限,上传资源失败");
            }
            LOGGER.info("user {} begin to downLoad resource resourceId is {}, version is {} ,ip is {}, 并代理成hadoop ", user, resourceId, version, ip);
            Map<String, Object> properties = new HashMap<>();
            boolean downloadResult = versionService.downloadResource(DEFAULT_PROXY_USER, resourceId, version, resp.getOutputStream(), properties);
            downloadModel.setEndTime(new Date(System.currentTimeMillis()));
            downloadModel.setState(0);
            if (downloadResult){
                message = Message.ok("Download resource successfully(下载资源成功)");
                message.setStatus(0);
                message.setMethod(URL_PREFIX + "download");
            }else{
                LOGGER.warn("ResourceId :{}, version:{} has a problem when user {} downloads the resource. The copied size is less than 0 (用户 {} 下载资源 resourceId: {}, version:{} 出现问题,复制的size小于0)", user, resourceId, version,user, resourceId, version);
                downloadModel.setState(1);
                message = Message.error("Failed to download the resource(下载资源失败)");
                message.setStatus(1);
                message.setMethod(URL_PREFIX + "download");
            }
            downloadService.addDownloadRecord(downloadModel);
            LOGGER.info("User {} ends downloading the resource {} (用户 {} 结束下载资源 {}) ", user, resourceId, user, resourceId);
        }catch(IOException e){
            LOGGER.error("IO Exception: ResourceId :{}, version:{} (用户 {} 下载资源 resourceId: {}, version:{} 出现IO异常)",  resourceId, version,user, resourceId, version, e);
            downloadModel.setEndTime(new Date());
            downloadModel.setState(1);
            downloadService.addDownloadRecord(downloadModel);
            throw new ErrorException(73562, "Sorry, the background IO error caused you to download the resources failed(抱歉,后台IO错误造成您本次下载资源失败)");
        }catch(final Throwable t){
            LOGGER.error("ResourceId :{}, version:{} abnormal when user {} downloads resource (用户 {} 下载资源 resourceId: {}, version:{} 出现异常)",  resourceId, version,user,user, resourceId, version );
            downloadModel.setEndTime(new Date());
            downloadModel.setState(1);
            downloadService.addDownloadRecord(downloadModel);
            throw new ErrorException(73561, "Sorry, the background service error caused you to download the resources failed (抱歉，后台服务出错导致您本次下载资源失败)");
        }finally{
            IOUtils.closeQuietly(resp.getOutputStream());
        }
        LOGGER.info("{} Download resource {} successfully ({} 下载资源 {} 成功)", user, resourceId, user, resourceId);
        return Message.messageToResponse(message);
    }



    @GET
    @Path("getProjectInfo")
    public Response getProjectInfo(@Context HttpServletRequest request, @QueryParam("projectName") String projectName){
        return Message.messageToResponse(Message.ok("Obtain project information successfully (获取工程信息成功)"));
    }


    @POST
    @Path("attachResourceAndProject")
    public Response attachResourceAndProject(@Context HttpServletRequest request, JsonNode jsonNode) throws ErrorException{
        String username = SecurityFilter.getLoginUsername(request);
        String projectName = jsonNode.get(PROJECT_NAME_STR).getTextValue();
        String resourceId = jsonNode.get("resourceId").getTextValue();
        LOGGER.info("begin to attach {}  and {}", projectName, username);
        bmlProjectService.attach(projectName, resourceId);
        return Message.messageToResponse(Message.ok("attach resource and project ok"));
    }

    @POST
    @Path("updateProjectUsers")
    public Response updateProjectUsers(@Context HttpServletRequest request, JsonNode jsonNode) throws ErrorException{
        String username = SecurityFilter.getLoginUsername(request);
        String projectName = jsonNode.get("projectName").getTextValue();
        LOGGER.info("{} begins to update project users for {}", username, projectName);
        List<String> editUsers = new ArrayList<>();
        List<String> accessUsers = new ArrayList<>();
        JsonNode editUsersNode = jsonNode.get("editUsers");
        if (editUsersNode.isArray()){
            editUsersNode.forEach(node -> editUsers.add(node.getTextValue()));
        }
        JsonNode accessUsersNode = jsonNode.get("accessUsers");
        if (accessUsersNode.isArray()){
            accessUsersNode.forEach(node -> accessUsers.add(node.getTextValue()));
        }
        bmlProjectService.updateProjectUsers(username, projectName, editUsers, accessUsers);
        return Message.messageToResponse(Message.ok("Updated project related user success(更新工程的相关用户成功)"));
    }



}
