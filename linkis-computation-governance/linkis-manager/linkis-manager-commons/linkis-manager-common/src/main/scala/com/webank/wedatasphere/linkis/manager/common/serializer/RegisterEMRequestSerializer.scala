package com.webank.wedatasphere.linkis.manager.common.serializer

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, ResourceSerializer}
import com.webank.wedatasphere.linkis.manager.common.protocol.em.RegisterEMRequest
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}


object RegisterEMRequestSerializer extends CustomSerializer[RegisterEMRequest](implicit formats => ( {
  case JObject(List(("serviceInstance", serviceInstance),
  ("labels", labels),
  ("nodeResource", nodeResource),
  ("user", user),
  ("alias", alias))) =>
    val registerEMRequest = new RegisterEMRequest
    registerEMRequest.setServiceInstance(serviceInstance.extract[ServiceInstance])
    registerEMRequest.setAlias(alias.extract[String])
    //registerEMRequest.setLabels(labels.extract[java.util.HashMap[String, Object]])
    registerEMRequest.setUser(user.extract[String])
    registerEMRequest.setNodeResource(nodeResource.extract[CommonNodeResource])
    registerEMRequest
}, {
  case c: RegisterEMRequest =>
    implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
    ("serviceInstance", Extraction.decompose(c.getServiceInstance)) ~
      ("labels", Extraction.decompose(c.getLabels)) ~
      ("nodeResource", Extraction.decompose(c.getNodeResource)) ~
      ("user", c.getUser) ~
      ("alias", c.getAlias)
}))
