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

package org.apache.linkis.cs.contextcache.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;

public class ContextCacheConf {

  public static final String KEYWORD_SCAN_PACKAGE =
      CommonVars.apply("wds.linkis.cs.keyword.scan.package", "org.apache.linkis.cs").getValue();
  public static final String KEYWORD_SPLIT =
      CommonVars.apply("wds.linkis.cs.keyword.split", ",").getValue();

  public static final int MAX_CACHE_SIZE =
      CommonVars.apply("wds.linkis.cs.cache.size", 3000).getValue();

  public static final long MAX_CACHE_READ_EXPIRE_MILLS =
      CommonVars.apply("wds.linkis.cs.cache.read.expire.mills", new TimeType("3h"))
          .getValue()
          .toLong();
}
