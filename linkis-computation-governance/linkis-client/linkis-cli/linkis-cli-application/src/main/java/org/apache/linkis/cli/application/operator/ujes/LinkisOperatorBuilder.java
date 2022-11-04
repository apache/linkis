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

import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.core.operator.JobOperatorBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisOperatorBuilder extends JobOperatorBuilder {
  private static Logger logger = LoggerFactory.getLogger(LinkisOperatorBuilder.class);

  @Override
  public LinkisJobOperator build() {

    ((LinkisJobOperator) targetObj)
        .setUJESClient(UJESClientFactory.getReusable(stdVarAccess, sysVarAccess));
    ((LinkisJobOperator) targetObj)
        .setServerUrl(stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_GATEWAY_URL));

    return (LinkisJobOperator) super.build();
  }

  @Override
  protected LinkisJobOperator getTargetNewInstance() {
    return new LinkisJobOperator();
  }
}
