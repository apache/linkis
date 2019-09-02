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

import com.webank.wedatasphere.linkis.metadata.dao.ViewDao;
import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.service.ColumnService;
import com.webank.wedatasphere.linkis.metadata.service.DataSourceService;
import com.webank.wedatasphere.linkis.metadata.service.PermissionService;
import com.webank.wedatasphere.linkis.metadata.service.ViewService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * Created by shanhuang on 9/13/18.
 */
@Service
public class ViewServiceImpl implements ViewService {

    @Autowired(required = false)
    ViewDao viewDao;

    @Autowired
    ColumnService columnService;

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    PermissionService permissionService;

    @Override
    public List<View> find(String name, String owner) {
        return viewDao.find(name, owner);
    }

    @Override
    public View getById(Integer id){
        return viewDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void create(View view, String userName) throws Exception {
        List<View> existing = viewDao.find(view.getName(), userName);
        if(CollectionUtils.isNotEmpty(existing)){
            throw new Exception(view.generateDuplicationMessage("视图"));
        }
        view.setInfoOnCreate(userName);
        viewDao.insert(view);

        view.getDataSource().setViewId(view.getId());
        dataSourceService.create(view.getDataSource(), userName);

        view.getColumns().forEach(column -> column.setViewId(view.getId()));
        columnService.create(view.getColumns(), userName);

    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void update(View view, String userName) throws Exception {
        view.setInfoOnUpdate(userName);
        viewDao.update(view);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void delete(Integer id) throws Exception {
        viewDao.deleteById(id);
    }

}
