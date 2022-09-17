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

package org.apache.linkis.cs.client.utils

import java.lang
import java.lang.reflect.Type

import com.google.gson._

object ContextClientUtils {

  implicit val gson = new GsonBuilder()
    .setPrettyPrinting()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .serializeNulls
    .registerTypeAdapter(
      classOf[java.lang.Double],
      new JsonSerializer[java.lang.Double] {

        override def serialize(
            t: lang.Double,
            `type`: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement =
          if (t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)

      }
    )
    .create

}
