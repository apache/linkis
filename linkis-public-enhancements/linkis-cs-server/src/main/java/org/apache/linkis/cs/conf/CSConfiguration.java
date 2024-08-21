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

package org.apache.linkis.cs.conf;

import org.apache.linkis.common.conf.CommonVars;

public class CSConfiguration {
  public static final CommonVars<String> CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST =
      CommonVars.apply("linkis.context.value.type.prefix.whitelist", "org.apache.linkis");

  public static final CommonVars<Boolean> ENABLE_CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST_CHECK =
      CommonVars.apply("linkis.context.value.type.prefix.whitelist.check.enable", true);
}
