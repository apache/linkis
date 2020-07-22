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
package com.webank.wedatasphere.linkis.cs.contextcache.cache.csid;

import com.webank.wedatasphere.linkis.cs.contextcache.cache.cskey.ContextKeyValueContext;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextIDMetric;

/**
 * @author peacewong
 * @date 2020/2/12 16:59
 */
public interface ContextIDValue {

    String getContextID();

    ContextKeyValueContext getContextKeyValueContext();

    void  refresh();

    ContextIDMetric getContextIDMetric();
}
