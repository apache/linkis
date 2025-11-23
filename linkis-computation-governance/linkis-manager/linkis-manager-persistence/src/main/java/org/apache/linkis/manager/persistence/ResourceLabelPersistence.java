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

package org.apache.linkis.manager.persistence;

import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.exception.PersistenceErrorException;
import org.apache.linkis.manager.label.entity.Label;

import java.util.List;
import java.util.Map;

public interface ResourceLabelPersistence {

  /**
   * 拿到的Label一定要在 Label_resource 表中有记录 labelKeyValues 是由多个Label打散好放到List中，方便存在重复的Key 1.
   * 只要labelValueSize达到要求就返回 2. 返回的Label一定要在labelResource中有记录,也就是外面还得有一层Join
   *
   * @return
   */
  List<PersistenceLabel> getResourceLabels(List<LabelKeyValue> labelKeyValues);

  /**
   * 拿到的Label一定要在 Label_resource 表中有记录 返回的Label一定要在labelResource中有记录,也就是外面还得有一层Join
   *
   * @return
   */
  List<PersistenceLabel> getResourceLabels(
      Map<String, Map<String, String>> labelKeyAndValuesMap, Label.ValueRelation valueRelation);

  /**
   * 判断ID是否存在，如果不存在则先插入，在插入关联关系
   *
   * @param label
   * @param persistenceResource
   */
  void setResourceToLabel(PersistenceLabel label, PersistenceResource persistenceResource);

  /**
   * 获取Resource 通过Label
   *
   * @param label
   * @return
   */
  List<PersistenceResource> getResourceByLabel(PersistenceLabel label);

  /**
   * 删除label和resource的关联关系
   *
   * @param label
   */
  void removeResourceByLabel(PersistenceLabel label) throws PersistenceErrorException;

  /** @param labels */
  void removeResourceByLabels(List<PersistenceLabel> labels);
}
