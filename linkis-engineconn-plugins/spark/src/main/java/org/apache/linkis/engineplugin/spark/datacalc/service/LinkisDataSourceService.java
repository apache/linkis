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

package org.apache.linkis.engineplugin.spark.datacalc.service;

import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient;
import org.apache.linkis.datasource.client.request.GetInfoPublishedByDataSourceNameAction;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.engineplugin.spark.datacalc.exception.DataSourceNotConfigException;
import org.apache.linkis.engineplugin.spark.datacalc.model.DataCalcDataSource;
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary;
import org.apache.linkis.storage.utils.StorageUtils;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisDataSourceService {

  private static final Logger logger = LoggerFactory.getLogger(LinkisDataSourceService.class);

  public static DataCalcDataSource getDatasource(String datasourceName) {
    LinkisDataSourceRemoteClient dataSourceClient = new LinkisDataSourceRemoteClient();
    GetInfoPublishedByDataSourceNameAction action =
        GetInfoPublishedByDataSourceNameAction.builder()
            .setDataSourceName(datasourceName)
            .setUser(StorageUtils.getJvmUser())
            .build(); // ignore parameter 'system'
    DataSource datasource =
        dataSourceClient.getInfoPublishedByDataSourceName(action).getDataSource();
    if (datasource == null) {
      int code = SparkErrorCodeSummary.DATA_CALC_DATASOURCE_NOT_CONFIG.getErrorCode();
      String errDesc = SparkErrorCodeSummary.DATA_CALC_DATASOURCE_NOT_CONFIG.getErrorDesc();
      String msg = MessageFormat.format(errDesc, datasourceName);
      logger.error(msg);
      throw new DataSourceNotConfigException(code, msg);
    }
    LinkisDataSourceContext dataSourceContext = new LinkisDataSourceContext(datasource);
    return dataSourceContext.getDataCalcDataSource();
  }
}
