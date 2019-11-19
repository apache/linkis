package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;


public class MdqTableFieldsInfoVO {
    private String name;
    private String type;
    private Integer length;
    private String alias;
    private String express;
    private Boolean isPrimary;
    private Boolean isPartitionField;
    private String rule;
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Boolean getPartitionField() {
        return isPartitionField;
    }

    public void setPartitionField(Boolean partitionField) {
        isPartitionField = partitionField;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
