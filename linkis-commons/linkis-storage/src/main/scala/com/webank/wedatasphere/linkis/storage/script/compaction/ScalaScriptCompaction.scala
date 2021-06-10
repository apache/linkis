package com.webank.wedatasphere.linkis.storage.script.compaction


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
