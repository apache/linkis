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

package org.apache.linkis.metadata.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModelLevelTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String odsName = ModelLevel.ODS.getName();
    String dwdName = ModelLevel.DWD.getName();
    String dwsName = ModelLevel.DWS.getName();
    String adsName = ModelLevel.ADS.getName();

    Assertions.assertEquals("原始数据层", odsName);
    Assertions.assertEquals("明细数据层", dwdName);
    Assertions.assertEquals("汇总数据层", dwsName);
    Assertions.assertEquals("应用数据层", adsName);
  }
}
