package com.webank.wedatasphere.linkis.bml.dao;

import com.webank.wedatasphere.linkis.bml.Entity.DownloadModel;

import org.apache.ibatis.annotations.Param;

/**
 * created by cooperyang on 2019/5/30
 * Description:
 */
public interface DownloadDao {

    void insertDownloadModel(@Param("downloadModel")DownloadModel downloadModel);


}
