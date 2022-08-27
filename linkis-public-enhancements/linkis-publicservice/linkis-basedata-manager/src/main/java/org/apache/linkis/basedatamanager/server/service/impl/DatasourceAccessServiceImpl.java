package org.apache.linkis.basedatamanager.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import org.apache.linkis.basedatamanager.server.domain.ErrorCodeEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceAccessService;
import org.apache.linkis.basedatamanager.server.dao.DatasourceAccessMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author jack
* @description 针对表【linkis_ps_datasource_access】的数据库操作Service实现
* @createDate 2022-08-13 15:17:35
*/
@Service
public class DatasourceAccessServiceImpl extends ServiceImpl<DatasourceAccessMapper, DatasourceAccessEntity>
    implements DatasourceAccessService {
    @Override
    public PageInfo getListByPage(String searchName, Integer currentPage, Integer pageSize) {
        PageHelper.startPage(currentPage,pageSize);
        List<ErrorCodeEntity> listByPage = this.getBaseMapper().getListByPage(searchName);
        PageInfo pageInfo = new PageInfo(listByPage);
        return pageInfo;
    }
}




