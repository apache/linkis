package org.apache.linkis.basedatamanager.server.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("linkis_ps_configuration_config_key")
public class ConfigurationConfigKey implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("`key`")
    private String key;

    private String description;

    private String name;

    private String defaultValue;

    private String validateType;

    private String validateRange;

    private String engineConnType;

    @TableField("is_hidden")
    private Integer hidden;

    @TableField("is_advanced")
    private Integer advanced;

    @TableField("`level`")
    private Integer level;

    @TableField("treeName")
    private String treeName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValidateType() {
        return validateType;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    public String getValidateRange() {
        return validateRange;
    }

    public void setValidateRange(String validateRange) {
        this.validateRange = validateRange;
    }

    public String getEngineConnType() {
        return engineConnType;
    }

    public void setEngineConnType(String engineConnType) {
        this.engineConnType = engineConnType;
    }

    public Integer getHidden() {
        return hidden;
    }

    public void setHidden(Integer hidden) {
        this.hidden = hidden;
    }

    public Integer getAdvanced() {
        return advanced;
    }

    public void setAdvanced(Integer advanced) {
        this.advanced = advanced;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }
}
