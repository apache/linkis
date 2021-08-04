package com.webank.wedatasphere.linkis.computation.client.once.action

/**
  * Created by enjoyyin on 2021/6/8.
  */
class KillEngineConnAction extends GetEngineConnAction {

  override def suffixURLs: Array[String] = Array("linkisManager", "killEngineConn")

}
object KillEngineConnAction {

  def newBuilder(): Builder = new Builder

  class Builder extends ServiceInstanceBuilder[KillEngineConnAction] {
    override protected def createGetEngineConnAction(): KillEngineConnAction = new KillEngineConnAction
  }

}