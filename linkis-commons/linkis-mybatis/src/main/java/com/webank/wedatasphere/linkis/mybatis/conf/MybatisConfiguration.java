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

package org.apache.linkis.mybatis.conf;

import org.apache.linkis.common.conf.CommonVars;

public class MybatisConfiguration {
    //Mybatis configuration
    public static final CommonVars<String> BDP_SERVER_MYBATIS_MAPPER_LOCATIONS = CommonVars.apply("wds.linkis.server.mybatis.mapperLocations", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_TYPEALIASESPACKAGE = CommonVars.apply("wds.linkis.server.mybatis.typeAliasesPackage", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_CONFIGLOCATION = CommonVars.apply("wds.linkis.server.mybatis.configLocation", "classpath:/mybatis-config.xml");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_BASEPACKAGE = CommonVars.apply("wds.linkis.server.mybatis.BasePackage", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_URL = CommonVars.apply("wds.linkis.server.mybatis.datasource.url", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_USERNAME =  CommonVars.apply("wds.linkis.server.mybatis.datasource.username", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_PASSWORD = CommonVars.apply("wds.linkis.server.mybatis.datasource.password", "");
    public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_DRIVER_CLASS_NAME = CommonVars.apply("wds.linkis.server.mybatis.datasource.driver-class-name", "com.mysql.jdbc.Driver");
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_INITIALSIZE = CommonVars.apply("wds.linkis.server.mybatis.datasource.initialSize", new Integer(1));
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MINIDLE = CommonVars.apply("wds.linkis.server.mybatis.datasource.minIdle", new Integer(1));
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MAXACTIVE = CommonVars.apply("wds.linkis.server.mybatis.datasource.maxActive", new Integer(20));
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MAXWAIT = CommonVars.apply("wds.linkis.server.mybatis.datasource.maxWait", new Integer(6000));
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_TBERM = CommonVars.apply("wds.linkis.server.mybatis.datasource.timeBetweenEvictionRunsMillis", new Integer(60000));
    public static final CommonVars<Integer> BDP_SERVER_MYBATIS_DATASOURCE_MEITM = CommonVars.apply("wds.linkis.server.mybatis.datasource.minEvictableIdleTimeMillis", new Integer(300000));
    public static final CommonVars<String> BDP_SERVER_MYBATIS_DATASOURCE_VALIDATIONQUERY = CommonVars.apply("wds.linkis.server.mybatis.datasource.validationQuery", "SELECT 1");

    public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTWHILEIDLE = CommonVars.apply("wds.linkis.server.mybatis.datasource.testWhileIdle", new Boolean(true));
    public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTONBORROW = CommonVars.apply("wds.linkis.server.mybatis.datasource.testOnBorrow", new Boolean(false));
    public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_TESTONRETURN = CommonVars.apply("wds.linkis.server.mybatis.datasource.testOnReturn", new Boolean(false));
    public static final CommonVars<Boolean> BDP_SERVER_MYBATIS_DATASOURCE_POOLPREPAREDSTATEMENTS = CommonVars.apply("wds.linkis.server.mybatis.datasource.poolPreparedStatements", new Boolean(true));
}
