package com.webank.wedatasphere.linkis.cs.client.test.bean;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.Resource;

/**
 * created by cooperyang on 2020/2/26
 * Description:
 */
public class ResourceValueBean implements Resource {

    private String resourceId;
    private String version;

    private final ContextType contextType =  ContextType.RESOURCE;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
