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

import org.apache.linkis.manager.engineplugin.hbase.exception.ExecutorInitException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.hbase.errorcode.HBaseErrorCodeSummary.HBASE_SHELL_ENV_INIT_FAILED;

public class HBaseShellSession implements ShellSession {
  private static final Logger LOGGER = LoggerFactory.getLogger(HBaseShellSession.class);
  private static final String SESSION_PROP_SEPARATOR = "$";

  private final String sessionId;
  private final int sessionInitMaxTimes;
  private final long sessionInitRetryInterval;
  private final long sessionInitTimeout;
  private final long sessionIdle;
  private final long sessionInitStartTime;
  private final boolean sessionDebugLog;
  private final Map<String, String> properties;

  private ScriptingContainer scriptingContainer;
  private StringWriter writer;
  private boolean isConnected;

  public HBaseShellSession(Builder builder) {
    this.properties = builder.properties;
    this.sessionId = builder.sessionId;
    this.sessionInitMaxTimes = builder.sessionInitMaxTimes;
    this.sessionInitRetryInterval = builder.sessionInitRetryInterval;
    this.sessionInitTimeout = builder.sessionInitTimeout;
    this.sessionIdle = builder.sessionIdle;
    this.sessionInitStartTime = System.currentTimeMillis();
    this.sessionDebugLog = builder.sessionDebugLog;
  }

  static class Builder {
    private String sessionId;
    private Map<String, String> properties;
    private int sessionInitMaxTimes;
    private long sessionInitRetryInterval;
    private long sessionInitTimeout;
    private long sessionIdle;
    private boolean sessionDebugLog;

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder properties(Map<String, String> properties) {
      this.properties = properties;
      return this;
    }

    public Builder sessionInitMaxTimes(int sessionInitMaxTimes) {
      this.sessionInitMaxTimes = sessionInitMaxTimes;
      return this;
    }

    public Builder sessionInitRetryInterval(long sessionInitRetryInterval) {
      this.sessionInitRetryInterval = sessionInitRetryInterval;
      return this;
    }

    public Builder sessionInitTimeout(long sessionInitTimeout) {
      this.sessionInitTimeout = sessionInitTimeout;
      return this;
    }

    public Builder sessionIdle(long sessionIdle) {
      this.sessionIdle = sessionIdle;
      return this;
    }

    public Builder sessionDebugLog(boolean sessionDebugLog) {
      this.sessionDebugLog = sessionDebugLog;
      return this;
    }

    public Builder properties(String key, String value) {
      if (this.properties == null) {
        this.properties = new HashMap<>();
      }
      this.properties.put(key, value);
      return this;
    }

    public HBaseShellSession build() {
      return new HBaseShellSession(this);
    }
  }

  public static Builder sessionBuilder() {
    return new Builder();
  }

  @Override
  public void open() {
    Thread t =
        new Thread(
            () -> {
              int initMaxTimes = this.getSessionInitMaxTimes();

              try {
                LOGGER.info("Starting create hbase shell session ......");
                createShellRunningEnv();
              } catch (Exception e) {
                LOGGER.error("error: ", e);
                for (int i = 0; i < initMaxTimes; i++) {
                  try {
                    createShellRunningEnv();
                  } catch (Exception ex) {
                    if (i == (initMaxTimes - 1)) {
                      LOGGER.error(
                          "After {} retries, HBase shell session initialization failed.",
                          initMaxTimes,
                          ex);
                      throw new ExecutorInitException(
                          HBASE_SHELL_ENV_INIT_FAILED.getErrorCode(),
                          HBASE_SHELL_ENV_INIT_FAILED.getErrorDesc());
                    }
                    shortSpin(this.getSessionInitRetryInterval());
                  }
                }
              }
            });
    t.setName("HBaseShellRunningEnvInitThread");
    t.setDaemon(true);
    t.start();
    shortSpin(10000);

    CompletableFuture<Boolean> future =
        CompletableFuture.supplyAsync(this::waitShellSessionConnected);
    try {
      this.isConnected = future.get(this.getSessionInitTimeout(), TimeUnit.MILLISECONDS);
      LOGGER.info("Created hbase shell session successfully.");
    } catch (InterruptedException | ExecutionException e) {
      this.isConnected = false;
      future.cancel(true);
      LOGGER.error("Initialize hbase shell session failed.", e);
      this.destroy();
    } catch (TimeoutException e) {
      LOGGER.error("Initialize hbase shell session timeout.", e);
      this.isConnected = false;
      future.cancel(true);
      this.destroy();
    }
  }

