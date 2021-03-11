/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.label.service.impl

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.node.ScoreServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.Label.ValueRelation
import com.webank.wedatasphere.linkis.manager.label.entity.{Feature, Label}
import com.webank.wedatasphere.linkis.manager.label.score.NodeLabelScorer
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.persistence.LabelManagerPersistence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


@Service
class DefaultNodeLabelService extends NodeLabelService with Logging {

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  @Autowired
  private var nodeLabelScorer: NodeLabelScorer = _
  /**
    * Attach labels to node instance
    * TODO 该方法需要优化,应该batch插入
    *
    * @param instance node instance
    * @param labels   label list
    */
  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def addLabelsToNode( instance: ServiceInstance, labels: util.List[Label[_]]): Unit = {
    if (null != labels && !labels.isEmpty) labels.foreach(addLabelToNode(instance, _))
  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def addLabelToNode(instance: ServiceInstance, label: Label[_]): Unit = {
    // TODO: 上层应该有个初始化清理表格的方法
    val persistenceLabel = labelFactory.convertLabel(label, classOf[PersistenceLabel])
    //Try to add
    tryToAddLabel(persistenceLabel);
    //2.查询出当前label的id
    val dbLabel = labelManagerPersistence.getLabelByKeyValue(persistenceLabel.getLabelKey, persistenceLabel.getStringValue)
    //3.根据instance 找出当前关联当前isntance的所有labels,看下有没和当前id重复的
    //2020/09/14 如果已经存在关联，则不关联
    //TODO 对于这样的记录查询，插入时有并发问题，可以考虑提供getLabelByServiceInstanceAndId方法，配合for update关键字，使用间隙锁
    val serviceRelationLabels = labelManagerPersistence.getLabelByServiceInstance(instance)
    if (!serviceRelationLabels.exists(_.getId.equals(dbLabel.getId))){
      //5.插入新的relation 需要抛出duplicateKey异常，回滚
      labelManagerPersistence.addLabelToNode(instance, util.Arrays.asList(dbLabel.getId))
    }
  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def updateLabelToNode(instance: ServiceInstance, label: Label[_]): Unit = {
    val persistenceLabel = this.labelFactory.convertLabel(label, classOf[PersistenceLabel])
    //Try to add
    tryToAddLabel(persistenceLabel)
    val dbLabel = labelManagerPersistence.getLabelByKeyValue(persistenceLabel.getLabelKey, persistenceLabel.getStringValue)
    //TODO: add method: getLabelsByServiceInstanceAndKey(instance, labelKey)
    val nodeLabels = this.labelManagerPersistence.getLabelByServiceInstance(instance)
    var needUpdate = true
    nodeLabels.filter(_.getLabelKey.equals(label.getLabelKey)).foreach( nodeLabel =>{
      if(nodeLabel.getId.equals(dbLabel.getId)){
        needUpdate = false
      }else{
        //TODO: add batch method: removeLabels(Ids)
        this.labelManagerPersistence.removeLabel(nodeLabel.getId)
      }
    })
    if (needUpdate) {
      this.labelManagerPersistence.addLabelToNode(instance, util.Arrays.asList())
    }
  }
  /**
   * Remove the labels related by node instance
   *
   * @param instance node instance
   * @param labels   labels
   */
  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def removeLabelsFromNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit = {
    //这里前提是表中保证了同个key，只会有最新的value保存在数据库中
    val dbLabels = labelManagerPersistence.getLabelByServiceInstance(instance).map(l => (l.getLabelKey, l)).toMap
    labelManagerPersistence.removeNodeLabels(instance, labels.map(l => dbLabels(l.getLabelKey).getId))
  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def removeLabelsFromNode(instance: ServiceInstance): Unit = {
    labelManagerPersistence.removeNodeLabels(instance, labelManagerPersistence.getLabelByServiceInstance(instance).map(_.getId))
  }

  /**
   * Get node instances by labels
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  override def getNodesByLabels(labels: util.List[Label[_]]): util.List[ServiceInstance] = {
    labels.flatMap(getNodesByLabel).distinct
  }

  override def getNodesByLabel(label: Label[_]): util.List[ServiceInstance] = {
    labelManagerPersistence.getNodeByLabelKeyValue(label.getLabelKey,label.getStringValue).distinct
  }

  override def getNodeLabels(instance: ServiceInstance): util.List[Label[_]] = {
    labelManagerPersistence.getLabelByServiceInstance(instance).map { label =>
      val realyLabel:Label[_] = labelFactory.createLabel(label.getLabelKey, label.getValue)
      realyLabel
    }
  }

  /**
    * Get scored node instances
    *
    * @param labels searchableLabel or other normal labels
    * @return
    */
  override def getScoredNodesByLabels(labels: util.List[Label[_]]): util.List[ScoreServiceInstance] = {
    getScoredNodeMapsByLabels(labels).map(_._1).toList
  }
  override def getScoredNodeMapsByLabels(labels: util.List[Label[_]]): util.Map[ScoreServiceInstance, util.List[Label[_]]] = {
    //Try to convert the label list to key value list
    val kvList = labels.map( label => Option(labelFactory.convertLabel(label, classOf[PersistenceLabel])))
      .filter(option => option.isDefined && Option(option.get.getValue).isDefined).map(_.get.getValue).toList
    if (kvList.nonEmpty) {
      //Get the persistence labels by kvList
      val persistenceLabels = labelManagerPersistence.getLabelsByValueList(kvList, ValueRelation.ALL)

      if (null != persistenceLabels && persistenceLabels.nonEmpty) {
        val labelsFromDB = persistenceLabels.map(label => labelFactory.createLabel(label.getLabelKey, label.getValue).asInstanceOf[Label[_]])
        val requireLabels = labels.filter(_.getFeature == Feature.CORE)
        val oldSize = requireLabels.size
        val newSize = requireLabels.count(label => labelsFromDB.exists { dbLabel =>
          dbLabel.getLabelKey.equals(label.getLabelKey) && label.getStringValue.equalsIgnoreCase(dbLabel.getStringValue)
        })
        if (oldSize != newSize) return new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
        //Extra the necessary labels whose feature equals Feature.CORE or Feature.SUITABLE
        val necessaryLabels = persistenceLabels.filter(label => requireLabels.exists(_.getLabelKey.equalsIgnoreCase(label.getLabelKey))).toList
        return getScoredNodeMapsByLabels(persistenceLabels, necessaryLabels)
      }

    }
    new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
  }

  private def getScoredNodeMapsByLabels(labels: util.List[PersistenceLabel],
                                        necessaryLabels: util.List[PersistenceLabel]): util.Map[ScoreServiceInstance, util.List[Label[_]]] = {
    //Get the in-degree relations ( Label -> Node )
    val inNodeDegree = labelManagerPersistence.getNodeRelationsByLabels(if (necessaryLabels.nonEmpty) necessaryLabels else labels)
    if (inNodeDegree.isEmpty) {
      return new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
    }
    val instanceLabels = new mutable.HashMap[ServiceInstance, ArrayBuffer[Label[_]]]()
    inNodeDegree.foreach { keyValue =>
      keyValue._2.foreach { instance =>
        if (!instanceLabels.contains(instance)) {
          instanceLabels.put(instance, new ArrayBuffer[Label[_]]())
        }
        val labelList = instanceLabels.get(instance)
        labelList.get.add(keyValue._1)
      }
    }
    val instances = if (necessaryLabels.nonEmpty) {
      //Cut the in-degree relations, drop inconsistent nodes
      instanceLabels.filter(entry => entry._2.size >= necessaryLabels.size).keys
    } else {
      instanceLabels.keys
    }
    //Get the out-degree relations ( Node -> Label )
    val outNodeDegree = labelManagerPersistence.getLabelRelationsByServiceInstance(instances.toList)
    //outNodeDegree cannot be empty
    if (outNodeDegree.nonEmpty) {
      //Rebuild in-degree relations
       inNodeDegree.clear()
       outNodeDegree.foreach{
         case(node, labels) =>
           labels.foreach( label => {
             if (!inNodeDegree.contains(label)) {
               val inNodes = new util.ArrayList[ServiceInstance]()
               inNodeDegree.put(label, inNodes)
             }
             val inNodes = inNodeDegree.get(label)
             inNodes.add(node)
           })
       }
       return nodeLabelScorer.calculate( inNodeDegree, outNodeDegree, labels).asInstanceOf[util.Map[ScoreServiceInstance, util.List[Label[_]]]]
    }
    new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
  }

  private def tryToAddLabel(persistenceLabel: PersistenceLabel): Unit = {
    //1.插入linkis_manager_label 表，这里表应该有唯一约束，key和valueStr　调用Persistence的addLabel即可，忽略duplicateKey异常
    persistenceLabel.setLabelValueSize(persistenceLabel.getValue.size())
    Utils.tryCatch(labelManagerPersistence.addLabel(persistenceLabel)) { t: Throwable =>
      warn(s"Failed to add label ${t.getMessage}")
    }
  }
}
