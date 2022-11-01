package org.apache.linkis.basedatamanager.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationKeyEngineRelation;

public interface ConfigurationKeyEngineRelationMapper extends BaseMapper<ConfigurationKeyEngineRelation> {

    int deleteByKeyId(@Param("keyId") Long keyId);
}
