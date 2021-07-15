package com.webank.wedatasphere.linkis.manager.label.entity.engine

import java.util
import com.webank.wedatasphere.linkis.manager.label.entity.{EngineNodeLabel, Feature, GenericLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum


import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel


class EngineConnModeLabel extends GenericLabel with EngineNodeLabel {

  setLabelKey("engineConnMode")

  override def getFeature = Feature.CORE

  @ValueSerialNum(0)
  def setEngineConnMode(engineConnMode: String): Unit = {
    if (null == getValue) setValue(new util.HashMap[String, String])
    getValue.put("engineConnMode", engineConnMode)
  }

  def getEngineConnMode: String = {
    if (null == getValue) return null
    getValue.get("engineConnMode")
  }

}

object EngineConnMode extends Enumeration {
  type EngineConnMode = Value
  val Computation = Value("computation")
  val Once = Value("once")
  val Cluster = Value("cluster")
  val Computation_With_Once = Value("computation_once")
  val Once_With_Cluster = Value("once_cluster")
  val Unknown = Value("unknown")

  implicit def toEngineConnMode(engineConnMode: String): EngineConnMode = engineConnMode match {
    case "computation" => Computation
    case "once" => Once
    case "cluster" => Cluster
    case "computation_once" => Computation_With_Once
    case "once_cluster" => Once_With_Cluster
    case _ => Unknown
  }

}