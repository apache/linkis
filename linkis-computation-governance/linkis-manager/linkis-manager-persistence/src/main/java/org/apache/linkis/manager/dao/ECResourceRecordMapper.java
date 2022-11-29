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

import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ECResourceRecordMapper {

  ECResourceInfoRecord getECResourceInfoRecord(@Param("ticketId") String ticketId);

  ECResourceInfoRecord getECResourceInfoRecordByInstance(@Param("instance") String instance);

  void updateECResourceInfoRecord(ECResourceInfoRecord resourceActionRecord);

  void insertECResourceInfoRecord(ECResourceInfoRecord resourceActionRecord);

  void deleteECResourceInfoRecordByTicketId(@Param("ticketId") String ticketId);

  void deleteECResourceInfoRecord(@Param("id") Integer id);

  List<ECResourceInfoRecord> getECResourceInfoHistory(
      @Param("username") String username,
      @Param("instance") String instance,
      @Param("endDate") Date endDate,
      @Param("startDate") Date startDate,
      @Param("engineType") String engineType);

  List<ECResourceInfoRecord> getECResourceInfoList(
      @Param("instances") List<String> instances, @Param("engineTypes") List<String> engineTypes);
}
