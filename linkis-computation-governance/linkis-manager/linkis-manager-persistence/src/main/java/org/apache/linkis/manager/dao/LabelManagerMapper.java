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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.label.entity.Label;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface LabelManagerMapper {

  void registerLabel(@Param("persistenceLabel") PersistenceLabel persistenceLabel);

  void registerLabelKeyValues(
      @Param("labelValueKeyAndContent") Map<String, String> labelValueKeyAndContent,
      @Param("labelId") int labelId);

  void replaceIntoLabelKeyValue(
      @Param("labelKey") String labelKey,
      @Param("labelStringValue") String labelStringValue,
      @Param("labelId") int labelId);

  PersistenceLabel getLabel(@Param("id") int id);

  void deleteLabel(@Param("id") int id);

  void deleteByLabel(
      @Param("labelKey") String labelKey, @Param("labelStringValue") String labelStringValue);

  void deleteLabelKeyVaules(@Param("id") int id);

  void updateLabel(
      @Param("id") int id, @Param("persistenceLabel") PersistenceLabel persistenceLabel);

  void addLabelServiceInstance(
      @Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);

  List<PersistenceLabel> getLabelByServiceInstance(@Param("instance") String instance);

  List<PersistenceLabel> getLabelByResource(
      @Param("persistenceResource") PersistenceResource persistenceResource);

  void addLabelsAndResource(
      @Param("resourceId") int resourceId, @Param("labelIds") List<Integer> labelIds);

  List<PersistenceResource> getResourcesByLabel(
      @Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

  List<Integer> getLabelIdsByInstance(@Param("instance") String instance);

  List<PersistenceLabel> getLabelsByInstance(@Param("instance") String instance);

  List<PersistenceNode> getInstanceByLabelId(@Param("labelId") int labelId);

  List<PersistenceResource> getResourcesByLabels(@Param("labels") List<? extends Label> labels);

  List<String> getInstanceIdsByLabelIds(@Param("labelIds") List<Integer> labelIds);

  List<PersistenceLabel> getLabelsByLabelIds(@Param("labelIds") List<Integer> labelIds);

  void addLabelsByUser(
      @Param("userName") String userName, @Param("labelIds") List<Integer> labelIds);

  List<String> getUserNameByLabelId(@Param("labelId") int labelId);

  List<String> getUserNamesByLabelIds(@Param("labelIds") List<Integer> labelIds);

  List<PersistenceLabel> getLabelsByUser(@Param("userName") String userName);

  List<PersistenceLabel> getLabelsByLabelKey(@Param("labelKey") String labelKey);

  void deleteLabelResourceByByTicketId(@Param("ticketId") String ticketId);

  void deleteLabelIdsAndInstance(
      @Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);

  void deleteLabelIdsByUser(
      @Param("userName") String userName, @Param("labelIds") List<Integer> labelIds);

  void deleteUserById(int labelId);

  /**
   * 通过lavel value 同时返回instance信息和label信息
   *
   * @param labelKeyAndValuesMap
   * @return
   */
  List<Map<String, Object>> dimListNodeRelationsByKeyValueMap(
      @Param("keyValueMap") Map<String, Map<String, String>> labelKeyAndValuesMap,
      @Param("valueRelation") String name);

  List<Map<String, Object>> getNodeRelationsByLabels(
      @Param("labels") List<PersistenceLabel> labels);

  /**
   * 通过instance信息，同时返回instance信息和label信息
   *
   * @param serviceInstances
   * @return
   */
  List<Map<String, Object>> listLabelRelationByServiceInstance(
      @Param("nodes") List<ServiceInstance> serviceInstances);

  /**
   * 通过labelkey 和StringValue找到唯一的label
   *
   * @param labelKey
   * @param stringValue
   * @return
   */
  PersistenceLabel getLabelByKeyValue(
      @Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

  List<ServiceInstance> getNodeByLabelKeyValue(
      @Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

  /**
   * @param values,可能是多个label的散列 all 和 and 类型的 查询
   * @return
   */
  List<PersistenceLabel> listResourceLabelByValues(@Param("values") List<LabelKeyValue> values);

  /**
   * 通过labelId获取到resource
   *
   * @param labelId
   * @return
   */
  List<PersistenceResource> listResourceByLaBelId(Integer labelId);

  /**
   * 通过labelValues获取实例资源，labelValues实际上为 {"instance":"ip:port","serviceName":"linkis-cg-engineconn"}
   * EC/ECM
   *
   * @param labelValues
   * @return
   */
  List<PersistenceResource> getInstanceResourceByLabelValue(
      @Param("labelValues") List<String> labelValues);

  /**
   * 通过labelId删除资源，还有资源和label的关联，并不会删除label表记录
   *
   * @param labelId
   */
  void deleteResourceByLabelId(Integer labelId);

  void deleteResourceByLabelIdInDirect(Integer labelId);

  /**
   * 同 {@link LabelManagerMapper#deleteResourceByLabelId(Integer)} 和下面的batchdelete有点重复，主要是sql给的是=
   * batch 的话给的是in
   *
   * @param singletonMap
   */
  Integer selectLabelIdByLabelKeyValuesMaps(
      @Param("labelKeyValues") Map<String, Map<String, String>> singletonMap);

  void deleteResourceByLabelKeyValuesMaps(Integer id);

  void deleteResourceByLabelKeyValuesMapsInDirect(Integer id);

  /**
   * 批量删除 不删除label
   *
   * @param notBlankIds
   */
  void batchDeleteResourceByLabelId(@Param("labelIds") List<Integer> notBlankIds);

  void batchDeleteResourceByLabelIdInDirect(@Param("labelIds") List<Integer> notBlankIds);

  /**
   * 批量删除 不删除label
   *
   * @param keyValueMaps
   */
  void batchDeleteResourceByLabelKeyValuesMaps(
      @Param("labelKeyValues") Map<String, Map<String, String>> keyValueMaps);

  /**
   * ALL,AND搜索，根据key，value找到label集合，不对instance和resource进行关联，效率稍比{@link
   * LabelManagerMapper#dimListLabelByKeyValueMap(Map, String)}
   *
   * @param keyValueMap
   * @return
   */
  @Deprecated
  List<PersistenceLabel> listLabelByKeyValueMap(
      @Param("keyValueMap") Map<String, Map<String, String>> keyValueMap);

  /**
   * 模糊搜索，根据value，valueRelation 找到label集合，不对instance和resource进行关联
   *
   * @param valueList
   * @return
   */
  List<PersistenceLabel> dimListLabelByValueList(
      @Param("valueList") List<Map<String, String>> valueList,
      @Param("valueRelation") String valueRelation);

  /**
   * 模糊搜索，根据key，value，valueRelation 找到label集合，不对instance和resource进行关联
   *
   * @param keyValueMap
   * @param name
   * @return
   */
  List<PersistenceLabel> dimListLabelByKeyValueMap(
      @Param("keyValueMap") Map<String, Map<String, String>> keyValueMap,
      @Param("valueRelation") String name);

  /**
   * 和{@link LabelManagerMapper#dimListLabelByKeyValueMap(Map, String)} 区别只是多了关联和资源表
   *
   * @param labelKeyAndValuesMap
   * @param name
   * @return
   */
  List<PersistenceLabel> dimlistResourceLabelByKeyValueMap(
      @Param("keyValueMap") Map<String, Map<String, String>> labelKeyAndValuesMap,
      @Param("valueRelation") String name);

  List<PersistenceLabelRel> listLabelBySQLPattern(
      @Param("pattern") String pattern, @Param("labelKey") String labelKey);
}
