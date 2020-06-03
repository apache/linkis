package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ListenerDomain;

/**
 * Created by patinousward on 2020/2/17.
 */
public class PersistenceContextIDListener implements ListenerDomain {

    private Integer id;

    private String source;

    private String contextId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
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
