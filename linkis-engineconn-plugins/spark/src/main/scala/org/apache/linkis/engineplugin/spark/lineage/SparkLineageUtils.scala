/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.spark.lineage

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Logging

import org.apache.spark.sql.{DataFrame, SQLContext}

import scala.reflect.runtime.{universe => ru}

object SparkLineageUtils extends Logging {

  private var reflectObjectHelper: ReflectObjectHelper = null

  private val LOCKER = new Object

  def extract(context: SQLContext, command: String, startTime: Long, df: DataFrame): Unit = {
    val reflectObject = getOrCreateExtractMethod()
    if (null != reflectObject) {
      val method = reflectObject.extractMethod
      val objMirror = reflectObject.objMirror
      objMirror.reflectMethod(method)(context, command, startTime, df)
    }
  }

  def getOrCreateExtractMethod(): ReflectObjectHelper = {
    if (null == reflectObjectHelper) LOCKER.synchronized {
      if (null == reflectObjectHelper) {
        val className = CommonVars(
          "linkis.spark.lineage.clazz",
          "org.apache.spark.sql.hive.thriftserver.LineageExtractor"
        ).getValue
        val classMirror = ru.runtimeMirror(getClass.getClassLoader)
        val classTest = classMirror.staticModule(className)
        val methods = classMirror.reflectModule(classTest)
        val objMirror = classMirror.reflect(methods.instance)
        val method = methods.symbol.typeSignature.member(ru.TermName("extract")).asMethod
        reflectObjectHelper = ReflectObjectHelper(method, objMirror)
      }
    }
    reflectObjectHelper
  }

}

case class ReflectObjectHelper(extractMethod: ru.MethodSymbol, objMirror: ru.InstanceMirror)
