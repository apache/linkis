package com.webank.wedatasphere.linkis.protocol.callback

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


// TODO: log type
case class LogCallbackProtocol(nodeId: String, logs: Array[String]) extends RequestProtocol

case class YarnAPPIdCallbackProtocol(nodeId: String, applicationId: String) extends RequestProtocol

case class YarnInfoCallbackProtocol(nodeId: String, uri: String) extends RequestProtocol
