package com.webank.wedatasphere.linkis.cs.listener.test;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/22
 */
public class TestContextKey implements ContextKey {
    private  String key;
    private ContextType contextType;
    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setKey(String key) {
        this.key=key;
    }

    @Override
    public ContextType getContextType() {
        return this.contextType;
    }

    @Override
    public void setContextType(ContextType contextType) {
        this.contextType=contextType;
    }

    @Override
    public ContextScope getContextScope() {
        return null;
    }

    @Override
    public void setContextScope(ContextScope contextScope) {

    }

    @Override
    public String getKeywords() {
        return null;
    }

    @Override
    public void setKeywords(String keywords) {

    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void setType(int type) {

    }
}
