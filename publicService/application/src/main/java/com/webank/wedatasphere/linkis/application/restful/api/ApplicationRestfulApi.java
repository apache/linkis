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

package com.webank.wedatasphere.linkis.application.restful.api;


import com.webank.wedatasphere.linkis.application.conf.ApplicationConfiguration;
import com.webank.wedatasphere.linkis.application.entity.*;
import com.webank.wedatasphere.linkis.application.exception.ApplicationException;
import com.webank.wedatasphere.linkis.application.util.ApplicationUtil;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.storage.FSFactory;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import com.webank.wedatasphere.linkis.application.service.ApplicationService;
import com.webank.wedatasphere.linkis.application.util.ApplicationUtils;
import lombok.Cleanup;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by johnnwang on 2019/1/18.
 */
@Component
@Path("/application")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationRestfulApi {

    @Autowired
    ApplicationService applicationService;

    @POST
    @Path("createApplication")
    public Response createApplication(@Context HttpServletRequest req, JsonNode json) throws IOException, ApplicationException {
        String creator = SecurityFilter.getLoginUsername(req);
        Long userId = applicationService.getUserIdByName(creator);
        String applicationName = json.get("name").getTextValue();
        String description = json.get("description").getTextValue();
        if (ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(applicationName)||
                ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(description)){
            throw new ApplicationException("Cannot create a name or describe it as(不能创建名字或者描述为)"+ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString()+"Applications(的应用)");
        }
        //The front end needs to be escaped(前端需要转义)
        String flows = json.get("flows").getTextValue();
        String jsonPath = ApplicationUtils.writeJsonToHDFS(creator,flows,false);
        Application workflowApplication = new Application()
                .setName(applicationName)
                .setCreateTime(new Date())
                .setUpdateTime(new Date())
                .setUserId(userId)
                .setDescription(description)
                .setVersion("1.0.0")
                .setIsPublished(false)
                .setIsTransfer(false)
                .setVisibility(false)
                .setSource("create by user")
                .setJsonPath(jsonPath)
                .setIsAsh(false);
        applicationService.createApplication(workflowApplication);
        return Message.messageToResponse(Message.ok());
    }



    @POST
    @Path("removeApplication")
    public Response removeApplication(@Context HttpServletRequest req, JsonNode json) throws IOException, ApplicationException {
        long applicationId = json.get("applicationId").getLongValue();
        Application oldApplication = applicationService.openApplication(applicationId);
        if(ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(oldApplication.getName())){
            throw new ApplicationException(ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString()+"Cannot be deleted(不能被删除)");
        }
        applicationService.removeApplication(applicationId);
        ApplicationUtils.removeOldJson(oldApplication.getJsonPath());
        return Message.messageToResponse(Message.ok());
    }


    @POST
    @Path("updateApplication")
    public Response updateApplication(@Context HttpServletRequest req, JsonNode json) throws IOException, ApplicationException {
        String applicationName =null;
        String description = null;
        String flows = null;
        try {
            applicationName = json.get("name").getTextValue();
        }catch (NullPointerException e){
        }
        try {
            description = json.get("description").getTextValue();
        }catch (NullPointerException e){
        }
        try {
            flows = json.get("flows").getTextValue();
        }catch (NullPointerException e){
        }
        long applicationId = json.get("applicationId").getLongValue();
        Application oldApplication = applicationService.openApplication(applicationId);
        if (ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(oldApplication.getName())){
            if (applicationName !=null || description !=null){
                throw new ApplicationException(ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString()+"Cannot modify name and description(不能修改名字和描述)");
            }
        }
        if (ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(applicationName)||
                ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString().equals(description)){
            throw new ApplicationException("Cannot change the name or description of the app to(不能将应用的名字或者描述改为)"+ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString()+"Applications(的应用)");
        }
        String creator = SecurityFilter.getLoginUsername(req);
        if (flows != null){
            ApplicationUtils.updateFile(creator, flows,oldApplication.getJsonPath());
        }
        Application workflowApplication = new Application().setName(applicationName).setDescription(description).setId(applicationId).setUpdateTime(new Date());
        applicationService.updateApplication(workflowApplication);
        return Message.messageToResponse(Message.ok());
    }


    @GET
    @Path("get")
    public Response getApplication(@Context HttpServletRequest req, @PathVariable Long applicationId) {
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/application/list")
    public Response listApplication(@Context HttpServletRequest req) throws IOException {
        String creator = SecurityFilter.getLoginUsername(req);
        Long userId = applicationService.getUserIdByName(creator);
        List<Application> workflowApplications = applicationService.listApplication(userId);
        return Message.messageToResponse(Message.ok().data("applications",workflowApplications));
    }


    @GET
    @Path("/{applicationId}/open")
    public Response openApplication(@Context HttpServletRequest req, @PathParam("applicationId") Long applicationId) throws IOException, ApplicationException {
        Application wf = applicationService.openApplication(applicationId);
        FsPath fsPath = new FsPath(wf.getJsonPath());
        FileSystem fs = (FileSystem)FSFactory.getFs(fsPath);
        fs.init(null);
        if(!fs.exists(fsPath)){
            throw new ApplicationException("The data development save file does not exist. Please delete the application!(数据开发保存文件不存在，请删除该应用)！");
        }
        @Cleanup InputStream is = fs.read(fsPath);
        @Cleanup BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null){
            stringBuffer.append(line);
        }
        return Message.messageToResponse(Message.ok().data("flows",stringBuffer.toString()));
    }


    @POST
    @Path("register")
    public Response regist(@Context HttpServletRequest req, JsonNode json) throws ApplicationException {
        return Message.messageToResponse(Message.ok());
    }


    @GET
    @Path("getBaseInfo")
    public Response getBaseInfo(@Context HttpServletRequest req) throws IOException, ApplicationException, NoSuchFieldException, IllegalAccessException {
        String creator = SecurityFilter.getLoginUsername(req);
        //String creator = "harrywan";
        ApplicationUser user = applicationService.getUserByName(creator);
        Boolean isFirstLogin = true;
        Long userId;
        if (user == null){
            user = new ApplicationUser().setIsFirstLogin(true)
                    .setUpdateTime(new Date())
                    .setCreateTime(new Date())
                    .setName(creator)
                    .setUserName(creator);
            userId = applicationService.addUser(user);
            Date date = new Date();
            ApplicationUtils.createExampleFile(creator);
            String flows = ApplicationUtil.getFlowsJson(creator,date);
            String jsonPath = ApplicationUtils.writeJsonToHDFS(creator,flows,true);
            //String jsonPath = "test";
            Application workflowApplication = new Application()
                    .setName(ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString())
                    .setCreateTime(date)
                    .setUpdateTime(date)
                    .setUserId(userId)
                    .setDescription(ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString())
                    .setVersion("1.0.0")
                    .setIsPublished(false)
                    .setIsTransfer(false)
                    .setVisibility(false)
                    .setInitialOrgId(Long.valueOf(ApplicationConfiguration.INIT_ORGID.getValue().toString()))
                    .setSource("create by system")
                    .setJsonPath(jsonPath)
                    .setIsAsh(false);
            applicationService.createApplication(workflowApplication);
            //// TODO: find the project that needs user initialization in the step1 projectList table, and
            //Need product initialization, product initialization, user initialization required
            //Need product initialization, product initialization, no user initialization
            //Need product initialization, the product has not been initialized (or initialization failed), requires user initialization
            //Need product initialization, the product has not been initialized (or initialization failed), no user initialization required
            //No product initialization is required (whether product initialization is required, it feels like it needs to return url, and json), requires user initialization
            //No product initialization is required, no user initialization is required
            //// TODO: step1 projectList表中找出需要用户初始化的项目，且
            //需要产品初始化的，已经产品初始化，需要用户初始化
            //需要产品初始化的，已经产品初始化，不需要用户初始化
            //需要产品初始化的，产品尚未初始化（或者初始化失败），需要用户初始化
            //需要产品初始化的，产品尚未初始化（或者初始化失败），不需要用户初始化
            //不需要产品初始化（是否需要产品初始化，感觉就是是否需要返回url，和json），需要用户初始化
            //不需要产品初始化，不需要用户初始化

            //Select the projectList table to view(选择projectList表，查看)
            /*List<Project> projects = */
            List<Project> allProjects = applicationService.listAllProject();
            allProjects.stream().forEach(f ->{applicationService.saveProjectUser(new ProjectUser().setUserId(userId).setProjectId(f.getId()).setIsInitSuccess(false).setIsNewFeature(true));});
            allProjects.stream().filter(f ->f.getIsUserNeedInit()).forEach(f ->{
                //todo Send http request or rpc request and return json（发送http请求 or  rpc请求 并返回json）
                Boolean isSucceed = applicationService.userInit(f.getUserInitUrl(),req);
                String json = "user";
                //Update the record of projectUser（更新projectUser的记录）
                applicationService.updateProjectUser(new ProjectUser().setUserId(userId).setJson(json).setProjectId(f.getId()).setIsInitSuccess(isSucceed));
            });
            //Returns the projectList filter condition (the init project has been init, but there is no init success, and the level is not high proj)
            //返回projectList过滤条件（已经init的project   需要init，但是没有init成功，而且level还不高的proj）
        }else {
            isFirstLogin = false;
            userId = user.getId();
            if (user.getIsFirstLogin()){
                applicationService.updateFirstLoginStatus(userId);
            }
            //Update last user initialization status（更新上次的用户初始化状态）
            //1. Use projecId, association, userId to filter, get the project, update its corresponding user's isNewFreature
            //1.用projecId，关联，userId过滤，得到的project，更新其对应的user的isNewFreature
            List<Project> initedProjects = applicationService.listInitedProject(userId);
            initedProjects.stream().forEach(f ->{
                applicationService.updateProjectUser(new ProjectUser().setProjectId(f.getId()).setUserId(userId).setIsNewFeature(false));
            });
            //2.Find all the projects, and the above collection is poor, that is, the newly registered project, the call to insert data and so on.
            //2.查出所有的projects，和上面的集合做差，就是新注册的project，对其调用插入数据等等记录即可
            List<Project> allProjects = applicationService.listAllProject();
            allProjects.removeAll(initedProjects);
            allProjects.stream().forEach(f ->{applicationService.saveProjectUser(new ProjectUser().setUserId(userId).setProjectId(f.getId()).setIsInitSuccess(false).setIsNewFeature(true));});
            allProjects.stream().filter(f ->f.getIsUserNeedInit()).forEach(f ->{
                //todo Send http request or rpc request and return json（发送http请求 or  rpc请求 并返回json）
                Boolean isSucceed = applicationService.userInit(f.getUserInitUrl(),req);
                String json = "user";
                //Update the record of projectUser（更新projectUser的记录）
                applicationService.updateProjectUser(new ProjectUser().setUserId(userId).setJson(json).setProjectId(f.getId()).setIsInitSuccess(isSucceed));
            });
        }
        //step:last：Last query data（最后查询数据)

        //Finally query all the data and return it to the front end.(最后查询出所有的数据，返回给前端)
        List<Project> allProjects = applicationService.listAllProject();
        if (allProjects.stream()
                .filter(f ->f.getIsProjectNeedInit()&&!f.getIsProjectInited()&&f.getLevel()>Integer.valueOf(ApplicationConfiguration.ISASH_LEVEL.getValue().toString()))
                .collect(Collectors.toList()).size()>0){
            throw new ApplicationException("There are key projects that have not been initialized yet!(有关键性project尚未进行初始化！)");
        };
/*        allProjects.stream()
                .filter(f ->f.getIsProjectInited()||(f.getIsProjectNeedInit() &&f.getLevel()<=Integer.valueOf(ApplicationConfiguration.ISASH_LEVEL.getValue().toString())))
                .forEach(f ->{if (!f.getIsProjectInited()){f.setIsAsh(true);}else {f.setIsAsh(false);}});*/
        List<ProjectVO> projectVOs = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        allProjects.stream().forEach(f ->{
            ProjectVO projectVO = applicationService.getProjectVO(userId, f.getId());
            if (projectVO.getIsProjectNeedInit() &&!projectVO.getIsProjectInited()&&projectVO.getLevel() <= Integer.valueOf(ApplicationConfiguration.ISASH_LEVEL.getValue().toString())){
                projectVO.setIsAsh(true);
            }else {
                projectVO.setIsAsh(false);
            }
            projectVOs.add(projectVO);
        });
        ArrayList<Org> orgs = new ArrayList<>();
        ArrayList<Role> roles = new ArrayList<>();
        ArrayList<Right> rights = new ArrayList<>();
        orgs.add(new Org());
        roles.add(new Role());
        rights.add(new Right());
        userInfo.setOrg(orgs);
        userInfo.setRight(rights);
        userInfo.setRole(roles);
        Map<String, String> basic = userInfo.getBasic();
        basic.put("userName",user.getUserName());
        basic.put("name",user.getName());
        basic.put("avartar",user.getAvatar());
        basic.put("email",user.getEmail());
        basic.put("isFirstLogin",String.valueOf(isFirstLogin));
        DWSParams dwsParams = new DWSParams();
        dwsParams.setFAQ(ApplicationConfiguration.DWS_PARAMS.getValue().toString());
        return Message.messageToResponse(Message.ok().data("projectMsg",projectVOs).data("userInfo",userInfo).data("DWSParams",dwsParams));
    }

    private Integer ISASH_LEVEL = Integer.valueOf(ApplicationConfiguration.ISASH_LEVEL.getValue().toString());
    private String DWS_PARAMS = ApplicationConfiguration.DWS_PARAMS.getValue().toString();
    @GET
    @Path("getBaseInfoNew")
    public Response getBaseInfoNew(@Context HttpServletRequest req) throws ApplicationException, IllegalAccessException, NoSuchFieldException, IOException {
        String creator = SecurityFilter.getLoginUsername(req);
        //String creator = "harrywan";
        ApplicationUser user = applicationService.getUserByName(creator);
        Boolean isFirstLogin = true;
        if (user == null){
            user = applicationService.firstLogin(req,creator);
        }else {
            isFirstLogin = false;
            applicationService.notFirstLogin(user,req);
        }
        Long userId = user.getId();
        List<Project> allProjects = applicationService.listAllProject();
        if (allProjects.stream()
                .filter(f ->f.getIsProjectNeedInit()&&!f.getIsProjectInited()&&f.getLevel()>ISASH_LEVEL)
                .collect(Collectors.toList()).size()>0){
            throw new ApplicationException("There are key projects that have not been initialized yet!(有关键性project尚未进行初始化！)");
        }
        List<ProjectVO> projectVOs = new ArrayList<>();
        allProjects.stream().forEach(f ->{
            ProjectVO projectVO = applicationService.getProjectVO(userId, f.getId());
            if (projectVO.getIsProjectNeedInit() &&!projectVO.getIsProjectInited()&&projectVO.getLevel() <= ISASH_LEVEL){
                projectVO.setIsAsh(true);
            }else {
                projectVO.setIsAsh(false);
            }
            projectVOs.add(projectVO);
        });
        // TODO: 2019/5/5 Optimized when there is logic in the follow-up(后续有逻辑的时候才进行优化)
        UserInfo userInfo = new UserInfo();
        ArrayList<Org> orgs = new ArrayList<>();
        ArrayList<Role> roles = new ArrayList<>();
        ArrayList<Right> rights = new ArrayList<>();
        orgs.add(new Org());
        roles.add(new Role());
        rights.add(new Right());
        userInfo.setOrg(orgs);
        userInfo.setRight(rights);
        userInfo.setRole(roles);
        Map<String, String> basic = userInfo.getBasic();
        basic.put("userName",user.getUserName());
        basic.put("name",user.getName());
        basic.put("avartar",user.getAvatar());
        basic.put("email",user.getEmail());
        basic.put("isFirstLogin",String.valueOf(isFirstLogin));
        DWSParams dwsParams = new DWSParams();
        dwsParams.setFAQ(DWS_PARAMS);
        return Message.messageToResponse(Message.ok().data("projectMsg",projectVOs).data("userInfo",userInfo).data("DWSParams",dwsParams));
    }
}
