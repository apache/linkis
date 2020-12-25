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

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKeyListener;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by patinousward on 2020/2/17.
 */
public interface ContextKeyListenerMapper {

    void createKeyListener(@Param("listener") PersistenceContextKeyListener listener);

    void remove(@Param("listener") ContextKeyListenerDomain contextKeyListenerDomain, @Param("keyId") Integer keyId);

    void removeAll(@Param("keyIds") List<Integer> keyIds);

    List<PersistenceContextKeyListener> getAll(@Param("keyIds") List<Integer> keyIds);
}
