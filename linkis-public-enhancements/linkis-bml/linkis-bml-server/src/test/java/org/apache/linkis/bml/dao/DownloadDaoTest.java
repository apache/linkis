package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.DownloadModel;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DownloadDaoTest extends BaseDaoTest {

    @Autowired DownloadDao downloadDao;

    @Test
    void insertDownloadModel() {
        DownloadModel downloadModel = new DownloadModel();
        downloadModel.setDownloader("test");
        downloadModel.setClientIp("192.143.253");
        downloadModel.setEndTime(new Date());
        downloadModel.setId(12);
        downloadModel.setState(1);
        downloadModel.setStartTime(new Date());
        downloadModel.setVersion("1.2");
        downloadModel.setResourceId("32");
        downloadDao.insertDownloadModel(downloadModel);
    }
}
