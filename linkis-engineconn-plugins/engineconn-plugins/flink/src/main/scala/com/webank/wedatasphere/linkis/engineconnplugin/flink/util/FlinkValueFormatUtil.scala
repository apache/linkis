package com.webank.wedatasphere.linkis.engineconnplugin.flink.util

import java.text.NumberFormat

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by enjoyyin on 2021/4/16.
  */
object FlinkValueFormatUtil {

  val FLINK_NF_FRACTION_LENGTH = CommonVars("wds.linkis.engine.flink.fraction.length", 30)

  private val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(FLINK_NF_FRACTION_LENGTH.getValue)

  def formatValue(value: Any): Any = value match {
    case value: String => value.replaceAll("\n|\t", " ")
    case value: Double => nf.format(value)
    case value: Any => value.toString
    case _ => null
  }

}
