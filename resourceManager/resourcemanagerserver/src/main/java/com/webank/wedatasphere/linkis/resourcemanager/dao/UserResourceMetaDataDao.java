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

package com.webank.wedatasphere.linkis.resourcemanager.dao;

import com.webank.wedatasphere.linkis.resourcemanager.domain.UserResourceMetaData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by shanhuang on 9/11/18.
 */
public interface UserResourceMetaDataDao {

    List<UserResourceMetaData> getAll();

    List<UserResourceMetaData> getByUser(@Param("user") String user);

    UserResourceMetaData getByTicketId(@Param("ticketId") String ticketId);

    void insert(UserResourceMetaData userResourceMetaData);

    void update(UserResourceMetaData userResourceMetaData);

    void deleteById(Integer id);

    void deleteByEmInstance(@Param("emApplicationName") String emApplicationName, @Param("emInstance") String emInstance);

    void deleteByUser(@Param("user") String user);
}
