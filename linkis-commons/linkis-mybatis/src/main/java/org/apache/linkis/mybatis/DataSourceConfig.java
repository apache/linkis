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
 
package org.apache.linkis.mybatis;

import org.apache.linkis.common.utils.JavaLog;
import org.apache.linkis.mybatis.conf.MybatisConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
@ConfigurationProperties
public class DataSourceConfig extends JavaLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean(name="dataSource", destroyMethod = "close")
    @ConditionalOnMissingBean
    public DataSource dataSource(){
        String dbUrl = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_URL.getValue();
        String username = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_USERNAME.getValue();
        String password = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_PASSWORD.getValue();
        String driverClassName = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_DRIVER_CLASS_NAME.getValue();
        int initialSize = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_INITIALSIZE.getValue();
        int minIdle = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MINIDLE.getValue();
        int maxActive = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MAXACTIVE.getValue();
        int maxWait = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MAXWAIT.getValue();
        int timeBetweenEvictionRunsMillis = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_TBERM.getValue();
        int minEvictableIdleTimeMillis = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_MEITM.getValue();
        String validationQuery = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_VALIDATIONQUERY.getValue();
        boolean testWhileIdle = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_TESTWHILEIDLE.getValue();
        boolean testOnBorrow = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_TESTONBORROW.getValue();
        boolean testOnReturn = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_TESTONRETURN.getValue();
        boolean poolPreparedStatements = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_POOLPREPAREDSTATEMENTS.getValue();
        boolean removeAbandoned = MybatisConfiguration.MYBATIS_DATASOURCE_REMOVE_ABANDONED_ENABLED.getValue();
        int removeAbandonedTimeout = MybatisConfiguration.MYBATIS_DATASOURCE_REMOVE_ABANDONED_TIMEOUT.getValue();
        BasicDataSource datasource = new BasicDataSource();
        info("Database connection address information(数据库连接地址信息)=" + dbUrl);
        datasource.setUrl(dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setRemoveAbandoned(removeAbandoned);
        datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        return datasource;
    }

}
