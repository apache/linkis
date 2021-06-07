package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.manager.label.entity.Label;

import java.util.List;


public interface LabelNode extends Node{

    List<Label> getLabels();

    void setLabels(List<Label> labels);
}
