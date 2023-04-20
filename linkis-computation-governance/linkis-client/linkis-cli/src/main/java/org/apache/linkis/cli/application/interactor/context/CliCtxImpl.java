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

package org.apache.linkis.cli.application.interactor.context;

import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.CmdType;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.var.VarAccess;

import java.util.Map;

public class CliCtxImpl implements CliCtx {
  private CmdType cmdType;
  private CmdTemplate cmdTemplate;
  private VarAccess varAccess;
  private Map<String, Object> extraMap;

  public CliCtxImpl(
      CmdType cmdType, CmdTemplate cmdTemplate, VarAccess varAccess, Map<String, Object> extraMap) {
    this.cmdType = cmdType;
    this.cmdTemplate = cmdTemplate;
    this.varAccess = varAccess;
    this.extraMap = extraMap;
  }

  @Override
  public CmdType getCmdType() {
    return cmdType;
  }

  @Override
  public CmdTemplate getTemplate() {
    return cmdTemplate;
  }

  @Override
  public VarAccess getVarAccess() {
    return varAccess;
  }

  @Override
  public Map<String, Object> getExtraMap() {
    return extraMap;
  }
}
