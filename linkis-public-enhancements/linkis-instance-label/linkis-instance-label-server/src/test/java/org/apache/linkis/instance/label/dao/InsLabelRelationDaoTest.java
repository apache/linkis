package org.apache.linkis.instance.label.dao;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.instance.label.entity.InstanceInfo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsLabelRelationDaoTest extends BaseDaoTest {

  @Autowired InsLabelRelationDao insLabelRelationDao;

  @Test
  public void testSearchInsDirectByValues() {
    Map<String, String> map = new HashMap<>();
    map.put("test", "test1");
    List<Map<String, String>> list = new ArrayList<>();
    list.add(map);
    String relation = "testRelation";
    List<InstanceInfo> instanceInfoList =
        insLabelRelationDao.searchInsDirectByValues(list, relation);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchInsDirectByLabels() {
    List<InsPersistenceLabel> labels = new ArrayList<>();
    InsPersistenceLabel insPersistenceLabel = new InsPersistenceLabel();
    insPersistenceLabel.setLabelKey("testKey");
    insPersistenceLabel.setStringValue("testVa");
    labels.add(insPersistenceLabel);
    List<InstanceInfo> instanceInfoList = insLabelRelationDao.searchInsDirectByLabels(labels);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchInsCascadeByValues() {
    Map<String, String> map = new HashMap<>();
    map.put("test", "test1");
    List<Map<String, String>> valueContent = new ArrayList<>();
    valueContent.add(map);
    String relation = "testRelation";
    List<InstanceInfo> instanceInfoList =
        insLabelRelationDao.searchInsCascadeByValues(valueContent, relation);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchInsCascadeByLabels() {
    List<InsPersistenceLabel> labels = new ArrayList<>();
    InsPersistenceLabel insPersistenceLabel = new InsPersistenceLabel();
    insPersistenceLabel.setLabelKey("testKey");
    insPersistenceLabel.setStringValue("testVa");
    labels.add(insPersistenceLabel);
    List<InstanceInfo> instanceInfoList = insLabelRelationDao.searchInsCascadeByLabels(labels);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchUnRelateInstances() {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setApplicationName("testApplicationName");
    List<InstanceInfo> instanceInfoList = insLabelRelationDao.searchUnRelateInstances(instanceInfo);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchLabelRelatedInstances() {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setApplicationName("testApplicationName");
    List<InstanceInfo> instanceInfoList =
        insLabelRelationDao.searchLabelRelatedInstances(instanceInfo);
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testSearchLabelsByInstance() {
    String instance = "testInstance";
    List<InsPersistenceLabel> insPersistenceLabelList =
        insLabelRelationDao.searchLabelsByInstance(instance);
    assertTrue(insPersistenceLabelList.size() <= 0);
  }

  @Test
  public void testListAllInstanceWithLabel() {
    List<InstanceInfo> instanceInfoList = insLabelRelationDao.listAllInstanceWithLabel();
    assertTrue(instanceInfoList.size() <= 0);
  }

  @Test
  public void testGetInstancesByNames() {
    String appName = "testAppName";
    List<ServiceInstance> serviceInstanceList = insLabelRelationDao.getInstancesByNames(appName);
    assertTrue(serviceInstanceList.size() <= 0);
  }

  @Test
  public void testDropRelationsByInstanceAndLabelIds() {
    String testInstance = "testInstance";
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelIds.add(2);
    insLabelRelationDao.dropRelationsByInstanceAndLabelIds(testInstance, labelIds);
  }

  @Test
  public void testDropRelationsByInstance() {
    String testInstance = "testInstance";
    insLabelRelationDao.dropRelationsByInstance(testInstance);
  }

  @Test
  public void testInsertRelations() {
    String testInstance = "testInstance";
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelIds.add(2);
    insLabelRelationDao.insertRelations(testInstance, labelIds);
  }

  @Test
  public void testExistRelations() {
    testInsertRelations();
    Integer labelId = 1;
    Integer integer = insLabelRelationDao.existRelations(1);
    assertTrue(integer == 1);
  }
}
