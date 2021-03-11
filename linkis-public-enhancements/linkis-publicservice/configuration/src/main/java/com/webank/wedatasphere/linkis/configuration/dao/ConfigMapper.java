/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.configuration.dao;


import com.webank.wedatasphere.linkis.configuration.entity.CategoryLabel;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKey;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKeyValue;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigMapper {

    List<ConfigKeyValue> getConfigByEngineUserCreator(@Param("engineType") String engineType, @Param("creator") String creator, @Param("userName") String userName);

    List<ConfigKeyValue> getConfigKeyByLabelIds(@Param("ids") List<Integer> ids);

    List<ConfigKeyValue> getConfigKeyValueByLabelId(@Param("labelId") Integer labelId);

    Long selectAppIDByAppName(@Param("name") String appName);

    void insertValue(ConfigValue configValue);

    void updateUserValue(@Param("value") String value, @Param("id") Long id);

    ConfigKey selectKeyByKeyID(@Param("id") Long keyID);

    List<ConfigKey> listKeyByEngineType(@Param("engineType") String engineType);

    void insertCreator(String creator);

    List<CategoryLabel> getCategory();

    void insertKey(ConfigKey key);

}
