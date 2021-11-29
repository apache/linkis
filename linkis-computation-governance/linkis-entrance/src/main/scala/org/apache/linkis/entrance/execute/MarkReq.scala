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
 
package org.apache.linkis.entrance.execute


import java.util
import scala.beans.BeanProperty
import scala.collection.JavaConversions.mapAsScalaMap



class MarkReq {


  /**
   * 只包含StartUp参数和RunTime参数
   */
  @BeanProperty
  var properties: util.Map[String, Object] = null

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
        if (getLabels.size() != other.getLabels.size()) {
          return false
        }
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

