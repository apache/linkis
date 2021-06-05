package com.webank.wedatasphere.linkis.manager.service.common.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait LabelFilter {

  def choseEngineLabel(labelList: util.List[Label[_]]): util.List[Label[_]]

  def choseEMLabel(labelList: util.List[Label[_]]): util.List[Label[_]]

}
