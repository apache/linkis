package com.webank.wedatasphere.linkis.manager.common.protocol.label

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


case class LabelUpdateRequest(labels: java.util.List[Label[_]]) extends RequestProtocol

case class LabelReportRequest(labels: java.util.List[Label[_]], serviceInstance: ServiceInstance) extends RequestProtocol
