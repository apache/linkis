package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import java.util.Date;


public class PersistenceResource {
    private int id;
    private String maxResource;
    private String minResource;
    private String usedResource;
    private String leftResource;
    private String expectedResource;
    private String lockedResource;

    private String resourceType;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    private String ticketId;

    private Date updateTime;
    private Date createTime;
    private String updator;
    private String creator;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaxResource() {
        return maxResource;
    }

    public void setMaxResource(String maxResource) {
        this.maxResource = maxResource;
    }

    public String getMinResource() {
        return minResource;
    }

    public void setMinResource(String minResource) {
        this.minResource = minResource;
    }

    public String getUsedResource() {
        return usedResource;
    }

    public void setUsedResource(String usedResource) {
        this.usedResource = usedResource;
    }

    public String getLeftResource() {
        return leftResource;
    }

    public void setLeftResource(String leftResource) {
        this.leftResource = leftResource;
    }

    public String getExpectedResource() {
        return expectedResource;
    }

    public void setExpectedResource(String expectedResource) {
        this.expectedResource = expectedResource;
    }

    public String getLockedResource() {
        return lockedResource;
    }

    public void setLockedResource(String lockedResource) {
        this.lockedResource = lockedResource;
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
