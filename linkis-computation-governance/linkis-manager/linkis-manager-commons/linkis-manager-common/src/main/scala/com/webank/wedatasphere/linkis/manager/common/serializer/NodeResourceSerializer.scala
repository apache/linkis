package com.webank.wedatasphere.linkis.manager.common.serializer

import com.webank.wedatasphere.linkis.manager.common.entity.resource._
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
