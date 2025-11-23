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

package org.apache.linkis.entrance.interceptor.impl;

import org.apache.linkis.governance.common.entity.job.JobRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateConfUtilsTest {

  @Test
  void getCustomTemplateConfName() {
    JobRequest js = new JobRequest();
    StringBuilder logBuilder = new StringBuilder();
    String sqlCode =
        ""
            + "--注解\n"
            + "select * from table;\n"
            + "   --注解 \n"
            + "--注解\n"
            + "   select \"--注解\" as test\n"
            + " --@set yy=123\n"
            + "  --注解";

    js.setExecutionCode(sqlCode);
    String res = TemplateConfUtils.getCustomTemplateConfName(js, "sql", logBuilder);
    assertEquals(res, "");

    String sqlCode2 =
        ""
            + "---@set 123=注解\n"
            + "select * from table;\n"
            + "   --注解 \n"
            + "--注解\n"
            + "   select \"--注解\" as test\n"
            + " --@set yy=123\n"
            + "  --注解";
    js.setExecutionCode(sqlCode2);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "sql", logBuilder);
    assertEquals(res, "");

    String sqlCode3 =
        ""
            + "---@set ec.resource.name=345\n"
            + "select * from table;\n"
            + "   --注解 \n"
            + "--注解\n"
            + "---@set ec.resource.name=456\n"
            + "   select \"--注解\" as test\n"
            + " --@set yy=123\n"
            + "  --注解";
    js.setExecutionCode(sqlCode3);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "sql", logBuilder);
    assertEquals(res, "345");

    String sqlCode4 =
        ""
            + "---@set ec.resource.name= name1 \n"
            + "   select \"--注解\" as test\n"
            + " --@set yy=123\n"
            + "  --注解";
    js.setExecutionCode(sqlCode4);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "sql", logBuilder);
    assertEquals(res, "name1");

    String sqlCode5 =
        ""
            + "##@set ec.resource.name=pyname1\n"
            + "select * from table;\n"
            + "   --注解 \n"
            + "#注解\n"
            + "##@set ec.resource.name= 123 \n"
            + "   select \"--注解\" as test\n"
            + "#@set yy=123\n"
            + "  #注解";
    js.setExecutionCode(sqlCode5);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "python", logBuilder);
    assertEquals(res, "pyname1");

    String sqlCode6 =
        ""
            + "///@set ec.resource.name= scalaname1 \n"
            + "   select \"//注解\" as test\n"
            + "//@set yy=123\n"
            + "  #注解";
    js.setExecutionCode(sqlCode6);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "scala", logBuilder);
    assertEquals(res, "scalaname1");

    String sqlCode7 =
        ""
            + "---@set ec.resource.name= hqlname1 \n"
            + "   select \"--注解\" as test\n"
            + " --@set yy=123\n"
            + "  --注解";
    js.setExecutionCode(sqlCode7);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "hql", logBuilder);
    assertEquals(res, "hqlname1");

    String sqlCode8 =
        "---@set ec.resource.name=linkis_test2;\n"
            + "        ---@set ec.resource.name=scriptis_test hive;\n"
            + "        select * from dss autotest.demo data limit 100;";
    js.setExecutionCode(sqlCode8);
    res = TemplateConfUtils.getCustomTemplateConfName(js, "hql", logBuilder);
    assertEquals(res, "linkis_test2");
  }

  @Test
  void getCustomTemplateConfName2() {
    JobRequest js = new JobRequest();
    StringBuilder logBuilder = new StringBuilder();
    String sqlCode9 =
        "---@set ec.resource.name=linkis_test2;\r\n---@set ec.resource.name=scriptis_test_hive;\r\n--@set limitn=100\r\nselect * from dss_autotest.demo_data  limit ${limitn};\r\n";
    js.setExecutionCode(sqlCode9);
    String res = TemplateConfUtils.getCustomTemplateConfName(js, "hql", logBuilder);
    assertEquals(res, "linkis_test2");
  }
}
