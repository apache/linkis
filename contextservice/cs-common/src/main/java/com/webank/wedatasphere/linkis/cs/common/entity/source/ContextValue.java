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
package com.webank.wedatasphere.linkis.cs.common.entity.source;

import com.webank.wedatasphere.linkis.cs.common.protocol.ContextValueType;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextValue {

    String getKeywords();

    void setKeywords(String keywords);

    Object getValue();

    void setValue(Object value);




}
