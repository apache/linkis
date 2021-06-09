package com.webank.wedatasphere.linkis.manager.persistence;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;

import java.util.List;
import java.util.Map;


public interface LabelManagerPersistence {

    //插入标签
    void addLabel(PersistenceLabel persistenceLabel);

    //移除标签
    void removeLabel(int id);

    void removeLabel(PersistenceLabel persistenceLabel);

    //更新标签
    void updateLabel(int id, PersistenceLabel persistenceLabel);

    //查询标签
    PersistenceLabel getLabel(int id);

    List<PersistenceLabel> getLabelByServiceInstance(ServiceInstance serviceInstance);

    List<PersistenceLabel> getLabelByResource(PersistenceResource persistenceResource);

    /**
     * ON DUPLICATE KEY UPDATE label_id = "xxx"
     *
     * @param serviceInstance
     * @param labelIds
     */
    void addLabelToNode(ServiceInstance serviceInstance, List<Integer> labelIds);

    List<PersistenceLabel> getLabelsByValue(Map<String, String> labelKeyValues, Label.ValueRelation valueRelation);

    List<PersistenceLabel> getLabelsByValueList(List<Map<String, String>> labelKeyValues, Label.ValueRelation valueRelation);

    PersistenceLabel getLabelsByKeyValue(String labelKey, Map<String, String> labelKeyValues, Label.ValueRelation valueRelation);

    List<PersistenceLabel> getLabelsByKeyValueMap(Map<String, Map<String, String>> labelKeyAndValuesMap, Label.ValueRelation valueRelation);

    List<PersistenceLabel> getLabelsByKey(String labelKey);

    void removeNodeLabels(ServiceInstance serviceInstance, List<Integer> labelIds);

    List<ServiceInstance> getNodeByLabel(int labelId);

    List<ServiceInstance> getNodeByLabels(List<Integer> labelIds);

    void addLabelToUser(String userName, List<Integer> labelIds);

    void removeLabelFromUser(String userName, List<Integer> labelIds);

    List<String> getUserByLabel(int label);

    List<String> getUserByLabels(List<Integer> labels);

    List<PersistenceLabel> getLabelsByUser(String userName);

    Map<PersistenceLabel, List<ServiceInstance>> getNodeRelationsByLabels(List<PersistenceLabel> labelIds);

    Map<ServiceInstance, List<PersistenceLabel>> getLabelRelationsByServiceInstance(List<ServiceInstance> serviceInstances);

    /**
     * 通过labelkey 和StringValue找到唯一的label，返回值可能为null
     *
     * @param labelKey
     * @param stringValue
     * @return
     */
    PersistenceLabel getLabelByKeyValue(String labelKey, String stringValue);

    /**
     * 通过labelkey 和StringValue找到唯一的label，并且找出他所包含的所有instance
     *
     * @param labelKey
     * @param stringValue
     * @return
     */
    List<ServiceInstance> getNodeByLabelKeyValue(String labelKey, String stringValue);
}
