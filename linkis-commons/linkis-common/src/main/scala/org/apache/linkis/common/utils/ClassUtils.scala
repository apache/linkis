/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.utils

import java.lang.reflect.Modifier

import org.apache.linkis.common.conf.Configuration
import org.reflections.Reflections

object ClassUtils {
  lazy val reflections = new Reflections(Configuration.REFLECT_SCAN_PACKAGE, this.getClass.getClassLoader)
  def jarOfClass(cls: Class[_]): Option[String] = {
    val uri = cls.getResource("/" + cls.getName.replace('.', '/') + ".class")
    if (uri != null) {
      val uriStr = uri.toString
      if (uriStr.startsWith("jar:file:")) {
        Some(uriStr.substring("jar:file:".length, uriStr.indexOf("!")))
      } else {
        None
      }
    } else {
      None
    }
  }

  def getClassInstance[T](className: String): T = {
    Thread.currentThread.getContextClassLoader.loadClass(className).asInstanceOf[Class[T]].newInstance()
  }

  def getFieldVal(o: Any, name: String): Any = {
    Utils.tryThrow {
      val field = o.getClass.getDeclaredField(name)
      field.setAccessible(true)
      field.get(o)
    } {
      case t: Throwable => throw t
    }
  }

  def setFieldVal(o: Any, name: String, value: Any): Unit = {
    Utils.tryThrow {
      val field = o.getClass.getDeclaredField(name)
      field.setAccessible(true)
      field.set(o, value.asInstanceOf[AnyRef])
    } {
      case t: Throwable => throw t
    }
  }

  def isInterfaceOrAbstract(clazz: Class[_]): Boolean = {
     clazz.isInterface || Modifier.isAbstract(clazz.getModifiers)
  }


}
