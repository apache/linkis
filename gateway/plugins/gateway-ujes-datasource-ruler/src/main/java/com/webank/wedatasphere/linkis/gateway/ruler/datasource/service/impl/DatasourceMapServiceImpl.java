package com.webank.wedatasphere.linkis.gateway.ruler.datasource.service.impl;

import com.webank.wedatasphere.linkis.gateway.ruler.datasource.cache.DatasourceMapCache;
import com.webank.wedatasphere.linkis.gateway.ruler.datasource.dao.DatasourceMapMapper;
import com.webank.wedatasphere.linkis.gateway.ruler.datasource.entity.DatasourceMap;
import com.webank.wedatasphere.linkis.gateway.ruler.datasource.service.DatasourceMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wang_zh
 * @date 2020/5/22
 */
@Service
public class DatasourceMapServiceImpl implements DatasourceMapService {

    @Autowired
    private DatasourceMapMapper datasourceMapMapper;

    @Autowired
    private DatasourceMapCache datasourceMapCache;

    private Map<String, DatasourceMap> DATASOURCE_MAP_CACHE = new ConcurrentHashMap<>();

    public String getInstanceByDatasource(String datasourceName) {
        DatasourceMap datasourceMap = datasourceMapCache.get(datasourceName);
        if (datasourceMap != null) return datasourceMap.getInstance();
        datasourceMap = datasourceMapMapper.getByDatasource(datasourceName);
        if (datasourceMap == null) return null;
        datasourceMapCache.cache(datasourceMap);
        return datasourceMap.getInstance();
    }

    @Override
    public long countByInstance(String instance) {
        return datasourceMapMapper.countByInstance(instance);
    }

    @Override
    public String insertDatasourceMap(String datasourceName, String instance, String serviceId) {
        try {
            DatasourceMap datasourceMap = new DatasourceMap(datasourceName, instance, serviceId);
            datasourceMapMapper.insert(datasourceMap);
            datasourceMapCache.cache(datasourceMap);
            return instance;
        } catch (DuplicateKeyException e) {
            return getInstanceByDatasource(datasourceName);
        }
    }

    @PostConstruct
    public void init() {
        // create linkis_datasource_map table if not exists
        datasourceMapMapper.createTableIfNotExists();
    }

}
