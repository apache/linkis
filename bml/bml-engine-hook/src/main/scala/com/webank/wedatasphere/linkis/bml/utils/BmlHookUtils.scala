package com.webank.wedatasphere.linkis.bml.utils

import com.webank.wedatasphere.linkis.common.utils.Utils

/**
  * created by cooperyang on 2019/9/24
  * Description:
  */
object BmlHookUtils {
  val WORK_DIR_STR = "user.dir"
  def getCurrentWorkDir:String = System.getProperty(WORK_DIR_STR)


  def deleteAllFiles(workDir:String):Unit = {

  }



}
