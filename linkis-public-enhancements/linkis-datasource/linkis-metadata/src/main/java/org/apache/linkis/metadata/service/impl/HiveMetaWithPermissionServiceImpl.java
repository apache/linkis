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
package org.apache.linkis.metadata.service.impl;

import org.apache.linkis.metadata.hive.dao.HiveMetaDao;

import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.util.DWSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HiveMetaWithPermissionServiceImpl  implements HiveMetaWithPermissionService {

    @Autowired
    private HiveMetaDao hiveMetaDao;

    @Override
    public List<String> getDbsOptionalUserName(String userName) {
        Boolean flag=DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
        if(flag) {
            return hiveMetaDao.getDbsByUser(userName);
        }else {
            return hiveMetaDao.getAllDbs();
        }
    }

    @Override
    public List<Map<String, Object>> getTablesByDbNameAndOptionalUserName(Map<String, String> map){
        Boolean flag= DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
        if(flag){
            return hiveMetaDao.getTablesByDbNameAndUser(map);
        }else{
            return hiveMetaDao.getTablesByDbName(map);
        }
    }
}
