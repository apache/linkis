package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * @author peacewong
  * @date 2021/1/13 10:42
  */
trait EngineReuseLabelChooser {


  def chooseLabels(labelList: util.List[Label[_]]): util.List[Label[_]]

}
