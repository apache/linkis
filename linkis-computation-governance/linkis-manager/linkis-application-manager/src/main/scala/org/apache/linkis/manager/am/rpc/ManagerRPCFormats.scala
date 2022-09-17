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

package org.apache.linkis.manager.am.rpc

import org.apache.linkis.manager.common.entity.resource.ResourceSerializer
import org.apache.linkis.manager.common.serializer.NodeResourceSerializer
import org.apache.linkis.manager.rm.ResultResourceSerializer
import org.apache.linkis.rpc.transform.RPCFormats

import org.springframework.stereotype.Component

import org.json4s.Serializer

@Component
class ManagerRPCFormats extends RPCFormats {

  override def getSerializers: Array[Serializer[_]] =
    Array(ResultResourceSerializer, ResourceSerializer, NodeResourceSerializer)

}
