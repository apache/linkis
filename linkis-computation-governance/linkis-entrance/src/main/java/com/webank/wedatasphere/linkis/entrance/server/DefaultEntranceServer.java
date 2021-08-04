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

package com.webank.wedatasphere.linkis.entrance.server;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.EntranceServer;
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceContextBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceServerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.event.EntranceLogEvent;
import com.webank.wedatasphere.linkis.entrance.event.EntranceLogListener;
import com.webank.wedatasphere.linkis.entrance.log.LogReader;
import com.webank.wedatasphere.linkis.rpc.Sender;

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
