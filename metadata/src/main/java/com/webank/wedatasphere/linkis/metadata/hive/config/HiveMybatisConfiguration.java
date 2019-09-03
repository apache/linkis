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
import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.metadata.hive.dao.HiveMetaDao;
import com.webank.wedatasphere.linkis.mybatis.conf.MybatisConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Named;
import java.io.IOException;
/**
 * Created by shanhuang on 9/13/18.
 */
@Configuration
@ConfigurationProperties
@AutoConfigureAfter(HiveDataSourceConfig.class)
@EnableTransactionManagement
public class HiveMybatisConfiguration extends JavaLog {

    @Autowired
    @Qualifier("hiveDataSource")
    private DruidDataSource dataSource;

    @Bean(name = "hiveSqlSessionFactory")
    public SqlSessionFactoryBean hiveSqlSessionFactory() {
        String typeAliasesPackage = "com.webank.wedatasphere.linkis.metadata.hive.dao";
        String mapperLocations = "classpath:com/webank/wedatasphere/linkis/metadata/hive/dao/impl/*.xml";
        String configLocation = MybatisConfiguration.BDP_SERVER_MYBATIS_CONFIGLOCATION.getValue();
        try {
            SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(dataSource);

            info("Mybatis typeAliasesPackage=" + typeAliasesPackage);
            info("Mybatis mapperLocations=" + mapperLocations);
            info("Mybatis configLocation=" + configLocation);

            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocations);
            sessionFactoryBean.setMapperLocations(resources);
            sessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));

            return sessionFactoryBean;
        } catch (IOException e) {
            error("mybatis resolver mapper*xml is error",e);
            return null;
        } catch (Exception e) {
            error("mybatis sqlSessionFactoryBean create error",e);
            return null;
        }
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MapperFactoryBean<HiveMetaDao> dbMapper(@Named("hiveSqlSessionFactory") final SqlSessionFactoryBean sqlSessionFactoryBean) throws Exception {
        MapperFactoryBean<HiveMetaDao> factoryBean = new MapperFactoryBean<>(HiveMetaDao.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactoryBean.getObject());
        return factoryBean;
    }


}
