package org.apache.linkis.instance.label.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;

import org.apache.linkis.instance.label.vo.InsPersistenceLabelSearchVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class InstanceLabelDaoTest extends BaseDaoTest{

    @Autowired
    InstanceLabelDao instanceLabelDao;

    @Test
    public void testSelectForUpdate() {
        testInsert();
        InsPersistenceLabel insPersistenceLabel=instanceLabelDao.selectForUpdate(1);
        assertTrue(insPersistenceLabel!=null);
    }

    @Test
    public void testSearchForUpdate() {
        testInsert();
        String labelKey="testKey";
        String labelValue="testValue";
        InsPersistenceLabel insPersistenceLabel=instanceLabelDao.searchForUpdate(labelKey,labelValue);
        assertTrue(insPersistenceLabel!=null);
    }

    @Test
    public void testInsertBatch() {
        List<InsPersistenceLabel> labels=new ArrayList<>();
        InsPersistenceLabel label=new InsPersistenceLabel();
        label.setLabelKey("testKey");
        label.setStringValue("testValue");
        label.setLabelValueSize(2);
        label.setId(1);
        labels.add(label);
        InsPersistenceLabel label1=new InsPersistenceLabel();
        label1.setLabelKey("testKey1");
        label1.setStringValue("testValue1");
        label1.setLabelValueSize(2);
        label1.setId(2);
        labels.add(label1);
        instanceLabelDao.insertBatch(labels);
    }

    @Test
    public void testInsert() {
        InsPersistenceLabel label=new InsPersistenceLabel();
        label.setLabelKey("testKey");
        label.setStringValue("testValue");
        label.setLabelValueSize(2);
        label.setId(1);
        instanceLabelDao.insert(label);
    }

    @Test
    public void testUpdateForLock() {
        testInsert();
        int  i= instanceLabelDao.updateForLock(1);
        assertTrue(i==1);
    }

    @Test
    public void testSearch() {
        testInsert();
        List<InsPersistenceLabelSearchVo> labelSearch=new ArrayList<>();
        InsPersistenceLabelSearchVo insPersistenceLabelSearchVo=new InsPersistenceLabelSearchVo();
        insPersistenceLabelSearchVo.setLabelKey("testKey");
        insPersistenceLabelSearchVo.setStringValue("testValue");
        labelSearch.add(insPersistenceLabelSearchVo);
        List<InsPersistenceLabel> list=instanceLabelDao.search(labelSearch);
        assertTrue(list.size()>=1);
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testDoInsertKeyValues() {
    }

    @Test
    public void testDoRemoveKeyValues() {
    }

    @Test
    public void testDoRemoveKeyValuesBatch() {
    }
}