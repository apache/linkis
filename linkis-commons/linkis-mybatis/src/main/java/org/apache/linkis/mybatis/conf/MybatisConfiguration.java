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

import org.apache.linkis.common.conf.CommonVars;

public class MybatisConfiguration {
  // Mybatis configuration
  public static final CommonVars<String> BDP_SERVER_MYBATIS_MAPPER_LOCATIONS =
      CommonVars.apply("wds.linkis.server.mybatis.mapperLocations", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_TYPEALIASESPACKAGE =
      CommonVars.apply("wds.linkis.server.mybatis.typeAliasesPackage", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_CONFIGLOCATION =
      CommonVars.apply("wds.linkis.server.mybatis.configLocation", "classpath:/mybatis-config.xml");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_BASEPACKAGE =
      CommonVars.apply("wds.linkis.server.mybatis.BasePackage", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_URL =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.url", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_USERNAME =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.username", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_PASSWORD =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.password", "");
  public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_DRIVER_CLASS_NAME =
      CommonVars.apply(
          "wds.linkis.server.mybatis.datasource.driver-class-name", "com.mysql.jdbc.Driver");
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_INITIALSIZE =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.initialSize", 1);
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MINIDLE =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.minIdle", 1);
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MAXACTIVE =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.maxActive", 20);
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MAXWAIT =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.maxWait", 60000);
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_TBERM =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.timeBetweenEvictionRunsMillis", 60000);
  public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MEITM =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.minEvictableIdleTimeMillis", 300000);
  public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_VALIDATIONQUERY =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.validationQuery", "SELECT 1");

  public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTWHILEIDLE =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.testWhileIdle", Boolean.TRUE);
  public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTONBORROW =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.testOnBorrow", Boolean.FALSE);
  public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTONRETURN =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.testOnReturn", Boolean.FALSE);
  public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_POOLPREPAREDSTATEMENTS =
      CommonVars.apply("wds.linkis.server.mybatis.datasource.poolPreparedStatements", Boolean.TRUE);
  public static final CommonVars<Boolean> MYBATIS_DATASOURCE_REMOVE_ABANDONED_ENABLED =
      CommonVars.apply("wds.linkis.server.mybatis.remove.abandoned.enabled", Boolean.TRUE);
  public static final CommonVars<Boolean> MYBATIS_DATASOURCE_KEEPALIVE_ENABLED =
      CommonVars.apply("linkis.server.mybatis.keepalive.enabled", Boolean.TRUE);
  public static final CommonVars<Boolean> MYBATIS_DATASOURCE_USE_PING_ENABLED =
      CommonVars.apply("linkis.server.mybatis.use.ping.enabled", Boolean.TRUE);

  public static final CommonVars<Integer> MYBATIS_DATASOURCE_REMOVE_ABANDONED_TIMEOUT =
      CommonVars.apply("wds.linkis.server.mybatis.remove.abandoned.timeout", 300);
  public static final CommonVars<String> BDP_SERVER_MYBATIS_PAGEHELPER_DIALECT =
      CommonVars.apply("linkis.server.mybatis.pagehelper.dialect", "mysql");
}
