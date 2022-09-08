package org.apache.linkis.instance.label.dao;

import org.apache.linkis.instance.label.entity.InsPersistenceLabel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class InstanceLabelDaoTest extends BaseDaoTest{

    @Autowired
    InstanceLabelDao instanceLabelDao;

    @Test
    public void testSelectForUpdate() {
    }

    @Test
    public void testSearchForUpdate() {
    }

    @Test
    public void testInsertBatch() {
    }

    @Test
    public void testInsert() {
        InsPersistenceLabel label=new InsPersistenceLabel();
        label.setLabelKey("testKey");
        label.setStringValue("testValue");
        instanceLabelDao.insert(label);
    }

    @Test
    public void testUpdateForLock() {
    }

    @Test
    public void testSearch() {
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