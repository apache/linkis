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

package org.apache.linkis.metadata.dao;

import org.apache.linkis.metadata.domain.mdq.po.MdqField;
import org.apache.linkis.metadata.domain.mdq.po.MdqImport;
import org.apache.linkis.metadata.domain.mdq.po.MdqLineage;
import org.apache.linkis.metadata.domain.mdq.po.MdqTable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MdqDaoTest extends BaseDaoTest {

  @Autowired private MdqDao mdqDao;

  private MdqTable createMdpTable() {
    MdqTable table = new MdqTable();
    table.setDatabase("hadoop_ind");
    table.setName("t_user");
    table.setAlias("t_user");
    table.setCreator("hadoop");
    table.setComment("test create");
    table.setCreateTime(new Date());
    table.setUsage("test");
    table.setLifecycle(0);
    table.setUseWay(0);
    table.setImport(false);
    table.setModelLevel(0);
    table.setPartitionTable(false);
    table.setModelLevel(0);
    table.setAvailable(true);
    return table;
  }

  private MdqField createMdpField() {
    MdqField mdqField = new MdqField();
    mdqField.setTableId(1L);
    mdqField.setName("name");
    mdqField.setType("string");
    mdqField.setComment("姓名字段");
    mdqField.setPartitionField(false);
    mdqField.setPrimary(false);
    mdqField.setLength(255);
    return mdqField;
  }

  @Test
  @DisplayName("activateTableTest")
  public void activateTableTest() {

    mdqDao.activateTable(1L);
    MdqTable mdqTable = mdqDao.selectTableForUpdate("ods_user_md_ind", "t_student_temp");
    Assertions.assertTrue(mdqTable.getAvailable().booleanValue());
  }

  @Test
  @DisplayName("selectTableByNameTest")
  public void selectTableByNameTest() {
    MdqTable mdqTable = mdqDao.selectTableByName("ods_user_md_ind", "t_student_temp", "hadoop");
    Assertions.assertNotNull(mdqTable);
  }

  @Test
  @DisplayName("listMdqFieldByTableIdTest")
  public void listMdqFieldByTableIdTest() {

    List<MdqField> mdqFields = mdqDao.listMdqFieldByTableId(1L);
    Assertions.assertTrue(mdqFields.size() > 0);
  }

  @Test
  @DisplayName("insertTableTest")
  public void insertTableTest() {
    MdqTable mdpTable = createMdpTable();
    mdqDao.insertTable(mdpTable);
    MdqTable mdqTableDao =
        mdqDao.selectTableByName(mdpTable.getDatabase(), mdpTable.getName(), mdpTable.getCreator());
    Assertions.assertNotNull(mdqTableDao);
  }

  @Test
  @DisplayName("insertImportTest")
  public void insertImportTest() {

    Assertions.assertAll(
        () -> {
          MdqImport mdqImport = new MdqImport();
          mdqImport.setTableId(1L);
          mdqImport.setArgs("name");
          mdqImport.setImportType(0);
          mdqDao.insertImport(mdqImport);
        });
  }

  @Test
  @DisplayName("insertLineageTest")
  public void insertLineageTest() {

    Assertions.assertAll(
        () -> {
          MdqLineage mdqLineage = new MdqLineage();
          mdqLineage.setTableId(1L);
          mdqLineage.setSourceTable("hadoop_ind");
          mdqLineage.setUpdateTime(new Date());
          mdqDao.insertLineage(mdqLineage);
        });
  }

  @Test
  @DisplayName("selectTableForUpdateTest")
  public void selectTableForUpdateTest() {
    MdqTable mdqTable = mdqDao.selectTableForUpdate("ods_user_md_ind", "t_student_temp");
    Assertions.assertNotNull(mdqTable);
  }

  @Test
  @DisplayName("deleteTableBaseInfoTest")
  public void deleteTableBaseInfoTest() {

    mdqDao.deleteTableBaseInfo(1L);
    MdqTable mdqTable = mdqDao.selectTableForUpdate("ods_user_md_ind", "t_student_temp");
    Assertions.assertNull(mdqTable);
  }

  @Test
  @DisplayName("insertFieldsTest")
  public void insertFieldsTest() {
    List<MdqField> list = new ArrayList<>(Arrays.asList(createMdpField()));
    mdqDao.insertFields(list);
    List<MdqField> mdqFields = mdqDao.listMdqFieldByTableId(1L);
    Assertions.assertTrue(mdqFields.size() > 0);
  }
}
