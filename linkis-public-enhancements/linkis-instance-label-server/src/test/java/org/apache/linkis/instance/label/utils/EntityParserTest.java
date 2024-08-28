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

package org.apache.linkis.instance.label.utils;

import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.vo.InstanceInfoVo;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** EntityParser Tester */
public class EntityParserTest {

  @Test
  public void testParseToInstanceVoInstanceInfo() throws Exception {
    // TODO: Test goes here...
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setId(1);
    instanceInfo.setInstance("testInstance");
    instanceInfo.setApplicationName("testApplicationName");
    InstanceInfoVo instanceInfoVo = EntityParser.parseToInstanceVo(instanceInfo);
    assertTrue(instanceInfoVo.getInstance().equals(instanceInfo.getInstance()));
  }

  @Test
  public void testParseToInstanceVoInstanceInfos() throws Exception {
    // TODO: Test goes here...
    List<InstanceInfo> list = new ArrayList<>();
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setId(1);
    instanceInfo.setInstance("testInstance");
    instanceInfo.setApplicationName("testApplicationName");
    list.add(instanceInfo);
    List<InstanceInfoVo> instanceInfoVoList = EntityParser.parseToInstanceVo(list);
    assertTrue(list.get(0).getInstance().equals(instanceInfoVoList.get(0).getInstance()));
  }
}
