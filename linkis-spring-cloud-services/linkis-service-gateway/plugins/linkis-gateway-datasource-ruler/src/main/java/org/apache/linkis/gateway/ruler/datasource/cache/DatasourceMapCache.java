/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.gateway.ruler.datasource.cache;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.gateway.ruler.datasource.dao.DatasourceMapMapper;
import org.apache.linkis.gateway.ruler.datasource.entity.DatasourceMap;
import org.apache.linkis.rpc.interceptor.ServiceInstanceUtils;
import org.apache.linkis.rpc.sender.eureka.EurekaClientRefreshUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component
public class DatasourceMapCache {

    private Logger logger = LoggerFactory.getLogger(DatasourceMapCache.class);

    private Map<String, DatasourceMap> DATASOURCE_MAP_CACHE = new ConcurrentHashMap<>();

    public void cache(DatasourceMap datasourceMap) {
        DATASOURCE_MAP_CACHE.put(datasourceMap.getDatasourceName(), datasourceMap);
    }

    public DatasourceMap get(String datasourceName) {
        return DATASOURCE_MAP_CACHE.get(datasourceName);
    }

    @Autowired
    private DatasourceMapMapper datasourceMapMapper;

    private final static long CLEAN_PERIOD = 3 * 60 * 1000;

    @PostConstruct
    public void init() {
        datasourceMapMapper.createTableIfNotExists();

        // init load all datasourceMap to cache
        List<DatasourceMap> datasourceMapList = datasourceMapMapper.listAll();
        if (datasourceMapList != null && !datasourceMapList.isEmpty()) {
            datasourceMapList.forEach(item -> cache(item));
        }

        // add a scheduler task to clean up datasourceMap
        Utils.defaultScheduler().scheduleWithFixedDelay(new CleanRunnable(), CLEAN_PERIOD, CLEAN_PERIOD, TimeUnit.MILLISECONDS);
    }

    class CleanRunnable implements Runnable {

        @Override
        public void run() {
            Set<DatasourceMap> datasourceMaps = new HashSet<>(DATASOURCE_MAP_CACHE.values());
            try {
                EurekaClientRefreshUtils.apply().refreshEurekaClient();
            } catch (Throwable t) {
                logger.warn("DatasourceMapCache clean runner refresh eureka client error, {}", t);
            }

            Set<String> badInstances = new HashSet<>();
            datasourceMaps.forEach(datasourceMap -> {
                String instance = datasourceMap.getInstance();
                if (badInstances.contains(instance)) return;
                ServiceInstance[] serviceInstances = ServiceInstanceUtils.getRPCServerLoader()
                        .getServiceInstances(datasourceMap.getServiceId());
                if (serviceInstances == null || serviceInstances.length < 1 || !Arrays.asList(serviceInstances).contains(instance)) {
                    badInstances.add(datasourceMap.getInstance());
                }
            });

            if (!badInstances.isEmpty())
                datasourceMapMapper.cleanBadInstances(badInstances);
        }
    }

}
