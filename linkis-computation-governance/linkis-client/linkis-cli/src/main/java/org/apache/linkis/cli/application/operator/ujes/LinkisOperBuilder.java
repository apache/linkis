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

package org.apache.linkis.cli.application.operator.ujes;

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.operator.JobOper;
import org.apache.linkis.cli.application.operator.JobOperBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisOperBuilder implements JobOperBuilder {
  private static Logger logger = LoggerFactory.getLogger(LinkisOperBuilder.class);

  @Override
  public JobOper build(CliCtx ctx) {
    LinkisJobOper jobOper = new LinkisJobOper();
    jobOper.setUJESClient(UJESClientFactory.getReusable(ctx.getVarAccess()));
    jobOper.setServerUrl(
        ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_COMMON_GATEWAY_URL));
    return jobOper;
  }
}
