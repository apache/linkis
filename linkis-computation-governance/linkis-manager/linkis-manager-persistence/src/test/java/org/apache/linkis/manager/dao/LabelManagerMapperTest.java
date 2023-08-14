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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.label.entity.Feature;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelManagerMapperTest extends BaseDaoTest {

  @Autowired LabelManagerMapper labelManagerMapper;

  @Test
  public void getLabel() {
    labelManagerMapper.getLabel(1);
  }

  @Test
  public void deleteLabel() {
    labelManagerMapper.deleteLabel(1);
  }

  @Test
  public void deleteByLabel() {
    labelManagerMapper.deleteByLabel("lk", "sv");
  }

  @Test
  public void deleteLabelKeyVaules() {
    labelManagerMapper.deleteLabelKeyVaules(1);
  }

  @Test
  public void updateLabel() {
    PersistenceLabel persistenceLabel = new PersistenceLabel();
    persistenceLabel.setLabelKey("lk2");
    persistenceLabel.setStringValue("sv2");
    persistenceLabel.setFeature(Feature.CORE);
    persistenceLabel.setLabelValueSize(2);
    persistenceLabel.setUpdateTime(new Date());
    persistenceLabel.setCreateTime(new Date());
    labelManagerMapper.updateLabel(1, persistenceLabel);
  }

  @Test
  public void addLabelServiceInstance() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.addLabelServiceInstance("ins", labelIds);
  }

  @Test
  public void getLabelByServiceInstance() {
    labelManagerMapper.getLabelByServiceInstance("ins");
  }

  @Test
  public void getLabelByResource() {
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(1);
    labelManagerMapper.getLabelByResource(persistenceResource);
  }

  @Test
  public void addLabelsAndResource() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.addLabelsAndResource(1, labelIds);
  }

  @Test
  public void getResourcesByLabel() {
    labelManagerMapper.getResourcesByLabel("sk", "sl");
  }

  @Test
  public void getLabelIdsByInstance() {
    labelManagerMapper.getLabelIdsByInstance("inst");
  }

  @Test
  public void getLabelsByInstance() {
    labelManagerMapper.getLabelsByInstance("inst");
  }

  @Test
  public void getInstanceByLabelId() {
    labelManagerMapper.getInstanceByLabelId(1);
  }

  @Test
  public void getResourcesByLabels() {
    List<PersistenceLabelRel> labelRels = new LinkedList<>();
    PersistenceLabelRel persistenceLabelRel = new PersistenceLabelRel();
    persistenceLabelRel.setLabelKey("lv");
    persistenceLabelRel.setStringValue("sv");
    labelRels.add(persistenceLabelRel);
    labelManagerMapper.getResourcesByLabels(labelRels);
  }

  @Test
  public void getInstanceIdsByLabelIds() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.getInstanceIdsByLabelIds(labelIds);
  }

  @Test
  public void getLabelsByLabelIds() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.getLabelsByLabelIds(labelIds);
  }

  @Test
  public void addLabelsByUser() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.addLabelsByUser("lldr", labelIds);
  }

  @Test
  public void getUserNameByLabelId() {
    labelManagerMapper.getUserNameByLabelId(1);
  }

  @Test
  public void getUserNamesByLabelIds() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.getUserNamesByLabelIds(labelIds);
  }

  @Test
  public void getLabelsByUser() {
    labelManagerMapper.getLabelsByUser("lldr");
  }

  @Test
  public void getLabelsByLabelKey() {
    labelManagerMapper.getLabelsByLabelKey("lldr");
  }

  @Test
  public void deleteLabelIdsAndInstance() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.deleteLabelIdsAndInstance("inst", labelIds);
  }

  @Test
  public void deleteLabelIdsByUser() {
    List<Integer> labelIds = new ArrayList<>();
    labelIds.add(1);
    labelManagerMapper.deleteLabelIdsByUser("lldr", labelIds);
  }

  @Test
  public void deleteUserById() {
    labelManagerMapper.deleteUserById(1);
  }

  @Test
  public void getLabelByKeyValue() {
    labelManagerMapper.getLabelByKeyValue("sk", "sl");
  }

  @Test
  public void listResourceLabelByValues() {
    List<LabelKeyValue> values = new ArrayList<>();
    LabelKeyValue labelKeyValue = new LabelKeyValue();
    labelKeyValue.setKey("key");
    labelKeyValue.setValue("val");
    values.add(labelKeyValue);
    labelManagerMapper.listResourceLabelByValues(values);
  }

  @Test
  public void listResourceByLaBelId() {
    labelManagerMapper.listResourceByLaBelId(1);
  }

  @Test
  public void deleteResourceByLabelId() {
    labelManagerMapper.deleteResourceByLabelId(1);
  }

  @Test
  public void deleteResourceByLabelIdInDirect() {
    labelManagerMapper.deleteResourceByLabelIdInDirect(1);
  }

  @Test
  public void selectLabelIdByLabelKeyValuesMaps() {
    Map<String, Map<String, String>> labelKeyAndValuesMap = new HashMap<>();
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    labelKeyAndValuesMap.put("kk", labelKeyValues);
    labelManagerMapper.selectLabelIdByLabelKeyValuesMaps(labelKeyAndValuesMap);
  }

  @Test
  public void deleteResourceByLabelKeyValuesMaps() {
    labelManagerMapper.deleteResourceByLabelKeyValuesMaps(1);
  }

  @Test
  public void deleteResourceByLabelKeyValuesMapsInDirect() {
    labelManagerMapper.deleteResourceByLabelKeyValuesMapsInDirect(1);
  }

  @Test
  public void batchDeleteResourceByLabelId() {
    List<Integer> notBlankIds = new ArrayList<>();
    notBlankIds.add(1);
    labelManagerMapper.batchDeleteResourceByLabelId(notBlankIds);
  }

  @Test
  public void batchDeleteResourceByLabelKeyValuesMaps() {
    Map<String, Map<String, String>> labelKeyAndValuesMap = new HashMap<>();
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    labelKeyAndValuesMap.put("kk", labelKeyValues);
    labelManagerMapper.batchDeleteResourceByLabelKeyValuesMaps(labelKeyAndValuesMap);
  }

  @Test
  public void listLabelByKeyValueMap() {
    Map<String, Map<String, String>> labelKeyAndValuesMap = new HashMap<>();
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    labelKeyAndValuesMap.put("kk", labelKeyValues);
    labelManagerMapper.listLabelByKeyValueMap(labelKeyAndValuesMap);
  }

  @Test
  public void dimListLabelByValueList() {
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    List<Map<String, String>> valueList = new ArrayList<>();
    valueList.add(labelKeyValues);
    labelManagerMapper.dimListLabelByValueList(valueList, "vr");
  }

  @Test
  public void dimListLabelByKeyValueMap() {
    Map<String, Map<String, String>> labelKeyAndValuesMap = new HashMap<>();
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    labelKeyAndValuesMap.put("kk", labelKeyValues);
    labelManagerMapper.dimListLabelByKeyValueMap(labelKeyAndValuesMap, "name");
  }

  @Test
  public void dimlistResourceLabelByKeyValueMap() {
    Map<String, Map<String, String>> labelKeyAndValuesMap = new HashMap<>();
    Map<String, String> labelKeyValues = new HashMap<>();
    labelKeyValues.put("k1", "v1");
    labelKeyAndValuesMap.put("kk", labelKeyValues);
    labelManagerMapper.dimlistResourceLabelByKeyValueMap(labelKeyAndValuesMap, "name");
  }

  @Test
  public void listLabelBySQLPattern() {
    labelManagerMapper.listLabelBySQLPattern("pattern", "name");
  }
}
