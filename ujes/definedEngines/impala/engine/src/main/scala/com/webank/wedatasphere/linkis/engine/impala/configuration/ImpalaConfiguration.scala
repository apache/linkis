package com.webank.wedatasphere.linkis.engine.impala.configuration

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
object ImpalaConfiguration {
  
    val IMPALA_COORDINATOR_HOSTS = CommonVars("wds.linkis.engine.impala.coordinator.hosts", "127.0.0.1")
    val IMPALA_COORDINATOR_PORT = CommonVars("wds.linkis.engine.impala.coordinator.port", 21050)
    val IMPALA_SSL = CommonVars("wds.linkis.engine.impala.ssl", true)
    val IMPALA_TRUST_FILEPATH = CommonVars("wds.linkis.engine.impala.trust.filePath", "/opt/pems4cdh/ca.jks")
    val IMPALA_TRUST_FILETYPE = CommonVars("wds.linkis.engine.impala.trust.fileType", "JKS")
    val IMPALA_TRUST_PASSWORD = CommonVars("wds.linkis.engine.impala.trust.password", "impala")
    val IMPALA_CREDENTIAL_USERNAME = CommonVars("wds.linkis.engine.impala.credential.username", "")
    val IMPALA_CREDENTIAL_PASSWORD = CommonVars("wds.linkis.engine.impala.credential.password", "")
    val IMPALA_LOGINTICKET = CommonVars("wds.linkis.engine.impala.loginTicket", true)
    val IMPALA_PARALLELLIMIT = CommonVars("wds.linkis.engine.impala.parallelLimit", 10)
    val IMPALA_JOB_QUEUE_ = CommonVars("wds.linkis.engine.impala.job.defalut.queue", "root.default")
    val IMPALA_JOB_DEFALUT_QUEUE = CommonVars("wds.linkis.engine.impala.job.defalut.queue", "root.default")

}
