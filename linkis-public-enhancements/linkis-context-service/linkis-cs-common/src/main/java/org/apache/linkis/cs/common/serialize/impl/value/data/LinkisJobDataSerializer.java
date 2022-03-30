/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.common.serialize.impl.value.data;

import org.apache.linkis.common.utils.JacksonUtils;
import org.apache.linkis.cs.common.entity.data.LinkisJobData;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.AbstractSerializer;

public class LinkisJobDataSerializer extends AbstractSerializer<LinkisJobData> {

    @Override
    public LinkisJobData fromJson(String json) throws CSErrorException {
        return JacksonUtils.fromJson(json, LinkisJobData.class);
    }

    @Override
    public String getType() {
        return "LinkisJobData";
    }

    @Override
    public boolean accepts(Object obj) {
        if (null != obj && obj.getClass().getName().equals(LinkisJobData.class.getName())) {
            return true;
        }
        return false;
    }
}
