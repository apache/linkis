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

package org.apache.linkis.datasourcemanager.common;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;

import java.util.HashMap;
import java.util.Map;

public class DataSources {

  /** Default HDFS name */
  private static final CommonVars<String> DEFAULT_HDFS_NAME =
      CommonVars.apply("wds.linkis.server.dsm.default.hdfs.name", ".LOCAL_HDFS");

  private static final Map<String, DataSource> DEFAULT_DATASOURCES = new HashMap<>();

  static {
    DataSourceType hdfsType = new DataSourceType();
    hdfsType.setName("hdfs");
    DataSource hdfs = new DataSource();
    hdfs.setDataSourceType(hdfsType);
    hdfs.setDataSourceName(DEFAULT_HDFS_NAME.getValue());
    DEFAULT_DATASOURCES.put(hdfs.getDataSourceName(), hdfs);
    DEFAULT_DATASOURCES
        .values()
        .forEach(dataSource -> dataSource.setCreateUser(System.getProperty("user.name")));
  }

  /**
   * Find the default data source by name
   *
   * @param dataSourceName data source name
   * @return data source
   */
  public static DataSource getDefault(String dataSourceName) {
    return DEFAULT_DATASOURCES.get(dataSourceName);
  }
}
