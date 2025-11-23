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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.instance.label.entity.InstanceInfo;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** Operate the relation between label and instance */
public interface InsLabelRelationDao {

  /**
   * Search instances
   *
   * @param valueContent key -> value map
   * @return
   */
  List<InstanceInfo> searchInsDirectByValues(
      @Param("valueMapList") List<Map<String, String>> valueContent,
      @Param("relation") String relation);

  List<InstanceInfo> searchInsDirectByLabels(List<InsPersistenceLabel> labels);

  /**
   * Search instances( will fetch the labels of instance cascade)
   *
   * @param valueContent key -> value map
   * @return
   */
  List<InstanceInfo> searchInsCascadeByValues(
      @Param("valueMapList") List<Map<String, String>> valueContent,
      @Param("relation") String relation);

  List<InstanceInfo> searchInsCascadeByLabels(List<InsPersistenceLabel> labels);

  /**
   * Search instances that are not related with other labels
   *
   * @param instanceInfo
   * @return
   */
  List<InstanceInfo> searchUnRelateInstances(InstanceInfo instanceInfo);

  List<InstanceInfo> searchLabelRelatedInstances(InstanceInfo instanceInfo);
  /**
   * Search labels
   *
   * @param instance instance value (http:port)
   * @return
   */
  List<InsPersistenceLabel> searchLabelsByInstance(String instance);

  /**
   * Search Instance with label
   *
   * @return
   */
  List<InstanceInfo> listAllInstanceWithLabel();

  List<ServiceInstance> getInstancesByNames(String appName);

  /**
   * Drop relationships by instance and label ids
   *
   * @param instance instance value (http:port)
   * @param labelIds label ids
   */
  void dropRelationsByInstanceAndLabelIds(
      @Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);

  void dropRelationsByInstance(String instance);
  /**
   * Insert relationship
   *
   * @param instance instance
   * @param labelIds label ids
   */
  void insertRelations(
      @Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);

  /**
   * If has relationship
   *
   * @param labelId
   * @return
   */
  Integer existRelations(Integer labelId);
}
