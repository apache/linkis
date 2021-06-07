package com.webank.wedatasphere.linkis.manager.label.entity

import java.util


class JobLabel extends GenericLabel {

  setLabelKey("job")

  def getJobId: String = Option(getValue).map(_.get("jobId")).orNull

  def setJobId(jobId: String): Unit = {
    if (null == getValue) setValue(new util.HashMap[String, String])
    getValue.put("jobId", jobId)
  }

}
