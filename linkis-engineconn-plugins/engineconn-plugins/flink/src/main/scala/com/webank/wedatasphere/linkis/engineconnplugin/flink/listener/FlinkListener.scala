package com.webank.wedatasphere.linkis.engineconnplugin.flink.listener

import com.webank.wedatasphere.linkis.common.listener.{Event, EventListener}
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.RowsType.RowsType

/**
  * Created by enjoyyin on 2021/4/16.
  */
trait FlinkListener extends EventListener {
  override def onEventError(event: Event, t: Throwable): Unit = {}
}

trait FlinkStatusListener extends FlinkListener {

  def onSuccess(rows: Int, rowsType: RowsType): Unit

  def onFailed(message: String, t: Throwable, rowsType: RowsType): Unit

}

trait InteractiveFlinkStatusListener extends FlinkStatusListener with Logging {

  private var isMarked = false
  private var cachedRows: Int = _

  def markSuccess(rows: Int): Unit = {
    isMarked = true
    cachedRows = rows
  }

  override final def onFailed(message: String, t: Throwable, rowsType: RowsType): Unit = if(isMarked) {
    info("Ignore this error, since we have marked this job as succeed.", t)
    onSuccess(cachedRows, rowsType)
    //isMarked = false
  } else tryFailed(message, t)

  def tryFailed(message: String, throwable: Throwable): Unit

}

object RowsType extends Enumeration {
  type RowsType = Value
  val Fetched, Affected = Value
}

trait FlinkStreamingResultSetListener extends FlinkListener {

  def onResultSetPulled(rows: Int): Unit

}