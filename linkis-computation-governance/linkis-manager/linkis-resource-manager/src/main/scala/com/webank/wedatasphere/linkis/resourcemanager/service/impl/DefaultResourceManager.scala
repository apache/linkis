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

package com.webank.wedatasphere.linkis.resourcemanager.service.impl

import java.util
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEMNode, AMEngineNode, InfoRMNode}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, NodeResource, Resource, ResourceType}
import com.webank.wedatasphere.linkis.manager.common.utils.ResourceUtils
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.{Label, ResourceLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.persistence.{NodeManagerPersistence, ResourceManagerPersistence}
import com.webank.wedatasphere.linkis.resourcemanager._
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer
import com.webank.wedatasphere.linkis.resourcemanager.exception.{RMErrorException, RMWarnException}
import com.webank.wedatasphere.linkis.resourcemanager.external.service.ExternalResourceService
import com.webank.wedatasphere.linkis.resourcemanager.protocol.{TimeoutEMEngineRequest, TimeoutEMEngineResponse}
import com.webank.wedatasphere.linkis.resourcemanager.service._
import com.webank.wedatasphere.linkis.resourcemanager.utils.{RMConfiguration, RMUtils}
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class DefaultResourceManager extends ResourceManager with Logging with InitializingBean {

  @Autowired
  var resourceManagerPersistence : ResourceManagerPersistence = _

  @Autowired
  var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  var resourceLockService: ResourceLockService = _

  @Autowired
  var labelResourceService: LabelResourceService = _

  @Autowired
  var externalResourceService: ExternalResourceService = _

  @Autowired
  var resourceLogService: ResourceLogService = _

  /*@Autowired
  var nodeHeartbeatMonitor: NodeHeartbeatMonitor = _*/

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  var requestResourceServices: Array[RequestResourceService] = _

  override def afterPropertiesSet(): Unit = {
    requestResourceServices = Array(
      new DefaultReqResourceService(labelResourceService),
      new DriverAndYarnReqResourceService(labelResourceService, externalResourceService)
    )
    // submit force release timeout lock job
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        info("Start force release timeout locks")
        resourceLockService.clearTimeoutLock(RMConfiguration.LOCK_RELEASE_TIMEOUT.getValue.toLong)
      }
    }, RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue.toLong, RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
    // submit NodeHeartbeatMonitor job
    //Utils.defaultScheduler.scheduleAtFixedRate(nodeHeartbeatMonitor, RMConfiguration.NODE_HEARTBEAT_INTERVAL.getValue.toLong, RMConfiguration.NODE_HEARTBEAT_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  /**
    * The registration method is mainly used to notify all RM nodes (including the node)
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
    *
    *
    */
  override def register(serviceInstance: ServiceInstance, resource: NodeResource): Unit = {
    info(s"Start processing registration of ServiceInstance: ${serviceInstance}")
    val eMInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName)
    eMInstanceLabel.setInstance(serviceInstance.getInstance)
    val labelContainer = new RMLabelContainer(Lists.newArrayList(eMInstanceLabel))

    Utils.tryFinally {
      // lock labels
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          labelContainer.setCurrentLabel(label.asInstanceOf[Label[_]])
          resourceLockService.tryLock(labelContainer)
        case _ =>
      }
      val emResource = labelResourceService.getLabelResource(eMInstanceLabel)
      if(emResource != null){
        warn(s"${serviceInstance} has been registered, now update resource.")
        if(!emResource.getResourceType.equals(resource.getResourceType)){
          throw new RMErrorException(110019, s"${serviceInstance} has been registered in ${emResource.getResourceType}, cannot be updated to ${resource.getResourceType}")
        }
      }
      // TODO get ID Label set label resource
      Utils.tryCatch{
        labelResourceService.setLabelResource(eMInstanceLabel, resource)
        resourceLogService.success(ChangeType.ECM_INIT, null, eMInstanceLabel)
      }{
        case exception: Exception => {
          resourceLogService.failed(ChangeType.ECM_INIT, null, eMInstanceLabel, exception)
        }
        case _ =>
      }
    } {
      //5.Release lock(释放锁)
      resourceLockService.unLock(labelContainer)
      info(s"finished processing registration of ${serviceInstance}")
    }
  }

  /**
    * The registration method is mainly used to notify all RM nodes (including the node), and the instance is offline.
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
    */
  override def unregister(serviceInstance: ServiceInstance): Unit = {
    // TODO get ID Label
    val eMInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName)
    eMInstanceLabel.setInstance(serviceInstance.getInstance)
    val labelContainer = new RMLabelContainer(Lists.newArrayList(eMInstanceLabel))

    Utils.tryFinally {
      // No need to lock for ECM unregistration
//      labelContainer.getLabels.toArray.foreach {
//        case label: Label[_] =>
//          labelContainer.setCurrentLabel(label.asInstanceOf[Label[_]])
//          resourceLockService.tryLock(labelContainer)
//        case _ =>
//      }

      // clear EM related engine resource records
      Utils.tryCatch{
        nodeManagerPersistence.getEngineNodeByEM(serviceInstance).foreach{ node =>
          val engineInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
          engineInstanceLabel.setInstance(node.getServiceInstance.getInstance)
          engineInstanceLabel.setServiceName(node.getServiceInstance.getApplicationName)
          labelResourceService.removeResourceByLabel(engineInstanceLabel)
        }
        labelResourceService.removeResourceByLabel(eMInstanceLabel)
        labelContainer.setCurrentLabel(eMInstanceLabel)
        resourceLockService.unLock(labelContainer)
        resourceLogService.success(ChangeType.ECM_CLEAR, null, eMInstanceLabel)
      }{
        case exception: Exception => {
          resourceLogService.failed(ChangeType.ECM_CLEAR, null, eMInstanceLabel, exception)
        }
        case _ =>
      }

    } {
      //5.Release lock(释放锁)
      //resourceLockService.unLock(labelContainer)
      info(s"ECMResourceClear:${serviceInstance}, usedResource:${Resource.initResource(ResourceType.Default).toJson}")
      info(s"finished processing unregistration of ${serviceInstance}")
    }
  }

  /**
    * Request resources, if not successful, return directly
    * 请求资源，如果不成功，直接返回
    *
    * @param labels
    * @param resource
    * @return
    */
  override def requestResource(labels: util.List[Label[_]], resource: NodeResource): ResultResource = {
    requestResource(labels, resource, -1)
  }

  /**
   * Request resources and wait for a certain amount of time until the requested resource is met
   * 请求资源，并等待一定的时间，直到满足请求的资源
   *
   * @param labels
   * @param resource
   * @param wait
   * @return
   */
  override def requestResource(labels: util.List[Label[_]], resource: NodeResource, wait: Long): ResultResource = {
    val labelContainer = labelResourceService.enrichLabels(new RMLabelContainer(labels))
    debug("start processing request resource for labels [" + labelContainer + "] and resource [" + resource + "]")
    // pre-check without lock
    val requestResourceService = getRequestResourceService(resource.getResourceType)
    try {
      labelContainer.getResourceLabels.foreach{ label =>
        labelContainer.setCurrentLabel(label)
        if (!requestResourceService.canRequest(labelContainer, resource)){
          return NotEnoughResource(s"Labels：$labels not enough resource")
        }
      }
    } catch {
      case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
    }

    Utils.tryFinally {
      // lock labels
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          labelContainer.setCurrentLabel(label)
          val locked = if(wait <= 0){
            resourceLockService.tryLock(labelContainer)
          } else {
            resourceLockService.tryLock(labelContainer, wait)
          }
          if (!locked){
            return NotEnoughResource("Wait for lock time out")
          }

          // check again with lock
          try {
            if (!requestResourceService.canRequest(labelContainer, resource))
              return NotEnoughResource(s"Labels：$labels not enough resource")
          } catch {
            case warn: RMWarnException => return NotEnoughResource(warn.getMessage)
          }
        case _ =>
      }

      resource.setLockedResource(resource.getMinResource)
      // lock label resources
      val labelResourceList = new util.HashMap[String, NodeResource]()
      labelContainer.getResourceLabels.foreach(label => {
        val usedResource = labelResourceService.getLabelResource(label)
        if(usedResource == null){
          info(s"Resource label: ${label.getStringValue} has no usedResource, please check, refuse request usedResource！" +
            s"(资源标签:${label.getStringValue}),缺少对应的资源记录，可能是该标签的资源没有初始化，请及时检查，此次申请资源失败！")
          throw new RMErrorException(110022, s"Resource label: ${label.getStringValue} has no usedResource, please check, refuse request usedResource！" +
            s"(资源标签:${label.getStringValue}),缺少对应的资源记录，可能是该标签的资源没有初始化，请及时检查，此次申请资源失败！")
        }
        labelResourceList.put(label.getStringValue, usedResource)
      })
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          val labelResource = labelResourceList.get(label.getStringValue)
          if(labelResource != null){
            labelResource.setLeftResource(labelResource.getLeftResource - resource.getLockedResource)
            labelResource.setLockedResource(labelResource.getLockedResource + resource.getLockedResource)
            labelResourceService.setLabelResource(label, labelResource)
            info(s"ResourceChanged:${label.getStringValue} --> ${labelResource}")
          }
        case _ =>
      }

      // record engine locked resource
      val tickedId = UUID.randomUUID().toString
      val emNode = new AMEMNode
      emNode.setServiceInstance(labelContainer.getEMInstanceLabel.getServiceInstance)
      val engineNode = new AMEngineNode
      engineNode.setEMNode(emNode)
      engineNode.setServiceInstance(ServiceInstance(labelContainer.getEngineServiceName, tickedId))
      engineNode.setNodeResource(resource)
      nodeManagerPersistence.addEngineNode(engineNode)

      val engineInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
      engineInstanceLabel.setServiceName(labelContainer.getEngineServiceName)
      engineInstanceLabel.setInstance(tickedId)

      nodeLabelService.addLabelToNode(engineNode.getServiceInstance,engineInstanceLabel)

      labelResourceService.setEngineConnLabelResource(engineInstanceLabel, resource)

      // fire timeout check scheduled job
      if (RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue > 0) Utils.defaultScheduler.schedule(
        new UnlockTimeoutResourceRunnable(labels, engineInstanceLabel),
        RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue,
        TimeUnit.MILLISECONDS
      )
      AvailableResource(tickedId)
    } {
      //5.Release lock(释放锁)
      resourceLockService.unLock(labelContainer)
      debug(s"finished processing requestResource of labels ${labels} and resource ${resource}")
    }
  }

  /**
   * When the resource is instantiated, the total amount of resources actually occupied is returned.
   * 当资源被实例化后，返回实际占用的资源总量
   *
   * @param labels
   * In general, resourceReleased will release the resources occupied by the user, but if the process that uses the resource does not have time to call the resourceReleased method to die, you need to unregister to release the resource.
   * @param usedResource
   */
  override def resourceUsed(labels: util.List[Label[_]], usedResource: NodeResource): Unit = {
    val labelContainer = labelResourceService.enrichLabels(new RMLabelContainer(labels))
    var lockedResource: NodeResource = null
    try {
      lockedResource = labelResourceService.getLabelResource(labelContainer.getEngineInstanceLabel)
    } catch {
      case e: NullPointerException  =>
        error(s"EngineInstanceLabel [${labelContainer.getEngineInstanceLabel}] cause NullPointerException")
        throw e
    }
    if (lockedResource == null) {
      throw new RMErrorException(110021, s"No locked resource found by engine ${labelContainer.getEngineInstanceLabel}")
    }
    /*if (!lockedResource.getLockedResource.equalsTo(usedResource.getUsedResource)) {
      throw new RMErrorException(110022, s"Locked ${lockedResource.getLockedResource}, but want to use ${usedResource.getUsedResource}")
    }*/
    info(s"resourceUsed ready:${labelContainer.getEMInstanceLabel.getServiceInstance}, used resource ${lockedResource.getLockedResource}")
    val addedResource = Resource.initResource(lockedResource.getResourceType) + lockedResource.getLockedResource
    Utils.tryFinally {
      // lock labels
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          labelContainer.setCurrentLabel(label.asInstanceOf[Label[_]])
          resourceLockService.tryLock(labelContainer)
        case _ =>
      }
      labelContainer.getResourceLabels.foreach {
        case engineInstanceLabel: EngineInstanceLabel =>
          Utils.tryCatch{
            lockedResource.setUsedResource(lockedResource.getLockedResource)
            lockedResource.setLockedResource(Resource.getZeroResource(lockedResource.getLockedResource))
            labelResourceService.setLabelResource(engineInstanceLabel, lockedResource)
            resourceLogService.success(ChangeType.ENGINE_INIT, engineInstanceLabel)
          }{
            case exception: Exception => {
              resourceLogService.failed(ChangeType.ENGINE_INIT, engineInstanceLabel, null, exception)
              throw exception
            }
            case _ =>
          }
        case label: Label[_] =>
          Utils.tryCatch{
            val labelResource = labelResourceService.getLabelResource(label)
            if(labelResource != null){
              labelResource.setLockedResource(labelResource.getLockedResource - addedResource)
              if(null == labelResource.getUsedResource) labelResource.setUsedResource(Resource.initResource(labelResource.getResourceType))
              labelResource.setUsedResource(labelResource.getUsedResource + addedResource)
              labelResourceService.setLabelResource(label, labelResource)
              if(label.isInstanceOf[EMInstanceLabel]){
                resourceLogService.success(ChangeType.ECM_RESOURCE_ADD, null, label.asInstanceOf[EMInstanceLabel])
              }
            }
          }{
            case exception: Exception => {
              if(label.isInstanceOf[EMInstanceLabel]){
                resourceLogService.failed(ChangeType.ECM_RESOURCE_ADD, null, label.asInstanceOf[EMInstanceLabel], exception)
              }
            }
          }
        case _ =>
      }
    }{
      resourceLockService.unLock(labelContainer)
    }
  }

  def timeCheck(labelResource: NodeResource, usedResource: NodeResource) = {
    if(labelResource.getCreateTime != null && usedResource.getCreateTime != null){
      if(labelResource.getCreateTime.getTime > usedResource.getCreateTime.getTime){
        throw new RMErrorException(10022,s"no need to clear this labelResource, labelResource:${labelResource} created time is after than usedResource:${usedResource}" +
          s"无需清理该标签的资源，该标签资源的创建时间晚于已用资源的创建时间")
      }
    }
  }

  /**
   * Method called when the resource usage is released
   * 当资源使用完成释放后，调用的方法
   *
   * @param labels
   */
  override def resourceReleased(labels: util.List[Label[_]]): Unit = {
    val labelContainer = labelResourceService.enrichLabels(new RMLabelContainer(labels))
    val usedResource = labelResourceService.getLabelResource(labelContainer.getEngineInstanceLabel)
    if(usedResource == null){
      throw new RMErrorException(110021, s"No used resource found by engine ${labelContainer.getEngineInstanceLabel}")
    }
    info(s"resourceRelease ready:${labelContainer.getEngineInstanceLabel.getServiceInstance},current node resource${usedResource}")
    Utils.tryFinally {
      // lock labels
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          labelContainer.setCurrentLabel(label.asInstanceOf[Label[_]])
          resourceLockService.tryLock(labelContainer)
        case _ =>
      }
      val tmpLabel = labelContainer.getLabels.find(_.isInstanceOf[EngineInstanceLabel]).getOrElse(null)
      if(tmpLabel != null){
        val engineInstanceLabel = tmpLabel.asInstanceOf[EngineInstanceLabel]
        Utils.tryCatch {
          labelResourceService.removeResourceByLabel(engineInstanceLabel)
          resourceLogService.success(ChangeType.ENGINE_CLEAR, engineInstanceLabel,null)
        }{
          case exception: Exception => {
            resourceLogService.failed(ChangeType.ENGINE_CLEAR, engineInstanceLabel, null, exception)
            throw exception
          }
          case _ =>
        }
      }
      labelContainer.getResourceLabels.foreach {
        case label: Label[_] =>
          Utils.tryCatch{
            val labelResource = labelResourceService.getLabelResource(label)
            if(labelResource != null){
              timeCheck(labelResource, usedResource)
              if(null != usedResource.getUsedResource){
                labelResource.setUsedResource(labelResource.getUsedResource - usedResource.getUsedResource)
                labelResource.setLeftResource(labelResource.getLeftResource + usedResource.getUsedResource)
              }
              if(null != usedResource.getLockedResource){
                labelResource.setLockedResource(labelResource.getLockedResource - usedResource.getLockedResource)
                labelResource.setLeftResource(labelResource.getLeftResource + usedResource.getLockedResource)
              }
              labelResourceService.setLabelResource(label, labelResource)
              if(label.isInstanceOf[EMInstanceLabel]){
                resourceLogService.success(ChangeType.ECM_Resource_MINUS, null, label.asInstanceOf[EMInstanceLabel])
              }
            }
          }{
            case exception: Exception => {
              if(label.isInstanceOf[EMInstanceLabel]){
                resourceLogService.failed(ChangeType.ECM_Resource_MINUS, null, label.asInstanceOf[EMInstanceLabel], exception)
              }
            }
            case _ =>
          }
        case _ =>
      }
    } {
      resourceLockService.unLock(labelContainer)
    }
  }

  /**
   * If the IP and port are empty, return the resource status of all modules of a module
   *   * Return the use of this instance resource if there is an IP and port
   *
   * @param serviceInstances
   * @return
   */



  override def getResourceInfo(serviceInstances: Array[ServiceInstance]): ResourceInfo = {
    val resourceInfo = new ResourceInfo(Lists.newArrayList())
    serviceInstances.map { serviceInstance =>
      val rmNode = new InfoRMNode
      var aggregatedResource: NodeResource = null
      serviceInstance.getApplicationName match {
        case GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue =>{
          val engineInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
          engineInstanceLabel.setServiceName(serviceInstance.getApplicationName)
          engineInstanceLabel.setInstance(serviceInstance.getInstance)
          aggregatedResource = labelResourceService.getLabelResource(engineInstanceLabel)
        }
        case GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue =>{
          val emInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
          emInstanceLabel.setServiceName(serviceInstance.getApplicationName)
          emInstanceLabel.setInstance(serviceInstance.getInstance)
          aggregatedResource = labelResourceService.getLabelResource(emInstanceLabel)
        }
      }
      rmNode.setServiceInstance(serviceInstance)
      rmNode.setNodeResource(aggregatedResource)
      resourceInfo.resourceInfo.add(rmNode)
    }
    resourceInfo
  }

  def getRequestResourceService(resourceType: ResourceType): RequestResourceService = {
    val requestResourceService = requestResourceServices.find(_.resourceType == resourceType)
    requestResourceService.getOrElse(requestResourceServices.find(_.resourceType == ResourceType.Default).get)
  }

  class UnlockTimeoutResourceRunnable(labels: util.List[Label[_]], engineInstanceLabel: EngineInstanceLabel) extends Runnable {
    override def run(): Unit = {
      info(s"check locked resource of ${engineInstanceLabel}")
      val resource = labelResourceService.getLabelResource(engineInstanceLabel)
      if(resource == null){
        info(s"${engineInstanceLabel} has no resource")
      }
      info(s"${engineInstanceLabel} has resource")
      if(resource != null
        && resource.getLockedResource != null
        && resource.getLockedResource > Resource.getZeroResource(resource.getLockedResource)){
        info(s"start to unlock timeout resource for ${engineInstanceLabel}")
        val labelContainer = labelResourceService.enrichLabels(new RMLabelContainer(labels))
        val timeoutEMEngineRequest = new TimeoutEMEngineRequest
        timeoutEMEngineRequest.setTicketId(engineInstanceLabel.getInstance())
        timeoutEMEngineRequest.setUser(labelContainer.getUserCreatorLabel.getUser)
        val timeoutEMEngineResponse = Sender.getSender(labelContainer.getEMInstanceLabel.getServiceInstance).ask(timeoutEMEngineRequest).asInstanceOf[TimeoutEMEngineResponse]
        if(timeoutEMEngineResponse.getCanReleaseResource){
          Utils.tryFinally {
            // lock labels
            labelContainer.getResourceLabels.foreach {
              case label: Label[_] =>
                labelContainer.setCurrentLabel(label.asInstanceOf[Label[_]])
                resourceLockService.tryLock(labelContainer)
              case _ =>
            }

            labelContainer.getResourceLabels.foreach {
              case engineInstanceLabel: EngineInstanceLabel =>
                Utils.tryCatch{
                  labelResourceService.removeResourceByLabel(engineInstanceLabel)
                  resourceLogService.success(ChangeType.ENGINE_CLEAR, engineInstanceLabel, null)
                }{
                  case exception: Exception => {
                    resourceLogService.failed(ChangeType.ENGINE_CLEAR,engineInstanceLabel, null, exception)
                    throw exception
                  }
                  case _ =>
                }
              case emInstanceLabel: EMInstanceLabel =>
                Utils.tryCatch{
                  val labelResource = labelResourceService.getLabelResource(emInstanceLabel)
                  if(labelResource != null){
                    labelResource.setLockedResource(labelResource.getLockedResource - resource.getLockedResource)
                    labelResource.setLeftResource(labelResource.getLeftResource + resource.getLockedResource)
                    labelResourceService.setLabelResource(emInstanceLabel, labelResource)
                    resourceLogService.success(ChangeType.ECM_Resource_MINUS, null, emInstanceLabel)
                  }else{
                    resourceLogService.failed(ChangeType.ECM_Resource_MINUS, null, emInstanceLabel)
                  }
                }{
                  case exception: Exception => {
                    resourceLogService.failed(ChangeType.ECM_Resource_MINUS, null, emInstanceLabel, exception)
                  }
                  case _ =>
                }
              case _ =>
            }
          } {
            resourceLockService.unLock(labelContainer)
            info(s"Timeout resource unlocked for ${engineInstanceLabel}")
          }
        } else {
          askAgainAfter(timeoutEMEngineResponse.getNextAskInterval)
        }
      }
      info(s"finished check locked resource of ${engineInstanceLabel}")
    }

    private def askAgainAfter(interval: Long): Unit ={
      val realInterval = if (interval == null) RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue else interval
      Utils.defaultScheduler.schedule(
        new UnlockTimeoutResourceRunnable(labels, engineInstanceLabel),
        realInterval,
        TimeUnit.MILLISECONDS
      )
      info(s"delayed resource unlocked for ${engineInstanceLabel}")
    }
  }

  override def resourceReport(labels: util.List[Label[_]], reportResource: NodeResource): Unit = {
    //TODO

  }
}
