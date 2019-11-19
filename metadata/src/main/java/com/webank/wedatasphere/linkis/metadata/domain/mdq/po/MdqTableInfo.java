package com.webank.wedatasphere.linkis.metadata.domain.mdq.po;

import java.util.Date;


public class MdqTableInfo {
    private Long id;
    private Long tableId;
    private Date tableLastUpdateTime;
    private Integer rowNum;
    private Integer fileNum;
    private String tableSize;
    private Integer partitionsNum;
    private Date updateTime;
    private Integer fieldNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Date getTableLastUpdateTime() {
        return tableLastUpdateTime;
    }

    public void setTableLastUpdateTime(Date tableLastUpdateTime) {
        this.tableLastUpdateTime = tableLastUpdateTime;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public String getTableSize() {
        return tableSize;
    }

    public void setTableSize(String tableSize) {
        this.tableSize = tableSize;
    }

    public Integer getPartitionsNum() {
        return partitionsNum;
    }

    public void setPartitionsNum(Integer partitionsNum) {
        this.partitionsNum = partitionsNum;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getFieldNum() {
        return fieldNum;
    }

    public void setFieldNum(Integer fieldNum) {
        this.fieldNum = fieldNum;
    }
}
