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

package com.webank.wedatasphere.linkis.resourcemanager.domain;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf;
import com.webank.wedatasphere.linkis.manager.label.builder.CombinedLabelBuilder;
import com.webank.wedatasphere.linkis.manager.label.entity.CombinedLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.ResourceLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RMLabelContainer {

    private static final Logger logger = LoggerFactory.getLogger(RMLabelContainer.class);
    private static CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();

    List<Label<?>> labels;
    List<Label<?>> lockedLabels;
    private EMInstanceLabel EMInstanceLabel;
    private EngineTypeLabel engineTypeLabel;
    private UserCreatorLabel userCreatorLabel;
    private EngineInstanceLabel engineInstanceLabel;
    private CombinedLabel combinedUserCreatorEngineTypeLabel;
    private Label currentLabel;

    public RMLabelContainer(List<Label<?>> labels) {
        this.labels = labels;
        this.lockedLabels = Lists.newArrayList();
        try{
            if(getUserCreatorLabel() != null && getEngineTypeLabel() != null){
                this.combinedUserCreatorEngineTypeLabel = (CombinedLabel) combinedLabelBuilder.build("", Lists.newArrayList(getUserCreatorLabel(), getEngineTypeLabel()));
                this.labels.add(combinedUserCreatorEngineTypeLabel);
            }
        }catch (Exception e){
            logger.warn("failed to get combinedUserCreatorEngineTypeLabel", e);
        }
        this.labels = LabelUtils.distinctLabel(this.labels, this.labels);
    }

    public List<Label<?>> getLabels() {
        return labels;
    }

    public List<Label<?>> getResourceLabels() {
        if (null != labels) {
            return labels.stream().filter(label -> label instanceof ResourceLabel).collect(Collectors.toList());
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

    public EMInstanceLabel getEMInstanceLabel() throws RMErrorException {
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

    public EngineTypeLabel getEngineTypeLabel() throws RMErrorException {
        if(engineTypeLabel == null){
            for (Label label : labels) {
                if(label instanceof EngineTypeLabel){
                    return (EngineTypeLabel) label;
                }
            }
        } else {
            return engineTypeLabel;
        }
        logger.warn("EngineTypeLabel not found");
        return null;
    }

    public UserCreatorLabel getUserCreatorLabel() throws RMErrorException {
        if(userCreatorLabel == null){
            for (Label label : labels) {
                if(label instanceof UserCreatorLabel){
                    return (UserCreatorLabel) label;
                }
            }
        } else {
            return userCreatorLabel;
        }
        return null;
    }

    public EngineInstanceLabel getEngineInstanceLabel() throws RMErrorException {
        if(engineInstanceLabel == null){
            for (Label label : labels) {
                if(label instanceof EngineInstanceLabel){
                    return (EngineInstanceLabel) label;
                }
            }
        } else {
            return engineInstanceLabel;
        }
        logger.warn("EngineInstanceLabel not found");
        return null;
    }

    public CombinedLabel getCombinedUserCreatorEngineTypeLabel() {
        return combinedUserCreatorEngineTypeLabel;
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

    public String getEngineServiceName() throws RMErrorException {
        return GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue();
    }

    public void sort(){
        // TODO lock sequence
    }

    @Override
    public String toString() {
        return "RMLabelContainer{" +
                "labels=" + labels +
                ", lockedLabels=" + lockedLabels +
                ", EMInstanceLabel=" + EMInstanceLabel +
                ", engineTypeLabel=" + engineTypeLabel +
                ", userCreatorLabel=" + userCreatorLabel +
                ", engineInstanceLabel=" + engineInstanceLabel +
                ", currentLabel=" + currentLabel +
                '}';
    }
}
