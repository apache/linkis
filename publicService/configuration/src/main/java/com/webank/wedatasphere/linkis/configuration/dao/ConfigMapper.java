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


import com.webank.wedatasphere.linkis.configuration.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by allenlliu on 2018/10/16.
 */
public interface ConfigMapper {

    Long selectAppIDByAppName(@Param("name") String appName);

    List<ConfigKeyValue> selectAppconfigByAppIDAndCreatorID(@Param("userName") String userName, @Param("creatorID") Long creatorID, @Param("appID") Long appID);

    List<ConfigTree> selectTreesByAppNameAndParentID(@Param("appName") String appName, @Param("parentID") Long parentID);

    ConfigTree selectTreeByTreeID(@Param("id") Long id);

    List<ConfigKey> selectKeysByTreeID(@Param("treeID") Long treeID, @Param("creatorID") Long creatorID);

    void insertValue(ConfigKeyUser configKeyUser);

    ConfigKeyUser selectValueByKeyId(@Param("keyID") Long keyID, @Param("userName") String userName, @Param("appID") Long appID);

    List<ConfigKeyValue> selectAllAppKVs(@Param("creatorID") Long creatorID, @Param("appID") Long appID);

    void updateUserValue(@Param("value") String value, @Param("id") Long id);

    ConfigKey selectKeyByKeyID(Long keyID);

    List<ConfigKey> listKeyByCreatorAndAppName(@Param("creatorID") Long creatorID, @Param("appID") Long appID);

    void insertCreator(String creator);

    Long selectTreeIDByKeyID(Long keyID);

    void insertKey(ConfigKey key);

    void insertKeyTree(@Param("keyID") Long keyID, @Param("treeID") Long treeID);

    ConfigTree selectTreeByAppIDAndName(@Param("appID") Long appID, @Param("treeName") String treeName);

}
