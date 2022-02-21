package org.apache.linkis.protocol.engine

case class JobProgressInfo(id: String, totalTasks: Int, runningTasks: Int, failedTasks: Int, succeedTasks: Int)
