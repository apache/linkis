package org.apache.linkis.engineplugin.spark.datacalc.service;

import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient;
import org.apache.linkis.datasource.client.request.GetInfoPublishedByDataSourceNameAction;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.engineplugin.spark.datacalc.model.DataCalcDataSource;
import org.apache.linkis.storage.utils.StorageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisDataSourceService {

  private static final Logger logger = LoggerFactory.getLogger(LinkisDataSourceService.class);

  private static final LinkisDataSourceRemoteClient dataSourceClient =
      new LinkisDataSourceRemoteClient();

  public static DataCalcDataSource getDatabase(String datasourceName) {
    GetInfoPublishedByDataSourceNameAction action =
        GetInfoPublishedByDataSourceNameAction.builder()
            .setDataSourceName(datasourceName)
            .setUser(StorageUtils.getJvmUser())
            .build(); // ignore parameter 'system'
    DataSource datasource =
        dataSourceClient.getInfoPublishedByDataSourceName(action).getDataSource();
    datasource.getConnectParams();
    return transform(datasource);
  }

  private static DataCalcDataSource transform(DataSource datasource) {
    DataCalcDataSource ds = new DataCalcDataSource();
    return ds;
  }
}
