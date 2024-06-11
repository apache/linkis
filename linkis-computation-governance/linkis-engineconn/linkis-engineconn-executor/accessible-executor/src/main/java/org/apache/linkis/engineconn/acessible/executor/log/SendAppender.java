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

package org.apache.linkis.engineconn.acessible.executor.log;

import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.engineconn.common.conf.EngineConnConstant;
import org.apache.linkis.engineconn.executor.listener.EngineConnSyncListenerBus;
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin(name = "Send", category = "Core", elementType = "appender", printObject = true)
public class SendAppender extends AbstractAppender {

  /** @fields serialVersionUID */
  private static final long serialVersionUID = -830237775522429777L;

  private static EngineConnSyncListenerBus engineConnSyncListenerBus =
      ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus();

  private LogCache logCache;
  private static final Logger logger = LoggerFactory.getLogger(SendAppender.class);

  private static final String IGNORE_WORDS =
      AccessibleExecutorConfiguration.ENGINECONN_IGNORE_WORDS().getValue();

  private static final String[] IGNORE_WORD_ARR = IGNORE_WORDS.split(",");

  private static final String PASS_WORDS =
      AccessibleExecutorConfiguration.ENGINECONN_PASS_WORDS().getValue();

  private static final String[] PASS_WORDS_ARR = PASS_WORDS.split(",");

  public SendAppender(
      final String name,
      final Filter filter,
      final Layout<? extends Serializable> layout,
      final boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    this.logCache = LogHelper.logCache();
    logger.info("SendAppender init success");
  }

  @Override
  public void append(LogEvent event) {
    if (engineConnSyncListenerBus == null) {
      return;
    }
    String logStr = new String(getLayout().toByteArray(event));
    if (event.getLevel().intLevel() == Level.INFO.intLevel()) {
      boolean flag = false;
      for (String ignoreLog : IGNORE_WORD_ARR) {
        if (logStr.contains(ignoreLog)) {
          flag = true;
          break;
        }
      }
      for (String word : PASS_WORDS_ARR) {
        if (logStr.contains(word)) {
          flag = false;
          break;
        }
      }
      if (!flag) {
        logStr = matchLog(logStr);
        logCache.cacheLog(logStr);
      }
    } else {
      logCache.cacheLog(logStr);
    }
  }

  @PluginFactory
  public static SendAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Filter") final Filter filter,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
    if (name == null) {
      LOGGER.error("No name provided for SendAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new SendAppender(name, filter, layout, ignoreExceptions);
  }

  public String matchLog(String logLine) {
    String yarnUrl = EngineConnConf.JOB_YARN_TASK_URL().getValue();
    if (StringUtils.isNotBlank(yarnUrl)) {
      Matcher hiveMatcher = Pattern.compile(EngineConnConstant.hiveLogReg()).matcher(logLine);
      if (hiveMatcher.find()) {
        logLine =
            hiveMatcher.replaceAll(
                EngineConnConstant.YARN_LOG_URL() + yarnUrl + hiveMatcher.group(1));
      }
    }
    return logLine;
  }
}
