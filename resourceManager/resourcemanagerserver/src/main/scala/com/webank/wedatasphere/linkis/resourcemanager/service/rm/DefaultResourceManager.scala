/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.resourcemanager.service.rm

import java.util.UUID
import java.util.concurrent.{ScheduledFuture, TimeUnit}

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy._
import com.webank.wedatasphere.linkis.resourcemanager._
import com.webank.wedatasphere.linkis.resourcemanager.domain._
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{ModuleRegisterEvent, _}
import com.webank.wedatasphere.linkis.resourcemanager.exception.{RMErrorException, RMWarnException}
import com.webank.wedatasphere.linkis.resourcemanager.schedule.EventGroupFactory
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, ResourceLockService, UserMetaData, UserResourceRecordService}
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 9/12/18.
  */
@Component
class DefaultResourceManager extends ResourceManager with Logging with InitializingBean {

  override protected val rmContext: RMContext = RMContext.getOrCreateRMContext()

  @Autowired
  var userResourceRecordService: UserResourceRecordService = _
  @Autowired
  var moduleResourceRecordService: ModuleResourceRecordService = _
  @Autowired
  var moduleResourceManager: ModuleResourceManager = _
  @Autowired
  var userResourceManager: UserResourceManager = _
  @Autowired
  var resourceLockService: ResourceLockService = _
  @Autowired
  var userMetaData: UserMetaData = _

  var requestResourceServices: Array[RequestResourceService] = _

  private var future: ScheduledFuture[_] = _

