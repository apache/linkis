package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.persistence.annotation.Ignore;

/**
 * Created by patinousward on 2020/2/12.
 */
@Ignore
public class PersistenceContextKeyValue implements ContextKeyValue {

    private Integer id;

    private String contextId;

    private ContextKey contextKey;

    private ContextValue contextValue;

    private String props;

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void setContextKey(ContextKey contextKey) {
        this.contextKey = contextKey;
    }

    @Override
    public ContextKey getContextKey() {
        return this.contextKey;
    }

    @Override
    public ContextValue getContextValue() {
        return this.contextValue;
    }

    @Override
    public void setContextValue(ContextValue contextValue) {
        this.contextValue = contextValue;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
}
