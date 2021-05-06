package com.webank.wedatasphere.linkis.bml.service;

import java.util.List;

public interface BmlProjectService {


    int createBmlProject(String projectName, String creator, List<String> editUsers, List<String> accessUsers);


    boolean checkEditPriv(String projectName, String username);

    boolean checkAccessPriv(String projectName, String username);

    void setProjectEditPriv(String projectName, List<String> editUsers);

    void addProjectEditPriv(String projectName, List<String> editUsers);

    void deleteProjectEditPriv(String projectName, List<String> editUsers);

    void setProjectAccessPriv(String projectName, List<String> accessUsers);

    void addProjectAccessPriv(String projectName, List<String> accessUsers);

    void deleteProjectAccessPriv(String projectName, List<String> accessUsers);


    String getProjectNameByResourceId(String resourceId);

    void addProjectResource(String resourceId, String projectName);

    void attach(String projectName, String resourceId);

    void updateProjectUsers(String username, String projectName, List<String> editUsers, List<String> accessUsers);
}
