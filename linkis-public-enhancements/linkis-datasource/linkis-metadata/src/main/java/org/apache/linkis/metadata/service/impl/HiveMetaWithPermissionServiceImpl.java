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
