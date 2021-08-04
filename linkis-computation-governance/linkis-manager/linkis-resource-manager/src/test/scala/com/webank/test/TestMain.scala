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

package com.webank.test


import com.webank.wedatasphere.linkis.manager.common.entity.resource.{ResourceSerializer, YarnResource}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}



class ResultResource

case class NotEnoughResource(reason: String = null) extends ResultResource

class AvailableResource(val ticketId: String) extends ResultResource

case class UserResultResource(ticketId: String, user: String) extends ResultResource

object ResultResourceSerializer1 extends CustomSerializer[ResultResource](implicit formats => ( {
  case JObject(List(("AvailableResource", JObject(List(("ticketId", ticketId)))))) => new AvailableResource(ticketId.extract[String])
}, {
  case r: AvailableResource => ("AvailableResource", ("ticketId", Extraction.decompose(r.ticketId)))
}))

object ResultResourceSerializer2 extends CustomSerializer[ResultResource](implicit formats => ( {
  case JObject(List(("NotEnoughResource", JObject(List(("reason", reason)))))) => NotEnoughResource(reason.extract[String])
  // case JObject(List(("ticketId", ticketId))) => new AvailableResource(ticketId.extract[String])
  case JObject(List(("UserResultResource", JObject(List(("ticketId", ticketId), ("user", user)))))) =>
    UserResultResource(ticketId.extract[String], user.extract[String])
}, {
  case r: NotEnoughResource => ("NotEnoughResource", ("reason", Extraction.decompose(r.reason)))
  case r: AvailableResource => ("ticketId", Extraction.decompose(r.ticketId))
  case r: UserResultResource => ("UserResultResource", ("ticketId", r.ticketId) ~ ("user", r.user))
}))

object TestMain {
  implicit val formats = DefaultFormats + ResultResourceSerializer1 + ResourceSerializer

  //implicit val formats = DefaultFormats + ResourceSerializer + ModuleResourceInfoSerializer + ResultResourceSerializer1  + ModuleInstanceSerializer   + ModuleInfoSerializer
  def main(args: Array[String]): Unit = {
    val yarnResource1 = new YarnResource(100, 10, 0, "ide")
    val yarnResource2 = new YarnResource(1, 1, 0, "ide")
    println(yarnResource1 > yarnResource2)
  }
}
