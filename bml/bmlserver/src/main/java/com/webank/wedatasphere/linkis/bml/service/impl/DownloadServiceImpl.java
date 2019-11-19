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
