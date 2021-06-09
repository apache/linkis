package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait EngineReuseLabelChooser {


  def chooseLabels(labelList: util.List[Label[_]]): util.List[Label[_]]

}
