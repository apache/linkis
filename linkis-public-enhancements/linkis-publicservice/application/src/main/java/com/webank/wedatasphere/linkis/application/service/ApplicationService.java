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

package com.webank.wedatasphere.linkis.application.service;

import com.webank.wedatasphere.linkis.application.entity.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created by johnnwang on 2019/1/18.
 */
public interface ApplicationService {
    void createApplication(Application application);

    void removeApplication(long applicationId);

    List<Application> listApplication(Long userId);

    Application openApplication(Long applicationId);

    void updateApplication(Application application) throws IOException;

    Long getUserIdByName(String creator);

    ApplicationUser getUserByName(String creator);

    Long addUser(ApplicationUser user);

    void updateFirstLoginStatus(Long id);

    List<Project> listAllProject();

    void saveProjectUser(ProjectUser projectUser);

    void updateProjectUser(ProjectUser projectUser);

    List<Project> listInitedProject(Long userId);

    ProjectVO getProjectVO(Long userId, Long projectId);

    Boolean userInit(String userInitUrl,HttpServletRequest req);

    ApplicationUser firstLogin(HttpServletRequest req,String creator) throws IllegalAccessException, NoSuchFieldException, IOException;

    void notFirstLogin(ApplicationUser user, HttpServletRequest req);

    //void register(ProjectRegister projectRegister);
}
