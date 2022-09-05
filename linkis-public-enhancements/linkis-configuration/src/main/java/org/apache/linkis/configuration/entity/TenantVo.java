package org.apache.linkis.configuration.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@ApiModel
public class TenantVo {

    @ApiModelProperty(name = "序号")
    private String id ;

    @NotBlank(message = "user不能为空")
    @ApiModelProperty(value = "用户(必填)", name = "user", required = true)
    private String user ;

    @NotBlank(message = "creator不能为空")
    @ApiModelProperty(value = "creator(必填)", name = "creator", required = true)
    private String creator ;

    @NotBlank
    @ApiModelProperty(name = "租户")
    private String tenantValue ;

    @ApiModelProperty(name = "创建时间")
    private Date createTime;

    @ApiModelProperty(name = "更新时间")
    private Date updateTime;

    @ApiModelProperty(name = "业务来源")
    private String desc ;

    @NotBlank
    @ApiModelProperty(name = "对接人")
    private String bussinessUser ;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTenantValue() {
        return tenantValue;
    }

    public void setTenantValue(String tenantValue) {
        this.tenantValue = tenantValue;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBussinessUser() {
        return bussinessUser;
    }

    public void setBussinessUser(String bussinessUser) {
        this.bussinessUser = bussinessUser;
    }
}
