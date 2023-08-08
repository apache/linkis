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

package org.apache.linkis.engineconnplugin.flink.util

import org.apache.linkis.common.utils.{ClassUtils, Utils}
import org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException

import java.text.MessageFormat

import scala.collection.convert.wrapAsScala._

object ClassUtil {

  def getInstance[T](clazz: Class[T], defaultValue: T): T = {
    val classes = ClassUtils.reflections
      .getSubTypesOf(clazz)
      .filterNot(ClassUtils.isInterfaceOrAbstract)
      .toArray
    if (classes.length <= 1) defaultValue
    else if (classes.length == 2) {
      val realClass = if (classes(0) == defaultValue.getClass) classes(1) else classes(0);
      Utils.tryThrow(realClass.newInstance) { t =>
        new JobExecutionException(
          MessageFormat.format(CREATE_INSTANCE_FAILURE.getErrorDesc, clazz.getSimpleName),
          t
        );
      }
    } else {
      throw new JobExecutionException(
        s"Too many subClasses of ${clazz.getSimpleName}, list: $classes."
      );
    }
  }

}
