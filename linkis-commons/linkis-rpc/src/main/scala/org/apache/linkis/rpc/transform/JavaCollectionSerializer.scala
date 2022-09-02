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

package org.apache.linkis.rpc.transform

import org.apache.linkis.server.BDPJettyServerHelper

import org.json4s.{CustomSerializer, JArray, JObject}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

// TODO is now only the simplest implementation, and there is a need to optimize it later.(TODO 现在只做最简单的实现，后续有需要再优化)

object JavaCollectionSerializer
    extends CustomSerializer[java.util.List[_]](implicit formats =>
      (
        { case j: JArray =>
          BDPJettyServerHelper.gson.fromJson(write(j), classOf[java.util.List[_]])
        },
        { case list: java.util.List[_] =>
          parse(BDPJettyServerHelper.gson.toJson(list))
        }
      )
    )

object JavaMapSerializer
    extends CustomSerializer[java.util.Map[_, _]](implicit formats =>
      (
        { case j: JObject =>
          BDPJettyServerHelper.gson.fromJson(write(j), classOf[java.util.Map[_, _]])
        },
        { case map: java.util.Map[_, _] =>
          parse(BDPJettyServerHelper.gson.toJson(map))
        }
      )
    )
