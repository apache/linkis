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

package org.apache.linkis.cli.application.operator;

import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.operator.JobOper;

import java.util.HashMap;
import java.util.Map;

public class OperManager {

  private static Map<String, JobOperBuilder> builderMap = new HashMap<>();

  public static void register(String name, JobOperBuilder builder) {
    builderMap.put(name, builder);
  }

  public static void remove(String name) {
    builderMap.remove(name);
  }

  public static JobOper getNew(String name, CliCtx ctx) {
    JobOperBuilder builder = builderMap.get(name);
    if (builder == null) {
      return null;
    }
    return builder.build(ctx);
  }
}
