package org.apache.linkis.entrance.scheduler.cache

import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.entrance.scheduler.EntranceGroupFactory
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager

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
