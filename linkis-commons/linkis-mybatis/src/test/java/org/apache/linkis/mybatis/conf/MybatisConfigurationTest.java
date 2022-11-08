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

package org.apache.linkis.mybatis.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MybatisConfigurationTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String bdpServerMybatisMapperLocations =
        MybatisConfiguration.BDP_SERVER_MYBATIS_MAPPER_LOCATIONS.getValue();
    String bdpServerMybatisTypealiasespackage =
        MybatisConfiguration.BDP_SERVER_MYBATIS_TYPEALIASESPACKAGE.getValue();
    String bdpServerMybatisConfiglocation =
        MybatisConfiguration.BDP_SERVER_MYBATIS_CONFIGLOCATION.getValue();
    String bdpServerMybatisBasepackage =
        MybatisConfiguration.BDP_SERVER_MYBATIS_BASEPACKAGE.getValue();
    String bdpServerMybatisDatasourceUrl =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_URL.getValue();
    String bdpServerMybatisDatasourceUsername =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_USERNAME.getValue();
    String bdpServerMybatisDatasourceDriverClassName =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_DRIVER_CLASS_NAME.getValue();
    Integer bdpServerMybatisDatasourceInitialsize =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_INITIALSIZE.getValue();
    Integer bdpServerMybatisDatasourceMinidle =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MINIDLE.getValue();
    Integer bdpServerMybatisDatasourceMaxactive =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MAXACTIVE.getValue();
    Integer bdpServerMybatisDatasourceMaxwait =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MAXWAIT.getValue();
    Integer bdpServerMybatisDatasourceTberm =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_TBERM.getValue();
    Integer bdpServerMybatisDatasourceMeitm =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MEITM.getValue();
    String bdpServerMybatisDatasourceValidationquery =
        MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_VALIDATIONQUERY.getValue();

    Assertions.assertEquals("", bdpServerMybatisMapperLocations);
    Assertions.assertEquals("", bdpServerMybatisTypealiasespackage);
    Assertions.assertEquals("classpath:/mybatis-config.xml", bdpServerMybatisConfiglocation);
    Assertions.assertEquals("", bdpServerMybatisBasepackage);
    Assertions.assertEquals("", bdpServerMybatisDatasourceUrl);
    Assertions.assertEquals("", bdpServerMybatisDatasourceUsername);
    Assertions.assertEquals("com.mysql.jdbc.Driver", bdpServerMybatisDatasourceDriverClassName);
    Assertions.assertTrue(1 == bdpServerMybatisDatasourceInitialsize.intValue());
    Assertions.assertTrue(1 == bdpServerMybatisDatasourceMinidle.intValue());
    Assertions.assertTrue(20 == bdpServerMybatisDatasourceMaxactive.intValue());
    Assertions.assertTrue(6000 == bdpServerMybatisDatasourceMaxwait.intValue());
    Assertions.assertTrue(60000 == bdpServerMybatisDatasourceTberm.intValue());
    Assertions.assertTrue(300000 == bdpServerMybatisDatasourceMeitm.intValue());
    Assertions.assertEquals("SELECT 1", bdpServerMybatisDatasourceValidationquery);
  }
}
