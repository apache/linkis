package com.webank.wedatasphere.linkis.engineconn.once.executor

import com.webank.wedatasphere.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.executor.ExecutorExecutionContext
import com.webank.wedatasphere.linkis.governance.common.entity.job.OnceExecutorContent
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}

/**
  * Created by enjoyyin on 2021/5/21.
  */
class OnceExecutorExecutionContext(engineCreationContext: EngineCreationContext,
                                   onceExecutorContent: OnceExecutorContent) extends ExecutorExecutionContext {

  private val resultSetFactory = ResultSetFactory.getInstance

  def getEngineCreationContext: EngineCreationContext = engineCreationContext

  def getOnceExecutorContent: OnceExecutorContent = onceExecutorContent

  override protected def getResultSetByType(resultSetType: String): ResultSet[_ <: MetaData, _ <: Record] =
    resultSetFactory.getResultSetByType(resultSetType)

  override protected def getDefaultResultSetByType: String = resultSetFactory.getResultSetType(0)

  override protected def newResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record],
                                            resultSetPath: FsPath,
                                            alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    ResultSetWriter.getResultSetWriter(resultSet, 0, resultSetPath, engineCreationContext.getUser)  // OnceExecutor doesn't need to cache resultSet.

}
