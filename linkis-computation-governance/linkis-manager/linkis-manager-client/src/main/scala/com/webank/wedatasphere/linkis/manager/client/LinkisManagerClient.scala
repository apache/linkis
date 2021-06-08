package com.webank.wedatasphere.linkis.manager.client

import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import com.webank.wedatasphere.linkis.httpclient.response.Result
import com.webank.wedatasphere.linkis.manager.client.request.{EngineAskAction, EngineCreateAction, EngineEMListAction, EngineListAction, EngineReuseAction, EngineStopAction, LinkisManagerAction}
import com.webank.wedatasphere.linkis.manager.client.response.{EngineAskResult, EngineCreateResult, EngineEMListResult, EngineListResult, EngineReuseResult, EngineStopResult}

import java.io.Closeable
import java.util.concurrent.TimeUnit

abstract class LinkisManagerClient extends Closeable {

  def listUserEngines(listUserEngines: EngineListAction): EngineListResult = executeJob(listUserEngines).asInstanceOf[EngineListResult]

  def listEMEngines(listEMEngines:EngineEMListAction):EngineEMListResult = executeJob(listEMEngines).asInstanceOf[EngineEMListResult]

  def askEngine(engineAskAction:EngineAskAction):EngineAskResult = executeJob(engineAskAction).asInstanceOf[EngineAskResult]

  def createEngine(engineCreateAction:EngineCreateAction):EngineCreateResult = executeJob(engineCreateAction).asInstanceOf[EngineCreateResult]

  def stopEngine(engineStopAction:EngineStopAction):EngineStopResult = executeJob(engineStopAction).asInstanceOf[EngineStopResult]

  def reuseEngine(engineReuseAction:EngineReuseAction):EngineReuseResult = executeJob(engineReuseAction).asInstanceOf[EngineReuseResult]

  protected[client] def executeJob(linkisManagerAction: LinkisManagerAction): Result

}
object LinkisManagerClient {
  def apply(clientConfig: DWSClientConfig): LinkisManagerClient = new LinkisManagerClientImpl(clientConfig)

  def apply(serverUrl: String): LinkisManagerClient = apply(serverUrl, 30000, 10)

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int): LinkisManagerClient =
    apply(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int,
            authenticationStrategy: AuthenticationStrategy, dwsVersion: String): LinkisManagerClient = {
    val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl(serverUrl)
      .connectionTimeout(30000).discoveryEnabled(false)
      .loadbalancerEnabled(false).maxConnectionSize(maxConnection)
      .retryEnabled(false).readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setDWSVersion(dwsVersion).build()
    apply(clientConfig)
  }

  def getDiscoveryClient(serverUrl: String): LinkisManagerClient = getDiscoveryClient(serverUrl, 30000, 10)

  def getDiscoveryClient(serverUrl: String, readTimeout: Int, maxConnection: Int): LinkisManagerClient =
    getDiscoveryClient(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def getDiscoveryClient(serverUrl: String, readTimeout: Int, maxConnection: Int,
                         authenticationStrategy: AuthenticationStrategy, dwsVersion: String): LinkisManagerClient = {
    val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl(serverUrl)
      .connectionTimeout(30000).discoveryEnabled(true)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(true).maxConnectionSize(maxConnection)
      .retryEnabled(false).readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy).setDWSVersion(dwsVersion).build()
    apply(clientConfig)
  }
}
