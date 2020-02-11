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
package com.webank.wedatasphere.linkis.metadata.hive.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.webank.wedatasphere.linkis.metadata.util.DWSConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.sql.SQLException;

import java.util.*;


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
        logger.info("数据库连接地址信息=" + url);
        if(StringUtils.isBlank(url) || StringUtils.isBlank(username)  || StringUtils.isBlank(password)) {
           throw new  RuntimeException("The metadata service depends on hive metadata JDBC information. " +
                   "Please configure hive.meta related parameters(metadata服务依赖hive元数据JDBC的信息，请配置hive.meta相关参数).");
        }
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
        dynamicDataSource.setDefaultTargetDataSource(hiveDataSource);
        return dynamicDataSource;
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DynamicDataSource dataSource) throws Exception {
        String typeAliasesPackage = DWSConfig.BDP_SERVER_MYBATIS_TYPEALIASESPACKAGE.getValue();
        //Configure the mapper scan to find all mapper.xml mapping files(配置mapper的扫描，找到所有的mapper.xml映射文件)
        String mapperLocations = DWSConfig.BDP_SERVER_MYBATIS_MAPPER_LOCATIONS.getValue();
        //Load the global configuration file(加载全局的配置文件)
        String configLocation = DWSConfig.BDP_SERVER_MYBATIS_CONFIGLOCATION.getValue();
        try {
            SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(dataSource);

            logger.info("Mybatis typeAliasesPackage=" + typeAliasesPackage);
            logger.info("Mybatis mapperLocations=" + mapperLocations);
            logger.info("Mybatis configLocation=" + configLocation);
            // Read configuration(读取配置)
            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

            //Set the location of the mapper.xml file(设置mapper.xml文件所在位置)
            if(StringUtils.isNotBlank(mapperLocations)) {
                String[] mapperArray = mapperLocations.split(",");
                List<Resource> resources = new ArrayList<>();
                for(String mapperLocation : mapperArray){
                    CollectionUtils.addAll(resources,new PathMatchingResourcePatternResolver().getResources(mapperLocation));
                }
                sessionFactoryBean.setMapperLocations(resources.toArray(new Resource[0]));
            }
           /* Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocations);
            sessionFactoryBean.setMapperLocations(resources);*/
           // Add mybatis database id provider configuration to support hive postgresql metadata(添加MyBatis配置以支持Hive PG元数据库)
            sessionFactoryBean.setDatabaseIdProvider(getDatabaseIdProvider());
//            Set the location of the mybatis-config.xml configuration file(设置mybatis-config.xml配置文件位置)
            sessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));

            return sessionFactoryBean.getObject();
        } catch (IOException e) {
            logger.error("mybatis resolver mapper*xml is error",e);
            return null;
        } catch (Exception e) {
            logger.error("mybatis sqlSessionFactoryBean create error",e);
            return null;
        }
    }

    private DatabaseIdProvider getDatabaseIdProvider() {
        VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties databaseIdProperties = new Properties();
        databaseIdProperties.put("MySQL", "mysql");
        databaseIdProperties.put("PostgreSQL", "postgresql");
        databaseIdProvider.setProperties(databaseIdProperties);
        return databaseIdProvider;
    }

    @Primary
    public PlatformTransactionManager annotationDrivenTransactionManager(@Qualifier("dataSource") DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactory(sqlSessionFactory);
        //Each table corresponds to the XXMapper.java interface type Java file
        //每张表对应的XXMapper.java interface类型的Java文件
        mapperScannerConfigurer.setBasePackage(DWSConfig.BDP_SERVER_MYBATIS_BASEPACKAGE.getValue());
        return mapperScannerConfigurer;
    }
}