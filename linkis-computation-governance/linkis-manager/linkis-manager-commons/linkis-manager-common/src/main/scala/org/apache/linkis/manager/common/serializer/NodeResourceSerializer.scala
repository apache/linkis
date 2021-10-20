/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.common.serializer

import org.apache.linkis.manager.common.entity.resource._
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}


object NodeResourceSerializer extends CustomSerializer[NodeResource](implicit formats => ( {
  case JObject(List(("resourceType", resourceType),
  ("maxResource", maxResource),
  ("minResource", minResource),
  ("usedResource", usedResource),
  ("lockedResource", lockedResource),
  ("expectedResource", expectedResource),
  ("leftResource", leftResource))) =>
    val resource = new CommonNodeResource
    resource.setResourceType(ResourceType.valueOf(resourceType.extract[String]))
    resource.setMaxResource(maxResource.extract[Resource])
    resource.setMinResource(minResource.extract[Resource])
    resource.setUsedResource(usedResource.extract[Resource])
    resource.setLockedResource(lockedResource.extract[Resource])
    resource.setExpectedResource(expectedResource.extract[Resource])
    resource.setLeftResource(leftResource.extract[Resource])
    resource
} , {
  case c: CommonNodeResource =>
    implicit val formats = DefaultFormats + ResourceSerializer
      ("resourceType", c.getResourceType.toString) ~
      ("maxResource", Extraction.decompose(c.getMaxResource)) ~
      ("minResource", Extraction.decompose(c.getMinResource)) ~
      ("usedResource", Extraction.decompose(c.getUsedResource)) ~
      ("lockedResource", Extraction.decompose(c.getLockedResource)) ~
      ("expectedResource", Extraction.decompose(c.getExpectedResource)) ~
      ("leftResource", Extraction.decompose(c.getLeftResource))
}))
