package com.webank.wedatasphere.linkis.cs.listener.test;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ValueBean;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/22
 */
public class TestContextValue implements ContextValue {
    private  Object value;

    private String keywords;


    @Override
    public String getKeywords() {
        return null;
    }

    @Override
    public void setKeywords(String keywords) {

    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value=value;
    }


}
