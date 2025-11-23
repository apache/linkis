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

package org.apache.linkis.metadata.service;

import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBO;
import org.apache.linkis.metadata.domain.mdq.vo.*;
import org.apache.linkis.metadata.exception.MdqIllegalParamException;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;

import java.io.IOException;
import java.util.List;

public interface MdqService {

  /**
   * 激活表操作，sparkEngine执行成功后使用rpc请求调用，参数是数据库主键id
   *
   * @param tableId
   */
  @Deprecated
  void activateTable(Long tableId);

  /**
   * 传入MdqTableBO 由sparkEnginerpc请求jsonStrig序列化得到，执行插入数据库的记录，返回数据库主键id，用来做激活的标识
   *
   * @param mdqTableBO
   * @return
   */
  Long persistTable(MdqTableBO mdqTableBO, String userName);

  MdqTableStatisticInfoVO getTableStatisticInfo(MetadataQueryParam queryParam, String partitionSort)
      throws IOException;

  /**
   * 产生sql给前台，和sparkEngine
   *
   * @param mdqTableBO
   * @return
   */
  String displaysql(MdqTableBO mdqTableBO);

  boolean isExistInMdq(String database, String tableName, String user);

  boolean isExistInHive(MetadataQueryParam queryParam);

  MdqTableBaseInfoVO getTableBaseInfoFromMdq(String database, String tableName, String user);

  MdqTableBaseInfoVO getTableBaseInfoFromHive(MetadataQueryParam queryParam);

  List<MdqTableFieldsInfoVO> getTableFieldsInfoFromMdq(
      String database, String tableName, String user);

  List<MdqTableFieldsInfoVO> getTableFieldsInfoFromHive(MetadataQueryParam queryParam);

  MdqTableStatisticInfoVO getTableStatisticInfoFromHive(
      MetadataQueryParam queryParam, String partitionSort) throws IOException;

  MdqTablePartitionStatisticInfoVO getPartitionStatisticInfo(
      MetadataQueryParam queryParam, String partitionName)
      throws IOException, MdqIllegalParamException;

  MdqTableStatisticInfoDTO getTableInfo(MetadataQueryParam queryParam) throws IOException;
}
