package com.webank.wedatasphere.linkis.storage.script.compaction

/**
  * Created by v_wbjftang on 2019/2/25.
  */
class ScalaScriptCompaction private extends CommonScriptCompaction{
  override def prefix: String = "//@set"

  override def belongTo(suffix: String): Boolean = suffix match {
    case "scala" => true
    case _ => false
  }

  override def prefixConf: String = "//conf@set"
}
object ScalaScriptCompaction{
  private val compaction: ScalaScriptCompaction = new ScalaScriptCompaction

  def apply(): CommonScriptCompaction = compaction
}
