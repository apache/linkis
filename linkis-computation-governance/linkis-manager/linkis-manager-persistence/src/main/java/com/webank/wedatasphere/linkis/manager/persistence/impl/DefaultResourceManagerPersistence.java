package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
import com.webank.wedatasphere.linkis.manager.dao.LabelManagerMapper;
import com.webank.wedatasphere.linkis.manager.dao.NodeManagerMapper;
import com.webank.wedatasphere.linkis.manager.dao.ResourceManagerMapper;
import com.webank.wedatasphere.linkis.manager.exception.PersistenceErrorException;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.persistence.ResourceManagerPersistence;

import java.util.List;


public class DefaultResourceManagerPersistence implements ResourceManagerPersistence {

    private ResourceManagerMapper resourceManagerMapper;

    private NodeManagerMapper nodeManagerMapper;

    private LabelManagerMapper labelManagerMapper;

    public ResourceManagerMapper getResourceManagerMapper() {
        return resourceManagerMapper;
    }

    public void setResourceManagerMapper(ResourceManagerMapper resourceManagerMapper) {
        this.resourceManagerMapper = resourceManagerMapper;
    }

    public NodeManagerMapper getNodeManagerMapper() {
        return nodeManagerMapper;
    }

    public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
        this.nodeManagerMapper = nodeManagerMapper;
    }

    public LabelManagerMapper getLabelManagerMapper() {
        return labelManagerMapper;
    }

    public void setLabelManagerMapper(LabelManagerMapper labelManagerMapper) {
        this.labelManagerMapper = labelManagerMapper;
    }

    @Override
    public void registerResource(PersistenceResource persistenceResource) throws PersistenceErrorException {
        // 直接注册
        resourceManagerMapper.registerResource(persistenceResource);
    }

    @Override
    public void registerResource(ServiceInstance serviceInstance, PersistenceResource persistenceResource) throws PersistenceErrorException {
        //保存资源
        resourceManagerMapper.registerResource(persistenceResource);
        //保存标签与资源的关系表
        int resourceId = persistenceResource.getId();

        //int instanceId = nodeManagerMapper.getNodeInstanceId(serviceInstance.getInstance(), serviceInstance.getApplicationName());
        List<Integer> labelIds = labelManagerMapper.getLabelIdsByInstance(serviceInstance.getInstance());

        labelManagerMapper.addLabelsAndResource(resourceId,labelIds);

    }


    @Override
    public List<PersistenceResource> getResourceByLabel(Label label) throws PersistenceErrorException {
        String labelKey = label.getLabelKey();
        String stringValue = label.getStringValue();
        List<PersistenceResource> persistenceResourceList = labelManagerMapper.getResourcesByLabel(labelKey,stringValue);
        return persistenceResourceList;
    }

    @Override
    public List<PersistenceResource> getResourceByUser(String user) throws PersistenceErrorException {
        List<PersistenceResource> persistenceResourceList = resourceManagerMapper.getResourceByUserName(user);
        return persistenceResourceList;
    }

    @Override
    public List<PersistenceResource> getResourceByServiceInstance(ServiceInstance serviceInstance, String resourceType) throws PersistenceErrorException {
        List<PersistenceResource> persistenceResourceList = resourceManagerMapper.getResourceByInstanceAndResourceType(serviceInstance.getInstance(), resourceType);
        return persistenceResourceList;
    }

    @Override
    public List<PersistenceResource> getResourceByServiceInstance(ServiceInstance serviceInstance) throws PersistenceErrorException {
        List<PersistenceResource> persistenceResourceList = resourceManagerMapper.getResourceByServiceInstance(serviceInstance.getInstance());
        return persistenceResourceList;
    }

    @Override
    public void deleteServiceInstanceResource(ServiceInstance serviceInstance) throws PersistenceErrorException {
        //移除资源
        resourceManagerMapper.deleteResourceByInstance(serviceInstance.getInstance());
        //移除关系
        resourceManagerMapper.deleteResourceAndLabelId(serviceInstance.getInstance());
    }

    @Override
    public void deleteExpiredTicketIdResource(String ticketId) throws PersistenceErrorException {
        //关联表-标签资源表 删除
        labelManagerMapper.deleteLabelResourceByByTicketId(ticketId);
        //删除资源表
        resourceManagerMapper.deleteResourceByTicketId(ticketId);
    }

    @Override
    public void nodeResourceUpdate(ServiceInstance serviceInstance,PersistenceResource persistenceResource) throws PersistenceErrorException {
        int resourceId = resourceManagerMapper.getNodeResourceUpdateResourceId(serviceInstance.getInstance());
        resourceManagerMapper.nodeResourceUpdateByResourceId(resourceId,persistenceResource);
    }

    @Override
    public PersistenceResource getNodeResourceByTicketId(String ticketId) {
        PersistenceResource persistenceResource = resourceManagerMapper.getNodeResourceByTicketId(ticketId);
        return persistenceResource;
    }

    @Override
    public void nodeResourceUpdate(String ticketId, PersistenceResource persistenceResource) {
        resourceManagerMapper.nodeResourceUpdate(ticketId,persistenceResource);
    }

    @Override
    public List<PersistenceLabel> getLabelsByTicketId(String ticketId) {
        List<PersistenceLabel> persistenceLabelList = resourceManagerMapper.getLabelsByTicketId(ticketId);
        return persistenceLabelList;
    }

    @Override
    public void lockResource(List<Integer> labelIds, PersistenceResource persistenceResource) {
        resourceManagerMapper.registerResource(persistenceResource);
        int resourceId = persistenceResource.getId();
        labelManagerMapper.addLabelsAndResource(resourceId,labelIds);
    }
}
