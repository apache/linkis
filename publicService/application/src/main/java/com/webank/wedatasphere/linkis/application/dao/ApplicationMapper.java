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

package com.webank.wedatasphere.linkis.application.dao;

import com.webank.wedatasphere.linkis.application.entity.*;
import org.apache.ibatis.annotations.Param;

import javax.ws.rs.Path;
import java.util.List;

/**
 * Created by johnnwang on 2019/1/18.
 */
public interface ApplicationMapper {
    void insertWFApplication(Application application);

    void deleteWFApplication(long applicationId);

    List<Application> listWFApplication(Long userId);

    Application selectWFApplication(Long applicationId);

    Integer updateWFApplication(Application application);

    Long selectUserIdByName(String userName);

    ApplicationUser selectUserByName(String creator);

    void inserUser(ApplicationUser user);

    void updateUserFirstLoginStatus(Long id);

    List<Project> selectAllProject();

    void insertProjectUser(ProjectUser projectUser);

    void updateProjectUser(ProjectUser projectUser);

    List<Project> listInitedProject(Long userId);

    ProjectVO getProjectVO(@Param("userId") Long userId, @Param("projectId") Long projectId);

    void register(Project project);

    void updateProjectByName(Project project);

    Project getPorjectByName(String name);
}
