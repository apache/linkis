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
package com.webank.wedatasphere.linkis.bml.Entity;

import java.util.Date;
import java.util.Map;

/**
 * Created by cooperyang on 2019/5/16.
 */
public class Resource {

    private static final String MAX_VERSION = "maxVersion";
    private static final String IS_PRIVATE = "isPrivate";
    private static final String RESOURCE_HEADER = "resourceHeader";
    private static final String DOWNLOAD_FILE_NAME = "downloadedFileName";
    private static final String SYSTEM = "system";
    private static final String IS_EXPIRE = "isExpire";
    private static final String EXPIRE_TYPE = "expireType";
    private static final String EXPIRE_TIME = "expireTime";
    private static final String UPDATER = "updator";




    private int id;

    private boolean isPrivate;

    private String resourceHeader;

    private String downloadedFileName;

    private String sys;

    private Date createTime;

    private boolean isExpire;

    private String expireType;

    /**
     *  expireTime的形式是 yyyy-MM-dd
     *  或 yyyy-MM-dd HH:mm:ss
     */
    private String expireTime;

    private Date updateTime;

    private String updator;

    private int maxVersion;

    private String resourceId;

    private String user;

    private String system;

    private boolean enableFlag;

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

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public Resource() {
    }

    public Resource(String resourceId, String user, String downloadedFileName){
        this.user = user;
        this.resourceId = resourceId;
        this.createTime = new Date(System.currentTimeMillis());
        this.setUpdateTime(new Date(System.currentTimeMillis()));
        this.enableFlag = true;
        this.downloadedFileName = downloadedFileName;
    }

    public boolean isEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(boolean enableFlag) {
        this.enableFlag = enableFlag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getResourceHeader() {
        return resourceHeader;
    }

    public void setResourceHeader(String resourceHeader) {
        this.resourceHeader = resourceHeader;
    }

    public String getDownloadedFileName() {
        return downloadedFileName;
    }

    public void setDownloadedFileName(String downloadedFileName) {
        this.downloadedFileName = downloadedFileName;
    }

    public String getSys() {
        return sys;
    }

    public void setSys(String sys) {
        this.sys = sys;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isExpire() {
        return isExpire;
    }

    public void setExpire(boolean expire) {
        isExpire = expire;
    }

    public String getExpireType() {
        return expireType;
    }

    public void setExpireType(String expireType) {
        this.expireType = expireType;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public static Resource createNewResource(String resourceId, String user, String downloadedFileName, Map<String, Object> properties){
        Resource resource = new Resource(resourceId, user, downloadedFileName);
        if (properties.get(MAX_VERSION) == null){
            resource.setMaxVersion(10);
        }else{
            resource.setMaxVersion(Integer.parseInt(properties.get(MAX_VERSION).toString()));
        }
        if (properties.get(IS_EXPIRE) == null){
            //默认是不过期的
            resource.setExpire(false);
        }else{
            resource.setExpire(properties.get(IS_EXPIRE).toString().equalsIgnoreCase("true"));
        }
        if (properties.get(SYSTEM) == null){
            resource.setSystem("WTSS");
        }else{
            resource.setSystem(properties.get(SYSTEM).toString());
        }
        if (properties.get(IS_PRIVATE) == null){
            resource.setPrivate(true);
        }else{
            resource.setPrivate(properties.get(IS_PRIVATE).toString().equalsIgnoreCase("true"));
        }
        if (properties.get(RESOURCE_HEADER) == null){
            resource.setResourceHeader(null);
        }else{
            resource.setResourceHeader((String)(properties.get(RESOURCE_HEADER)));
        }
        //如果资源是过期的，需要设置资源过期的类型和时间
        if (resource.isExpire()){
            if (properties.get(EXPIRE_TYPE) == null){
                resource.setExpireType("time");
            }else{
                resource.setExpireType((String)(properties.get(EXPIRE_TYPE)));
            }
            if (properties.get(EXPIRE_TIME) == null){
                //默认设置50天过期
                resource.setExpireTime("50d");
            }else{
                resource.setExpireTime((String)(properties.get(EXPIRE_TIME)));
            }
        }
        return resource;
    }



}
