package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.manager.label.entity.Label;

import java.util.List;

/**
 * @author peacewong
 * @date 2020/6/10 16:17
 */
public interface LabelNode extends Node{

    List<Label> getLabels();

    void setLabels(List<Label> labels);
}
