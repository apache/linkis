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

package org.apache.linkis.manager.am.service;

import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ECResourceInfoService {

  ECResourceInfoRecord getECResourceInfoRecord(String ticketId);

  ECResourceInfoRecord getECResourceInfoRecordByInstance(String instance);

  void deleteECResourceInfoRecordByTicketId(String ticketId);

  void deleteECResourceInfoRecord(Integer id);

  List<ECResourceInfoRecord> getECResourceInfoRecordList(
      String instance,
      Date endDate,
      Date startDate,
      String username,
      String engineType,
      String status);

  /**
   * @param creatorUserList engineconn creator list
   * @param engineTypeList engineconn type list
   * @param statusStrList engineconn status string list
   * @param queueName
   * @param ecInstancesList
   * @param isCrossCluster
   * @return
   */
  List<Map<String, Object>> getECResourceInfoList(
      List<String> creatorUserList,
      List<String> engineTypeList,
      List<String> statusStrList,
      String queueName,
      List<String> ecInstancesList,
      Boolean isCrossCluster);
}
