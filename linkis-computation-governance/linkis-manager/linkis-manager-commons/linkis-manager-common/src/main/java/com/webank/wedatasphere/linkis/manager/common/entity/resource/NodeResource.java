package com.webank.wedatasphere.linkis.manager.common.entity.resource;

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

import java.io.Serializable;


public interface NodeResource extends Serializable, RequestProtocol {

    ResourceType getResourceType();

    void setResourceType(ResourceType resourceType);

    void setMinResource(Resource minResource);

    Resource getMinResource();

    void setMaxResource(Resource maxResource);

    Resource getMaxResource();

    void setUsedResource(Resource usedResource);

    Resource getUsedResource();

    void setLockedResource(Resource lockedResource);

    Resource getLockedResource();

    void setExpectedResource(Resource expectedResource);

    Resource getExpectedResource();

    void setLeftResource(Resource leftResource);

    Resource
    getLeftResource();

}
