/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.hbase.shell;

import java.util.HashMap;
import java.util.Map;

import org.apache.linkis.manager.engineplugin.hbase.HBaseConnectionManager;

public class HBaseShellSessionManager {
    private volatile static Map<String, HBaseShellSession> shellSessionMap;

    public static HBaseShellSession getHBaseShellSession(Map<String, String> prop) {
        String sessionId = HBaseConnectionManager.getInstance().generateUniqueConnectionKey(prop);

        if (shellSessionMap == null || !shellSessionMap.containsKey(sessionId)) {
            synchronized (HBaseShellSessionManager.class) {
                if (shellSessionMap == null || !shellSessionMap.containsKey(sessionId)) {
                    if (shellSessionMap == null) {
                        shellSessionMap = new HashMap<>(2);
                    }
                    if (!shellSessionMap.containsKey(sessionId)) {
                        HBaseShellSession shellSession = HBaseShellSession.sessionBuilder()
                                .sessionId(sessionId)
                                .sessionInitMaxTimes(HBaseShellSessionConfig.maxRetryTimes(prop))
                                .sessionInitRetryInterval(HBaseShellSessionConfig.initRetryInterval(prop))
                                .sessionInitTimeout(HBaseShellSessionConfig.initTimeout(prop))
                                .sessionIdle(HBaseShellSessionConfig.idleTimeMs(prop))
                                .sessionDebugLog(HBaseShellSessionConfig.openDebugLog(prop))
                                .properties(prop)
                                .build();
                        shellSession.open();
                        shellSessionMap.put(sessionId, shellSession);
                        return shellSession;
                    }
                }
            }
        }
       return shellSessionMap.get(sessionId);
    }

}
