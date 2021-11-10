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
 
package org.apache.linkis.entrance.server;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.annotation.EntranceContextBeanAnnotation;
import org.apache.linkis.entrance.annotation.EntranceServerBeanAnnotation;
import org.apache.linkis.entrance.event.EntranceLogEvent;
import org.apache.linkis.entrance.event.EntranceLogListener;
import org.apache.linkis.entrance.log.LogReader;
import org.apache.linkis.rpc.Sender;

import javax.annotation.PostConstruct;

/**
 * Description:
 */
@EntranceServerBeanAnnotation
public class DefaultEntranceServer extends EntranceServer{

    @EntranceContextBeanAnnotation.EntranceContextAutowiredAnnotation
    private EntranceContext entranceContext;

    public DefaultEntranceServer() {
    }

    public DefaultEntranceServer(EntranceContext entranceContext) {
        this.entranceContext = entranceContext;
    }

    @Override
    @PostConstruct
    public void init() {
        getEntranceWebSocketService();
        addRunningJobEngineStatusMonitor();
    }

    @Override
    public String getName() {
        return Sender.getThisInstance();
    }

    @Override
    public EntranceContext getEntranceContext() {
        return entranceContext;
    }

    @Override
    public LogReader logReader(String execId) {
        return getEntranceContext().getOrCreateLogManager().getLogReader(execId);
    }

    private void addRunningJobEngineStatusMonitor() {

    }

}
