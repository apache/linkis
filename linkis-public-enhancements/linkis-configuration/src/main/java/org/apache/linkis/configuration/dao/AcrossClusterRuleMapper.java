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

package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.AcrossClusterRule;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AcrossClusterRuleMapper {

  AcrossClusterRule getAcrossClusterRule(@Param("id") Long id, @Param("username") String username);

  void deleteAcrossClusterRule(@Param("id") Long id);

  void deleteAcrossClusterRuleByBatch(@Param("ids") List<Long> ids);

  void deleteAcrossClusterRuleByUsername(@Param("username") String username);

  void deleteAcrossClusterRuleByCrossQueue(@Param("crossQueue") String crossQueue);

  void updateAcrossClusterRule(@Param("acrossClusterRule") AcrossClusterRule acrossClusterRule);

  void updateAcrossClusterRuleByBatch(
      @Param("ids") List<Long> ids,
      @Param("acrossClusterRule") AcrossClusterRule acrossClusterRule);

  void insertAcrossClusterRule(@Param("acrossClusterRule") AcrossClusterRule acrossClusterRule);

  List<AcrossClusterRule> queryAcrossClusterRuleList(
      @Param("username") String username,
      @Param("creator") String creator,
      @Param("clusterName") String clusterName);

  void validAcrossClusterRule(
      @Param("isValid") String isValid, @Param("id") Long id, @Param("username") String username);

  /**
   * Query across cluster resource rule by username.
   *
   * @param username
   * @return
   */
  AcrossClusterRule queryAcrossClusterRuleByUserName(@Param("username") String username);

  void validAcrossClusterRuleByBatch(
      @Param("ids") List<Long> ids, @Param("isValid") String isValid);
}
