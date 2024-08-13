/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.instance.label.dao;

import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.instance.label.entity.InsPersistenceLabelValue;
import org.apache.linkis.instance.label.vo.InsPersistenceLabelSearchVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstanceLabelDaoTest extends BaseDaoTest {

  @Autowired InstanceLabelDao instanceLabelDao;

  @Test
  public void testSelectForUpdate() {
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setLabelKey("testKey");
    label.setStringValue("testValue");
    label.setLabelValueSize(2);
    label.setId(1);
    instanceLabelDao.insert(label);
    InsPersistenceLabel insPersistenceLabel = instanceLabelDao.selectForUpdate(1);
    // assertTrue(insPersistenceLabel != null);
  }

  @Test
  public void testSearchForUpdate() {
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setLabelKey("testKey");
    label.setStringValue("testValue");
    label.setLabelValueSize(2);
    label.setId(1);
    instanceLabelDao.insert(label);
    String labelKey = "testKey";
    String labelValue = "testValue";
    InsPersistenceLabel insPersistenceLabel =
        instanceLabelDao.searchForUpdate(labelKey, labelValue);
    assertTrue(insPersistenceLabel != null);
  }

  /**
   * When using the h2 library for testing,if the function(on conflict) is not supported,an error
   * will be reported, and the pg physical library will not guarantee an error pg使用h2库测试时不支持函数（on
   * conflict）会报错，pg实体库不会报错
   */
  @Test
  public void testInsertBatch() {
    List<InsPersistenceLabel> labels = new ArrayList<>();
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setLabelKey("testKey");
    label.setStringValue("testValue");
    label.setLabelValueSize(2);
    label.setId(1);
    labels.add(label);
    InsPersistenceLabel label1 = new InsPersistenceLabel();
    label1.setLabelKey("testKey1");
    label1.setStringValue("testValue1");
    label1.setLabelValueSize(2);
    label1.setId(2);
    labels.add(label1);
    instanceLabelDao.insertBatch(labels);
  }

  @Test
  public void testInsert() {
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setLabelKey("testKey");
    label.setStringValue("testValue");
    label.setLabelValueSize(2);
    label.setId(1);
    instanceLabelDao.insert(label);
  }

  @Test
  public void testUpdateForLock() {
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setLabelKey("testKey");
    label.setStringValue("testValue");
    label.setLabelValueSize(2);
    label.setId(1);
    instanceLabelDao.insert(label);
    int i = instanceLabelDao.updateForLock(1);
    // assertTrue(i == 1);
  }

  @Test
  public void testSearch() {
    testInsert();
    List<InsPersistenceLabelSearchVo> labelSearch = new ArrayList<>();
    InsPersistenceLabelSearchVo insPersistenceLabelSearchVo = new InsPersistenceLabelSearchVo();
    insPersistenceLabelSearchVo.setLabelKey("testKey");
    insPersistenceLabelSearchVo.setStringValue("testValue");
    labelSearch.add(insPersistenceLabelSearchVo);
    List<InsPersistenceLabel> list = instanceLabelDao.search(labelSearch);
    assertTrue(list.size() >= 1);
  }

  @Test
  public void testRemove() {
    testInsert();
    InsPersistenceLabel label = new InsPersistenceLabel();
    label.setId(1);
    instanceLabelDao.remove(label);
  }

  @Test
  public void testDoInsertKeyValues() {
    List<InsPersistenceLabelValue> keyValues = new ArrayList<>();
    InsPersistenceLabelValue insPersistenceLabelValue = new InsPersistenceLabelValue();
    insPersistenceLabelValue.setLabelId(1);
    insPersistenceLabelValue.setValueKey("testValueKey");
    insPersistenceLabelValue.setValueContent("testValueContent");
    keyValues.add(insPersistenceLabelValue);
    instanceLabelDao.doInsertKeyValues(keyValues);
  }

  @Test
  public void testDoRemoveKeyValues() {
    testDoInsertKeyValues();
    instanceLabelDao.doRemoveKeyValues(1);
  }

  @Test
  public void testDoRemoveKeyValuesBatch() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    instanceLabelDao.doRemoveKeyValuesBatch(labelIds);
  }
}
