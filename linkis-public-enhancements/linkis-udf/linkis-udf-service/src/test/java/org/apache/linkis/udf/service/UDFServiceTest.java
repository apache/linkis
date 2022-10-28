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

import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.udf.dao.UDFDao;
import org.apache.linkis.udf.dao.UDFTreeDao;
import org.apache.linkis.udf.dao.UDFVersionDao;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFVersion;
import org.apache.linkis.udf.service.impl.UDFServiceImpl;
import org.apache.linkis.udf.vo.UDFAddVo;
import org.apache.linkis.udf.vo.UDFInfoVo;
import org.apache.linkis.udf.vo.UDFVersionVo;

import java.util.*;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class UDFServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(UDFServiceTest.class);

  @InjectMocks private UDFServiceImpl udfServiceImpl;

  @Mock private UDFDao udfDao;

  @Mock private UDFTreeDao udfTreeDao;

  @Mock private UDFVersionDao udfVersionDao;

  @Mock private CommonLockService commonLockService;

  @Test
  @DisplayName("deleteUDFTest")
  public void deleteUDFTest() {
    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setShared(false);
    Mockito.when(udfDao.getUDFById(Mockito.anyLong())).thenReturn(udfInfo);
    Assertions.assertAll(
        () -> {
          Boolean deleteUDF = udfServiceImpl.deleteUDF(4L, "hadoop");
          Assertions.assertTrue(deleteUDF.booleanValue());
        });
  }

  @Test
  @DisplayName("getUDFByIdTest")
  public void getUDFByIdTest() {

    Assertions.assertAll(
        () -> {
          UDFInfo udfInfo = udfServiceImpl.getUDFById(4L, "hadoop");
          Assertions.assertNull(udfInfo);
        });
  }

  @Test
  @DisplayName("deleteLoadInfoTest")
  public void deleteLoadInfoTest() {

    Assertions.assertAll(
        () -> {
          Boolean deleteLoadInfo = udfServiceImpl.deleteLoadInfo(4L, "hadoop");
          Assertions.assertTrue(deleteLoadInfo.booleanValue());
        });
  }

  @Test
  @DisplayName("addLoadInfoTest")
  public void addLoadInfoTest() {
    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setUdfType(2);
    Mockito.when(udfDao.getUDFById(Mockito.anyLong())).thenReturn(udfInfo);
    Assertions.assertAll(
        () -> {
          Boolean addLoadInfo = udfServiceImpl.addLoadInfo(7L, "hadoop");
          Assertions.assertTrue(addLoadInfo.booleanValue());
        });
  }

  @Test
  @DisplayName("getUDFSByTreeIdAndUserTest")
  public void getUDFSByTreeIdAndUserTest() {
    List<UDFInfoVo> udfInfoVoList = udfServiceImpl.getUDFSByTreeIdAndUser(13L, "hadoop", "all");
    Assertions.assertTrue(udfInfoVoList.size() == 0);
  }

  @Test
  @DisplayName("getUDFInfoByTreeIdTest")
  public void getUDFInfoByTreeIdTest() {
    List<UDFInfoVo> udfInfoVos = udfServiceImpl.getUDFInfoByTreeId(13L, "hadoop", "all");
    Assertions.assertTrue(udfInfoVos.size() == 0);
  }

  @Test
  @DisplayName("getSharedUDFsTest")
  public void getSharedUDFsTest() {
    List<UDFInfoVo> sharedUDFs = udfServiceImpl.getSharedUDFs("hadoop", "all");
    Assertions.assertTrue(sharedUDFs.size() == 0);
  }

  @Test
  @DisplayName("getExpiredUDFsTest")
  public void getExpiredUDFsTest() {
    List<UDFInfoVo> expiredUDFs = udfServiceImpl.getExpiredUDFs("hadoop", "all");
    Assertions.assertTrue(expiredUDFs.size() == 0);
  }

  @Test
  @DisplayName("isUDFManagerTest")
  public void isUDFManagerTest() {
    Boolean isUdfManager = udfServiceImpl.isUDFManager("hadoop");
    Assertions.assertFalse(isUdfManager.booleanValue());
  }

  @Test
  @DisplayName("setUDFSharedInfoTest")
  public void setUDFSharedInfoTest() {

    Assertions.assertAll(() -> udfServiceImpl.setUDFSharedInfo(true, 13L));
  }

  @Test
  @DisplayName("setUdfExpireTest")
  public void setUdfExpireTest() {
    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setShared(true);
    Mockito.when(udfDao.getUDFById(Mockito.anyLong())).thenReturn(udfInfo);
    Mockito.when(udfDao.getUserLoadCountByUdfId(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(2L);
    Assertions.assertAll(
        () -> {
          udfServiceImpl.setUdfExpire(13L, "hadoop");
        });
  }

  @Test
  @DisplayName("getAllSharedUsersByUdfIdTest")
  public void getAllSharedUsersByUdfIdTest() {
    List<String> users = udfServiceImpl.getAllSharedUsersByUdfId("hadoop", 13L);
    Assertions.assertTrue(users.size() == 0);
  }

  @Test
  @DisplayName("addSharedUserTest")
  public void addSharedUserTest() {
    Set<String> sharedUsers = new HashSet<>();
    sharedUsers.add("tangxr");
    sharedUsers.add("baoyang");
    Assertions.assertAll(() -> udfServiceImpl.addSharedUser(sharedUsers, 4L));
  }

  @Test
  @DisplayName("publishUdfTest")
  public void publishUdfTest() {
    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setShared(true);
    Mockito.when(udfDao.getUDFById(Mockito.anyLong())).thenReturn(udfInfo);
    Assertions.assertAll(
        () -> {
          udfServiceImpl.publishUdf(3L, "v000001");
        });
  }

  @Test
  @DisplayName("publishLatestUdfTest")
  public void publishLatestUdfTest() {

    UDFVersion udfVersion = new UDFVersion();
    udfVersion.setBmlResourceVersion("v000001");
    Mockito.when(udfVersionDao.selectLatestByUdfId(Mockito.anyLong())).thenReturn(udfVersion);
    Assertions.assertAll(
        () -> {
          udfServiceImpl.publishLatestUdf(4L);
        });
  }

  @Test
  @DisplayName("getUdfVersionListTest")
  public void getUdfVersionListTest() {

    List<UDFVersionVo> udfVersionList = udfServiceImpl.getUdfVersionList(4L);
    Assertions.assertTrue(udfVersionList.size() == 0);
  }

  @Test
  @DisplayName("getManagerPagesTest")
  public void getManagerPagesTest() {
    Collection<Integer> udfType = new ArrayList<>();
    udfType.add(3);
    udfType.add(4);
    Assertions.assertAll(
        () -> {
          PageInfo<UDFAddVo> managerPages =
              udfServiceImpl.getManagerPages("test", udfType, "hadoop", 0, 10);
          Assertions.assertTrue(managerPages.getSize() == 0);
        });
  }

  @Test
  @DisplayName("allUdfUsersTest")
  public void allUdfUsersTest() {

    List<String> users = udfServiceImpl.allUdfUsers();
    Assertions.assertTrue(users.size() == 0);
  }

  @Test
  @DisplayName("getUserDirectoryTest")
  public void getUserDirectoryTest() {
    List<String> userDirectory = udfServiceImpl.getUserDirectory("hadoop", "function");
    Assertions.assertTrue(userDirectory.size() == 0);
  }

  @Test
  @DisplayName("getAllUDFSByUserNameTest")
  public void getAllUDFSByUserNameTest() {
    Assertions.assertAll(
        () -> {
          List<UDFInfoVo> udfs = udfServiceImpl.getAllUDFSByUserName("hadoop");
          Assertions.assertTrue(udfs.size() == 0);
        });
  }
}
