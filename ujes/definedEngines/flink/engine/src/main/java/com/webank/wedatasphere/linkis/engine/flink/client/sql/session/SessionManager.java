/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.flink.client.sql.session;

import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.client.config.entries.ExecutionEntry;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Session manager.
 */
public class SessionManager {
    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    private final DefaultContext defaultContext;

    private final long idleTimeout;
    private final long checkInterval;

    private final Map<String, Session> sessions;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture          timeoutCheckerFuture;

    public SessionManager(DefaultContext defaultContext) {
        this.defaultContext = defaultContext;
        Environment env = defaultContext.getDefaultEnv();
        this.idleTimeout = env.getSession().getIdleTimeout();
        this.checkInterval = env.getSession().getCheckInterval();
        this.sessions = new ConcurrentHashMap<>();
    }

    public void open() {
        if (checkInterval > 0 && idleTimeout > 0) {
            timeoutCheckerFuture = executorService.scheduleAtFixedRate(() -> {
                LOG.info("Start to remove expired session, current session count: {}", sessions.size());
                for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                    String  sessionId = entry.getKey();
                    Session session   = entry.getValue();
                    if (isSessionExpired(session)) {
                        LOG.info("Session: {} is expired, close it...", sessionId);
                        closeSession(session);
                    }
                }
                LOG.info("Remove expired session finished, current session count: {}", sessions.size());
            }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void close() {
        LOG.info("Start to close SessionManager");
        if (executorService != null) {
            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                Session session   = entry.getValue();
                this.closeSession(session);
            }
            timeoutCheckerFuture.cancel(true);
            executorService.shutdown();
        }
        LOG.info("SessionManager is closed");
    }

    public String createSession(
            String planner,
            String executionType,
            Map<String, String> properties) {
        Map<String, String> sessionProperties = new HashMap<>(properties);
        sessionProperties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_PLANNER, planner);
        sessionProperties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_TYPE, executionType);
        if (executionType.equalsIgnoreCase(ExecutionEntry.EXECUTION_TYPE_VALUE_BATCH)) {
            // for batch mode we ensure that results are provided in materialized form
            sessionProperties.put(
                    Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_RESULT_MODE,
                    ExecutionEntry.EXECUTION_RESULT_MODE_VALUE_TABLE);
        } else {
            // for streaming mode we ensure that results are provided in changelog form
            sessionProperties.put(
                    Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_RESULT_MODE,
                    ExecutionEntry.EXECUTION_RESULT_MODE_VALUE_CHANGELOG);
        }

        Environment sessionEnvironment = Environment.enrich(
                defaultContext.getDefaultEnv(), sessionProperties, Collections.emptyMap());

        String         sessionId      = SessionID.generate().toHexString();
        SessionContext sessionContext = new SessionContext(sessionId, sessionEnvironment, defaultContext);

        Session session = new Session(sessionContext);
        sessions.put(sessionId, session);
        LOG.info("Session: {} is created.  planner: {}, executionType: {}, properties: {}.",
                sessionId, planner, executionType, properties);
        return sessionId;
    }

    public void closeSession(String sessionId) {
        Session session = getSession(sessionId);
        closeSession(session);
    }

    private void closeSession(Session session) {
        String sessionId = session.getContext().getSessionId();
        sessions.remove(sessionId);
        LOG.info("Session: {} is closed.", sessionId);
    }

    public Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            String msg = String.format("Session: %s does not exist.", sessionId);
            LOG.error(msg);
            throw new SqlGatewayException(msg);
        }
        session.touch();
        return session;
    }

    private boolean isSessionExpired(Session session) {
        if (idleTimeout > 0) {
            return (System.currentTimeMillis() - session.getLastVisitedTime()) > idleTimeout;
        } else {
            return false;
        }
    }
}
