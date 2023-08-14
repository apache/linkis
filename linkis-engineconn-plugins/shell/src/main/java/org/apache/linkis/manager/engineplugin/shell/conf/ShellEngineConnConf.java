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

package org.apache.linkis.manager.engineplugin.shell.conf;

import org.apache.linkis.common.conf.CommonVars;

public class ShellEngineConnConf {
  public static final int SHELL_ENGINECONN_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.shell.concurrent.limit", 30).getValue();

  public static final int LOG_SERVICE_MAX_THREAD_SIZE =
      CommonVars.apply("linkis.engineconn.shell.log.max.thread.size", 50).getValue();
  public static final int SHELL_ENGINECONN_OUTPUT_PRINT_LIMIT =
      CommonVars.apply("wds.linkis.engineconn.shell.output.print.limit", 1000).getValue();
}
