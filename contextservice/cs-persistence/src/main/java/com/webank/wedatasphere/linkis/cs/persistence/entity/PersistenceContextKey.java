package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.persistence.annotation.Ignore;

/**
 * Created by patinousward on 2020/2/12.
 */
@Ignore
public class PersistenceContextKey implements ContextKey {

    private String key;

    private String keywords;

    private ContextScope contextScope;

    private ContextType contextType;

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public ContextType getContextType() {
        return this.contextType;
    }

    @Override
    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public ContextScope getContextScope() {
        return this.contextScope;
    }

    @Override
    public void setContextScope(ContextScope contextScope) {
        this.contextScope = contextScope;
    }

    @Override
    public String getKeywords() {
        return this.keywords;
    }

    @Override
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void setType(int type) {

    }
}
