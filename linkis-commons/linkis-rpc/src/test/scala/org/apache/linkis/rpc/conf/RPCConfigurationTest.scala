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

package org.apache.linkis.rpc.conf

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class RPCConfigurationTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val bdprpcbroadcastthreadsize = RPCConfiguration.BDP_RPC_BROADCAST_THREAD_SIZE.getValue
    val bdprpceurekaservicerefreshinterval =
      RPCConfiguration.BDP_RPC_EUREKA_SERVICE_REFRESH_INTERVAL.getValue.toLong
    val bdprpcreceiverasynconsumerthreadmax =
      RPCConfiguration.BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX.getValue
    val bdprpcreceiverasynqueuecapacity =
      RPCConfiguration.BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY.getValue
    val bdprpcsenderasynconsumerthreadmax =
      RPCConfiguration.BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX.getValue
    val bdprpcsenderasynqueuecapacity = RPCConfiguration.BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY.getValue
    val publicserviceappprefix = RPCConfiguration.PUBLIC_SERVICE_APP_PREFIX
    val enablepublicservice = RPCConfiguration.ENABLE_PUBLIC_SERVICE.getValue
    val publicserviceapplicationname = RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME.getValue
    val publicservicelist = RPCConfiguration.PUBLIC_SERVICE_LIST
    val computationgovernanceappprefix = RPCConfiguration.COMPUTATION_GOVERNANCE_APP_PREFIX
    val linkismanagerservicemerged = RPCConfiguration.LINKIS_MANAGER_SERVICE_MERGED.getValue
    val linkismanagerapplicationname = RPCConfiguration.LINKIS_MANAGER_APPLICATION_NAME.getValue
    val linkismanagerservicelist = RPCConfiguration.LINKIS_MANAGER_SERVICE_LIST
    val metadataqueryservicelist = RPCConfiguration.METADATAQUERY_SERVICE_LIST

    Assertions.assertTrue(25 == bdprpcbroadcastthreadsize.intValue())
    Assertions.assertTrue(1000L == bdprpceurekaservicerefreshinterval.longValue())
    Assertions.assertTrue(400 == bdprpcreceiverasynconsumerthreadmax.intValue())
    Assertions.assertTrue(5000 == bdprpcreceiverasynqueuecapacity.intValue())
    Assertions.assertTrue(100 == bdprpcsenderasynconsumerthreadmax.intValue())
    Assertions.assertTrue(2000 == bdprpcsenderasynqueuecapacity)
    Assertions.assertEquals("linkis-ps-", publicserviceappprefix)
    Assertions.assertTrue(enablepublicservice)
    Assertions.assertEquals("linkis-ps-publicservice", publicserviceapplicationname)
    Assertions.assertTrue(publicservicelist.size > 0)
    Assertions.assertEquals("linkis-cg-", computationgovernanceappprefix)
    Assertions.assertTrue(linkismanagerservicemerged)
    Assertions.assertEquals("linkis-cg-linkismanager", linkismanagerapplicationname)
    Assertions.assertTrue(linkismanagerservicelist.size > 0)
    Assertions.assertTrue(metadataqueryservicelist.size > 0)

  }

}
