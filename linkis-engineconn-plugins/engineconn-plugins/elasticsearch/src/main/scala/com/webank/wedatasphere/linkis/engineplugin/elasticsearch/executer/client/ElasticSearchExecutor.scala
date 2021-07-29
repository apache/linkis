package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client

import java.io.IOException
import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.impl.ElasticSearchExecutorImpl
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse

import scala.collection.JavaConverters._

/**
 *
 * @author wang_zh
 * @date 2020/5/12
 */
trait ElasticSearchExecutor extends Logging {

  @throws(classOf[IOException])
  def open : Unit

  def executeLine(code: String, alias: String): ExecuteResponse

  def close: Unit

}

object ElasticSearchExecutor {

  def apply(runType:String, storePath: String, properties: util.Map[String, Object]): ElasticSearchExecutor = {
    val newProperties = new util.HashMap[String, String]()
    properties.asScala.foreach {
      case (key: String, value: Object) if value != null =>
        newProperties.put(key, String.valueOf(value))
      case _ =>
    }
    new ElasticSearchExecutorImpl(runType, storePath, newProperties)
  }

}
