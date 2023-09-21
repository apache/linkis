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

package org.apache.linkis.manager.rm.domain;

import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.apache.linkis.manager.label.conf.LabelManagerConf;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.ResourceLabel;
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.LabelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMLabelContainer {

  private static final Logger logger = LoggerFactory.getLogger(RMLabelContainer.class);
  private static CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();

  List<Label<?>> labels;
  List<Label<?>> lockedLabels;
  private EMInstanceLabel EMInstanceLabel;
  private EngineTypeLabel engineTypeLabel;
  private UserCreatorLabel userCreatorLabel;
  private EngineInstanceLabel engineInstanceLabel;
  private ClusterLabel clusterLabel;
  private CombinedLabel combinedResourceLabel;
  private Label currentLabel;

  public RMLabelContainer(List<Label<?>> labels) {
    this.labels = labels;
    this.lockedLabels = Lists.newArrayList();
    try {
      if (getUserCreatorLabel() != null && getEngineTypeLabel() != null) {
        List<Label> combinedLabel = Lists.newArrayList(getUserCreatorLabel(), getEngineTypeLabel());
        ClusterLabel clusterLabel = getClusterLabel();
        if (shouldCombinedClusterLabel(clusterLabel)) {
          combinedLabel.add(clusterLabel);
        }
        this.combinedResourceLabel = (CombinedLabel) combinedLabelBuilder.build("", combinedLabel);
        this.labels.add(combinedResourceLabel);
      }
    } catch (Exception e) {
      logger.warn("failed to get combinedResourceLabel", e);
    }
    this.labels = LabelUtils.distinctLabel(this.labels, labels);
  }

  public List<Label<?>> getLabels() {
    return labels;
  }

  public List<Label<?>> getResourceLabels() {
    if (null != labels) {
      List<Label<?>> resourceLabels =
          labels.stream()
              .filter(label -> label instanceof ResourceLabel)
              .sorted(
                  new Comparator<Label<?>>() {
                    @Override
                    public int compare(Label<?> label1, Label<?> label12) {
                      return label1.getLabelKey().compareTo(label12.getLabelKey());
                    }
                  })
              .collect(Collectors.toList());
      Collections.reverse(resourceLabels);
      return resourceLabels;
    }
    return new ArrayList<>();
  }

  public Label find(Class labelClass) {
    for (Label label : labels) {
      if (labelClass.isInstance(label)) {
        return label;
      }
    }
    return null;
  }

  public EMInstanceLabel getEMInstanceLabel() {
    if (EMInstanceLabel == null) {
      for (Label label : labels) {
        if (label instanceof EMInstanceLabel) {
          return (EMInstanceLabel) label;
        }
      }
    } else {
      return EMInstanceLabel;
    }
    logger.warn("EMInstanceLabel not found");
    return null;
  }

  public EngineTypeLabel getEngineTypeLabel() {
    if (engineTypeLabel == null) {
      for (Label label : labels) {
        if (label instanceof EngineTypeLabel) {
          return (EngineTypeLabel) label;
        }
      }
    } else {
      return engineTypeLabel;
    }
    logger.warn("EngineTypeLabel not found");
    return null;
  }

  public UserCreatorLabel getUserCreatorLabel() {
    if (userCreatorLabel == null) {
      for (Label label : labels) {
        if (label instanceof UserCreatorLabel) {
          return (UserCreatorLabel) label;
        }
      }
    } else {
      return userCreatorLabel;
    }
    return null;
  }

  public EngineInstanceLabel getEngineInstanceLabel() {
    if (engineInstanceLabel == null) {
      for (Label label : labels) {
        if (label instanceof EngineInstanceLabel) {
          return (EngineInstanceLabel) label;
        }
      }
    } else {
      return engineInstanceLabel;
    }
    logger.warn("EngineInstanceLabel not found");
    return null;
  }

  public ClusterLabel getClusterLabel() {
    if (clusterLabel == null) {
      for (Label label : labels) {
        if (label instanceof ClusterLabel) {
          return (ClusterLabel) label;
        }
      }
    } else {
      return clusterLabel;
    }
    logger.warn("ClusterLabel not found");
    return null;
  }

  private boolean shouldCombinedClusterLabel(ClusterLabel clusterLabel) {
    return !(clusterLabel == null
        || (LabelManagerConf.COMBINED_WITHOUT_YARN_DEFAULT
            && clusterLabel
                .getClusterName()
                .equals(RMConfiguration.DEFAULT_YARN_CLUSTER_NAME.getValue())
            && clusterLabel.getClusterType().equals(RMConfiguration.DEFAULT_YARN_TYPE.getValue())));
  }

  public CombinedLabel getCombinedResourceLabel() {
    return combinedResourceLabel;
  }

  public Label getCurrentLabel() {
    return currentLabel;
  }

  public void setCurrentLabel(Label currentLabel) {
    this.currentLabel = currentLabel;
  }

  public List<Label<?>> getLockedLabels() {
    return lockedLabels;
  }

  public String getEngineServiceName() {
    return GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue();
  }

  public void sort() {
    // TODO lock sequence
  }

  @Override
  public String toString() {
    return "RMLabelContainer{"
        + "labels="
        + labels
        + ", lockedLabels="
        + lockedLabels
        + ", EMInstanceLabel="
        + EMInstanceLabel
        + ", engineTypeLabel="
        + engineTypeLabel
        + ", userCreatorLabel="
        + userCreatorLabel
        + ", engineInstanceLabel="
        + engineInstanceLabel
        + ", clusterLabel="
        + clusterLabel
        + ", currentLabel="
        + currentLabel
        + '}';
  }
}
