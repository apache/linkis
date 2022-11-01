package org.apache.linkis.basedatamanager.server.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("linkis_ps_configuration_key_engine_relation")
public class ConfigurationKeyEngineRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long configKeyId;

    private Long engineTypeLabelId;

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

    public Long getEngineTypeLabelId() {
        return engineTypeLabelId;
    }

    public void setEngineTypeLabelId(Long engineTypeLabelId) {
        this.engineTypeLabelId = engineTypeLabelId;
    }
}
