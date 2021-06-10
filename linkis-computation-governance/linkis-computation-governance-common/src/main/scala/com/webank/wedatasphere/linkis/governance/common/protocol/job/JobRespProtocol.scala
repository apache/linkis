package com.webank.wedatasphere.linkis.governance.common.protocol.job

import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder}

import java.util
import scala.beans.BeanProperty


class JobRespProtocol {

  @BeanProperty
  var id: Long = _
  @BeanProperty
  var status: Int = _
  @BeanProperty
  var msg: String = _
  @BeanProperty
  var data: util.Map[String, Object] = new util.HashMap[String, Object]()

  override def equals(o: Any): Boolean = {
    if (this == o) return true

    if (o == null || (getClass != o.getClass)) return false

    val that = o.asInstanceOf[JobRespProtocol]

    new EqualsBuilder()
      .append(status, that.status)
      .append(msg, that.msg)
      .append(data, that.data)
      .isEquals
  }

  override def hashCode(): Int = {
    new HashCodeBuilder(17, 37)
      .append(status)
      .append(msg)
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
