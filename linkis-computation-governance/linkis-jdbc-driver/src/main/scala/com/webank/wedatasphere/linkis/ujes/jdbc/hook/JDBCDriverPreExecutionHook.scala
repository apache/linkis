package com.webank.wedatasphere.linkis.ujes.jdbc.hook

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}

import scala.collection.mutable.ArrayBuffer

/**
  * @author peacewong
  * @date 2020/5/29 17:27
  */
trait JDBCDriverPreExecutionHook {

  def callPreExecutionHook(sql: String): String

}

object JDBCDriverPreExecutionHook extends Logging{

  private val preExecutionHooks:Array[JDBCDriverPreExecutionHook] = {
    val hooks = new ArrayBuffer[JDBCDriverPreExecutionHook]()
    CommonVars("wds.linkis.jdbc.pre.hook.class", "com.webank.wedatasphere.linkis.ujes.jdbc.hook.impl.TableauPreExecutionHook").getValue.split(",") foreach {
      hookStr => Utils.tryCatch{
        val clazz = Class.forName(hookStr.trim)
        val obj = clazz.newInstance()
        obj match {
          case hook:JDBCDriverPreExecutionHook => hooks += hook
          case _ => warn(s"obj is not a engineHook obj is ${obj.getClass}")
        }
      }{
        case e:Exception => error(s"failed to load class ${hookStr}", e)
      }
    }
    hooks.toArray
  }

  def getPreExecutionHooks = preExecutionHooks
}