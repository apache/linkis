package org.apache.linkis.engineconnplugin.datax.util

import org.apache.linkis.common.utils.{ClassUtils, Utils}
import org.apache.linkis.engineconnplugin.datax.client.exception.JobExecutionException

import scala.collection.convert.wrapAsScala._

object ClassUtil {

  def getInstance[T](clazz: Class[T], defaultValue: T): T = {
    val classes = ClassUtils.reflections.getSubTypesOf(clazz).filterNot(ClassUtils.isInterfaceOrAbstract).toArray
    if (classes.length <= 1) defaultValue
    else if (classes.length == 2) {
      val realClass = if (classes(0) == defaultValue.getClass) classes(1) else classes(0);
      Utils.tryThrow(realClass.newInstance) { t =>
        new JobExecutionException(s"New a instance of ${clazz.getSimpleName} failed!", t);
      }
    } else {
      throw new JobExecutionException(s"Too many subClasses of ${clazz.getSimpleName}, list: $classes.");
    }
  }
}
