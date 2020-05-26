package com.webank.wedatasphere.linkis.cs.keyword;

import com.webank.wedatasphere.linkis.cs.common.annotation.KeywordMethod;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ValueBean;

/**
 * @author peacewong
 * @date 2020/2/13 16:44
 */
public class TestContextValue  implements ContextValue {

    private  Object value;

    private String keywords;

    @KeywordMethod(splitter = "-")
    @Override
    public String getKeywords() {
        return this.keywords;
    }

    @Override
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @KeywordMethod(regex = "hello")
    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }
}
