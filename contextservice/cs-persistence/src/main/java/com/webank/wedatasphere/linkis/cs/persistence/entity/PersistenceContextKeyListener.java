package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ListenerDomain;

/**
 * Created by patinousward on 2020/2/17.
 */
public class PersistenceContextKeyListener implements ListenerDomain {

    private Integer id;

    private String source;

    private Integer keyId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKeyId() {
        return keyId;
    }

    public void setKeyId(Integer keyId) {
        this.keyId = keyId;
    }

    @Override
    public String getSource() {
        return this.source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
