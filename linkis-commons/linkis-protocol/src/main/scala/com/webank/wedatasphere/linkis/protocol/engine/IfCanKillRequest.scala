package com.webank.wedatasphere.linkis.protocol.engine


case class IfCanKillRequest(engineInstance:String)

case class IfCanKillResponse(flag:Boolean, reason:String)