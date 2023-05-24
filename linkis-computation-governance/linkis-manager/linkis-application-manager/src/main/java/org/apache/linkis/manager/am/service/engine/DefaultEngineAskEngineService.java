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

package org.apache.linkis.manager.am.service.engine;

import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.governance.common.utils.JobUtils;
import org.apache.linkis.governance.common.utils.LoggerUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.engine.*;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineAskEngineService extends AbstractEngineService
    implements EngineAskEngineService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineAskEngineService.class);

  private EngineCreateService engineCreateService;
  private EngineReuseService engineReuseService;
  private AtomicInteger idCreator = new AtomicInteger();
  private String idPrefix = Sender.getThisServiceInstance().getInstance();

  private static final ThreadPoolExecutor EXECUTOR =
      LinkisUtils.newCachedThreadPool(
          AMConfiguration.ASK_ENGINE_ASYNC_MAX_THREAD_SIZE, "AskEngineService-Thread-", true);

  @Autowired
  public DefaultEngineAskEngineService(
      EngineCreateService engineCreateService, EngineReuseService engineReuseService) {
    this.engineCreateService = engineCreateService;
    this.engineReuseService = engineReuseService;
  }

  @Receiver
  public Object askEngine(EngineAskRequest engineAskRequest, Sender sender) {
    String taskId = JobUtils.getJobIdFromStringMap(engineAskRequest.getProperties());
    LoggerUtils.setJobIdMDC(taskId);
    logger.info(
        String.format(
            "received task: %s, engineAskRequest %s", taskId, engineAskRequest.toString()));
    if (!engineAskRequest.getLabels().containsKey(LabelKeyConstant.EXECUTE_ONCE_KEY)) {
      EngineReuseRequest engineReuseRequest = new EngineReuseRequest();
      engineReuseRequest.setLabels(engineAskRequest.getLabels());
      engineReuseRequest.setTimeOut(engineAskRequest.getTimeOut());
      engineReuseRequest.setUser(engineAskRequest.getUser());
      engineReuseRequest.setProperties(engineAskRequest.getProperties());
      EngineNode reuseNode = null;
      try {
        reuseNode = engineReuseService.reuseEngine(engineReuseRequest, sender);
      } catch (Exception t) {
        if (t instanceof LinkisRetryException) {
          logger.info(
              String.format(
                  "task: %s user %s reuse engine failed %s",
                  taskId, engineAskRequest.getUser(), t.getMessage()));
        } else {
          logger.info(
              String.format(
                  "task: %s user %s reuse engine failed", taskId, engineAskRequest.getUser()),
              t);
        }
      }

      if (reuseNode != null) {
        logger.info(
            String.format(
                "Finished to ask engine for task: %s user %s by reuse node %s",
                taskId, engineAskRequest.getUser(), reuseNode));
        return reuseNode;
      }
    }

    String engineAskAsyncId = getAsyncId();
    CompletableFuture<EngineNode> createNodeThread =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                LoggerUtils.setJobIdMDC(taskId);
                logger.info(
                    String.format(
                        "Task: %s start to async(%s) createEngine, %s",
                        taskId, engineAskAsyncId, engineAskRequest.getCreateService()));
                engineAskRequest.getLabels().remove("engineInstance");
                EngineCreateRequest engineCreateRequest = new EngineCreateRequest();
                engineCreateRequest.setLabels(engineAskRequest.getLabels());
                engineCreateRequest.setTimeout(engineAskRequest.getTimeOut());
                engineCreateRequest.setUser(engineAskRequest.getUser());
                engineCreateRequest.setProperties(engineAskRequest.getProperties());
                engineCreateRequest.setCreateService(engineAskRequest.getCreateService());
                EngineNode createNode =
                    engineCreateService.createEngine(engineCreateRequest, sender);

                long timeout =
                    engineCreateRequest.getTimeout() <= 0
                        ? AMConfiguration.ENGINE_START_MAX_TIME.getValue().toLong()
                        : engineCreateRequest.getTimeout();
                EngineNode createEngineNode = getEngineNodeManager().useEngine(createNode, timeout);
                if (createEngineNode == null) {
                  String message =
                      String.format(
                          "create engine%s success, but to use engine failed",
                          createNode.getServiceInstance());
                  throw new LinkisRetryException(AMConstant.EM_ERROR_CODE, message);
                }

                logger.info(
                    String.format(
                        "Task: %s finished to ask engine for user %s by create node %s",
                        taskId, engineAskRequest.getUser(), createEngineNode));
                return createEngineNode;
              } finally {
                LoggerUtils.removeJobIdMDC();
              }
            },
            EXECUTOR);

    createNodeThread.whenComplete(
        (EngineNode engineNode, Throwable exception) -> {
          LoggerUtils.setJobIdMDC(taskId);
          if (exception != null) {
            boolean retryFlag;
            if (exception instanceof LinkisRetryException) {
              retryFlag = true;
            } else if (exception instanceof RetryableException) {
              retryFlag = true;
            } else {
              Throwable rootCauseException = ExceptionUtils.getRootCause(exception);
              if (rootCauseException instanceof SocketTimeoutException) {
                retryFlag = true;
              } else if (rootCauseException instanceof TimeoutException) {
                retryFlag = true;
              } else {
                retryFlag = false;
              }
            }
            String msg =
                String.format(
                    "Task: %s Failed to async(%s) createEngine, can Retry %s",
                    taskId, engineAskAsyncId, retryFlag);
            if (!retryFlag) {
              logger.info(msg, exception);
            } else {
              logger.info(
                  String.format(
                      "msg: %s canRetry Exception: %s", msg, exception.getClass().getName()),
                  exception);
            }
            sender.send(
                new EngineCreateError(
                    engineAskAsyncId, ExceptionUtils.getRootCauseMessage(exception), retryFlag));
          } else {
            logger.info(
                String.format(
                    "Task: %s Success to async(%s) createEngine %s",
                    taskId, engineAskAsyncId, engineNode));
            sender.send(new EngineCreateSuccess(engineAskAsyncId, engineNode));
          }
          LoggerUtils.removeJobIdMDC();
        });
    LoggerUtils.removeJobIdMDC();
    return new EngineAskAsyncResponse(engineAskAsyncId, Sender.getThisServiceInstance());
  }

  private String getAsyncId() {
    return idPrefix + "_" + idCreator.getAndIncrement();
  }
}
