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

package org.apache.linkis.manager.engineplugin.common.loader.entity

import org.junit.jupiter.api.{DisplayName, Test}
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.junit.jupiter.api.Assertions.assertTrue

class EngineConnPluginInfoTest {

  @Test
  @DisplayName("testEngineConnPluginInfoEquals")
  def testEngineConnPluginInfoEquals(): Unit = {


    val engineTypeLabel = new EngineTypeLabel
    engineTypeLabel.setVersion("3.0.1")
    engineTypeLabel.setEngineType("spark")


    val engineConnPluginInfo1 = new EngineConnPluginInfo(engineTypeLabel, -1, "1", "v001", null)
    val engineConnPluginInfo2 = new EngineConnPluginInfo(engineTypeLabel, -1, "1", "v001", null)
    assertTrue(engineConnPluginInfo1 == engineConnPluginInfo2)

  }

}
