package com.webank.wedatasphere.linkis.manager.common.entity.resource;

public class CommonNodeResource implements NodeResource {

    private ResourceType resourceType;

    private Resource maxResource;

    private Resource minResource;

    private Resource usedResource;

    private Resource lockedResource;

    private Resource expectedResource;

    private Resource leftResource;

    public static NodeResource initNodeResource(ResourceType resourceType){
        CommonNodeResource commonNodeResource = new CommonNodeResource();
        commonNodeResource.setResourceType(resourceType);
        Resource zeroResource = Resource.initResource(resourceType);
        commonNodeResource.setMaxResource(zeroResource);
        commonNodeResource.setMinResource(zeroResource);
        commonNodeResource.setUsedResource(zeroResource);
        commonNodeResource.setLockedResource(zeroResource);
        commonNodeResource.setExpectedResource(zeroResource);
        commonNodeResource.setLeftResource(zeroResource);
        return commonNodeResource;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }
    @Override
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public Resource getMaxResource() {
        return maxResource;
    }

    @Override
    public void setMaxResource(Resource maxResource) {
        this.maxResource = maxResource;
    }

    @Override
    public Resource getMinResource() {
        return minResource;
    }

    @Override
    public void setMinResource(Resource minResource) {
        this.minResource = minResource;
    }

    @Override
    public Resource getUsedResource() {
        return usedResource;
    }

    @Override
    public void setUsedResource(Resource usedResource) {
        this.usedResource = usedResource;
    }

    @Override
    public Resource getLockedResource() {
        return lockedResource;
    }

    @Override
    public void setLockedResource(Resource lockedResource) {
        this.lockedResource = lockedResource;
    }

    @Override
    public Resource getExpectedResource() {
        return expectedResource;
    }

    @Override
    public void setExpectedResource(Resource expectedResource) {
        this.expectedResource = expectedResource;
    }

    @Override
    public Resource getLeftResource() {
        if(this.leftResource == null && getMaxResource() != null && getUsedResource() != null) {
            return getMaxResource().minus(getUsedResource());
        } else {
            return this.leftResource;
        }
    }

    @Override
    public void setLeftResource(Resource leftResource) {
        this.leftResource = leftResource;
    }

    @Override
    public String toString() {
        return "CommonNodeResource{" +
                "resourceType=" + resourceType +
                ", maxResource=" + maxResource +
                ", minResource=" + minResource +
                ", usedResource=" + usedResource +
                ", lockedResource=" + lockedResource +
                ", expectedResource=" + expectedResource +
                ", leftResource=" + leftResource +
                '}';
    }
}
