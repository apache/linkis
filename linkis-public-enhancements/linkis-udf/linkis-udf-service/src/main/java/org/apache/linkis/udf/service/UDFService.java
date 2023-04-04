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

package org.apache.linkis.udf.service;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.vo.*;

import java.util.*;

import com.github.pagehelper.PageInfo;

public interface UDFService {

  long addUDF(UDFAddVo udfInfo, String userName) throws Exception;

  void updateUDF(UDFUpdateVo udfUpdateVo, String userName) throws Exception;

  Boolean deleteUDF(Long id, String userName) throws UDFException;

  UDFInfo getUDFById(Long id, String userName) throws UDFException;

  Boolean deleteLoadInfo(Long id, String userName) throws UDFException;

  Boolean addLoadInfo(Long id, String userName) throws UDFException;

  List<UDFInfo> getUDFSByUserName(String userName) throws UDFException;

  List<UDFInfoVo> getUDFSByTreeIdAndUser(Long treeId, String userName, String category)
      throws UDFException;

  /* List<UDFInfo> getSysUDF();

  List<UDFInfo> getSysUDFByTreeId(Integer treeId);*/

  List<UDFInfoVo> getUDFInfoByTreeId(Long treeId, String userName, String category)
      throws UDFException;

  List<UDFInfoVo> getUDFInfoByIds(String username, Long[] ids, String category) throws UDFException;

  Map<String, List<String>> generateInitSql(String userName) throws UDFException;

  Iterator<String> getAllLoadJars(String userName) throws UDFException;

  List<UDFInfo> getSharedUDFByUserName(String userName) throws UDFException;

  List<UDFInfo> getSharedUDFByTreeId(Integer treeId, String userName) throws UDFException;

  List<UDFInfo> getSharedUDFInfos(Long id, String userName, String category);

  List<UDFInfoVo> getSharedUDFs(String userName, String category);

  List<UDFInfoVo> getExpiredUDFs(String userName, String category);

  Boolean isUDFManager(String userName);

  void checkSharedUsers(Set<String> sharedUsers, String userName, String udfname)
      throws UDFException;

  UDFInfo addSharedUDFInfo(UDFInfo sharedUDFInfo) throws UDFException;

  void setUDFSharedInfo(boolean iShared, Long id);

  Long getAllShareUDFInfoIdByUDFId(String userName, String udfName);

  void setUdfExpire(Long shareUDFId, String userName) throws UDFException;

  List<String> getAllSharedUsersByUdfId(String userName, long udfId);

  void addSharedUser(Set<String> sharedUsers, Long udfId);

  void removeSharedUser(Collection<String> oldsharedUsers, Long udfId);

  //    FsPath copySharedUdfFile(String userName, UDFInfo udfInfo) throws IOException;

  UDFInfo createSharedUdfInfo(UDFInfo udfInfo, Long shareParentId, FsPath sharedPath)
      throws Exception;

  void handoverUdf(Long udfId, String handoverUser) throws UDFException;

  void publishUdf(Long udfId, String version) throws UDFException;

  void publishLatestUdf(Long udfId) throws UDFException;

  void rollbackUDF(Long udfId, String version, String userName) throws UDFException;

  List<UDFVersionVo> getUdfVersionList(long udfId);

  PageInfo<UDFAddVo> getManagerPages(
      String udfName, Collection<Integer> udfType, String createUser, int curPage, int pageSize)
      throws Exception;

  String downLoadUDF(long udfId, String version, String user) throws Exception;

  DownloadVo downloadToLocal(long udfId, String version, String user) throws Exception;

  List<String> allUdfUsers();

  List<String> getUserDirectory(String user, String category);

  List<UDFInfoVo> getAllUDFSByUserName(String userName) throws UDFException;

  /**
   * * Get udf information according to udfnameList
   *
   * @param userNameList
   * @param createUser
   * @return
   */
  List<UDFAddVo> getUdfByNameList(List<String> userNameList, String createUser);

  /**
   * * Get the udf version information according to udfname and creator
   *
   * @param udfName
   * @param createUser
   * @return
   */
  UDFVersionVo getUdfVersionInfo(String udfName, String createUser);
}
