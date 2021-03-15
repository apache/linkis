package com.webank.wedatasphere.linkis.bml.utils

object BmlHookUtils {
  val WORK_DIR_STR = "user.dir"
  def getCurrentWorkDir:String = System.getProperty(WORK_DIR_STR)


  def deleteAllFiles(workDir:String):Unit = {

  }



}
