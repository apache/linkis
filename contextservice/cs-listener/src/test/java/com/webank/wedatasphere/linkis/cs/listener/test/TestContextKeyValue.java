package com.webank.wedatasphere.linkis.cs.listener.test;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;

/**
 * @author chaogefeng
 * @date 2020/2/22 16:46
 */
public class TestContextKeyValue implements ContextKeyValue {

    private ContextKey contextKey;

    private ContextValue contextValue;

    @Override
    public ContextKey getContextKey() {
        return this.contextKey;
    }

    @Override
    public void setContextKey(ContextKey contextKey) {
        this.contextKey = contextKey;
    }

    @Override
    public ContextValue getContextValue() {
        return this.contextValue;
    }

    @Override
    public void setContextValue(ContextValue contextValue) {
        this.contextValue = contextValue;
    }
}
