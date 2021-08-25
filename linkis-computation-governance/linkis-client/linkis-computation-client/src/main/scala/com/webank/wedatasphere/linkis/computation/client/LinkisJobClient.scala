package com.webank.wedatasphere.linkis.computation.client

import java.io.Closeable

import com.webank.wedatasphere.linkis.computation.client.interactive.InteractiveJob
import com.webank.wedatasphere.linkis.computation.client.once.OnceJob


/**
 * This class is only used to provide a unified entry for user to build a LinkisJob conveniently and simply.
 * Please keep this class lightweight enough, do not set too many field to confuse user.
 */
object LinkisJobClient extends Closeable {

  val config = LinkisJobBuilder

  val interactive = InteractiveJob
  val once = OnceJob

  override def close(): Unit = {
    if(config.justGetDefaultUJESClient != null) {
      config.justGetDefaultUJESClient.close()
    }
  }
}
