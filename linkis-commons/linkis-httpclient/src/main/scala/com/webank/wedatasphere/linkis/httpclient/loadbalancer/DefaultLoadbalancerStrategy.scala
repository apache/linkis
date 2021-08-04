/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by ${USER} on ${DATE}.
 */

package com.webank.wedatasphere.linkis.httpclient.loadbalancer

import scala.util.Random


object DefaultLoadbalancerStrategy extends LoadBalancerStrategy {
  override def createLoadBalancer(): LoadBalancer = new AbstractLoadBalancer {

    override def chooseServerUrl(requestBody: String): String = {
      val serverUrls = getAllServerUrls.filterNot(getAllUnhealthyServerUrls.contains)
      Random.shuffle(serverUrls.toList).head
    }

  }
}
