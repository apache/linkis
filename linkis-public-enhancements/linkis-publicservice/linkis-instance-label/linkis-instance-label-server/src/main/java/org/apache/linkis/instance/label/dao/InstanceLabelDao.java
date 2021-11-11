/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Operate label of instance
 */
public interface InstanceLabelDao {

    /**
     * Select by id ( tip: select ... for update)
     * @param labelId label id
     * @return
     */
    InsPersistenceLabel selectForUpdate(Integer labelId);

    /**
     * Select by label_key and label_value (tip: select ... for update)
     * @param labelKey label key
     * @param labelValue label value
     * @return
     */
    InsPersistenceLabel searchForUpdate(@Param("labelKey") String labelKey, @Param("labelValue") String labelValue);

    /**
     * Insert label entities
     * If you want to get id from multiple insert operation, use MyBatis 3.3.x
     * @param labels labels
     */
    void insertBatch(List<InsPersistenceLabel> labels);

    /**
     * Insert label
     * @param label label entity
     */
    void insert(InsPersistenceLabel label);

    /**
     * To fetch update lock of record in transaction
     * @param labelId
     */
    int updateForLock(Integer labelId);
    /**
     * Search labels by key and string value
     * @param labelSearch search vo
     * @return
     */
    List<InsPersistenceLabel> search(List<InsPersistenceLabelSearchVo> labelSearch);

    /**
     * Remove label
     * @param label label entity
     */
    void remove(InsPersistenceLabel label);

    /**
     * Insert key -> value of label
     * @param keyValues key -> value
     */
    void doInsertKeyValues(List<InsPersistenceLabelValue> keyValues);

    /**
     * Remove key -> value map from label id
     * @param labelId
     */
    void doRemoveKeyValues(Integer labelId);

    void doRemoveKeyValuesBatch(List<Integer> labelIds);


}
