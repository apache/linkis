package com.webank.wedatasphere.linkis.cs.client.test.bean;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ValueBean;

/**
 * created by cooperyang on 2020/2/26
 * Description:
 */
public class ClientTestContextValue implements ContextValue {



    private Object value;

    private String keywords;

    @Override
    public String getKeywords() {
        return this.keywords;
    }

    @Override
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }
}
