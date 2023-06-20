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

package org.apache.linkis.governance.common.conf

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class GovernanceCommonConfTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val conffilterrm = GovernanceCommonConf.CONF_FILTER_RM
    val sparkengineversion = GovernanceCommonConf.SPARK_ENGINE_VERSION.getValue
    val hiveengineversion = GovernanceCommonConf.HIVE_ENGINE_VERSION.getValue
    val pythonengineversion = GovernanceCommonConf.PYTHON_ENGINE_VERSION.getValue
    val pythoncodeparserswitch = GovernanceCommonConf.PYTHON_CODE_PARSER_SWITCH.getValue
    val scalacodeparserswitch = GovernanceCommonConf.SCALA_CODE_PARSER_SWITCH.getValue
    val engineconnspringname = GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue
    val engineconnmanagerspringname = GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue
    val engineconnportrange = GovernanceCommonConf.ENGINE_CONN_PORT_RANGE.getValue
    val managerservicename = GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue
    val entranceservicename = GovernanceCommonConf.ENTRANCE_SERVICE_NAME.getValue
    val enginedefaultlimit = GovernanceCommonConf.ENGINE_DEFAULT_LIMIT.getValue
    val skippythonparser = GovernanceCommonConf.SKIP_PYTHON_PARSER.getValue
    val resultsetstorepath = GovernanceCommonConf.RESULT_SET_STORE_PATH.getValue
    val errorcodedesclen = GovernanceCommonConf.ERROR_CODE_DESC_LEN

    Assertions.assertEquals("wds.linkis.rm", conffilterrm)
    Assertions.assertEquals("3.2.1", sparkengineversion)
    Assertions.assertEquals("3.1.3", hiveengineversion)
    Assertions.assertEquals("python2", pythonengineversion)
    Assertions.assertFalse(pythoncodeparserswitch)
    Assertions.assertFalse(scalacodeparserswitch)
    Assertions.assertEquals("linkis-cg-engineconn", engineconnspringname)
    Assertions.assertEquals("linkis-cg-engineconnmanager", engineconnmanagerspringname)
    Assertions.assertEquals("-", engineconnportrange)
    Assertions.assertEquals("linkis-cg-linkismanager", managerservicename)
    Assertions.assertEquals("linkis-cg-entrance", entranceservicename)
    Assertions.assertTrue(5000 == enginedefaultlimit.intValue())
    Assertions.assertTrue(skippythonparser)
    Assertions.assertEquals("hdfs:///tmp/linkis/", resultsetstorepath)
    Assertions.assertTrue(512 == errorcodedesclen)

  }

}
