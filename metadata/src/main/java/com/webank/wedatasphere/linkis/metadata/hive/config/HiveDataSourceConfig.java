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
import com.alibaba.druid.util.StringUtils;
import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.metadata.util.DWSConfig;
import com.webank.wedatasphere.linkis.metadata.util.HiveUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by shanhuang on 9/13/18.
 */
@Configuration
@ConfigurationProperties
public class HiveDataSourceConfig extends JavaLog {

    @Bean(name="hiveDataSource",destroyMethod = "close")
//    @Primary
    public DruidDataSource dataSource(){
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
        String url = DWSConfig.HIVE_META_URL.getValue();
        String username = DWSConfig.HIVE_META_USER.getValue();
        String password = DWSConfig.HIVE_META_PASSWORD.getValue();
        if(StringUtils.isEmpty(url) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            HiveConf hiveConf = HiveUtils.getDefaultConf(com.webank.wedatasphere.linkis.common.conf.Configuration.HADOOP_ROOT_USER().getValue());
            url = hiveConf.get("javax.jdo.option.ConnectionURL");
            username = hiveConf.get("javax.jdo.option.ConnectionUserName");
            password = hiveConf.get("javax.jdo.option.ConnectionPassword");
            info("Database connection address information from hiveConf");
        }
        DruidDataSource dataSource = new DruidDataSource();
        info("Database connection address information =(数据库连接地址信息=)" + url);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
//        info("Database connection address information =(数据库连接地址信息=)" + url);
//        dataSource.setUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
//        dataSource.setRemoveAbandoned(removeAbandoned);
//        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);

        return dataSource;
    }

}
