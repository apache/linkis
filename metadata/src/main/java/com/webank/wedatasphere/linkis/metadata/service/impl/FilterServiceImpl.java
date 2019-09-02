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

import com.webank.wedatasphere.linkis.metadata.dao.FilterDao;
import com.webank.wedatasphere.linkis.metadata.domain.Filter;
import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * Created by shanhuang on 9/13/18.
 */
@Service
public class FilterServiceImpl implements FilterService {

    @Autowired(required = false)
    FilterDao filterDao;

    @Override
    public void create(Filter filter, View view, String userName) throws Exception {
        filter.setInfoOnCreate(userName);
        filterDao.insert(filter);

        Filter left = filter;
        Filter right = left.getRight();
        while(right != null){
            right.setPermissionId(left.getPermissionId());
            right.setInfoOnCreate(userName);
            filterDao.insert(right);
            left.setRightId(right.getId());
            filterDao.update(left);
            left = right;
            right = left.getRight();
        }
    }

    @Override
    public void update(Filter filter, View view, String userName) throws Exception {
        filter.setInfoOnUpdate(userName);
        filterDao.update(filter);
    }

    @Override
    public void delete(Integer id) throws Exception {
        filterDao.deleteById(id);
    }
}
