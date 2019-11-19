package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;

import java.util.Date;


public class BaseBO {
    private String database;
    private String name;
    private String alias;
    private Boolean isPartitionTable;
    private String creator;
    private String comment;
    private Date createTime;
    private Date latestAccessTime;

    public Date getLatestAccessTime() {
        return latestAccessTime;
    }

    public void setLatestAccessTime(Date latestAccessTime) {
        this.latestAccessTime = latestAccessTime;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Boolean getPartitionTable() {
        return isPartitionTable;
    }

    public void setPartitionTable(Boolean partitionTable) {
        isPartitionTable = partitionTable;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
