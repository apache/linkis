package com.webank.wedatasphere.linkis.bml.model;

import java.util.Date;

/**
 * created by cooperyang on 2019/5/14
 * Description:
 */
public abstract class AbstractAuditable {
    private Date created;
    private Date updated;
    private String createdBy;
    private String updatedBy;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setInfoOnCreate(String user) {
        Date current = new Date();
        this.setCreated(current);
        this.setUpdated(current);
        this.setCreatedBy(user);
        this.setUpdatedBy(user);
    }

    public void setInfoOnUpdate(String user) {
        Date current = new Date();
        this.setUpdated(current);
        this.setUpdatedBy(user);
    }

}
