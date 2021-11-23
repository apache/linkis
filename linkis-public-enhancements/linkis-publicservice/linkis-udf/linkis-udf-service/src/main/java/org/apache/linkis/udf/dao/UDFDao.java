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
 
package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFManager;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;


public interface UDFDao {

    void addUDF(UDFInfo udfInfo);

    void updateUDF(UDFInfo udfInfo);

    void deleteUDF(Long id, String createUser);

    UDFInfo getUDFById(Long id);

    void deleteLoadInfo(Long id, String userName);

    void deleteAllLoadInfo(Long id);

    void addLoadInfo(Long id, String userName);

    List<UDFInfo> getUDFSByUserName(String userName);

    List<UDFInfo> getUDFSByTreeIdAndUser(Long treeId, String userName, Collection<Integer> categoryCodes);

    /*List<UDFInfo> getSharedUDFByUserName(String userName);

    List<UDFInfo> getSharedUDFByTreeId(Long treeId, String userName);

    List<UDFInfo> getSysUDF();

    List<UDFInfo> getSysUDFByTreeId(Long treeId);*/

    List<UDFInfo>  getUDFInfoByTreeId(Long treeId, String userName, Collection<Integer> categoryCodes);

    List<UDFInfo> getLoadedUDFs(String userName);

    long getSameSysCount(String udfName);

    long getSameLoadCount(String userName,String udfName);

    List<UDFInfo> getSameJarUDF(String userName, String path);

    long getSameNameCountByUser(String udfName, String userName);

    List<UDFInfo> selectSharedUDFInfosByTreeIdAndUserName(@Param("TreeId") Long id, @Param("userName") String userName, @Param("categoryCodes") Collection<Integer> categoryCodes);

    UDFManager selectUDFManager(String userName);

    List<String> selectAllUser();

    long getShareSameNameCountByUser(@Param("udfName") String udfName);

    void insertUDFSharedUser(@Param("udfId")Long udfId, @Param("shareUserName") String shareUserName);

    void updateUDFIsShared(@Param("isShared") Boolean isShared, @Param("id") long id);

    Long selectAllShareUDFInfoIdByUDFId(@Param("userName") String userName,@Param("udfName")String udfName);

    Long selectIdByUserName(@Param("userName") String userName);

    void updateSharedUDFInfoExpire(Long shareUDFId);

    List<String> selectAllShareUsersByUDFIdAndUseName(@Param("userName") String userName,@Param("udfName")String udfName);

    void insertSharedUser(@Param("addSharedUser")String sharedUser,@Param("udfId") Long udfId);

    void deleteSharedUser(@Param("removeSharedUser") String oldsharedUser,@Param("udfId") Long udfId);

    void deleteAllSharedUser(@Param("udfId") Long udfId);
}
