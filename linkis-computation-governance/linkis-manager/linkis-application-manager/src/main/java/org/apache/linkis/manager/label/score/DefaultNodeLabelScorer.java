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

package org.apache.linkis.manager.label.score;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;
import org.apache.linkis.manager.label.entity.Feature;

import org.apache.commons.lang3.tuple.Pair;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static scala.collection.JavaConverters.*;

/** Default scorer to traversal the network between node and label */
@Component
public class DefaultNodeLabelScorer implements NodeLabelScorer {

  public Map<ScoreServiceInstance, List<PersistenceLabel>> calculate(
      Map<PersistenceLabel, List<ServiceInstance>> inNodeDegree,
      Map<ServiceInstance, List<PersistenceLabel>> outNodeDegree,
      List<PersistenceLabel> labels) {
    // First, traversal out degree (Node -> Label)
    double baseCore = LabelCommonConfig.LABEL_SCORER_BASE_CORE.getValue();

    Map<ServiceInstance, Double> nodeScores = new HashMap<>();
    Map<String, PersistenceLabel> labelIdToEntity =
        traversalAndScoreOnOutDegree(baseCore, nodeScores, outNodeDegree, labels);

    // Second, filter the unknown label on in-degree (Label -> Node)
    traversalAndScoreOnInDegree(baseCore, nodeScores, inNodeDegree, labelIdToEntity);

    // At last, normalize and output
    return normalizeAndOutput(nodeScores, outNodeDegree);
  }

  private Map<String, PersistenceLabel> traversalAndScoreOnOutDegree(
      double baseCore,
      Map<ServiceInstance, Double> nodeScores,
      Map<ServiceInstance, List<PersistenceLabel>> outNodeDegree,
      List<PersistenceLabel> labels) {
    // Group count feature in labels
    Map<Feature, Integer> ftCounts = new HashMap<>();
    BiFunction<Feature, Integer, Integer> countFunction =
        new BiFunction<Feature, Integer, Integer>() {
          @Override
          public Integer apply(Feature t, Integer count) {
            Integer count0 = count;
            if (count0 == null) count0 = 0;
            count0 = count0 + 1;
            return count0;
          }
        };

    Map<String, PersistenceLabel> labelIdToEntity =
        labels.stream()
            .map(
                label -> {
                  ftCounts.compute(label.getFeature(), countFunction);
                  return Pair.of(String.valueOf(label.getId()), label);
                })
            .collect(Collectors.toMap(Pair::getKey, Pair::getRight));

    for (Map.Entry<ServiceInstance, List<PersistenceLabel>> entry : outNodeDegree.entrySet()) {
      ServiceInstance node = entry.getKey();
      List<PersistenceLabel> outLabels = entry.getValue();
      // expression: base_core / feature_count * feature_boost
      Double scoreOutDegree =
          outLabels.stream()
              .map(
                  label -> {
                    if (labelIdToEntity.containsKey(String.valueOf(label.getId()))) {
                      Feature feature =
                          label.getFeature() != null ? label.getFeature() : Feature.OPTIONAL;
                      return (baseCore / ftCounts.get(feature).doubleValue()) * feature.getBoost();
                    } else {
                      return 0d;
                    }
                  })
              .reduce(0d, Double::sum);
      nodeScores.put(node, scoreOutDegree);
    }
    return new HashMap<>(labelIdToEntity);
  }

  private void traversalAndScoreOnInDegree(
      double baseCore,
      Map<ServiceInstance, Double> nodeScores,
      Map<PersistenceLabel, List<ServiceInstance>> inNodeDegree,
      Map<String, PersistenceLabel> labelIdToEntity) {
    // Unrelated in-degree
    int relateLimit = LabelCommonConfig.LABEL_SCORER_RELATE_LIMIT.getValue();
    double relateSum = 0d;
    int relateCount = 0;

    Map<PersistenceLabel, Double> filteredInNodeDegree = new HashMap<>();
    for (Map.Entry<PersistenceLabel, List<ServiceInstance>> entry : inNodeDegree.entrySet()) {

      PersistenceLabel label = entry.getKey();
      List<ServiceInstance> nodes = entry.getValue();
      double score;
      if (nodes.size() <= relateLimit) {
        score = 0d;
      } else {
        score = nodes.size() * 1.0 / nodeScores.size() * 1d;
      }

      if (!labelIdToEntity.containsKey(String.valueOf(label.getId())) && score > 0) {
        relateSum += score;
        relateCount += 1;
        filteredInNodeDegree.put(label, score);
      }
    }

    for (Map.Entry<PersistenceLabel, Double> entry : filteredInNodeDegree.entrySet()) {
      PersistenceLabel label = entry.getKey();
      Double score = entry.getValue();
      List<ServiceInstance> nodes = inNodeDegree.get(label);
      if (nodes != null) {
        double minScore =
            Math.min(
                Feature.UNKNOWN.getBoost() * score / relateSum,
                Feature.UNKNOWN.getBoost() * nodes.size() / relateCount);
        nodes.forEach(
            node -> {
              Double nodeScore = nodeScores.get(node);
              if (nodeScore != null) {
                nodeScores.put(node, nodeScore + minScore);
              }
            });
      }
    }
  }

  /**
   * Normalize
   *
   * @param nodeScores
   * @param outNodeDegree
   */
  public static Map<ScoreServiceInstance, List<PersistenceLabel>> normalizeAndOutput(
      Map<ServiceInstance, Double> nodeScores,
      Map<ServiceInstance, List<PersistenceLabel>> outNodeDegree) {
    // Average value
    double average =
        nodeScores.values().stream().mapToDouble(Double::doubleValue).sum() / nodeScores.size();

    double deviation =
        Math.sqrt(
            nodeScores.values().stream().mapToDouble(score -> Math.pow(score - average, 2)).sum()
                * (1.0d / nodeScores.size()));

    double[] offset = {0d};

    Map<ScoreServiceInstance, List<PersistenceLabel>> rawOutput =
        nodeScores.entrySet().stream()
            .map(
                entry -> {
                  ServiceInstance node = entry.getKey();
                  double score = entry.getValue();
                  ScoreServiceInstance labelScoreServiceInstance =
                      new LabelScoreServiceInstance(node);
                  double scoreCalculate = deviation != 0 ? (score - average) / deviation : score;
                  if (scoreCalculate < offset[0]) {
                    offset[0] = scoreCalculate;
                  }
                  labelScoreServiceInstance.setScore(scoreCalculate);
                  return Pair.of(labelScoreServiceInstance, outNodeDegree.get(node));
                })
            .collect(
                Collectors.toMap(
                    Pair::getKey, Pair::getValue, (existingValue, newValue) -> newValue));

    rawOutput
        .keySet()
        .forEach(instance -> instance.setScore(instance.getScore() + Math.abs(offset[0])));

    return new HashMap<>(rawOutput);
  }
}
