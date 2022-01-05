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
 
package org.apache.linkis.engineconn.acessible.executor.log;

import org.apache.linkis.common.log.LogUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

public abstract class AbstractLogCache implements LogCache {
    protected String generateLog(LogEvent event) {
        if (event.getLevel() == Level.INFO) {
            return LogUtils.generateInfo(event.getMessage().toString());
        } else if (event.getLevel() == Level.WARN) {
            return LogUtils.generateWarn(event.getMessage().toString());
        } else if (event.getLevel() == Level.ERROR) {
            return LogUtils.generateERROR(event.getMessage().toString());
        } else if (event.getLevel() == Level.FATAL) {
            return LogUtils.generateSystemError(event.getMessage().toString());
        }
        return "";
    }
}
