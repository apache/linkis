package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;

import java.util.Date;


public class PersistenceLabel extends GenericLabel {
    private int id;
    private int labelValueSize;

    private Date updateTime;
    private Date createTime;
    private String updator;
    private String creator;
    private String stringValue;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getLabelValueSize() {
        if(labelValueSize == 0){
            setLabelValueSize(getValue().size());
        }
        return labelValueSize;
    }

    public void setLabelValueSize(int labelValueSize) {
        this.labelValueSize = labelValueSize;
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

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue == null ? LabelUtils.Jackson.toJson(value, null) : this.stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PersistenceLabel that = (PersistenceLabel) o;

        if (!this.getLabelKey().equals(that.getLabelKey())) return false;
        return stringValue.equals(that.stringValue);
    }

    @Override
    public int hashCode() {
        int result = this.getLabelKey().hashCode();
        result = 31 * result + this.stringValue.hashCode();
        return result;
    }

}
