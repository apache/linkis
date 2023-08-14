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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.dao.LabelManagerMapper;
import org.apache.linkis.manager.dao.ResourceManagerMapper;
import org.apache.linkis.manager.entity.Tunple;
import org.apache.linkis.manager.exception.PersistenceErrorException;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.persistence.ResourceLabelPersistence;
import org.apache.linkis.manager.util.PersistenceUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultResourceLabelPersistence implements ResourceLabelPersistence {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultResourceLabelPersistence.class);

  private LabelManagerMapper labelManagerMapper;

  private ResourceManagerMapper resourceManagerMapper;

  public LabelManagerMapper getLabelManagerMapper() {
    return labelManagerMapper;
  }

  public void setLabelManagerMapper(LabelManagerMapper labelManagerMapper) {
    this.labelManagerMapper = labelManagerMapper;
  }

  public void setResourceManagerMapper(ResourceManagerMapper resourceManagerMapper) {
    this.resourceManagerMapper = resourceManagerMapper;
  }

  /**
   * 拿到的Label一定要在 Label_resource 表中有记录 labelKeyValues 是由多个Label打散好放到List中，方便存在重复的Key 1.
   * 只要labelValueSize达到要求就返回 2. 返回的Label一定要在labelResource中有记录,也就是外面还得有一层Join
   *
   * @return
   */
  @Override
  @Deprecated
  public List<PersistenceLabel> getResourceLabels(List<LabelKeyValue> labelKeyValues) {
    if (CollectionUtils.isEmpty(labelKeyValues)) return Collections.emptyList();
    return Collections.emptyList();
  }

  @Override
  @Deprecated
  public List<PersistenceLabel> getResourceLabels(
      Map<String, Map<String, String>> labelKeyAndValuesMap, Label.ValueRelation valueRelation) {
    if (PersistenceUtils.KeyValueMapIsEmpty(labelKeyAndValuesMap)) return Collections.emptyList();
    return null;
  }

  @Override
  // @Transactional(rollbackFor = Throwable.class)
  public void setResourceToLabel(PersistenceLabel label, PersistenceResource persistenceResource) {
    if (label == null || persistenceResource == null) return;
    if (label.getId() <= 0) {
      if (StringUtils.isEmpty(label.getStringValue())) return;
      PersistenceLabel resourceLabel =
          labelManagerMapper.getLabelByKeyValue(label.getLabelKey(), label.getStringValue());
      if (null == resourceLabel) {
        labelManagerMapper.registerLabel(label);
      } else {
        label.setId(resourceLabel.getId());
      }
    }

    List<PersistenceResource> resourceByLabels =
        labelManagerMapper.listResourceByLaBelId(label.getId());
    if (CollectionUtils.isNotEmpty(resourceByLabels)) {
      if (resourceByLabels.size() > 1) {
        logger.warn("Label[" + label + "]has resource size > 1");
      }
      PersistenceResource resourceToUpdate = Iterables.getFirst(resourceByLabels, null);
      resourceManagerMapper.nodeResourceUpdateByResourceId(
          resourceToUpdate.getId(), persistenceResource);
    } else {
      synchronized (this) {
        List<PersistenceResource> resources =
            labelManagerMapper.listResourceByLaBelId(label.getId());
        if (CollectionUtils.isEmpty(resources)) {
          persistenceResource.setCreator(persistenceResource.getUpdator());
          resourceManagerMapper.registerResource(persistenceResource);
          labelManagerMapper.addLabelsAndResource(
              persistenceResource.getId(), Collections.singletonList(label.getId()));
        }
      }
    }
  }

  @Override
  public List<PersistenceResource> getResourceByLabel(PersistenceLabel label) {
    // label id 不为空，则直接通过label_id 查询，否则通过 value_key and value_content 查询
    if (label == null) return Collections.emptyList();
    if (label.getId() != null && label.getId() > 0) {
      return labelManagerMapper.listResourceByLaBelId(label.getId());
    } else {
      PersistenceLabel dbLabel =
          labelManagerMapper.getLabelByKeyValue(label.getLabelKey(), label.getStringValue());
      if (null == dbLabel) {
        return Collections.emptyList();
      } else {
        return labelManagerMapper.listResourceByLaBelId(dbLabel.getId());
      }
    }
    /*else {
        Map<String, String> value = label.getValue();
        if (MapUtils.isEmpty(value)) return Collections.emptyList();
        String dimType = Label.ValueRelation.ALL.name();
        return labelManagerMapper.dimListResourceBykeyValueMap(Collections.singletonMap(label.getLabelKey(), value), dimType);
    }*/
  }

  @Override
  // @Transactional(rollbackFor = Throwable.class)
  public void removeResourceByLabel(PersistenceLabel label) {
    // label id 不为空，则直接通过label_id 查询，否则通过 value_key and value_content 查询
    int labelId = label.getId();
    if (labelId <= 0) {
      PersistenceLabel labelByKeyValue =
          labelManagerMapper.getLabelByKeyValue(label.getLabelKey(), label.getStringValue());
      if (labelByKeyValue == null) {
        throw new PersistenceErrorException(
            210001, "label not found, this label may be removed already.");
      }
      labelId = labelByKeyValue.getId();
    }
    if (labelId > 0) {
      labelManagerMapper.deleteResourceByLabelIdInDirect(labelId);
      labelManagerMapper.deleteResourceByLabelId(labelId);
    }
  }

  @Override
  public void removeResourceByLabels(List<PersistenceLabel> labels) {
    // label id 不为空，则直接通过label_id 查询，否则通过 value_key and value_content 查询
    List<PersistenceLabel> notBlankIds =
        labels.stream().filter(l -> l.getId() != null).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(notBlankIds)) {
      List<Integer> ids =
          notBlankIds.stream().map(PersistenceLabel::getId).collect(Collectors.toList());
      labelManagerMapper.batchDeleteResourceByLabelId(ids);
      labelManagerMapper.batchDeleteResourceByLabelIdInDirect(ids);
    }
    List<PersistenceLabel> blankIds =
        labels.stream().filter(l -> l.getId() == null).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(blankIds)) {
      Map<String, Map<String, String>> keyValueMaps =
          blankIds.stream()
              .map(PersistenceUtils::entryToTunple)
              .collect(
                  Collectors.toMap(
                      Tunple::getKey, Tunple::getValue, (existingValue, newValue) -> newValue));
      // labelManagerMapper.batchDeleteResourceByLabelKeyValuesMaps(keyValueMaps);
    }
  }
}
