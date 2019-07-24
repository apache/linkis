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

package com.webank.wedatasphere.linkis.engine.log;

import org.apache.logging.log4j.core.LogEvent;

import java.util.List;

/**
 * created by enjoyyin on 2018/11/27
 * Description: Cache with time as storage unit（以时间作为存储单位的缓存方式）
 */
public class TimeLogCache extends AbstractLogCache{
    @Override
    public void cacheLog(String log) {

    }

    @Override
    public List<String> getLog(int num) {
        return null;
    }

    @Override
    public List<String> getRemain() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
