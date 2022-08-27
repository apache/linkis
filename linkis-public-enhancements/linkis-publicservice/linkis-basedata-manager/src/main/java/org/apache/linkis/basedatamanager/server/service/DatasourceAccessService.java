package org.apache.linkis.basedatamanager.server.service;

import com.github.pagehelper.PageInfo;
import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jack
* @description 针对表【linkis_ps_datasource_access】的数据库操作Service
* @createDate 2022-08-13 15:17:35
*/
public interface DatasourceAccessService extends IService<DatasourceAccessEntity> {
    public PageInfo getListByPage(String searchName, Integer currentPage, Integer pageSize);
}
