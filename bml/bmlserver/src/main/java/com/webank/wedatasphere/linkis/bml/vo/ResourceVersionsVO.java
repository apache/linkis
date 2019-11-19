package com.webank.wedatasphere.linkis.bml.vo;

import com.webank.wedatasphere.linkis.bml.Entity.Version;

import java.util.List;

/**
 * Created by v_wbjjianli on 2019/5/16.
 */
public class ResourceVersionsVO {

    private String resourceId;

    private String user;

    //private String system;

    private List<Version> versions;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

//    public String getSystem() {
//        return system;
//    }
//
//    public void setSystem(String system) {
//        this.system = system;
//    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
}
