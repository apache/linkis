package com.webank.wedatasphere.linkis.computation.client.once.action

/**
  * Created by enjoyyin on 2021/6/8.
  */
abstract class ServiceInstanceBuilder[T <: GetEngineConnAction] private[action]() {
  private var applicationName: String = _
  private var instance: String = _
  private var user: String = _

  def setApplicationName(applicationName: String): this.type = {
    this.applicationName = applicationName
    this
  }

  def setInstance(instance: String): this.type = {
    this.instance = instance
    this
  }

  def setUser(user: String): this.type = {
    this.user = user
    this
  }

  protected def createGetEngineConnAction(): T

  def build(): T = {
    val getEngineConnAction = createGetEngineConnAction()
    getEngineConnAction.setUser(user)
    getEngineConnAction.addRequestPayload("applicationName", applicationName)
    getEngineConnAction.addRequestPayload("instance", instance)
    getEngineConnAction
  }

}