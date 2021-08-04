package com.webank.wedatasphere.linkis.engineconnplugin.flink.ql

import com.webank.wedatasphere.linkis.common.utils.ClassUtils
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException

import scala.collection.convert.WrapAsScala._

/**
  * Created by enjoyyin on 2021/6/7.
  */
object GrammarFactory {

  private val grammars = ClassUtils.reflections.getSubTypesOf(classOf[Grammar]).filterNot(ClassUtils.isInterfaceOrAbstract).map(_.newInstance).toArray

  def getGrammars: Array[Grammar] = grammars

  def apply(sql: String, context: FlinkEngineConnContext): Grammar = getGrammar(sql, context)
    .getOrElse(throw new SqlExecutionException("Not support grammar " + sql))

  def getGrammar(sql: String, context: FlinkEngineConnContext): Option[Grammar] = grammars.find(_.canParse(sql)).map(_.copy(context, sql))

}
