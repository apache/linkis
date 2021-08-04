package com.webank.wedatasphere.linkis.engineconnplugin.flink.listener

import java.util

import scala.collection.JavaConversions._

/**
  * Created by enjoyyin on 2021/4/20.
  */
trait FlinkListenerGroup {

  def addFlinkListener(flinkListener: FlinkListener): Unit

  def getFlinkListeners: util.List[FlinkListener]

  def setFlinkListeners(flinkListeners: util.List[FlinkListener]): Unit

}

abstract class FlinkListenerGroupImpl extends FlinkListenerGroup {

  private var flinkListeners: util.List[FlinkListener] = _

  override def addFlinkListener(flinkListener: FlinkListener): Unit = {
    if (flinkListeners == null) flinkListeners = new util.ArrayList[FlinkListener]
    flinkListeners.add(flinkListener)
  }

  override def getFlinkListeners: util.List[FlinkListener] = flinkListeners

  override def setFlinkListeners(flinkListeners: util.List[FlinkListener]): Unit = {
    this.flinkListeners = flinkListeners
  }

  private def getFlinkListeners[T <: FlinkListener](clazz: Class[T]): util.List[T] = flinkListeners match {
    case listeners: util.List[FlinkListener] =>
      listeners.filter(clazz.isInstance).map(_.asInstanceOf[T])
    case _ => new util.ArrayList[T]
  }

  def getFlinkStatusListeners: util.List[FlinkStatusListener] = getFlinkListeners(classOf[FlinkStatusListener])

  def getFlinkStreamingResultSetListeners: util.List[FlinkStreamingResultSetListener] = getFlinkListeners(classOf[FlinkStreamingResultSetListener])

}