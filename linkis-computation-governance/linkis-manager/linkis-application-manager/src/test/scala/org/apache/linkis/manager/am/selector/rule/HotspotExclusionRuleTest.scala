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

package org.apache.linkis.manager.am.selector.rule

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.node.AMEMNode

import scala.collection.mutable.ArrayBuffer

import org.junit.jupiter.api.Test

class HotspotExclusionRuleTest {

  @Test def testRuleFiltering(): Unit = {
    val hotspotExclusionRule = new HotspotExclusionRule()
    val bufferNodes = new ArrayBuffer[AMEMNode]()
    for (i <- 0 until 3) {
      val amEmNode = new AMEMNode();
      amEmNode.setServiceInstance(ServiceInstance("ecm", s"ecm:$i"))
      bufferNodes.append(amEmNode)
    }
    val res = hotspotExclusionRule.ruleFiltering(bufferNodes.toArray)
    for (i <- 0 until 3) {
      assert(res(i).getServiceInstance.equals(bufferNodes(i).getServiceInstance))
    }
  }

  @Test def testRandomFiltering(): Unit = {
    val hotspotExclusionRule = new HotspotExclusionRule()
    val bufferNodes = new ArrayBuffer[AMEMNode]()
    for (i <- 0 until 9) {
      val amEmNode = new AMEMNode();
      amEmNode.setServiceInstance(ServiceInstance("ecm", s"ecm:$i"))
      bufferNodes.append(amEmNode)
    }
    val res = hotspotExclusionRule.ruleFiltering(bufferNodes.toArray)
    for (i <- 4 until 9) {
      assert(res(i).getServiceInstance.equals(bufferNodes(i).getServiceInstance))
    }
  }

  @Test def testRandomFilteringOverTen(): Unit = {
    val hotspotExclusionRule = new HotspotExclusionRule()
    val bufferNodes = new ArrayBuffer[AMEMNode]()
    for (i <- 0 until 15) {
      val amEmNode = new AMEMNode();
      amEmNode.setServiceInstance(ServiceInstance("ecm", s"ecm:$i"))
      bufferNodes.append(amEmNode)
    }
    val res = hotspotExclusionRule.ruleFiltering(bufferNodes.toArray)
    for (i <- 0 until 4) {
      // scalastyle:off println
      println(res(i).getServiceInstance)
      // scalastyle:on println
    }
    for (i <- 5 until 15) {
      assert(res(i).getServiceInstance.equals(bufferNodes(i).getServiceInstance))
    }
  }

}
