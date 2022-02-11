package org.apache.linkis.gateway.authentication.entity;

import java.util.Date;

public class TokenEntity {
    private String id;
    private String tokenName;
    private String legalUsersStr;
    private String legalHostsStr;
    private String businessOwner;
    private Date createTime;
    private Date updateTime;
    private Long elapseDay;
    private String updateBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getLegalUsersStr() {
        return legalUsersStr;
    }

    public void setLegalUsersStr(String legalUsersStr) {
        this.legalUsersStr = legalUsersStr;
    }

    public String getLegalHostsStr() {
        return legalHostsStr;
    }

    public void setLegalHostsStr(String legalHostsStr) {
        this.legalHostsStr = legalHostsStr;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getElapseDay() {
        return elapseDay;
    }

    public void setElapseDay(Long elapseDay) {
        this.elapseDay = elapseDay;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
