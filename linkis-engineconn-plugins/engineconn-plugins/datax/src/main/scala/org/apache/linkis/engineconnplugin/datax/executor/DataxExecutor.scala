package org.apache.linkis.engineconnplugin.datax.executor

import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor, YarnExecutor}
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.engineconnplugin.datax.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.datax.context.DataxEngineConnContext

import java.util

trait DataxExecutor extends LabelExecutor with ResourceExecutor {
  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]


  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = throw new JobExecutionException("Datax Engine Not support method for requestExpectedResource.")
  protected val dataxEngineConnContext: DataxEngineConnContext
}
