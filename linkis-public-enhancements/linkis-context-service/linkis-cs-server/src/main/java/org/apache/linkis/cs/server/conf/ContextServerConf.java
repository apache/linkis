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

package org.apache.linkis.cs.server.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;

public class ContextServerConf {

  public static final String KEYWORD_SCAN_PACKAGE =
      CommonVars.apply("wds.linkis.cs.keyword.scan.package", "org.apache.linkis.cs").getValue();

  public static final int CS_SCHEDULER_MAX_RUNNING_JOBS =
      CommonVars.apply("wds.linkis.cs.running.jobs.max", 100).getValue();
  public static final long CS_SCHEDULER_MAX_ASK_EXECUTOR_TIMES =
      CommonVars.apply("wds.linkis.cs.ask.executor.times.max", new TimeType("1s"))
          .getValue()
          .toLong();

  public static final long CS_SCHEDULER_JOB_WAIT_MILLS =
      CommonVars.apply("wds.linkis.cs.job.wait.mills", 10000).getValue();

  public static final String CS_LABEL_SUFFIX =
      CommonVars.apply("wds.linkis.cs.label.suffix", "").getValue();
}
