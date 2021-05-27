package com.webank.wedatasphere.linkis.orchestrator.computation.utils



import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{AbstractExecTask, ExecTask, PhysicalContext}

import java.util

object TaskTreeUtil {

  def getAllTask[A <: ExecTask](physicalContext: PhysicalContext): util.List[A] = {
    val rsList = new util.ArrayList[A]()
    if (null == physicalContext) {
      return rsList
    }
    val rootTask = physicalContext.getRootTask

    def visit(task: Any): Unit = {
      task match {
        case a: A =>
          rsList.add(a)
        case _ =>
      }
    }

    traverseTask(rootTask, visit)
    rsList
  }

  def traverseTask(rootTask: ExecTask, visit: Any => Unit): Unit = {
    rootTask.getChildren.foreach(task => traverseTask(task, visit))
    visit(rootTask)
  }

}
