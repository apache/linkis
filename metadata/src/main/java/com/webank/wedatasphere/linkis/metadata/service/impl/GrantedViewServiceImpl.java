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

package com.webank.wedatasphere.linkis.metadata.service.impl;

import com.google.common.collect.Sets;
import com.webank.wedatasphere.linkis.metadata.domain.Column;
import com.webank.wedatasphere.linkis.metadata.domain.Filter;
import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.service.DataSourceService;
import com.webank.wedatasphere.linkis.metadata.service.GrantedViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
/**
 * Created by shanhuang on 9/13/18.
 */
public class GrantedViewServiceImpl implements GrantedViewService {

    @Autowired
    DataSourceService dataSourceService;


    @Transactional(rollbackFor=Exception.class)
    public void createGrantedView(View view, String userName) throws Exception{
        Map<String, Collection<Column>> userToColumnsMap = getUserToColumnsMap(view);
        Map<String, Collection<Filter>> userToRowsMap = getUserToRowsMap(view);
        Set<String> users = Sets.union(userToColumnsMap.keySet(), userToRowsMap.keySet());

        String dataSourceSql = dataSourceService.analyseDataSourceSql(view.getDataSource());

        for(String user : users){
            //TODO velocity

            //submit create view script
        }

    }



    private Map<String, Collection<Column>> getUserToColumnsMap(View view){

        return null;
    }

    private Map<String, Collection<Filter>> getUserToRowsMap(View view){
        return null;
    }
}
