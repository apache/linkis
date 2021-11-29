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
 
package org.apache.linkis.metadata.hive.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.metadata.util.DWSConfig;
import org.apache.linkis.metadata.util.HiveUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.sql.SQLException;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement(order = 2)
public class LinkisMybatisConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DruidDataSource hiveDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        int initialSize = 1;
        int minIdle = 1;
        int maxActive = 20;
        int maxWait = 6000;
        int timeBetweenEvictionRunsMillis = 60000;
        int minEvictableIdleTimeMillis = 300000;
        String validationQuery = "SELECT 1";
        boolean testWhileIdle = true;
        boolean testOnBorrow = true;
        boolean testOnReturn = true;

        String url =  DWSConfig.HIVE_META_URL.getValue();
        String username =  DWSConfig.HIVE_META_USER.getValue();
        String password = DWSConfig.HIVE_META_PASSWORD.getValue();

        org.apache.hadoop.conf.Configuration hiveConf = null;
        if(StringUtils.isBlank(url) || StringUtils.isBlank(username)  || StringUtils.isBlank(password)) {
             hiveConf = HiveUtils.getDefaultConf(HadoopConf.HADOOP_ROOT_USER().getValue());
            logger.info("从配置文件中读取hive数据库连接地址");
            url = hiveConf.get("javax.jdo.option.ConnectionURL");
            username = hiveConf.get("javax.jdo.option.ConnectionUserName");
            password = hiveConf.get("javax.jdo.option.ConnectionPassword");
            if (DWSConfig.HIVE_PASS_ENCODE_ENABLED.getValue()) {
                logger.info("hive meta password is encode ");
                password = HiveUtils.decode(password);
            }

        }

        logger.info("数据库连接地址信息=" + url);
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);
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
        return datasource;
    }

    private DruidDataSource mysqlDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        String dbUrl = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_URL.getValue();
        String username = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_USERNAME.getValue();
        String password = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_PASSWORD.getValue();
        String driverClassName = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_DRIVER_CLASS_NAME.getValue();
        int initialSize = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_INITIALSIZE.getValue();
        int minIdle = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_MINIDLE.getValue();
        int maxActive = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_MAXACTIVE.getValue();
        int maxWait = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_MAXWAIT.getValue();
        int timeBetweenEvictionRunsMillis = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_TBERM.getValue();
        int minEvictableIdleTimeMillis = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_MEITM.getValue();
        String validationQuery = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_VALIDATIONQUERY.getValue();
        boolean testWhileIdle = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_TESTWHILEIDLE.getValue();
        boolean testOnBorrow = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_TESTONBORROW.getValue();
        boolean testOnReturn = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_TESTONRETURN.getValue();
        boolean poolPreparedStatements = DWSConfig.BDP_SERVER_MYBATIS_DATASOURCE_POOLPREPAREDSTATEMENTS.getValue();
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
        logger.info("Database connection address information(数据库连接地址信息)=" + dbUrl);
        return datasource;
    }

    @Bean(name = "dataSource")
    public DynamicDataSource mutiDataSource() {

        DruidDataSource hiveDataSource = hiveDataSource();
        DruidDataSource mysqlDataSource = mysqlDataSource();

        try {
            hiveDataSource.init();
            mysqlDataSource.init();
        } catch (SQLException sql) {
            logger.error("连接数据库失败：",sql);
        }

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put(DSEnum.FIRST_DATA_SOURCE, hiveDataSource);
        hashMap.put(DSEnum.SECONDE_DATA_SOURCE, mysqlDataSource);
        dynamicDataSource.setTargetDataSources(hashMap);
        dynamicDataSource.setDefaultTargetDataSource(mysqlDataSource);
        return dynamicDataSource;
    }

    @Primary
    public PlatformTransactionManager annotationDrivenTransactionManager(@Qualifier("dataSource") DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}