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

package org.apache.linkis.cli.application.interactor.job.version;

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.utils.LoggerManager;

import java.util.HashMap;
import java.util.Map;

public class VersionJob implements Job {
  private CliCtx ctx;

  @Override
  public void build(CliCtx cliCtx) {
    this.ctx = cliCtx;
  }

  @Override
  public JobResult run() {
    String version = (String) ctx.getExtraMap().get(CliKeys.VERSION);
    Map<String, String> extraMap = new HashMap<>();
    extraMap.put(CliKeys.VERSION, version);
    LoggerManager.getPlaintTextLogger().info("Version=" + version);
    return new VersionJobResult(true, "ok", extraMap);
  }

  @Override
  public void onDestroy() {}
}
