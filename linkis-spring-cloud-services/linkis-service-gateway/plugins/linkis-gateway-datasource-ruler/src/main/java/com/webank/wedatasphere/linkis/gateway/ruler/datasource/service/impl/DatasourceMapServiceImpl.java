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
