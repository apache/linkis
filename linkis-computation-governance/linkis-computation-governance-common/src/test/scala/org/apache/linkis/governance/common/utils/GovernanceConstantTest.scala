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

package org.apache.linkis.governance.common.utils

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class GovernanceConstantTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val tasksourcemapkey = GovernanceConstant.TASK_SOURCE_MAP_KEY
    val taskresourcesstr = GovernanceConstant.TASK_RESOURCES_STR
    val taskresourceidstr = GovernanceConstant.TASK_RESOURCE_ID_STR
    val taskresourceversionstr = GovernanceConstant.TASK_RESOURCE_VERSION_STR
    val taskresourcefilenamestr = GovernanceConstant.TASK_RESOURCE_FILE_NAME_STR
    val requestenginestatusbatchlimit = GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT

    Assertions.assertEquals("source", tasksourcemapkey)
    Assertions.assertEquals("resources", taskresourcesstr)
    Assertions.assertEquals("resourceId", taskresourceidstr)
    Assertions.assertEquals("version", taskresourceversionstr)
    Assertions.assertEquals("fileName", taskresourcefilenamestr)
    Assertions.assertTrue(500 == requestenginestatusbatchlimit.intValue())

  }

}
