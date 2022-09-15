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

package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFManager;
import org.apache.linkis.udf.vo.UDFAddVo;
import org.apache.linkis.udf.vo.UDFInfoVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDFDaoTest extends BaseDaoTest {

  private static final Logger LOG = LoggerFactory.getLogger(UDFDaoTest.class);

  @Autowired private UDFDao udfDao;

  @Test
  @DisplayName("addUDFTest")
  public void addUDFTest() {
    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setId(6L);
    udfInfo.setCreateUser("hadoop");
    udfInfo.setUdfType(3);
    udfInfo.setTreeId(13L);
    udfInfo.setCreateTime(new Date());
    udfInfo.setUpdateTime(new Date());
    udfInfo.setSys("IDE");
    udfInfo.setClusterName("ALL");
    udfInfo.setUdfName("udfNameTest");
    udfInfo.setExpire(null);
    udfInfo.setShared(null);

    Assertions.assertAll(() -> udfDao.addUDF(udfInfo));
  }

  @Test
  @DisplayName("updateUDFTest")
  public void updateUDFTest() {

    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setId(4L);
    udfInfo.setCreateUser("hadoop");
    udfInfo.setUdfName("updateTest");
    udfInfo.setUdfType(3);
    udfInfo.setTreeId(13L);
    udfInfo.setUpdateTime(new Date());

    Assertions.assertAll(() -> udfDao.updateUDF(udfInfo));
  }

  @Test
  @DisplayName("deleteUDFTest")
  public void deleteUDFTest() {
    Assertions.assertAll(() -> udfDao.deleteUDF(4L, "hadoop"));
  }

  @Test
  @DisplayName("getUDFByIdTest")
  public void getUDFByIdTest() {
    UDFInfo udfInfo = udfDao.getUDFById(4L);
    Assertions.assertNotNull(udfInfo);
  }

  @Test
  @DisplayName("deleteLoadInfoTest")
  public void deleteLoadInfoTest() {
    Assertions.assertAll(() -> udfDao.deleteLoadInfo(4L, "hadoop"));
  }

  @Test
  @DisplayName("addLoadInfoTest")
  public void addLoadInfoTest() {
    Assertions.assertAll(() -> udfDao.addLoadInfo(7L, "hadoop"));
  }

  @Test
  @DisplayName("getUDFSByUserNameTest")
  public void getUDFSByUserNameTest() {
    List<UDFInfo> udfInfoList = udfDao.getUDFSByUserName("hadoop");
    Assertions.assertTrue(udfInfoList.size() > 0);
  }

  @Test
  @DisplayName("getUDFSByTreeIdAndUserTest")
  public void getUDFSByTreeIdAndUserTest() {
    Collection<Integer> categoryCodes = new ArrayList<>();
    categoryCodes.add(3);
    categoryCodes.add(4);
    List<UDFInfoVo> udfInfoVoList = udfDao.getUDFSByTreeIdAndUser(13L, "hadoop", categoryCodes);
    Assertions.assertTrue(udfInfoVoList.size() == 2);
  }

  @Test
  @DisplayName("getUDFSByUsersTest")
  public void getUDFSByUsersTest() {
    Collection<String> users = new ArrayList<>();
    users.add("hadoop");
    List<UDFInfoVo> udfInfoVoList = udfDao.getUDFSByUsers(users);
    Assertions.assertTrue(udfInfoVoList.size() == 4);
  }

  @Test
  @DisplayName("getSharedUDFByUserTest")
  public void getSharedUDFByUserTest() {
    List<UDFInfoVo> udfInfoVos = udfDao.getSharedUDFByUser("hadoop");
    Assertions.assertTrue(udfInfoVos.size() == 0);
  }

  @Test
  @DisplayName("getUDFInfoByTreeIdTest")
  public void getUDFInfoByTreeIdTest() {
    Collection<Integer> categoryCodes = new ArrayList<>();
    categoryCodes.add(3);
    categoryCodes.add(4);
    Exception exception =
        Assertions.assertThrows(
            Exception.class, () -> udfDao.getUDFInfoByTreeId(13L, "hadoop", categoryCodes));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("getLoadedUDFsTest")
  public void getLoadedUDFsTest() {

    Exception exception =
        Assertions.assertThrows(Exception.class, () -> udfDao.getLoadedUDFs("hadoop"));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("getLoadedUDFIdsTest")
  public void getLoadedUDFIdsTest() {
    List<Long> loadedUDFIds = udfDao.getLoadedUDFIds("hadoop");
    Assertions.assertTrue(loadedUDFIds.size() == 1);
  }

  @Test
  @DisplayName("getSameLoadCountTest")
  public void getSameLoadCountTest() {

    long loadCount = udfDao.getSameLoadCount("hadoop", "test");
    Assertions.assertTrue(loadCount == 1L);
  }

  @Test
  @DisplayName("getSameJarUDFTest")
  public void getSameJarUDFTest() {
    Exception exception =
        Assertions.assertThrows(
            Exception.class,
            () ->
                udfDao.getSameJarUDF(
                    "hadoop", "file:///home/hadoop/logs/linkis/hadoop/baoyang/udf/udfPy.py"));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("getSameNameCountByUserTest")
  public void getSameNameCountByUserTest() {

    long counts = udfDao.getSameNameCountByUser("test", "hadoop");
    Assertions.assertTrue(counts == 1L);
  }

  @Test
  @DisplayName("selectSharedUDFInfosByTreeIdAndUserNameTest")
  public void selectSharedUDFInfosByTreeIdAndUserNameTest() {
    Collection<Integer> categoryCodes = new ArrayList<>();
    categoryCodes.add(3);
    categoryCodes.add(4);
    Exception exception =
        Assertions.assertThrows(
            Exception.class,
            () -> udfDao.selectSharedUDFInfosByTreeIdAndUserName(10L, "hadoop", null));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("selectUDFManagerTest")
  public void selectUDFManagerTest() {
    UDFManager udfManager = udfDao.selectUDFManager("hadoop");
    Assertions.assertNotNull(udfManager);
  }

  @Test
  @DisplayName("selectAllUserTest")
  public void selectAllUserTest() {
    List<String> allUser = udfDao.selectAllUser();
    Assertions.assertTrue(allUser.size() > 0);
  }

  @Test
  @DisplayName("getShareSameNameCountByUserTest")
  public void getShareSameNameCountByUserTest() {
    long count = udfDao.getShareSameNameCountByUser("test", "hadoop");
    Assertions.assertTrue(count == 0L);
  }

  @Test
  @DisplayName("getShareSameNameCountExcludeUserTest")
  public void getShareSameNameCountExcludeUserTest() {
    long count = udfDao.getShareSameNameCountExcludeUser("test", "hadoop", "baoyang");
    Assertions.assertTrue(count == 0L);
  }

  @Test
  @DisplayName("insertUDFSharedUserTest")
  public void insertUDFSharedUserTest() {
    Assertions.assertAll(() -> udfDao.insertUDFSharedUser(3L, "hadoop"));
  }

  @Test
  @DisplayName("updateUDFIsSharedTest")
  public void updateUDFIsSharedTest() {

    Assertions.assertAll(() -> udfDao.updateUDFIsShared(true, 3L));
  }

  @Test
  @DisplayName("selectAllShareUDFInfoIdByUDFIdTest")
  public void selectAllShareUDFInfoIdByUDFIdTest() {

    Assertions.assertAll(() -> udfDao.selectAllShareUDFInfoIdByUDFId("hadoop", "test"));
  }

  @Test
  @DisplayName("insertSharedUserTest")
  public void insertSharedUserTest() {
    Assertions.assertAll(() -> udfDao.insertSharedUser("hadoop", 3L));
  }

  @Test
  @DisplayName("deleteSharedUserTest")
  public void deleteSharedUserTest() {
    Assertions.assertAll(() -> udfDao.deleteSharedUser("hadoop", 3L));
  }

  @Test
  @DisplayName("deleteAllSharedUserTest")
  public void deleteAllSharedUserTest() {
    Assertions.assertAll(() -> udfDao.deleteAllSharedUser(3l));
  }

  @Test
  @DisplayName("getSharedUserCountByUdfIdTest")
  public void getSharedUserCountByUdfIdTest() {
    long counts = udfDao.getSharedUserCountByUdfId(3L);
    Assertions.assertTrue(counts == 0L);
  }

  @Test
  @DisplayName("getUserLoadCountByUdfIdTest")
  public void getUserLoadCountByUdfIdTest() {
    long count = udfDao.getUserLoadCountByUdfId(3L, "baoyang");
    Assertions.assertTrue(count == 1L);
  }

  @Test
  @DisplayName("updateLoadUserTest")
  public void updateLoadUserTest() {
    Assertions.assertAll(() -> udfDao.updateLoadUser(3L, "hadoop", "hadoop"));
  }

  @Test
  @DisplayName("getUdfInfoByPagesTest")
  public void getUdfInfoByPagesTest() {
    Collection<Integer> udfTypes = new ArrayList<>();
    udfTypes.add(3);
    udfTypes.add(4);
    List<UDFAddVo> udfAddVos = udfDao.getUdfInfoByPages("test", udfTypes, "hadoop");
    Assertions.assertTrue(udfAddVos.size() > 0);
  }

  @Test
  public void getLatesetPublishedUDF() {
    Collection<Integer> udfTypes = new ArrayList<>();
    udfTypes.add(3);
    udfTypes.add(4);
    Exception exception =
        Assertions.assertThrows(
            Exception.class, () -> udfDao.getLatesetPublishedUDF("hadoop", udfTypes));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }
}
