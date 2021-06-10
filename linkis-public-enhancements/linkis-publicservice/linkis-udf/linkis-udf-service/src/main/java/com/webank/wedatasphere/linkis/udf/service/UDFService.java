/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.udf.service;

import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.udf.entity.UDFInfo;
import com.webank.wedatasphere.linkis.udf.excepiton.UDFException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public interface UDFService {

    UDFInfo addUDF(UDFInfo udfInfo,  String userName) throws UDFException;

    UDFInfo updateUDF(UDFInfo udfInfo, String userName) throws UDFException;

    Boolean deleteUDF(Long id,  String userName) throws UDFException;

    UDFInfo getUDFById(Long id, String userName) throws UDFException;

    Boolean deleteLoadInfo(Long id, String userName) throws UDFException;

    Boolean addLoadInfo(Long id, String userName) throws UDFException;

    List<UDFInfo> getUDFSByUserName(String userName) throws UDFException;

    List<UDFInfo> getUDFSByTreeIdAndUser(Long treeId, String userName, String category) throws UDFException;

   /* List<UDFInfo> getSysUDF();

    List<UDFInfo> getSysUDFByTreeId(Integer treeId);*/

    List<UDFInfo>  getUDFInfoByTreeId(Long treeId, String userName, String category) throws UDFException;

    Map<String, List<String>> generateInitSql(String userName) throws UDFException;

    Iterator<String> getAllLoadJars(String userName) throws UDFException;

    List<UDFInfo> getSharedUDFByUserName(String userName) throws UDFException;

    List<UDFInfo> getSharedUDFByTreeId(Integer treeId, String userName) throws UDFException;

    List<UDFInfo> getSharedUDFInfos(Long id, String userName, String category);

    Boolean isUDFManager(String userName);

    void checkSharedUsers(List<String> sharedUsers, String userName)throws UDFException;

    UDFInfo addSharedUDFInfo(UDFInfo sharedUDFInfo)throws UDFException;

    void addUDFSharedUsers(List<String> sharedUsers, Long id);


    void setUDFSharedInfo(boolean iShared, Long id);

    Long getAllShareUDFInfoIdByUDFId(String userName, String udfName);

    void setSharedUDFInfoExpire(Long shareUDFId);

    List<String> getAllgetSharedUsersByUDFIdAndUseName(String userName, String udfName);

    void addSharedUser(List<String> sharedUsers, Long udfId);

    void removeSharedUser(List<String> oldsharedUsers, Long udfId);

    FsPath copySharedUdfFile(String userName, UDFInfo udfInfo) throws IOException;

    UDFInfo createSharedUdfInfo(UDFInfo udfInfo, Long shareParentId, FsPath sharedPath) throws Exception;
}
