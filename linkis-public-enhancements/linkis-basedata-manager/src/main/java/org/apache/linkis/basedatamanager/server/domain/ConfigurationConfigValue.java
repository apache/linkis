package org.apache.linkis.basedatamanager.server.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("linkis_ps_configuration_config_value")
public class ConfigurationConfigValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long configKeyId;

    private String configValue;

    private Integer configLabelId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfigKeyId() {
        return configKeyId;
    }

    public void setConfigKeyId(Long configKeyId) {
        this.configKeyId = configKeyId;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Integer getConfigLabelId() {
        return configLabelId;
    }

    public void setConfigLabelId(Integer configLabelId) {
        this.configLabelId = configLabelId;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
