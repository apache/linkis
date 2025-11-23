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

package org.apache.linkis.common.commonvars;

import org.apache.linkis.common.conf.BDPConfiguration;
import org.apache.linkis.common.conf.CommonVars;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonVarsTest {

  @Test
  public void testGetVars() {
    {
      String testKeyNotHotload = "wds.linkis.test___test___test.key1";
      String defaultValueNotHotload = "defaultValueNotHotload";
      CommonVars<String> strVar1 = CommonVars.apply(testKeyNotHotload, defaultValueNotHotload);
      assertEquals(defaultValueNotHotload, strVar1.defaultValue());
      assertEquals(defaultValueNotHotload, strVar1.getValue());
    }

    {
      String testKeyNotHotloadSet = "wds.linkis.test___test___test.key2";
      String defaultValueNotHotloadSet1 = "defaultValueNotHotloadSet1";
      String defaultValueNotHotloadSet2 = "defaultValueNotHotloadSet2";
      String valueNotHotloadSet1 = "valueNotHotloadSet1";
      String valueNotHotloadSet2 = "valueNotHotloadSet2";
      CommonVars<String> strVar2 =
          CommonVars.apply(testKeyNotHotloadSet, defaultValueNotHotloadSet1);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      assertEquals(defaultValueNotHotloadSet1, strVar2.getValue());

      BDPConfiguration.setIfNotExists(testKeyNotHotloadSet, valueNotHotloadSet1);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      //      assertEquals(valueNotHotloadSet1, strVar2.getValue());
      BDPConfiguration.setIfNotExists(testKeyNotHotloadSet, valueNotHotloadSet2);
      //      assertEquals(valueNotHotloadSet1, strVar2.getValue());

      BDPConfiguration.set(testKeyNotHotloadSet, valueNotHotloadSet2);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      //      assertEquals(valueNotHotloadSet2, strVar2.getValue());
    }
  }

  @Test
  public void testGetHotloadVars() {
    {
      String testKeyHotload = "wds.linkis.test___test___test.key1";
      String defaultValueHotload = "defaultValueHotload";
      CommonVars<String> strVar1 = CommonVars.apply(testKeyHotload, defaultValueHotload);
      assertEquals(defaultValueHotload, strVar1.defaultValue());
      assertEquals(defaultValueHotload, strVar1.getValue());
    }

    {
      String testKeyHotloadSet = "wds.linkis.test___test___test.hotload.key2";
      String defaultValueNotHotloadSet1 = "defaultValueNotHotloadSet1";
      String defaultValueNotHotloadSet2 = "defaultValueNotHotloadSet2";
      String valueNotHotloadSet1 = "valueNotHotloadSet1";
      String valueNotHotloadSet2 = "valueNotHotloadSet2";
      CommonVars<String> strVar2 = CommonVars.apply(testKeyHotloadSet, defaultValueNotHotloadSet1);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      assertEquals(defaultValueNotHotloadSet1, strVar2.getValue());

      BDPConfiguration.setIfNotExists(testKeyHotloadSet, valueNotHotloadSet1);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      //      assertEquals(valueNotHotloadSet1, strVar2.getValue());
      BDPConfiguration.setIfNotExists(testKeyHotloadSet, valueNotHotloadSet2);
      //      assertEquals(valueNotHotloadSet1, strVar2.getValue());

      BDPConfiguration.set(testKeyHotloadSet, valueNotHotloadSet2);
      assertEquals(defaultValueNotHotloadSet1, strVar2.defaultValue());
      //      assertEquals(valueNotHotloadSet2, strVar2.getValue());
    }
  }
}
