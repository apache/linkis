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

package org.apache.linkis.cli.core.interactor.job;

import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.job.JobSubType;
import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.entity.present.PresentWay;

public abstract class AbstractJob implements Job {
  protected String cid;
  protected CmdType cmdType;
  protected JobSubType subType;
  protected JobOperator operator;
  private PresentWay presentWay;

  @Override
  public String getCid() {
    return cid;
  }

  public void setCid(String cid) {
    this.cid = cid;
  }

  @Override
  public CmdType getCmdType() {
    return cmdType;
  }

  public void setCmdType(CmdType cmdType) {
    this.cmdType = cmdType;
  }

  @Override
  public JobSubType getSubType() {
    return subType;
  }

  public void setSubType(JobSubType subType) {
    this.subType = subType;
  }

  @Override
  public JobOperator getJobOperator() {
    return operator;
  }

  public void setOperator(JobOperator operator) {
    this.operator = operator;
  }

  @Override
  public PresentWay getPresentWay() {
    return presentWay;
  }

  public void setPresentWay(PresentWay presentWay) {
    this.presentWay = presentWay;
  }
}
