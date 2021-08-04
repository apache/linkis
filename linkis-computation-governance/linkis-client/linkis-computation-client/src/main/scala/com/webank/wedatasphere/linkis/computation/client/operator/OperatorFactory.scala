package com.webank.wedatasphere.linkis.computation.client.operator

import com.webank.wedatasphere.linkis.common.utils.ClassUtils
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESJobException

/**
  * Created by enjoyyin on 2021/6/6.
  */
trait OperatorFactory {

  def createOperatorByName(operatorName: String): Operator[_]

}

object OperatorFactory {

  private val operatorFactory = new OperatorFactoryImpl

  def apply(): OperatorFactory = operatorFactory

}

import scala.collection.convert.WrapAsScala._
class OperatorFactoryImpl extends OperatorFactory {

  private val operatorClasses: Map[String, Class[_ <: Operator[_]]] = ClassUtils.reflections.getSubTypesOf(classOf[Operator[_]])
    .filterNot(ClassUtils.isInterfaceOrAbstract).map { clazz =>
      clazz.newInstance().getName -> clazz
    }.toMap

  override def createOperatorByName(operatorName: String): Operator[_] = operatorClasses.get(operatorName)
    .map(_.newInstance()).getOrElse(throw new UJESJobException(s"Cannot find $operatorName operator."))

}