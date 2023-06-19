package org.apache.linkis.configuration.entity;



public class TemplateConfigKeyVo {

    /**
     * 表字段 : id
     * 字段类型 : bigint(19)
     */
    private Long id;

    private String key;

    /**
     * 配置值
     * 表字段 : config_value
     * 字段类型 : varchar(200)
     */
    private String configValue;

    /**
     * 上限值
     * 表字段 : max_value
     * 字段类型 : varchar(50)
     */
    private String maxValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}