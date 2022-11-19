package org.apache.linkis.basedatamanager.server.service;

import com.github.pagehelper.PageInfo;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @description Database operation Service for table [linkis_ps_dm_datasource_type_key]
* @createDate 2022-11-19 10:53:47
*/
public interface DatasourceTypeKeyService extends IService<DatasourceTypeKeyEntity> {
    PageInfo getListByPage(String searchName, Integer currentPage, Integer pageSize);
}
