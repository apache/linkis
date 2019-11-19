package com.webank.wedatasphere.linkis.bml.client;

import java.util.Map;

/**
 * created by cooperyang on 2019/5/15
 * Description:
 */
public abstract class AbstractBmlClient implements BmlClient{
    protected String user;
    protected java.util.Map<String, Object> properties;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
