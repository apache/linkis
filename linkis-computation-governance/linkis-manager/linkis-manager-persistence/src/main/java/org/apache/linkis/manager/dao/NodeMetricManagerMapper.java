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

import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface NodeMetricManagerMapper {

  void addNodeMetrics(@Param("nodeMetrics") PersistenceNodeMetrics nodeMetrics);

  Integer checkInstanceExist(@Param("instance") String instance);

  List<PersistenceNodeMetrics> getNodeMetricsByInstances(
      @Param("instances") List<String> instances);

  PersistenceNodeMetrics getNodeMetricsByInstance(@Param("instance") String instance);

  void updateNodeMetrics(
      @Param("nodeMetrics") PersistenceNodeMetrics nodeMetrics, @Param("instance") String instance);

  void deleteNodeMetrics(@Param("instance") String instance);

  void deleteNodeMetricsByInstance(@Param("instance") String instance);

  List<PersistenceNodeMetricsEntity> getAllNodeMetrics();

  int updateNodeStatus(
      @Param("instance") String instance,
      @Param("instanceStatus") int instanceStatus,
      @Param("oldStatus") int oldStatus);
}
