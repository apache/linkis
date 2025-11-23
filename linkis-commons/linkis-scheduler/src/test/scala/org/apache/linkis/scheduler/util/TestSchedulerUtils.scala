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

package org.apache.linkis.scheduler.util

import org.apache.linkis.scheduler.util.SchedulerUtils.{
  getCreatorFromGroupName,
  getEngineTypeFromGroupName,
  getUserFromGroupName,
  isSupportPriority
}

import org.junit.jupiter.api.{Assertions, Test}

class TestSchedulerUtils {

  @Test
  def testIsSupportPriority: Unit = {
    // set linkis.fifo.queue.support.priority.users=hadoop
    // set linkis.fifo.queue.support.priority.creators=IDE  or ALL_CREATORS
    val bool: Boolean = isSupportPriority("IdE_haDoop_hive")
    Assertions.assertEquals(false, bool)
  }

  @Test
  def testShellDangerCode: Unit = {
    var groupName = "IDE_hadoop_hive"
    var username: String = getUserFromGroupName(groupName)
    var engineType: String = getEngineTypeFromGroupName(groupName)
    var creator: String = getCreatorFromGroupName(groupName)
    Assertions.assertEquals("hadoop", username)
    Assertions.assertEquals("hive", engineType)
    Assertions.assertEquals("IDE", creator)
    groupName = "APP_TEST_v_hadoop_hive"
    username = getUserFromGroupName(groupName)
    engineType = getEngineTypeFromGroupName(groupName)
    creator = getCreatorFromGroupName(groupName)
    Assertions.assertEquals("v_hadoop", username)
    Assertions.assertEquals("hive", engineType)
    Assertions.assertEquals("APP_TEST", creator)

    groupName = "TEST_v_hadoop_hive"
    username = getUserFromGroupName(groupName)
    engineType = getEngineTypeFromGroupName(groupName)
    creator = getCreatorFromGroupName(groupName)
    Assertions.assertEquals("v_hadoop", username)
    Assertions.assertEquals("hive", engineType)
    Assertions.assertEquals("TEST", creator)

    groupName = "APP_TEST_hadoop_hive"
    username = getUserFromGroupName(groupName)
    engineType = getEngineTypeFromGroupName(groupName)
    creator = getCreatorFromGroupName(groupName)
    Assertions.assertEquals("hadoop", username)
    Assertions.assertEquals("hive", engineType)
    Assertions.assertEquals("APP_TEST", creator)
  }

}
