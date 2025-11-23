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

package org.apache.linkis.metadata.hive.config;

import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.metadata.condition.DataSourceCondition;
import org.apache.linkis.metadata.util.DWSConfig;
import org.apache.linkis.metadata.util.HiveUtils;
import org.apache.linkis.mybatis.DataSourceUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableTransactionManagement(order = 2)
public class LinkisMybatisConfig {

  private static final Logger logger = LoggerFactory.getLogger(LinkisMybatisConfig.class);

  private DataSource hiveDataSource() {

    String url = DWSConfig.HIVE_META_URL.getValue();
    String username = DWSConfig.HIVE_META_USER.getValue();
    String password = DWSConfig.HIVE_META_PASSWORD.getValue();

    if (StringUtils.isBlank(url)
        || StringUtils.isBlank(username)
        || StringUtils.isBlank(password)) {
      org.apache.hadoop.conf.Configuration hiveConf =
          HiveUtils.getDefaultConf(HadoopConf.HADOOP_ROOT_USER().getValue());
      logger.info("from hive conf to pares meta store JDBC url");
      url = hiveConf.get("javax.jdo.option.ConnectionURL");
      username = hiveConf.get("javax.jdo.option.ConnectionUserName");
      password = hiveConf.get("javax.jdo.option.ConnectionPassword");
      if (DWSConfig.HIVE_PASS_ENCODE_ENABLED.getValue()) {
        logger.info("hive meta password is encode ");
        password = HiveUtils.decode(password);
      }
    }

    return DataSourceUtils.buildDataSource(url, username, password);
  }

  private DataSource mysqlDataSource() {
    return DataSourceUtils.buildDataSource(null, null, null);
  }

  @Bean(name = "dataSource")
  @Conditional(DataSourceCondition.class)
  public DynamicDataSource mutiDataSource() {
    DataSource hiveDataSource = hiveDataSource();
    DataSource mysqlDataSource = mysqlDataSource();
    DynamicDataSource dynamicDataSource = new DynamicDataSource();
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put(DSEnum.FIRST_DATA_SOURCE, hiveDataSource);
    hashMap.put(DSEnum.SECONDE_DATA_SOURCE, mysqlDataSource);
    dynamicDataSource.setTargetDataSources(hashMap);
    dynamicDataSource.setDefaultTargetDataSource(mysqlDataSource);
    return dynamicDataSource;
  }
}
