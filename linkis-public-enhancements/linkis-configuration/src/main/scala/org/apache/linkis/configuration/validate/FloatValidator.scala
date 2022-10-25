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

package org.apache.linkis.configuration.validate

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary.ERROR_VALIDATOR_RANGE
import org.apache.linkis.configuration.exception.ConfigurationException

import com.google.gson.GsonBuilder

class FloatValidator extends Validator with Logging {

  override def validate(value: String, range: String): Boolean = {
    try {
      val rangArray = new GsonBuilder().create().fromJson(range, classOf[Array[Double]])
      if (rangArray.size != 2) {
        throw new ConfigurationException(ERROR_VALIDATOR_RANGE.getErrorDesc)
      }
      value.toDouble >= rangArray.sorted.apply(0) && value.toDouble <= rangArray.sorted.apply(1)
    } catch {
      case e: NumberFormatException => logger.info(s"${value}不能转换为double，校验失败"); return false
      case e: Exception => throw e
    }
  }

  override var kind: String = "FloatInterval"
}

/* object FloatValidator extends App {
  print(new FloatValidator().validate("3", "[1.12,5.22]"))
} */
