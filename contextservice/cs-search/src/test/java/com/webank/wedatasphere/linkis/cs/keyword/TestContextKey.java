package com.webank.wedatasphere.linkis.cs.keyword;

import com.webank.wedatasphere.linkis.cs.common.annotation.KeywordMethod;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;

/**
 * @author peacewong
 * @date 2020/2/13 16:32
 */
public class TestContextKey implements ContextKey {

    private  String key;

    private String keywords;

    private ContextScope contextScope;

    private ContextType contextType;

    @KeywordMethod
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
        return contextType;
    }

    @Override
    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public ContextScope getContextScope() {
        return contextScope;
    }

    @Override
    public void setContextScope(ContextScope contextScope) {
        this.contextScope = contextScope;
    }

    @KeywordMethod(splitter = ",")
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
