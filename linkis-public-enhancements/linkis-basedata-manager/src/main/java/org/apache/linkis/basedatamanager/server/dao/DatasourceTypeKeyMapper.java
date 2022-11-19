package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @description Database operations Mapper for table [linkis_ps_dm_datasource_type_key]
* @createDate 2022-11-19 10:53:47
* @Entity generator.domain.LinkisPsDmDatasourceTypeKey
*/
public interface DatasourceTypeKeyMapper extends BaseMapper<DatasourceTypeKeyEntity> {
    List<DatasourceTypeEntity> getListByPage(String searchName);
}




