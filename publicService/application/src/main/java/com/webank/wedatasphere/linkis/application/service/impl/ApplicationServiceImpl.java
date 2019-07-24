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

package com.webank.wedatasphere.linkis.application.service.impl;

import com.webank.wedatasphere.linkis.application.conf.ApplicationConfiguration;
import com.webank.wedatasphere.linkis.application.dao.ApplicationMapper;
import com.webank.wedatasphere.linkis.application.entity.*;
import com.webank.wedatasphere.linkis.application.exception.ApplicationException;
import com.webank.wedatasphere.linkis.application.service.ApplicationService;
import com.webank.wedatasphere.linkis.application.util.ApplicationUtil;
import com.webank.wedatasphere.linkis.application.util.ApplicationUtils;
import lombok.Cleanup;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by johnnwang on 2019/1/18.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    ApplicationMapper applicationMapper;

    @Override
    public void createApplication(Application application) {
        applicationMapper.insertWFApplication(application);
    }

    @Override
    public void removeApplication(long applicationId) {
        applicationMapper.deleteWFApplication(applicationId);
    }

    @Override
    public List<Application> listApplication(Long userId) {
        return applicationMapper.listWFApplication(userId);
    }

    @Override
    public Application openApplication(Long applicationId) {

        return applicationMapper.selectWFApplication(applicationId);
    }

    @Override
    public void updateApplication(Application application) throws IOException {
        Application oldApplication = applicationMapper.selectWFApplication(application.getId());
        application.setVersion(oldApplication.getVersion());
        Integer row = applicationMapper.updateWFApplication(application);
/*        if (application.getJsonPath() != null &&row !=0){
            ApplicationUtils.removeOldJson(oldApplication.getJsonPath());
        }*/
    }

    @Override
    public Long getUserIdByName(String creator) {
        return applicationMapper.selectUserIdByName(creator);
    }

    @Override
    public ApplicationUser getUserByName(String creator) {
        return applicationMapper.selectUserByName(creator);
    }

    @Override
    public Long addUser(ApplicationUser user) {
        applicationMapper.inserUser(user);
        return user.getId();
    }

    @Override
    public void updateFirstLoginStatus(Long id) {
        applicationMapper.updateUserFirstLoginStatus(id);
    }

    @Override
    public List<Project> listAllProject() {
        return applicationMapper.selectAllProject();
    }

    @Override
    public void saveProjectUser(ProjectUser projectUser) {
        try{
            applicationMapper.insertProjectUser(projectUser);
        }catch (Exception e){
            //Violation of the unique constraint, temporarily not processed(违反唯一约束，暂时不做处理)
            LOGGER.info(e.getMessage());
        }
    }


    @Override
    public void updateProjectUser(ProjectUser projectUser) {
        applicationMapper.updateProjectUser(projectUser);
    }


    @Override
    public List<Project> listInitedProject(Long userId) {
        return applicationMapper.listInitedProject(userId);
    }

    @Override
    public ProjectVO getProjectVO(Long userId, Long projectId) {
        return applicationMapper.getProjectVO(userId, projectId);
    }

    @Override
    public Boolean userInit(String userInitUrl, HttpServletRequest req) {
        if (StringUtils.isEmpty(userInitUrl)) {
            return false;
        }
        try {
            @Cleanup CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                    .setConnectionRequestTimeout(1000).setSocketTimeout(5000).build();
            HttpGet httpGet = new HttpGet(userInitUrl);
            httpGet.setConfig(config);
            LOGGER.info(req.getHeader("Cookie"));
            httpGet.setHeader("Cookie", req.getHeader("Cookie"));
            @Cleanup CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Initialize the user, the http request response status code is(初始化用户，http请求响应状态码为)："+statusCode);
            if(response.getStatusLine().getStatusCode()!=200){
                throw new ApplicationException("Http request response status code is abnormal(http请求响应状态码异常)");
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return false;
        }
        return true;
    }

    private String JSON_ROOT_PATH = ApplicationConfiguration.JSON_ROOT_PATH.getValue().toString();
    private String DEFAULT_APPLICATIO_NAME = ApplicationConfiguration.DEFAULT_APPLICATIO_NAME.getValue().toString();
    private Long INIT_ORGID = Long.valueOf(ApplicationConfiguration.INIT_ORGID.getValue().toString());

    @Override
    @Transactional
    public ApplicationUser firstLogin(HttpServletRequest req,String creator) throws IllegalAccessException, NoSuchFieldException, IOException {
        Date date = new Date();
        ApplicationUser user = createFirstLoginUser(creator,date);
        String jsonPath = JSON_ROOT_PATH +creator+"/application/"+ "application_"+creator+"_init.wf";
        createApplication(createFirstLoginApp(date,user.getId(),jsonPath));
        List<Project> allProjects = listAllProject();
        allProjects.stream().forEach(f ->{saveProjectUser(new ProjectUser().setUserId(user.getId()).setProjectId(f.getId()).setIsInitSuccess(false).setIsNewFeature(true));});
        allProjects.stream().filter(f ->f.getIsUserNeedInit()).forEach(f ->{
            Map<String, Object> map = userInitNew(f.getUserInitUrl(), req);
            Object isSucceed = map.getOrDefault("isSucceed", false);
            Object json = map.getOrDefault("json", "no userInitJson");
            updateProjectUser(new ProjectUser().setUserId(user.getId()).setJson((String) json).setProjectId(f.getId()).setIsInitSuccess((Boolean) isSucceed));
        });
        ApplicationUtils.createExampleFile(creator);
        String flows = ApplicationUtil.getFlowsJson(creator,date);
        ApplicationUtils.writeJsonToHDFSNew(jsonPath,creator,flows);
        return user;
    }

    @Override
    @Transactional
    public void notFirstLogin(ApplicationUser user, HttpServletRequest req) {
        if (user.getIsFirstLogin()){
            updateFirstLoginStatus(user.getId());
        }
        List<Project> initedProjects = listInitedProject(user.getId());
        initedProjects.stream().forEach(f ->{
            updateProjectUser(new ProjectUser().setProjectId(f.getId()).setUserId(user.getId()).setIsNewFeature(false));
        });
        List<Project> allProjects = listAllProject();
        allProjects.removeAll(initedProjects);
        allProjects.stream().forEach(f ->{saveProjectUser(new ProjectUser().setUserId(user.getId()).setProjectId(f.getId()).setIsInitSuccess(false).setIsNewFeature(true));});
        allProjects.stream().filter(f ->f.getIsUserNeedInit()).forEach(f ->{
            Map<String, Object> map = userInitNew(f.getUserInitUrl(), req);
            Object isSucceed = map.getOrDefault("isSucceed", false);
            Object json = map.getOrDefault("json", "no userInitJson");
            updateProjectUser(new ProjectUser().setUserId(user.getId()).setJson((String) json).setProjectId(f.getId()).setIsInitSuccess((Boolean) isSucceed));
        });
    }

    private Application createFirstLoginApp(Date date,Long userId,String jsonPath){
        Application workflowApplication = new Application()
                .setName(DEFAULT_APPLICATIO_NAME)
                .setCreateTime(date)
                .setUpdateTime(date)
                .setUserId(userId)
                .setDescription(DEFAULT_APPLICATIO_NAME)
                .setVersion("1.0.0")
                .setIsPublished(false)
                .setIsTransfer(false)
                .setVisibility(false)
                .setInitialOrgId(INIT_ORGID)
                .setSource("create by system")
                .setJsonPath(jsonPath)
                .setIsAsh(false);
        return workflowApplication;
    }

    private ApplicationUser createFirstLoginUser(String creator,Date date){
        ApplicationUser user = new ApplicationUser()
                .setIsFirstLogin(true)
                .setUpdateTime(date)
                .setCreateTime(date)
                .setName(creator)
                .setUserName(creator);
        Long userId = addUser(user);
        user.setId(userId);
        return user;
    }


    public Map<String,Object> userInitNew(String userInitUrl, HttpServletRequest req) {
        HashMap<String, Object> objectHashMap = new HashMap<>();
        if (StringUtils.isEmpty(userInitUrl)) {
            return objectHashMap;
        }
        try {
            @Cleanup CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                    .setConnectionRequestTimeout(1000).setSocketTimeout(5000).build();
            HttpGet httpGet = new HttpGet(userInitUrl);
            httpGet.setConfig(config);
            LOGGER.info(req.getHeader("Cookie"));
            httpGet.setHeader("Cookie", req.getHeader("Cookie"));
            @Cleanup CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Initialize the user, the http request response status code is(初始化用户，http请求响应状态码为)："+statusCode);
            if(response.getStatusLine().getStatusCode()!=200){
                throw new ApplicationException("Http request response status code is abnormal(http请求响应状态码异常)");
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return objectHashMap;
        }
        objectHashMap.put("isSucceed",true);
        // TODO: 2019/5/5 Encapsulation returns json(封装返回json)
        //objectHashMap.put("json","");
        return objectHashMap;
    }

    /*@Override
    public void register(ProjectRegister projectRegister) {
        LOGGER.info(projectRegister.name()+"服务正在进行注册！");
        Project project = new Project();
        project.setName(projectRegister.name());
        project.setIsProjectNeedInit(projectRegister.isProjectNeedInit());
        project.setIsUserNeedInit(projectRegister.isUserNeedInit());
        project.setJson(projectRegister.projectInitJson());
        project.setLevel(projectRegister.level());
        project.setUrl(projectRegister.url());
        project.setUserInitUrl(projectRegister.userInitUrl());
        if(projectRegister.isProjectNeedInit()){
            project.setIsProjectInited(true);
        }else {
            project.setIsProjectInited(false);
        }
        synchronized (this){
            Project projectExist = applicationMapper.getPorjectByName(projectRegister.name());
            if (projectExist ==null){
                applicationMapper.register(project);
            }else {
                applicationMapper.updateProjectByName(project);
            }
        }

    }*/
}
