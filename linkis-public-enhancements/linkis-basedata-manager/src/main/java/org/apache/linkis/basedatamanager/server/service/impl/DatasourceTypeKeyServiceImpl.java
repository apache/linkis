package org.apache.linkis.basedatamanager.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceTypeKeyService;
import org.apache.linkis.basedatamanager.server.dao.DatasourceTypeKeyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @description Database operation Service implementation for table [linkis_ps_dm_datasource_type_key]
* @createDate 2022-11-19 10:53:47
*/
@Service
public class DatasourceTypeKeyServiceImpl extends ServiceImpl<DatasourceTypeKeyMapper, DatasourceTypeKeyEntity>
    implements DatasourceTypeKeyService {

    @Override
    public PageInfo getListByPage(String searchName, Integer currentPage, Integer pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        List<DatasourceTypeEntity> listByPage = this.getBaseMapper().getListByPage(searchName);
        PageInfo pageInfo = new PageInfo(listByPage);
        return pageInfo;
    }
}




