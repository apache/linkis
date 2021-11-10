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
 
package org.apache.linkis.ecm.server.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.ecm.core.engineconn.EngineConn;
import org.apache.linkis.ecm.server.service.EngineConnKillService;
import org.apache.linkis.ecm.server.service.EngineConnListService;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineSuicideRequest;
import org.apache.linkis.message.annotation.Receiver;
import org.apache.linkis.rpc.Sender;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class DefaultEngineConnKillService implements EngineConnKillService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEngineConnKillService.class);

    private EngineConnListService engineConnListService;

    public void setEngineConnListService(EngineConnListService engineConnListService) {
        this.engineConnListService = engineConnListService;
    }

    @Override
    @Receiver
    public EngineStopResponse dealEngineConnStop(EngineStopRequest engineStopRequest) {
        EngineConn engineConn = getEngineConnByServiceInstance(engineStopRequest.getServiceInstance());
        EngineStopResponse response = new EngineStopResponse();
        if (null != engineConn) {
            if(!killEngineConnByPid(engineConn)) {
                response.setStopStatus(false);
                response.setMsg("Kill engine " + engineConn.getServiceInstance().toString() + " failed.");
            } else {
                response.setStopStatus(true);
                response.setMsg("Kill engine " + engineConn.getServiceInstance().toString() + " succeed.");
            }
        } else {
            logger.warn("Cannot find engineconn : " + engineStopRequest.getServiceInstance().toString() + " in this engineConnManager engineConn list, cannot kill.");
            response.setStopStatus(true);
            response.setMsg("EngineConn " + engineStopRequest.getServiceInstance().toString() + " was not found in this engineConnManager.");
        }
        if (!response.getStopStatus()) {
            EngineSuicideRequest request = new EngineSuicideRequest(engineStopRequest.getServiceInstance(), engineStopRequest.getUser());
            try {
                Sender.getSender(engineStopRequest.getServiceInstance()).send(request);
                response.setStopStatus(true);
                response.setMsg(response.getMsg() + " Now send suicide request to engine.");
            } catch (Exception e) {
                response.setMsg(response.getMsg() + " Sended suicide request to engine error, " + e.getMessage());
            }
        }
        return response;
    }

    private EngineConn getEngineConnByServiceInstance(ServiceInstance serviceInstance) {
        if (null == serviceInstance) {
            return null;
        }
        List<EngineConn> engineConnList = engineConnListService.getEngineConns();
        for (EngineConn engineConn : engineConnList) {
            if (null != engineConn && serviceInstance.equals(engineConn.getServiceInstance())) {
                return engineConn;
            }
        }
        return null;
    }

    private boolean killEngineConnByPid(EngineConn engineConn) {
        logger.info("try to kill {} toString with pid({}).", engineConn.getServiceInstance().toString(), engineConn.getPid());
        if (StringUtils.isNotBlank(engineConn.getPid())) {
            String k15cmd = "sudo kill " + engineConn.getPid();
            String k9cmd = "sudo kill -9 " + engineConn.getPid();
            int tryNum = 0;
            try {
                while (isProcessAlive(engineConn.getPid()) && tryNum <= 3) {
                    logger.info("{} still alive with pid({}), use shell command to kill it. try {}++", engineConn.getServiceInstance().toString(), engineConn.getPid(), tryNum++);
                    if (tryNum <= 3) {
                        Utils.exec(k15cmd.split(" "), 3000L);
                    } else {
                        logger.info("{} still alive with pid({}). try {}, use shell command to kill -9 it", engineConn.getServiceInstance().toString(), engineConn.getPid(), tryNum);
                        Utils.exec(k9cmd.split(" "), 3000L);
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while killing engine {} with pid({})." + engineConn.getServiceInstance().toString(), engineConn.getPid());
            }
            if (isProcessAlive(engineConn.getPid())) {
                return false;
            } else {
                return true;
            }
        } else {
            logger.warn("cannot kill {} with empty pid.", engineConn.getServiceInstance().toString());
            return false;
        }
    }

    private boolean isProcessAlive(String pid) {
        String findCmd = "ps -ef | grep " + pid + " | grep EngineConnServer | awk '{print \"exists_\"$2}' | grep " + pid;
        List<String> cmdList = new ArrayList<>();
        cmdList.add("bash");
        cmdList.add("-c");
        cmdList.add(findCmd);
        try {
            String rs = Utils.exec(cmdList.toArray(new String[0]), 5000L);
            return null != rs && rs.contains("exists_" + pid);
        } catch (Exception e) {
            // todo when thread catch exception , it should not be return false
            logger.warn("Method isProcessAlive failed, " + e.getMessage());
            return false;
        }
    }

}