  private void shortSpin(long interval) {
    if (interval <= 0) {
      return;
    }
    try {
      Thread.sleep(interval);
    } catch (InterruptedException e) {
      LOGGER.warn("Ignore error.", e);
    }
  }

  private void createShellRunningEnv() throws IOException {
    this.scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
    this.writer = new StringWriter();
    scriptingContainer.setOutput(this.writer);
    Properties sysProps = System.getProperties();
    String prop = "";
    if (this.isSessionDebugLog()) {
      prop = "-d".concat(SESSION_PROP_SEPARATOR);
    }
    if (properties != null && !properties.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String key : properties.keySet()) {
        sb.append("-D");
        sb.append(key);
        sb.append("=");
        sb.append(properties.get(key));
        sb.append(SESSION_PROP_SEPARATOR);
      }
      prop = prop + sb.substring(0, sb.length() - 1);
    }
    if (StringUtils.isNotBlank(prop)) {
      sysProps.setProperty("hbase.ruby.args", prop);
    }
    try (InputStream in =
        this.getClass().getClassLoader().getResourceAsStream("hbase-ruby/hirb.rb")) {
      this.scriptingContainer.runScriptlet(in, "hirb.rb");
    }
  }

  private boolean waitShellSessionConnected() {
    while (true) {
      Result result = executeCmd("list_namespace");
      String r = result.getResult();
      if (result.isSuccess() && StringUtils.isNotBlank(r)) {
        return true;
      }
      shortSpin(200L);
    }
  }

  @Override
  public Result execute(String cmd) {
    if (!this.isConnected()) {
      String errorMsg =
          String.format(
              "The current hbase shell session [%s] is not connected successfully,"
                  + " please try again.",
              this);
      return Result.failed(
          errorMsg,
          new ExecutorInitException(
              HBASE_SHELL_ENV_INIT_FAILED.getErrorCode(),
              HBASE_SHELL_ENV_INIT_FAILED.getErrorDesc()));
    }
    return executeCmd(cmd);
  }

  @Override
  public void destroy() {
    if (!this.isConnected()) {
      LOGGER.info("The hbase shell session has closed.");
    }
    if (this.scriptingContainer != null) {
      this.scriptingContainer.terminate();
    }
    this.setConnected(false);
    LOGGER.info("The hbase shell session destroy successfully.");
  }

  private Result executeCmd(String cmd) {
    try {
      this.writer.getBuffer().setLength(0);
      Object o = this.scriptingContainer.runScriptlet(cmd);
      this.writer.flush();
      String res = writer.toString();
      if (StringUtils.isBlank(res) && o != null) {
        res = o.toString();
      }
      return Result.ok(res);
    } catch (Exception e) {
      return Result.failed(getStackTrace(e), e);
    }
  }

  public String getStackTrace(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public int getSessionInitMaxTimes() {
    return sessionInitMaxTimes;
  }

  public long getSessionInitRetryInterval() {
    return sessionInitRetryInterval;
  }

  public long getSessionInitTimeout() {
    return sessionInitTimeout;
  }

  public boolean isConnected() {
    return isConnected;
  }

  public void setConnected(boolean connected) {
    isConnected = connected;
  }

  public long getSessionIdle() {
    return sessionIdle;
  }

  public long getSessionInitStartTime() {
    return sessionInitStartTime;
  }

  public boolean isSessionDebugLog() {
    return sessionDebugLog;
  }

  @Override
  public String toString() {
    return this.getSessionId();
  }
}
