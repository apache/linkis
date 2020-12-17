package com.webank.wedatasphere.linkis.storage.script.compaction

/**
 * @Classname ShellScriptCompaction
 * @Description TODO
 * @Date 2020/12/17 9:11
 * @Created by limeng
 */
class ShellScriptCompaction private extends CommonScriptCompaction{
  override def prefixConf: String = "#conf@set"

  override def prefix: String = "#@set"

  override def belongTo(suffix: String): Boolean ={
    suffix match {
      case "sh"=>true
      case _=>false
    }
  }
}
object ShellScriptCompaction{
  val shellScriptCompaction:ShellScriptCompaction=new ShellScriptCompaction

  def apply(): CommonScriptCompaction = shellScriptCompaction
}
