package com.webank.wedatasphere.linkis.manager.common.protocol.label

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol

/**
  * @author peacewong
  * @date 2020/9/10 11:09
  */
case class LabelUpdateRequest(labels: java.util.List[Label[_]]) extends RequestProtocol

case class LabelReportRequest(labels: java.util.List[Label[_]], serviceInstance: ServiceInstance) extends RequestProtocol
