package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.linkis.basedatamanager.server.domain.ErrorCodeEntity;

import java.util.List;

/**
* @author jack
* @description 针对表【linkis_ps_datasource_access】的数据库操作Mapper
* @createDate 2022-08-13 15:17:35
* @Entity org.apache.linkis.basedatamanager.server.domain.LinkisPsDatasourceAccess
*/
public interface DatasourceAccessMapper extends BaseMapper<DatasourceAccessEntity> {
    public List<ErrorCodeEntity> getListByPage(String searchName);
}




