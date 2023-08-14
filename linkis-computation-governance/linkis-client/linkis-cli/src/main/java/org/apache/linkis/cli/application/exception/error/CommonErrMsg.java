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

package org.apache.linkis.cli.application.exception.error;

public enum CommonErrMsg implements ErrorMsg {

  /** CmdTemplate */
  TemplateGenErr("Cannot generate template. :{0}"),
  TemplateCheckErr("Illegal template.: {0}"),
  TemplateFitErr("Cannot fit input into template: {0}"),

  /** Parser */
  ParserInitErr("Failed to init parser: {0}"),
  ParserParseErr("Failed to parse. {0}"),

  /** Properties */
  PropsLoaderInitErr("Failed to init PropertiesLoader: {0}"),
  PropsReaderInitErr("Failed to init PropertiesReader: {0}"),
  PropsReaderErr("Error reading properties: {0}"),
  PropsLoaderErr("Failed to load properties: {0}"),
  /** VarAccess */
  VarAccessInitErr("Failed to init VarAccess: {0}"),
  VarAccessErr("Cannot access var: {0}"),
  /** Validator */
  ValidationErr("Validation failed: {0}"),

  /** Builder */
  BuilderInitErr("Cannot init Builder. Message: {0}"),
  BuilderBuildErr("Failed to build. Message: {0}"),

  /** Executor */
  ExecutionInitErr("Fail when initiating execution. Message: {0}"),
  ExecutionErr("Error occured during execution: {0}"),
  ExecutionResultErr("Error occured when processing execute result: {0}"),
  /** Transformer */
  TransformerException("Transformer failed to transform: {0}"),

  /** Presenter */
  PresenterInitErr("Presenter is not inited: {0}"),
  PresentModelErr("Presenter Model error: {0}"),
  PresentDriverErr("Presenter driver failed to display display. Message: {0}"),
  PresenterErr("Presenter failed: {0}"),

  /** Event and Listener */
  EventErr("Event failed: {0}"),
  ListenerErr("Listener failed: {0}"),

  /** Unknown Exception */
  UnknownErr("Unknown Exception, cause: {0}"),

  /** for handler */
  CannotHandleErr("Handler Exception: Exception cannot be handled: {0}");

  private String template;

  CommonErrMsg(String msg) {
    this.template = msg;
  }

  @Override
  public String getMsgTemplate() {
    return this.template;
  }

  @Override
  public void setMsgTemplate(String template) {
    this.template = template;
  }
}
