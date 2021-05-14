/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.orchestrator.ecm.entity

import java.util

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineAskRequest

import scala.beans.BeanProperty
import scala.collection.JavaConversions._



trait MarkReq {


  def createEngineConnAskReq(): EngineAskRequest

  def getPolicyObj(): Policy

  def setPolicyObj(policy: Policy): Unit


  /**
    * 只包含StartUp参数
    */
  @BeanProperty
  var properties: util.Map[String, String] = null

  /**
    * 启动engineConn必要Label
    */
  @BeanProperty
  var labels: util.Map[String, AnyRef] = null

  /**
    * executeUser
    */
  @BeanProperty
  var user: String = null

  /**
    * 启动的服务：如linkis-entrance
    */
  @BeanProperty
  var createService: String = null

  @BeanProperty
  var description: String = null

  @BeanProperty
  var engineConnCount: Int = _

}

class DefaultMarkReq extends MarkReq {

  private var policy: Policy = _

  override def createEngineConnAskReq(): EngineAskRequest = {
    val engineAskRequest = new EngineAskRequest
    engineAskRequest.setCreateService(getCreateService)
    engineAskRequest.setDescription(getDescription)
    engineAskRequest.setLabels(getLabels)
    engineAskRequest.setProperties(getProperties)
    engineAskRequest.setUser(getUser)
    engineAskRequest
  }

  override def getPolicyObj(): Policy = this.policy

  override def setPolicyObj(policy: Policy): Unit = this.policy = policy

  override def equals(obj: Any): Boolean = {
    var flag = false
    if (null != obj && obj.isInstanceOf[MarkReq]) {
      val other = obj.asInstanceOf[MarkReq]

      if (other.getUser != getUser) {
        return flag
      }

     /* if (other.getProperties != null && getProperties != null) {
        val iterator = other.getProperties.iterator
        while (iterator.hasNext) {
          val next = iterator.next()
          if (!next._2.equalsIgnoreCase(getProperties.get(next._1))) {
            return flag
          }
        }
      }*/
      if (other.getLabels != null && getLabels != null) {
        val iterator = other.getLabels.iterator
        while (iterator.hasNext) {
          val next = iterator.next()
          if (null == next._2 || !next._2.equals(getLabels.get(next._1))) {
            return false
          }
        }
      }
      flag = true
    }
    flag
  }


}
