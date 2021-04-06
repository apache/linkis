package com.webank.wedatasphere.linkis.engine.impala.executor

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorFactory}
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory.{Protocol, Transport}
import com.webank.wedatasphere.linkis.engine.impala.configuration.ImpalaConfiguration
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.hadoop.security.UserGroupInformation
import org.springframework.stereotype.Component

import scala.util.Random


/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 *  
 */
@Component
class ImpalaEngineExecutorFactory extends EngineExecutorFactory with Logging{

  private val BDP_QUEUE_NAME: String = "wds.linkis.yarnqueue"

  override def createExecutor(options: JMap[String, String]): EngineExecutor = {
    import scala.collection.JavaConversions._
    val ugi = UserGroupInformation.getCurrentUser
    var queueName =  options.getOrDefault(BDP_QUEUE_NAME, ImpalaConfiguration.IMPALA_JOB_DEFALUT_QUEUE.getValue)
    var impalaClient = ImpalaClientFactory.builder().withProtocol(Protocol.Thrift).withTransport(Transport.HiveServer2)
      .withCoordinator(getCoordinator(ImpalaConfiguration.IMPALA_COORDINATOR_HOSTS.getValue.split("\\,")), ImpalaConfiguration.IMPALA_COORDINATOR_PORT.getValue)
      .withSSL(ImpalaConfiguration.IMPALA_SSL.getValue)
      .withConnectionTimeout(ImpalaConfiguration.IMPALA_CONNECTION_TIMEOUT.getValue)
      .withTrustStore(ImpalaConfiguration.IMPALA_TRUST_FILEPATH.getValue, ImpalaConfiguration.IMPALA_TRUST_PASSWORD.getValue)
      .withLoginTicket(ImpalaConfiguration.IMPALA_LOGINTICKET.getValue.asInstanceOf[Boolean])
      .withParallelLimit(ImpalaConfiguration.IMPALA_PARALLELLIMIT.getValue)
      .withHeartBeatsInSecond(2)
      .withSubmitQueue(queueName)
      .build();
     info("Success to create Impala client Executor," + impalaClient.getExecutionCount)
     new ImpalaEngineExecutor(5000, impalaClient, ugi)
  }

  /**
   * 随机取一个地址；
   */
  def getCoordinator(ipList: Array[String]): String = Random.shuffle(ipList.toList).head

}