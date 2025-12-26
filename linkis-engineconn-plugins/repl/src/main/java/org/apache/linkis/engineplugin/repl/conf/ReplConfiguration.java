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

package org.apache.linkis.engineplugin.repl.conf;

import org.apache.linkis.common.conf.CommonVars;

public class ReplConfiguration {

  public static final CommonVars<Integer> ENGINE_CONCURRENT_LIMIT =
      CommonVars.apply("linkis.engineconn.concurrent.limit", 100);

  public static final CommonVars<String> REPL_TYPE =
      CommonVars.apply("linkis.repl.type", ReplType.JAVA);

  public static final CommonVars<String> CLASSPATH_DIR =
      CommonVars.apply("linkis.repl.classpath.dir", "");

  public static final CommonVars<String> METHOD_NAME =
      CommonVars.apply("linkis.repl.method.name", "");

  public static final CommonVars<Integer> ENGINE_DEFAULT_LIMIT =
      CommonVars.apply("linkis.repl.default.limit", 5000);
}
