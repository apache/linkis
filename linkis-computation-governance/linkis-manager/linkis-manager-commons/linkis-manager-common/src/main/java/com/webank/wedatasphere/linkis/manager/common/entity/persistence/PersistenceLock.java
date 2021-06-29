package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import java.util.Date;


public class PersistenceLock {
    private int id;
    private String lockObject;
    private Long timeOut;

    private Date updateTime;
    private Date createTime;
    private String updator;
    private String creator;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLockObject() {
        return lockObject;
    }

    public void setLockObject(String lockObject) {
        this.lockObject = lockObject;
    }

    public Long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
