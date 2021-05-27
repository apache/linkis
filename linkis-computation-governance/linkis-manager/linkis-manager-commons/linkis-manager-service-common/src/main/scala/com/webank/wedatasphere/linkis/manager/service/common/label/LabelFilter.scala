package com.webank.wedatasphere.linkis.manager.service.common.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * @author peacewong
  * @date 2020/8/9 21:14
  */
trait LabelFilter {

  def choseEngineLabel(labelList: util.List[Label[_]]): util.List[Label[_]]

  def choseEMLabel(labelList: util.List[Label[_]]): util.List[Label[_]]

}
