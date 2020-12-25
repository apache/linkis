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
package com.webank.wedatasphere.linkis.cs.persistence.dao;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextIDListener;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by patinousward on 2020/2/17.
 */
public interface ContextIDListenerMapper {

    void createIDListener(@Param("listener") PersistenceContextIDListener listener);

    void remove(@Param("listener") PersistenceContextIDListener listener);

    void removeAll(@Param("contextID") ContextID contextID);

    List<PersistenceContextIDListener> getAll(@Param("contextID") ContextID contextID);
}
