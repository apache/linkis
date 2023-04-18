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

package org.apache.linkis.manager.engineplugin.shell.factory;

import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration;
import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.engineconn.common.engineconn.EngineConn;
import org.apache.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory;
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor;
import org.apache.linkis.engineconn.executor.entity.LabelExecutor;
import org.apache.linkis.manager.engineplugin.shell.conf.ShellEngineConnConf;
import org.apache.linkis.manager.engineplugin.shell.executor.ShellEngineConnConcurrentExecutor;
import org.apache.linkis.manager.engineplugin.shell.executor.ShellEngineConnExecutor;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel;
import org.apache.linkis.manager.label.entity.engine.EngineType;
import org.apache.linkis.manager.label.entity.engine.RunType;

import scala.Enumeration;
import scala.Function0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellEngineConnFactory implements ComputationSingleExecutorEngineConnFactory {
  private static final Logger logger = LoggerFactory.getLogger(ShellEngineConnFactory.class);

  @Override
  public LabelExecutor newExecutor(
      int id, EngineCreationContext engineCreationContext, EngineConn engineConn) {
    if (AccessibleExecutorConfiguration.ENGINECONN_SUPPORT_PARALLELISM()) {
      return new ShellEngineConnConcurrentExecutor(
          id, ShellEngineConnConf.SHELL_ENGINECONN_CONCURRENT_LIMIT);
    } else {
      return new ShellEngineConnExecutor(id);
    }
  }

  @Override
  public Enumeration.Value getEngineConnType() {
    return EngineType.SHELL();
  }

  @Override
  public Enumeration.Value getRunType() {
    return RunType.SHELL();
  }

  @Override
  public CodeLanguageLabel getDefaultCodeLanguageLabel() {
    return ComputationSingleExecutorEngineConnFactory.super.getDefaultCodeLanguageLabel();
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public void trace(Function0<String> message) {
    ComputationSingleExecutorEngineConnFactory.super.trace(message);
  }

  @Override
  public void debug(Function0<String> message) {
    ComputationSingleExecutorEngineConnFactory.super.debug(message);
  }

  @Override
  public void info(Function0<String> message) {
    ComputationSingleExecutorEngineConnFactory.super.info(message);
  }

  @Override
  public void info(Function0<String> message, Throwable t) {
    ComputationSingleExecutorEngineConnFactory.super.info(message, t);
  }

  @Override
  public void warn(Function0<String> message) {
    ComputationSingleExecutorEngineConnFactory.super.warn(message);
  }

  @Override
  public void warn(Function0<String> message, Throwable t) {
    ComputationSingleExecutorEngineConnFactory.super.warn(message, t);
  }

  @Override
  public void error(Function0<String> message, Throwable t) {
    ComputationSingleExecutorEngineConnFactory.super.error(message, t);
  }

  @Override
  public void error(Function0<String> message) {
    ComputationSingleExecutorEngineConnFactory.super.error(message);
  }

  @Override
  public ComputationExecutor newExecutor(
      int id,
      EngineCreationContext engineCreationContext,
      EngineConn engineConn,
      Label<?>[] labels) {
    return ComputationSingleExecutorEngineConnFactory.super.newExecutor(
        id, engineCreationContext, engineConn, labels);
  }

  @Override
  public boolean canCreate(Label<?>[] labels) {
    return ComputationSingleExecutorEngineConnFactory.super.canCreate(labels);
  }

  @Override
  public ComputationExecutor createExecutor(
      EngineCreationContext engineCreationContext, EngineConn engineConn, Label<?>[] labels) {
    return ComputationSingleExecutorEngineConnFactory.super.createExecutor(
        engineCreationContext, engineConn, labels);
  }

  @Override
  public String[] getSupportRunTypes() {
    return ComputationSingleExecutorEngineConnFactory.super.getSupportRunTypes();
  }

  @Override
  public ComputationExecutor createExecutor(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {
    return ComputationSingleExecutorEngineConnFactory.super.createExecutor(
        engineCreationContext, engineConn);
  }

  @Override
  public Object createEngineConnSession(EngineCreationContext engineCreationContext) {
    return ComputationSingleExecutorEngineConnFactory.super.createEngineConnSession(
        engineCreationContext);
  }

  @Override
  public EngineConn createEngineConn(EngineCreationContext engineCreationContext) {
    return ComputationSingleExecutorEngineConnFactory.super.createEngineConn(engineCreationContext);
  }
}
