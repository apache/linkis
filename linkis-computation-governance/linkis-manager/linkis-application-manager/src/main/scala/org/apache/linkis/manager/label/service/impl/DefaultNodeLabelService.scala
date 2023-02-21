/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.label.service.impl

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.label.LabelManagerUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.conf.LabelManagerConf
import org.apache.linkis.manager.label.entity.{Feature, Label}
import org.apache.linkis.manager.label.score.NodeLabelScorer
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.label.utils.LabelUtils
import org.apache.linkis.manager.persistence.LabelManagerPersistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

import java.util
import java.util.List
import java.util.stream.Collectors

import scala.collection.JavaConverters._
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
   * Attach labels to node instance TODO 该方法需要优化,应该batch插入
   *
   * @param instance
   *   node instance
   * @param labels
   *   label list
   */
  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def addLabelsToNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit = {
    if (null != labels && !labels.isEmpty) labels.asScala.foreach(addLabelToNode(instance, _))
  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def addLabelToNode(instance: ServiceInstance, label: Label[_]): Unit = {
    val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
    // Try to add
    val labelId = tryToAddLabel(persistenceLabel)
    if (labelId > 0) {
      val serviceRelationLabels = labelManagerPersistence.getLabelByServiceInstance(instance)
      if (!serviceRelationLabels.asScala.exists(_.getId.equals(labelId))) {
        labelManagerPersistence.addLabelToNode(instance, util.Arrays.asList(labelId))
      }
    }

  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def updateLabelToNode(instance: ServiceInstance, label: Label[_]): Unit = {
    val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
    // Try to add
    val labelId = tryToAddLabel(persistenceLabel)
    if (labelId <= 0) return
    // val dbLabel = labelManagerPersistence.getLabelByKeyValue(persistenceLabel.getLabelKey, persistenceLabel.getStringValue)
    // TODO: add method: getLabelsByServiceInstanceAndKey(instance, labelKey)
    val nodeLabels = this.labelManagerPersistence.getLabelByServiceInstance(instance)
    var needUpdate = true
    val needRemoveIds = new java.util.ArrayList[Integer]()
    nodeLabels.asScala
      .filter(_.getLabelKey.equals(label.getLabelKey))
      .foreach(nodeLabel => {
        if (nodeLabel.getId.equals(labelId)) {
          needUpdate = false
        } else {
          needRemoveIds.add(nodeLabel.getId)
        }
      })
    if (null != needRemoveIds && needRemoveIds.asScala.nonEmpty) {
      this.labelManagerPersistence.removeNodeLabels(instance, needRemoveIds)
    }
    if (needUpdate) {
      val labelIds = new util.ArrayList[Integer]()
      labelIds.add(labelId)
      this.labelManagerPersistence.addLabelToNode(instance, labelIds)
    }
  }

  override def updateLabelsToNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit = {
    val newKeyList = labels.asScala.map(label => label.getLabelKey)
    val nodeLabels = labelManagerPersistence.getLabelByServiceInstance(instance)
    val oldKeyList = nodeLabels.asScala.map(label => label.getLabelKey)
    val willBeDelete = oldKeyList.diff(newKeyList)
    val willBeAdd = newKeyList.diff(oldKeyList)
    val willBeUpdate = oldKeyList.diff(willBeDelete)
    val modifiableKeyList = LabelUtils.listAllUserModifiableLabel()
    if (null != willBeDelete && willBeDelete.nonEmpty) {
      nodeLabels.asScala.foreach(nodeLabel => {
        if (
            modifiableKeyList.contains(nodeLabel.getLabelKey) && willBeDelete
              .contains(nodeLabel.getLabelKey)
        ) {
          val labelIds = new util.ArrayList[Integer]()
          labelIds.add(nodeLabel.getId)
          labelManagerPersistence.removeNodeLabels(instance, labelIds)
        }
      })
    }

    /**
     * update step:
     * 1.delete relations of old labels 2.add new relation between new labels and instance
     */
    if (null != willBeUpdate && willBeUpdate.nonEmpty) {
      labels.asScala.foreach(label => {
        if (
            modifiableKeyList.contains(label.getLabelKey) && willBeUpdate
              .contains(label.getLabelKey)
        ) {
          nodeLabels.asScala
            .filter(_.getLabelKey.equals(label.getLabelKey))
            .foreach(oldLabel => {
              val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
              val labelIds = new util.ArrayList[Integer]()
              labelIds.add(oldLabel.getId)
              labelManagerPersistence.removeNodeLabels(instance, labelIds)
              val newLabelId = tryToAddLabel(persistenceLabel)
              labelIds.remove(oldLabel.getId)
              labelIds.add(newLabelId)
              labelManagerPersistence.addLabelToNode(instance, labelIds)
            })
        }
      })
    }
    if (null != willBeAdd && willBeAdd.nonEmpty) {
      labels.asScala
        .filter(label => willBeAdd.contains(label.getLabelKey))
        .foreach(label => {
          if (modifiableKeyList.contains(label.getLabelKey)) {
            val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
            val labelId = tryToAddLabel(persistenceLabel)
            if (labelId > 0) {
              val labelIds = new util.ArrayList[Integer]()
              labelIds.add(labelId)
              labelManagerPersistence.addLabelToNode(instance, labelIds)
            }
          }
        })
    }
  }

  /**
   * Remove the labels related by node instance
   *
   * @param instance
   *   node instance
   * @param labels
   *   labels
   */
  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def removeLabelsFromNode(
      instance: ServiceInstance,
      labels: util.List[Label[_]]
  ): Unit = {
    // 这里前提是表中保证了同个key，只会有最新的value保存在数据库中
    val dbLabels = labelManagerPersistence
      .getLabelByServiceInstance(instance)
      .asScala
      .map(l => (l.getLabelKey, l))
      .toMap
    labelManagerPersistence.removeNodeLabels(
      instance,
      labels.asScala.map(l => dbLabels(l.getLabelKey).getId).asJava
    )
  }

  @Transactional(rollbackFor = Array(classOf[Exception]))
  override def removeLabelsFromNode(instance: ServiceInstance, isEngine: Boolean): Unit = {
    val labels = labelManagerPersistence
      .getLabelByServiceInstance(instance)
      .asScala
    val removeLabels = if (isEngine) {
      labels
    } else {
      labels.filter(label => !LabelManagerConf.LONG_LIVED_LABEL.contains(label.getLabelKey))
    }
    labelManagerPersistence.removeNodeLabels(instance, removeLabels.map(_.getId).asJava)
  }

  /**
   * Get node instances by labels
   *
   * @param labels
   *   searchableLabel or other normal labels
   * @return
   */
  override def getNodesByLabels(labels: util.List[Label[_]]): util.List[ServiceInstance] = {
    labels.asScala.flatMap(label => getNodesByLabel(label).asScala).distinct
  }.asJava

  override def getNodesByLabel(label: Label[_]): util.List[ServiceInstance] = {
    val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
    labelManagerPersistence
      .getNodeByLabelKeyValue(persistenceLabel.getLabelKey, persistenceLabel.getStringValue)
      .asScala
      .distinct
  }.asJava

  override def getNodeLabels(instance: ServiceInstance): util.List[Label[_]] = {
    labelManagerPersistence.getLabelByServiceInstance(instance).asScala.map { label =>
      val realyLabel: Label[_] = labelFactory.createLabel(
        label.getLabelKey,
        if (!CollectionUtils.isEmpty(label.getValue)) label.getValue else label.getStringValue
      )
      realyLabel
    }
  }.asJava

  /**
   * Get scored node instances
   *
   * @param labels
   *   searchableLabel or other normal labels
   * @return
   */
  override def getScoredNodesByLabels(
      labels: util.List[Label[_]]
  ): util.List[ScoreServiceInstance] = {
    getScoredNodeMapsByLabels(labels).asScala.map(_._1).toList
  }.asJava

  /**
   *   1. Get the key value of the label 2.
   * @param labels
   * @return
   */
  override def getScoredNodeMapsByLabels(
      labels: util.List[Label[_]]
  ): util.Map[ScoreServiceInstance, util.List[Label[_]]] = {
    // Try to convert the label list to key value list
    if (null != labels && labels.asScala.nonEmpty) {
      // Get the persistence labels by kvList
      val requireLabels = labels.asScala.filter(_.getFeature == Feature.CORE)
      // Extra the necessary labels whose feature equals Feature.CORE or Feature.SUITABLE
      val necessaryLabels = requireLabels.map(LabelManagerUtils.convertPersistenceLabel)
      val inputLabels = labels.asScala.map(LabelManagerUtils.convertPersistenceLabel)
      return getScoredNodeMapsByLabels(inputLabels.asJava, necessaryLabels.asJava)
    }
    new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
  }

  /**
   *   1. Get the relationship between the incoming label and node 2. get all instances by input
   *      labels 3. get instance all labels 4. Judge labels
   * @param labels
   * @param necessaryLabels
   * @return
   */
  private def getScoredNodeMapsByLabels(
      labels: util.List[PersistenceLabel],
      necessaryLabels: util.List[PersistenceLabel]
  ): util.Map[ScoreServiceInstance, util.List[Label[_]]] = {
    // Get the in-degree relations ( Label -> Nodes )
    val inNodeDegree = labelManagerPersistence.getNodeRelationsByLabels(
      if (necessaryLabels.asScala.nonEmpty) necessaryLabels else labels
    )
    if (inNodeDegree.isEmpty) {
      return new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
    }
    // serviceInstance --> labels
    val instanceLabels = new mutable.HashMap[ServiceInstance, ArrayBuffer[Label[_]]]()
    inNodeDegree.asScala.foreach { keyValue =>
      keyValue._2.asScala.foreach { instance =>
        if (!instanceLabels.contains(instance)) {
          instanceLabels.put(instance, new ArrayBuffer[Label[_]]())
        }
        val labelList = instanceLabels.get(instance)
        labelList.get.asJava.add(keyValue._1)
      }
    }
    // getAll instances
    val instances = if (necessaryLabels.asScala.nonEmpty) {
      // Cut the in-degree relations, drop inconsistent nodes
      instanceLabels.filter(entry => entry._2.size >= necessaryLabels.size).keys
    } else {
      instanceLabels.keys
    }

    // Get the out-degree relations ( Node -> Label )
    val outNodeDegree =
      labelManagerPersistence.getLabelRelationsByServiceInstance(instances.toList.asJava)
    // outNodeDegree cannot be empty
    if (outNodeDegree.asScala.nonEmpty) {
      val necessaryLabelKeys =
        if (null == necessaryLabels || necessaryLabels.isEmpty) new mutable.HashSet[String]()
        else {
          necessaryLabels.asScala.map(_.getLabelKey).toSet
        }
      // Rebuild in-degree relations
      inNodeDegree.clear()
      val removeNodes = new ArrayBuffer[ServiceInstance]()
      outNodeDegree.asScala.foreach { case (node, iLabels) =>
        // The core tag must be exactly the same
        if (null != necessaryLabels) {
          val coreLabelKeys = iLabels.asScala
            .map(ManagerUtils.persistenceLabelToRealLabel)
            .filter(_.getFeature == Feature.CORE)
            .map(_.getLabelKey)
            .toSet
          if (
              necessaryLabelKeys.asJava.containsAll(
                coreLabelKeys.asJava
              ) && coreLabelKeys.size == necessaryLabelKeys.size
          ) {
            iLabels.asScala.foreach(label => {
              if (!inNodeDegree.asScala.contains(label)) {
                val inNodes = new util.ArrayList[ServiceInstance]()
                inNodeDegree.put(label, inNodes)
              }
              val inNodes = inNodeDegree.get(label)
              inNodes.add(node)
            })
          } else {
            removeNodes += node
          }
        }
      }

      // Remove nodes with mismatched labels
      if (removeNodes.nonEmpty && removeNodes.size == outNodeDegree.size()) {
        logger.info(
          s"The entered labels${necessaryLabels} do not match the labels of the node itself"
        )
      }

      removeNodes.foreach(outNodeDegree.remove(_))
      return nodeLabelScorer
        .calculate(inNodeDegree, outNodeDegree, labels)
        .asInstanceOf[util.Map[ScoreServiceInstance, util.List[Label[_]]]]
    }
    new util.HashMap[ScoreServiceInstance, util.List[Label[_]]]()
  }

  private def tryToAddLabel(persistenceLabel: PersistenceLabel): Int = {
    if (persistenceLabel.getId <= 0) {
      val label = labelManagerPersistence.getLabelByKeyValue(
        persistenceLabel.getLabelKey,
        persistenceLabel.getStringValue
      )
      if (null == label) {
        persistenceLabel.setLabelValueSize(persistenceLabel.getValue.size())
        Utils.tryCatch(labelManagerPersistence.addLabel(persistenceLabel)) { t: Throwable =>
          logger.warn(s"Failed to add label ${t.getClass}")
        }
      } else {
        persistenceLabel.setId(label.getId)
      }
    }
    persistenceLabel.getId
  }

  override def getNodeLabelsByInstanceList(
      serviceInstanceList: util.List[ServiceInstance]
  ): util.HashMap[String, util.List[Label[_]]] = {
    val resultMap = new util.HashMap[String, util.List[Label[_]]]()
    val map = labelManagerPersistence.getLabelRelationsByServiceInstance(serviceInstanceList)
    serviceInstanceList.asScala.foreach(serviceInstance => {
      val LabelList = map
        .get(serviceInstance)
        .asScala
        .map { label =>
          val realyLabel: Label[_] = labelFactory.createLabel(
            label.getLabelKey,
            if (!CollectionUtils.isEmpty(label.getValue)) label.getValue else label.getStringValue
          )
          realyLabel
        }
        .toList
        .asJava
      resultMap.put(serviceInstance.toString, LabelList)
    })
    resultMap
  }

}
