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

package org.apache.linkis.manager.label.score

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.entity.Feature

import org.springframework.stereotype.Component

import java.util
import java.util.function.BiFunction

import scala.collection.JavaConverters._

/**
 * Default scorer to traversal the network between node and label
 */
@Component
class DefaultNodeLabelScorer extends NodeLabelScorer {

  override def calculate(
      inNodeDegree: util.Map[PersistenceLabel, util.List[ServiceInstance]],
      outNodeDegree: util.Map[ServiceInstance, util.List[PersistenceLabel]],
      labels: util.List[PersistenceLabel]
  ): util.Map[ScoreServiceInstance, util.List[PersistenceLabel]] = {
    // First, traversal out degree (Node -> Label)
    val baseCore = LabelCommonConfig.LABEL_SCORER_BASE_CORE.getValue.toDouble;
    val nodeScores = new util.HashMap[ServiceInstance, Double]
    val labelIdToEntity =
      traversalAndScoreOnOutDegree(baseCore, nodeScores, outNodeDegree, labels)
    // Second, filter the unknown label on in-degree (Label -> Node)
    traversalAndScoreOnInDegree(baseCore, nodeScores, inNodeDegree, labelIdToEntity)
    // At last, normalize and output
    normalizeAndOutput(nodeScores, outNodeDegree)
  }

  /**
   * Traversal out degree
   * @param nodeScores
   */
  private def traversalAndScoreOnOutDegree(
      baseCore: Double,
      nodeScores: util.Map[ServiceInstance, Double],
      outNodeDegree: util.Map[ServiceInstance, util.List[PersistenceLabel]],
      labels: util.List[PersistenceLabel]
  ): util.Map[String, PersistenceLabel] = {
    // Group count feature in labels
    val ftCounts = new util.HashMap[Feature, Integer]
    val countFunction: BiFunction[Feature, Integer, Integer] =
      new BiFunction[Feature, Integer, Integer] {
        override def apply(t: Feature, count: Integer): Integer = {
          var count0 = count
          if (Option(count0).isEmpty) count0 = 0
          count0 = count0 + 1
          count0
        }
      }
    val labelIdToEntity = labels.asScala
      .map(label => {
        ftCounts.compute(label.getFeature, countFunction)
        (String.valueOf(label.getId), label)
      })
      .toMap
    outNodeDegree.asScala.foreach { case (node, outLabels) =>
      // expression: base_core / feature_count * feature_boost
      val scoreOutDegree = outLabels.asScala
        .map(label => {
          if (labelIdToEntity.contains(String.valueOf(label.getId))) {
            val feature = Option(label.getFeature).getOrElse(Feature.OPTIONAL)
            (baseCore / ftCounts.get(feature).toDouble) * feature.getBoost
          } else { 0 }
        })
        .foldLeft(0d)(_ + _)
      nodeScores.put(node, scoreOutDegree)
    }
    labelIdToEntity
  }.asJava

  private def traversalAndScoreOnInDegree(
      baseCore: Double,
      nodeScores: util.Map[ServiceInstance, Double],
      inNodeDegree: util.Map[PersistenceLabel, util.List[ServiceInstance]],
      labelIdToEntity: util.Map[String, PersistenceLabel]
  ): Unit = {
    // Unrelated in-degree
    val relateLimit = LabelCommonConfig.LABEL_SCORER_RELATE_LIMIT.getValue;
    var relateSum = 0d
    var relateCount = 0
    inNodeDegree.asScala
      .map { case (label, nodes) =>
        if (nodes.size() <= relateLimit) {
          (label, 0d)
        } else {
          (label, nodes.size().asInstanceOf[Double] / nodeScores.size().asInstanceOf[Double])
        }
      }
      .filter { case (label, score) =>
        val isMatch = !labelIdToEntity.containsKey(String.valueOf(label.getId)) && score > 0
        if (isMatch) {
          relateSum += score
          relateCount += 1
        }
        isMatch
      }
      .foreach { case (label, score) =>
        val nodes = inNodeDegree.get(label)
        if (Option(nodes).isDefined) {
          val minScore = math.min(
            Feature.UNKNOWN.getBoost * score / relateSum,
            Feature.UNKNOWN.getBoost * nodes.size().asInstanceOf[Double] / relateCount
              .asInstanceOf[Double]
          )
          nodes.asScala.foreach(node => {
            val nodeScore = nodeScores.get(node)
            if (Option(nodeScore).isDefined) {
              nodeScores.put(node, nodeScore + minScore)
            }
          })
        }
      }
  }

  /**
   * Normalize
   * @param nodeScores
   * @param outNodeDegree
   */
  private def normalizeAndOutput(
      nodeScores: util.Map[ServiceInstance, Double],
      outNodeDegree: util.Map[ServiceInstance, util.List[PersistenceLabel]]
  ): util.Map[ScoreServiceInstance, util.List[PersistenceLabel]] = {
    // Average value
    val average =
      nodeScores.values().asScala.foldLeft(0d)(_ + _) / nodeScores.size.asInstanceOf[Double]
    val deviation = math.sqrt(
      nodeScores
        .values()
        .asScala
        .foldLeft(0d)((sum, score) => {
          sum + math.pow(score - average, 2)
        }) * (1.0d / nodeScores.size.asInstanceOf[Double])
    )
    var offset = 0d
    val rawOutput = nodeScores.asScala.map { case (node, score) =>
      val labelScoreServiceInstance: ScoreServiceInstance = new LabelScoreServiceInstance(node)
      val scoreCalculate = if (deviation != 0) { (score - average) / deviation }
      else score
      if (scoreCalculate < offset) {
        offset = scoreCalculate
      }
      labelScoreServiceInstance.setScore(scoreCalculate)
      (labelScoreServiceInstance, outNodeDegree.get(node))
    }
    rawOutput.foreach { case (instance, _) =>
      instance.setScore(instance.getScore + math.abs(offset))
    }
    if (null != rawOutput && rawOutput.nonEmpty) {
      new util.HashMap[ScoreServiceInstance, util.List[PersistenceLabel]](rawOutput.toMap.asJava)
    } else {
      new util.HashMap[ScoreServiceInstance, util.List[PersistenceLabel]]()
    }
  }

}
