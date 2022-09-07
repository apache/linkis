package org.apache.linkis.instance.label.dao;


import org.apache.ibatis.annotations.Param;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsLabelRelationDaoTest extends  BaseDaoTest{


    @Autowired
    InsLabelRelationDao insLabelRelationDao;

    @Test
    public void testSearchInsDirectByValues() {
        Map<String,String> map=new HashMap<>();
        map.put("test","test1");
        List<Map<String, String>> list=new ArrayList<>();
        list.add(map);
        String relation="testRelation";
        List<InstanceInfo> instanceInfoList=insLabelRelationDao.searchInsDirectByValues(list,relation);
        assertTrue(instanceInfoList.size() <= 0);
    }

    @Test
    public void testSearchInsDirectByLabels() {
        List<InsPersistenceLabel> labels=new ArrayList<>();
        InsPersistenceLabel insPersistenceLabel=new InsPersistenceLabel();
        insPersistenceLabel.setLabelKey("testKey");
        insPersistenceLabel.setStringValue("testVa");
        labels.add(insPersistenceLabel);
        List<InstanceInfo> instanceInfoList=insLabelRelationDao.searchInsDirectByLabels(labels);
        assertTrue(instanceInfoList.size() <= 0);
    }

    @Test
    public void testSearchInsCascadeByValues() {
        Map<String,String> map=new HashMap<>();
        map.put("test","test1");
        List<Map<String, String>> valueContent=new ArrayList<>();
        valueContent.add(map);
        String relation="testRelation";
        List<InstanceInfo> instanceInfoList=insLabelRelationDao.searchInsCascadeByValues(valueContent,relation);
        assertTrue(instanceInfoList.size() <= 0);
    }

    @Test
    public void testSearchInsCascadeByLabels() {
        List<InsPersistenceLabel> labels=new ArrayList<>();
        InsPersistenceLabel insPersistenceLabel=new InsPersistenceLabel();
        insPersistenceLabel.setLabelKey("testKey");
        insPersistenceLabel.setStringValue("testVa");
        labels.add(insPersistenceLabel);
        List<InstanceInfo> instanceInfoList=insLabelRelationDao.searchInsCascadeByLabels(labels);
        assertTrue(instanceInfoList.size() <= 0);
    }

    @Test
    public void testSearchUnRelateInstances() {
    }

    @Test
    public void testSearchLabelRelatedInstances() {
    }

    @Test
    public void testSearchLabelsByInstance() {
    }

    @Test
    public void testListAllInstanceWithLabel() {
    }

    @Test
    public void testGetInstancesByNames() {
    }

    @Test
    public void testDropRelationsByInstanceAndLabelIds() {
    }

    @Test
    public void testDropRelationsByInstance() {
    }

    @Test
    public void testInsertRelations() {
    }

    @Test
    public void testExistRelations() {
    }
}