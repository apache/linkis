package com.webank.wedatasphere.linkis.cs.listener.callback.imp;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;

import java.util.Objects;

public class ContextKeyValueBean {

    private ContextKey csKey;
    private ContextValue csValue;
    private ContextID csID;

    public ContextID getCsID() {
        return csID;
    }

    public void setCsID(ContextID csID) {
        this.csID = csID;
    }

    public ContextKey getCsKey() {
        return csKey;
    }

    public void setCsKey(ContextKey csKey) {
        this.csKey = csKey;
    }

    public ContextValue getCsValue() {
        return csValue;
    }

    public void setCsValue(ContextValue csValue) {
        this.csValue = csValue;
    }


    @Override
    public int hashCode() {
        return Objects.hash(csKey, csValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContextKeyValueBean csmapKey = (ContextKeyValueBean) o;
        return Objects.equals(csKey, csmapKey.csKey) &&
                Objects.equals(csValue, csmapKey.csValue);
    }
}
