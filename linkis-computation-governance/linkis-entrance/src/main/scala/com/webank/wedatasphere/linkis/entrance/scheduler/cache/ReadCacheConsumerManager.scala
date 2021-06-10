package com.webank.wedatasphere.linkis.entrance.scheduler.cache

import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceGroupFactory
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager

class ReadCacheConsumerManager(maxParallelismUsers: Int, persistenceManager: PersistenceManager) extends ParallelConsumerManager(maxParallelismUsers){

  override protected def createConsumer(groupName: String): FIFOUserConsumer = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getGroup(groupName)
    if(groupName.endsWith(EntranceGroupFactory.CACHE)){
      info("Create cache consumer with group: " + groupName)
      new ReadCacheConsumer(getSchedulerContext, getOrCreateExecutorService, group, persistenceManager)
    } else {
      info("Create normal consumer with group: " + groupName)
      new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
    }
  }

}
