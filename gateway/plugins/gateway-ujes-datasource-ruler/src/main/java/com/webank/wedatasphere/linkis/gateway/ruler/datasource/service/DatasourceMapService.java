package com.webank.wedatasphere.linkis.gateway.ruler.datasource.service;

/**
 * @author wang_zh
 * @date 2020/5/22
 */
public interface DatasourceMapService {

    String getInstanceByDatasource(String datasourceName);

    long countByInstance(String instance);

    String insertDatasourceMap(String datasourceName, String instance, String serviceId);

}
