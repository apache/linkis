package com.webank.wedatasphere.linkis.protocol.engine


case class EngineStateTransitionRequest(engineInstance:String, state:String)

case class EngineStateTransitionResponse(engineInstance:String, state:String, result:Boolean, message:String)