  override def afterPropertiesSet(): Unit = {
    requestResourceServices = Array(new DefaultReqResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager),
      new YarnReqResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager),
      new DriverAndYarnReqResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager))

    future = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        moduleResourceManager.getModuleNames().foreach { moduleName =>
          val existsEngineInstances = Sender.getInstances(moduleName)
          moduleResourceManager.getModuleInstancesByName(moduleName).foreach { moduleResourceRecord =>
            val interval = System.currentTimeMillis() - moduleResourceRecord.getRegisterTime

            //TODO  Remove waiting(去掉等待)
            val moduleInstance = ServiceInstance(moduleResourceRecord.getEmApplicationName, moduleResourceRecord.getEmInstance)
            if (interval > RMConfiguration.RM_REGISTER_INTERVAL_TIME.getValue && !existsEngineInstances.contains(moduleInstance)) {
              warn(s"${moduleInstance} has been killed, now remove it, and unregister it for RM.")
              Utils.tryAndWarn(unregister(moduleInstance))
            }
          }
        }
      }
    }, RMConfiguration.RM_COMPLETED_SCAN_INTERVAL.getValue, RMConfiguration.RM_COMPLETED_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)

    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        info("Start scanning engines.")
        val engineToRecordMap = userResourceRecordService.getAll().filter(_.getEngineApplicationName != null).groupBy[String](_.getEngineApplicationName)
        engineToRecordMap.keySet.foreach { engineName =>
          info(s"processing engine ${engineName}")
          val existingEngineInstances = Sender.getInstances(engineName)
          engineToRecordMap.get(engineName) match {
            case Some(engineRecords) =>
              engineRecords.foreach { engineRecord =>
                val engineInstance = ServiceInstance(engineRecord.getEngineApplicationName, engineRecord.getEngineInstance)
                if (System.currentTimeMillis - engineRecord.getUsedTime > RMConfiguration.RM_ENGINE_RELEASE_THRESHOLD.getValue.toLong
                  && !existingEngineInstances.contains(engineInstance)) {
                  info(s"engine ${engineInstance} no longer exists in service list, releasing it.")
                  val engineResultResource = UserResultResource(engineRecord.getTicketId, engineRecord.getUser)
                  val moduleInstance = ServiceInstance(engineRecord.getEmApplicationName, engineRecord.getEmInstance)
                  resourceReleased(engineResultResource, moduleInstance)
                }
              }
            case None =>
          }
        }
        info("Complete scanning engines.")
      }
    }, 0, RMConfiguration.RM_ENGINE_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  /**
    * The registration method is mainly used to notify all RM nodes (including the node)
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
    *
    * @param moduleInfo
    */
  override def register(moduleInfo: ModuleInfo): Unit = {
    info(s"Get a module registration request: $moduleInfo")
    if (moduleInfo.totalResource < Resource.getZeroResource(moduleInfo.totalResource) || moduleInfo.protectedResource < Resource.getZeroResource(moduleInfo.protectedResource))
      throw new RMErrorException(11002, s"Failed to register module : $ModuleInfo,Resources cannot be negative")
    if (moduleInfo.protectedResource > moduleInfo.totalResource)
      throw new RMErrorException(11002, s"Failed to register module : $ModuleInfo,protectedResource must be less than totalResource")
    moduleResourceManager.dealModuleRegisterEvent(new ModuleRegisterEvent(EventScope.Instance, moduleInfo))
  }

  /**
    * The registration method is mainly used to notify all RM nodes (including the node), and the instance is offline.
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
    */
  override def unregister(moduleInstance: ServiceInstance): Unit = {
    info(s"Get a module unregister request: $moduleInstance")
    moduleResourceManager.dealModuleUnregisterEvent(new ModuleUnregisterEvent(EventScope.Instance, moduleInstance))
  }

  /**
    * Whether the instance of the module has been overloaded
    *   * TODO is not available when it is larger than the protection resource
    * 是否该module的实例，已经使用超载了
    * TODO 大于保护资源则不可用
    *
    * @param moduleInstance
    * @return
    */
  override def instanceCanService(moduleInstance: ServiceInstance): Boolean = {
    val moduleResourceRecord = moduleResourceRecordService.getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    val leftResource = moduleResourceRecordService.deserialize(moduleResourceRecord.getLeftResource)
    val protectedResource = moduleResourceRecordService.deserialize(moduleResourceRecord.getProtectedResource)
    info(s"Module(模块): $moduleInstance Remaining resources(剩余资源)：${leftResource} Protect resources(保护资源)：${protectedResource}")
    leftResource > protectedResource
  }

  /**
    * Request resources, if not successful, return directly
    * 请求资源，如果不成功，直接返回
    *
    * @param user
    * @param resource
    * @return
    */
  override def requestResource(moduleInstance: ServiceInstance, user: String, creator: String, resource: Resource): ResultResource = {
    info(s"Get resource request of user:$user,info:moduleInstance:$moduleInstance,creator:$creator,resource:$resource")
    //1.Determine if there are still events(判断是否还有事件)
    if (hasModuleInstanceEvent(moduleInstance)) {
      warn(s"There are still unconsumed event of module: $moduleInstance")
      return NotEnoughResource(s"There are still unconsumed event of module:$moduleInstance")
    }
    if (hasUserEvent(user, moduleInstance.getApplicationName)) {
      warn(s"There are still unconsumed event of user: $user")
      return NotEnoughResource(s"There are still unconsumed event of user: $user")
    }
    //2.Can I apply?(是否可以申请)
    val reqService = getRequestResourceService(moduleInstance)
    try {
      if (!reqService.canRequest(moduleInstance, user, creator, resource))
        return NotEnoughResource(s"user：$user not enough resource")
    } catch {
      case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
    }


    //3.Lock engine, lock user(锁引擎，锁用户)
    Utils.tryFinally {
      resourceLockService.tryLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.tryLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
      try {
        if (!reqService.canRequest(moduleInstance, user, creator, resource))
          return NotEnoughResource(s"user：$user not enough resource")
      } catch {
        case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
      }
      //4.Broadcast resource application(广播资源申请)
      val tickedId = UUID.randomUUID().toString
      val userPreUsedResource = UserPreUsedResource(tickedId, moduleInstance, resource)
      userResourceManager.dealUserPreUsedEvent(new UserPreUsedEvent(EventScope.User, user, creator, userPreUsedResource))

      if (RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue > 0) Utils.defaultScheduler.schedule(
        new ClearPreUsedRunnable(new ClearPrdUsedEvent(EventScope.User, user, userPreUsedResource)),
        RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue,
        TimeUnit.MILLISECONDS
      )

      info(s"$user succeed to request resource：$resource")
      AvailableResource(tickedId)
    } {
      //5.Release lock(释放锁)
      resourceLockService.unLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.unLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
    }
  }

  /**
    * Request resources and wait for a certain amount of time until the requested resource is met
    * 请求资源，并等待一定的时间，直到满足请求的资源
    *
    * @param user
    * @param resource
    * @param wait
    * @return
    */
  override def requestResource(moduleInstance: ServiceInstance, user: String, creator: String, resource: Resource, wait: Long): ResultResource = {
    info(s"Get resource request of user:$user,info:moduleInstance:$moduleInstance,creator:$creator,resource:$resource")
    val start = System.currentTimeMillis
    //1.Determine if there are any more events(判断是否还有事件)
    if (hasModuleInstanceEvent(moduleInstance)) {
      warn(s"There are still unconsumed event of module: $moduleInstance")
      return NotEnoughResource(s"There are still unconsumed event of module:$moduleInstance")
    }
    if (hasUserEvent(user, moduleInstance.getApplicationName)) {
      warn(s"There are still unconsumed event of user: $user")
      return NotEnoughResource(s"There are still unconsumed event of user: $user")
    }
    //2.Can I apply?(是否可以申请)
    val reqService = getRequestResourceService(moduleInstance)
    try {
      if (!reqService.canRequest(moduleInstance, user, creator, resource))
        return NotEnoughResource(s"user：$user not enough resource")
    } catch {
      case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
    }

    //3.Lock engine, lock user(锁引擎，锁用户)
    Utils.tryFinally {
      resourceLockService.tryLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance, wait)
      if (System.currentTimeMillis() - start > wait)
        return NotEnoughResource("time out")
      val newWait = wait - (System.currentTimeMillis() - start)
      resourceLockService.tryLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance, newWait)
      if (System.currentTimeMillis() - start > wait)
        return NotEnoughResource("time out")

      try {
        if (!reqService.canRequest(moduleInstance, user, creator, resource))
          return NotEnoughResource(s"user：$user not enough resource")
      } catch {
        case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
      }

      //4.Broadcast resource application(广播资源申请)
      val tickedId = UUID.randomUUID().toString
      val userPreUsedResource = UserPreUsedResource(tickedId, moduleInstance, resource)
      userResourceManager.dealUserPreUsedEvent(new UserPreUsedEvent(EventScope.User, user, creator, userPreUsedResource))

      if (RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue > 0) Utils.defaultScheduler.schedule(
        new ClearPreUsedRunnable(new ClearPrdUsedEvent(EventScope.User, user, userPreUsedResource)),
        RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue,
        TimeUnit.MILLISECONDS
      )

      info(s"$user succeed to request resource：$resource")
      AvailableResource(tickedId)
    } {
      //5.Release lock(释放锁)
      resourceLockService.unLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.unLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
    }
  }

  /**
    * When the resource is instantiated, the total amount of resources actually occupied is returned.
    * 当资源被实例化后，返回实际占用的资源总量
    *
    * @param resource
    *                       In general, resourceReleased will release the resources occupied by the user, but if the process that uses the resource does not have time to call the resourceReleased method to die,
    *                       you need to unregister to release the resource.
    * @param moduleInstance 一般情况下，会由resourceReleased释放用户占用的资源，但是如果该使用资源的进程没来得及调用resourceReleased方法就死掉了，就需要unregister来释放了
    * @param realUsed
    */
  override def resourceInited(resource: ResultResource, moduleInstance: ServiceInstance, realUsed: Resource, engineInstance: ServiceInstance): Unit = {
    val userResultResource = resource.asInstanceOf[UserResultResource]
    val user = userResultResource.user
    //1.Determine if there are any more events(判断是否还有事件)
    if (hasModuleInstanceEvent(moduleInstance)) {
      throw new RMWarnException(111004, s"There are still unconsumed event of module: $moduleInstance")
    }

    if (hasUserEvent(user, moduleInstance.getApplicationName)) {
      throw new RMWarnException(111004, s"There are still unconsumed event of user: $user")
    }

    Utils.tryFinally {
      //2.Lock engine, lock user(锁引擎，锁用户)
      resourceLockService.tryLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.tryLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
      //3.Broadcast resource use(广播资源使用)
      val tickedId = userResultResource.ticketId
      val usedResource: UserUsedResource = UserUsedResource(tickedId, moduleInstance, realUsed, engineInstance)
      val userUsedEvent = new UserUsedEvent(EventScope.User, user, usedResource)
      userResourceManager.dealUserUsedEvent(userUsedEvent)
      //notifyEngine.notifyToAll(new WaitReleasedEvent(new ClearUsedEvent(EventScope.User, user, usedResource), RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue))
    } {
      //4.Release lock(释放锁)
      resourceLockService.unLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.unLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
      info(s"resource: $resource resourceInited Processed(处理完毕)")
    }
  }

  /**
    * Method called when the resource usage is released
    * 当资源使用完成释放后，调用的方法
    *
    * @param resultResource
    */
  override def resourceReleased(resultResource: ResultResource, moduleInstance: ServiceInstance): Unit = {
    val userResultResource = resultResource.asInstanceOf[UserResultResource]
    val user = userResultResource.user
    //1.Determine if there are any more events(判断是否还有事件)
    if (hasModuleInstanceEvent(moduleInstance)) {
      throw new RMWarnException(111004, s"There are still unconsumed event of module: $moduleInstance")
    }

    if (hasUserEvent(user, moduleInstance.getApplicationName)) {
      throw new RMWarnException(111004, s"There are still unconsumed event of user: $user")
    }

    //2.Lock engine, lock user(锁引擎，锁用户)
    Utils.tryFinally {
      resourceLockService.tryLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.tryLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
      //3.Broadcast resource use(广播资源使用)
      val tickedId = userResultResource.ticketId
      val userReleasedResource = UserReleasedResource(tickedId, moduleInstance)
      val userReleasedEvent = new UserReleasedEvent(EventScope.User, user, userReleasedResource)
      userResourceManager.dealUserReleasedEvent(userReleasedEvent)
    } {
      //4.Release lock(释放锁)
      resourceLockService.unLock(null, moduleInstance.getApplicationName, moduleInstance.getInstance)
      resourceLockService.unLock(user, moduleInstance.getApplicationName, moduleInstance.getInstance)
      info(s"resultResource:$resultResource resourceReleased Processed(处理完毕)")
    }
  }

  /**
    * If the IP and port are empty, return the resource status of all modules of a module
    *   * Return the use of this instance resource if there is an IP and port
    * 如果IP 和 端口传空，则返回某个module全部实例的资源情况
    * 如果存在IP 和端口  则返回该实例资源的使用
    *
    * @param moduleInstance
    * @return
    */
  override def getModuleResourceInfo(moduleInstance: ServiceInstance): Array[ModuleResourceInfo] = {
    moduleResourceManager.getModuleResourceInfo(moduleInstance)
  }


  def hasModuleInstanceEvent(moduleInstance: ServiceInstance): Boolean = {
    val groupFactory = rmContext.getScheduler.getSchedulerContext.getOrCreateGroupFactory.asInstanceOf[EventGroupFactory]
    val groupName = groupFactory.getGroupNameByModule(moduleInstance)
    val consumer = rmContext.getScheduler.getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val hasRunning = consumer.getRunningEvents.exists {
      case registerEvent: ModuleRegisterEvent => registerEvent.moduleName == moduleInstance.getApplicationName
      case unregisterEvent: ModuleUnregisterEvent => unregisterEvent.moduleName == moduleInstance.getApplicationName
      case _ => false
    }
    if (hasRunning) return true
    val hasWaiting = consumer.getConsumeQueue.getWaitingEvents.exists {
      case registerEvent: ModuleRegisterEvent => registerEvent.moduleName == moduleInstance.getApplicationName
      case unregisterEvent: ModuleUnregisterEvent => unregisterEvent.moduleName == moduleInstance.getApplicationName
      case _ => false
    }
    hasWaiting
  }

  def hasUserEvent(user: String, moduleName: String): Boolean = {
    val groupFactory = rmContext.getScheduler.getSchedulerContext.getOrCreateGroupFactory.asInstanceOf[EventGroupFactory]
    val groupName = groupFactory.getGroupNameByUser(user)
    val consumer = rmContext.getScheduler.getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val hasRunning = consumer.getRunningEvents.exists {
      case userPreUsedEvent: UserPreUsedEvent => userPreUsedEvent.moduleName == moduleName && userPreUsedEvent.user == user
      case userUsedEvent: UserUsedEvent => userUsedEvent.moduleName == moduleName && userUsedEvent.user == user
      case userReleasedEvent: UserReleasedEvent => userReleasedEvent.moduleName == moduleName && userReleasedEvent.user == user
      case clearPrdUsedEvent: ClearPrdUsedEvent => clearPrdUsedEvent.moduleName == moduleName && clearPrdUsedEvent.user == user
      case clearUsedEvent: ClearUsedEvent => clearUsedEvent.moduleName == moduleName && clearUsedEvent.user == user
      case _ => false
    }
    if (hasRunning) return true
    val hasWaiting = consumer.getConsumeQueue.getWaitingEvents.exists {
      case userPreUsedEvent: UserPreUsedEvent => userPreUsedEvent.moduleName == moduleName && userPreUsedEvent.user == user
      case userUsedEvent: UserUsedEvent => userUsedEvent.moduleName == moduleName && userUsedEvent.user == user
      case userReleasedEvent: UserReleasedEvent => userReleasedEvent.moduleName == moduleName && userReleasedEvent.user == user
      case clearPrdUsedEvent: ClearPrdUsedEvent => clearPrdUsedEvent.moduleName == moduleName && clearPrdUsedEvent.user == user
      case clearUsedEvent: ClearUsedEvent => clearUsedEvent.moduleName == moduleName && clearUsedEvent.user == user
      case _ => false
    }
    hasWaiting
  }

  def getRequestResourceService(moduleInstance: ServiceInstance): RequestResourceService = {
    //2.Can I apply?(是否可以申请)
    val requestResourceService = moduleResourceRecordService.getModulePolicy(moduleInstance.getApplicationName) match {
      case Yarn => requestResourceServices.find(requestResourceService => requestResourceService != null && requestResourceService.requestPolicy == Yarn)
      case DriverAndYarn => requestResourceServices.find(requestResourceService => requestResourceService != null && requestResourceService.requestPolicy == DriverAndYarn)
      case _ => requestResourceServices.find(requestResourceService => requestResourceService != null && requestResourceService.requestPolicy == Default)
    }
    if (requestResourceService.isEmpty)
      new RMErrorException(111004, s"The module：$moduleInstance not request Resource Service")
    requestResourceService.get
  }

  class ClearPreUsedRunnable(clearPrdUsedEvent: ClearPrdUsedEvent) extends Runnable {
    override def run(): Unit = {
      val userResourceRecord = userResourceRecordService.getUserModuleRecord(clearPrdUsedEvent.user, clearPrdUsedEvent.ticketId)
      if (userResourceRecord != null && userResourceRecord.getUserUsedResource == null) {
        userResourceManager.dealClearPrdUsedEvent(clearPrdUsedEvent)
      }
    }
  }

}
