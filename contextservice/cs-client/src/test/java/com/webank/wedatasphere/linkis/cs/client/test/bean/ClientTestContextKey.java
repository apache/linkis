package com.webank.wedatasphere.linkis.cs.client.test.bean;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;

/**
 * created by cooperyang on 2020/2/26
 * Description:
 */
public class ClientTestContextKey implements ContextKey {


    private final String key = "cooperyang.txt";




    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setKey(String key) {

    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void setType(int type) {

    }

    @Override
    public ContextType getContextType() {
        return null;
    }

    @Override
    public void setContextType(ContextType contextType) {

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
}
