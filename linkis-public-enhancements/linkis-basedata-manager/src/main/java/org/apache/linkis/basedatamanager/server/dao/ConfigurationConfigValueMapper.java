package org.apache.linkis.basedatamanager.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigValue;

public interface ConfigurationConfigValueMapper extends BaseMapper<ConfigurationConfigValue> {

    int updateByKeyId(@Param("configValue") ConfigurationConfigValue configValue);

    int deleteByKeyId(@Param("keyId") Long keyId);
}
