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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.entity.node.{EMNode, Node}

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import scala.collection.mutable.ArrayBuffer

/**
 * new ecm node will be added to last
 */
@Component
@Order(7)
class NewECMStandbyRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes && !nodes.isEmpty) {
      Utils.tryCatch {
        if (nodes.head.isInstanceOf[EMNode]) {
          val newEcmArray = new ArrayBuffer[EMNode]()
          val nullStarttimeArray = new ArrayBuffer[EMNode]()
          val sortedArray = new ArrayBuffer[EMNode](nodes.size)
          val nowTime = System.currentTimeMillis()
          nodes
            .map(_.asInstanceOf[EMNode])
            .foreach(node => {
              if (null != node.getStartTime) {
                if (
                    nowTime - node.getStartTime.getTime < AMConfiguration.EM_NEW_WAIT_MILLS.getValue
                ) {
                  logger.info(
                    s"EMNode : ${node.getServiceInstance.getInstance} with createTime : ${node.getStartTime} is new, will standby."
                  )
                  newEcmArray.append(node)
                } else {
                  sortedArray.append(node)
                }
              } else {
                nullStarttimeArray.append(node)
              }
            })
          if (newEcmArray.size > 0) {
            newEcmArray
              .sortWith((n1, n2) => n1.getStartTime.getTime < n2.getStartTime.getTime)
              .foreach(node => sortedArray.append(node))
          }
          if (nullStarttimeArray.size > 0) {
            nullStarttimeArray.foreach(node => sortedArray.append(node))
          }
          return sortedArray.toArray
        } else {
          return nodes
        }
      } { case e: Exception =>
        logger.error(s"Sort Failed because : ${e.getMessage}, will not sort.")
        return nodes
      }
    } else {
      nodes
    }
  }

}
