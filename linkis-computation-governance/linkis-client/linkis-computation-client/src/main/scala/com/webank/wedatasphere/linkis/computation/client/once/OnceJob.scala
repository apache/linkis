package com.webank.wedatasphere.linkis.computation.client.once

import com.webank.wedatasphere.linkis.computation.client.job.{AbstractLinkisJob, SubmittableLinkisJob}
import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.computation.client.once.action.{GetEngineConnAction, KillEngineConnAction}

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait OnceJob extends AbstractLinkisJob {

  protected val linkisManagerClient: LinkisManagerClient
  protected val user: String

  protected var engineConnId: String = _
  protected var serviceInstance: ServiceInstance = _

  protected def wrapperEC[T](op: => T): T = wrapperObj(serviceInstance, "Please submit job first.")(op)

  protected override def doKill(): Unit = wrapperEC {
    linkisManagerClient.killEngineConn(KillEngineConnAction.newBuilder().setApplicationName(serviceInstance.getApplicationName)
      .setInstance(serviceInstance.getInstance).setUser(user).build())
  }

  def getNodeInfo: util.Map[String, Any] = wrapperEC {
    linkisManagerClient.getEngineConn(GetEngineConnAction.newBuilder().setApplicationName(serviceInstance.getApplicationName)
    .setInstance(serviceInstance.getInstance).setUser(user).build()).getNodeInfo
  }

  protected def getStatus(nodeInfo: util.Map[String, Any]): String = nodeInfo.get("nodeStatus") match {
    case status: String => status
  }

  protected def getServiceInstance(nodeInfo: util.Map[String, Any]): ServiceInstance = nodeInfo.get("serviceInstance") match {
    case serviceInstance: util.Map[String, Any] =>
      ServiceInstance(getAs(serviceInstance, "applicationName"), getAs(serviceInstance, "instance"))
  }

  protected def getAs[T](map: util.Map[String, Any], key: String): T = map.get(key).asInstanceOf[T]

}

trait SubmittableOnceJob extends OnceJob with SubmittableLinkisJob