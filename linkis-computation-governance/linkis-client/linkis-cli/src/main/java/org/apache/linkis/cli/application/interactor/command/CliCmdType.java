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

package org.apache.linkis.cli.application.interactor.command;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.entity.command.CmdType;

public enum CliCmdType implements CmdType {
  UNIVERSAL(CliConstants.UNIVERSAL_SUBCMD, 1, CliConstants.UNIVERSAL_SUBCMD_DESC);

  private int id;
  private String name;
  private String desc;

  CliCmdType(String name, int id) {
    this.id = id;
    this.name = name;
    this.desc = null;
  }

  CliCmdType(String name, int id, String desc) {
    this.id = id;
    this.name = name;
    this.desc = desc;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getDesc() {
    return this.desc;
  }
}
