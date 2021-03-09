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
package com.webank.wedatasphere.linkis.bml.service.impl;

import com.webank.wedatasphere.linkis.bml.Entity.DownloadModel;
import com.webank.wedatasphere.linkis.bml.dao.DownloadDao;
import com.webank.wedatasphere.linkis.bml.service.DownloadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * created by cooperyang on 2019/6/3
 * Description:
 */
@Service
public class DownloadServiceImpl implements DownloadService {


    @Autowired
    DownloadDao downloadDao;

    @Override
    public void addDownloadRecord(DownloadModel downloadModel) {
        downloadDao.insertDownloadModel(downloadModel);
    }
}
