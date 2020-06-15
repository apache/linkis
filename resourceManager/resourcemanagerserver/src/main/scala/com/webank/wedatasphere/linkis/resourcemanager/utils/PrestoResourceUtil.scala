package com.webank.wedatasphere.linkis.resourcemanager.utils

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.ByteType
import com.webank.wedatasphere.linkis.resourcemanager.PrestoResource
import dispatch.{Http, as}
import org.json4s.JsonAST.{JInt, JString}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
 * Created by yogafire on 2020/5/8
 */
object PrestoResourceUtil {

  private implicit val executor = ExecutionContext.global

  def getGroupInfo(groupName: String, prestoUrl: String): (PrestoResource, PrestoResource) = {
    val url = dispatch.url(prestoUrl + "/v1/resourceGroupState/" + groupName.replaceAll("\\.", "/"))

    val future = Http(url > as.json4s.Json).map { resp =>
      val maxMemory: Long = new ByteType((resp \ "softMemoryLimit").asInstanceOf[JString].values).toLong
      val maxInstances: Int = (resp \ "hardConcurrencyLimit").asInstanceOf[JInt].values.toInt + (resp \ "maxQueuedQueries").asInstanceOf[JInt].values.toInt
      val maxResource = new PrestoResource(maxMemory, maxInstances, groupName, prestoUrl)

      val usedMemory: Long = new ByteType((resp \ "memoryUsage").asInstanceOf[JString].values).toLong
      val usedInstances: Int = (resp \ "numRunningQueries").asInstanceOf[JInt].values.toInt + (resp \ "numQueuedQueries").asInstanceOf[JInt].values.toInt
      val usedResource = new PrestoResource(usedMemory, usedInstances, groupName, prestoUrl)
      (maxResource, usedResource)
    }

    Await.result(future, Duration(10, TimeUnit.SECONDS))
  }
}