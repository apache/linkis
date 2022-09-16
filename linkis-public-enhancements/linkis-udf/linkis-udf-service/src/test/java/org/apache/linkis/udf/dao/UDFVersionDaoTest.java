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

import org.apache.linkis.udf.entity.UDFVersion;
import org.apache.linkis.udf.vo.UDFVersionVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDFVersionDaoTest extends BaseDaoTest {

  private static final Logger LOG = LoggerFactory.getLogger(UDFVersionDaoTest.class);

  @Autowired private UDFVersionDao udfVersionDao;

  @Test
  @DisplayName("addUdfVersionTest")
  public void addUdfVersionTest() {
    UDFVersion udfVersion = new UDFVersion();
    udfVersion.setId(99L);
    udfVersion.setUdfId(1L);
    udfVersion.setPath("file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/udfPy.py");
    udfVersion.setBmlResourceId("fe124e5e-4fdd-4509-aa93-10c3748ba34a");
    udfVersion.setBmlResourceVersion("v000006");
    udfVersion.setPublished(true);
    udfVersion.setRegisterFormat("udf.register(\"pyUdfTest\",test)");
    udfVersion.setUseFormat("int pyUdfTest(api)");
    udfVersion.setDescription("test it");
    udfVersion.setCreateTime(new Date());
    udfVersion.setMd5("0774ebbaef1efae6e7554ad569235d2f");
    udfVersionDao.addUdfVersion(udfVersion);

    UDFVersion udfIdAndVersion = udfVersionDao.selectByUdfIdAndVersion(1L, "v000006");
    Assertions.assertNotNull(udfIdAndVersion);
  }

  @Test
  @DisplayName("selectLatestByUdfIdTest")
  public void selectLatestByUdfIdTest() {
    UDFVersion udfVersion = udfVersionDao.selectLatestByUdfId(1L);
    Assertions.assertNotNull(udfVersion);
  }

  @Test
  @DisplayName("selectByUdfIdAndVersionTest")
  public void selectByUdfIdAndVersionTest() {
    UDFVersion udfVersion = udfVersionDao.selectByUdfIdAndVersion(1L, "v000001");
    Assertions.assertNotNull(udfVersion);
  }

  @Test
  @DisplayName("updatePublishStatusTest")
  public void updatePublishStatusTest() {
    udfVersionDao.updatePublishStatus(3L, "v000001", false);
    List<UDFVersionVo> versionVos = udfVersionDao.getAllVersionByUdfId(2L);
    Assertions.assertTrue(versionVos.size() == 1);
    Assertions.assertFalse(versionVos.get(0).getPublished());
  }

  @Test
  @DisplayName("getAllVersionsTest")
  public void getAllVersionsTest() {
    List<UDFVersion> allVersions = udfVersionDao.getAllVersions(1L);
    Assertions.assertTrue(allVersions.size() > 0);
  }

  @Test
  @DisplayName("deleteVersionByUdfIdTest")
  public void deleteVersionByUdfIdTest() {
    udfVersionDao.deleteVersionByUdfId(4L);
    List<UDFVersion> allVersions = udfVersionDao.getAllVersions(4L);
    Assertions.assertTrue(allVersions.size() == 0);
  }

  @Test
  @DisplayName("getSameJarCountTest")
  public void getSameJarCountTest() {
    Exception exception =
        Assertions.assertThrows(
            Exception.class, () -> udfVersionDao.getSameJarCount("hadoop", "activation.jar"));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("getOtherSameJarCountTest")
  public void getOtherSameJarCountTest() {

    Exception exception =
        Assertions.assertThrows(
            Exception.class,
            () -> udfVersionDao.getOtherSameJarCount("hadoop", "activation.jar", 2L));
    LOG.info("assertThrows pass, the error message: {} ", exception.getMessage());
  }

  @Test
  @DisplayName("updateResourceIdByUdfIdTest")
  public void updateResourceIdByUdfIdTest() {

    udfVersionDao.updateResourceIdByUdfId(
        2L, "0de8c361-22ce-4402-bf6f-xxxxxxxxx", "hadoop", "hadoop");
    List<UDFVersionVo> versionVos = udfVersionDao.getAllVersionByUdfId(2L);
    Assertions.assertTrue(versionVos.size() == 1);

    Assertions.assertEquals(
        "0de8c361-22ce-4402-bf6f-xxxxxxxxx", versionVos.get(0).getBmlResourceId());
  }

  @Test
  @DisplayName("updateResourceIdByUdfIdTest")
  public void updateUDFVersionTest() {

    UDFVersion udfVersion = new UDFVersion();
    udfVersion.setId(3L);
    udfVersion.setPath("file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/activation.jar");
    udfVersion.setRegisterFormat("0");
    udfVersion.setUseFormat("string jarUdf(name)");
    udfVersion.setDescription("updateTests");
    udfVersionDao.updateUDFVersion(udfVersion);

    List<UDFVersionVo> versionVos = udfVersionDao.getAllVersionByUdfId(2L);
    Assertions.assertTrue(versionVos.size() == 1);
    Assertions.assertEquals("updateTests", versionVos.get(0).getDescription());
  }
}
