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

package org.apache.linkis.cli.application.operator.once;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OnceJobConstantsTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    Integer maxLogSizeOnce = OnceJobConstants.MAX_LOG_SIZE_ONCE;
    Integer idxForLogTypeAll = OnceJobConstants.IDX_FOR_LOG_TYPE_ALL;
    String logIgnoreKeywords = OnceJobConstants.LOG_IGNORE_KEYWORDS;

    Assertions.assertTrue(5000 == maxLogSizeOnce.intValue());
    Assertions.assertTrue(3 == idxForLogTypeAll.intValue());
    Assertions.assertEquals("[SpringContextShutdownHook],[main]", logIgnoreKeywords);
  }
}
