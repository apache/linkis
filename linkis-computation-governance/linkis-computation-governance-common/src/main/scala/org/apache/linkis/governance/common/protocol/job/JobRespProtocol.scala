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
 
package org.apache.linkis.governance.common.protocol.job

import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder}

import java.util
import scala.beans.BeanProperty


class JobRespProtocol {

  @BeanProperty
  var id: Long = _
  /**
   * 0 success
   * 1 failed
   * 2 retry
   */
  /**
    * 0 success
    * 1 failed
    * 2 retry
    */
  @BeanProperty
  var status: Int = 0
  @BeanProperty
  var msg: String = _
  @BeanProperty
  var exception : Exception = _
  @BeanProperty
  var data: util.Map[String, Object] = new util.HashMap[String, Object]()


  override def equals(o: Any): Boolean = {
    if (this == o) return true

    if (o == null || (getClass != o.getClass)) return false

    val that = o.asInstanceOf[JobRespProtocol]

    new EqualsBuilder()
      .append(status, that.status)
      .append(msg, that.msg)
      .append(exception, that.exception)
      .append(data, that.data)
      .isEquals
  }

  override def hashCode(): Int = {
    new HashCodeBuilder(17, 37)
      .append(status)
      .append(msg)
      .append(exception)
      .append(data)
      .toHashCode()
  }


  override def toString: String = {
    "JobResponse{" +
      "status=" + status +
      ", msg='" + msg + "'" +
      ", data=" + data + "}"
  }

}
