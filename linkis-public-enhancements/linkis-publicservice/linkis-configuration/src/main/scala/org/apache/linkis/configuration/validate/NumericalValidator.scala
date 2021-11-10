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
 
package org.apache.linkis.configuration.validate

import com.google.gson.GsonBuilder
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.configuration.exception.ConfigurationException

class NumericalValidator extends Validator with Logging {

  override def validate(value: String, range: String): Boolean = {
    Utils.tryCatch{
      val rangArray = new GsonBuilder().create().fromJson(range, classOf[Array[Int]])
      val valueInt = Integer.parseInt(value)
      if (rangArray.size != 2) {
        throw new ConfigurationException("error validator range！")
      }
      valueInt >= rangArray.sorted.apply(0) && valueInt <= rangArray.sorted.apply(1)
    }{
      case e: NumberFormatException => info(s"${value} cannot be converted to int, validation failed(${value}不能转换为int，校验失败)"); return false
      //If there is a problem with range, then an exception is thrown.(如果range出问题，那么还是抛出异常)
      /*case e:JsonSyntaxException =>info(s"${range}Cannot convert to int, check failed(不能转换为int，校验失败)"); return false*/
      case e: Exception => throw e
    }
  }

  override var kind: String = "NumInterval"
}

/*object NumericalValidator {
  def main(args: Array[String]): Unit = {
    print(new NumericalValidator().validate("22.5", "[17.5,150]"))
  }
}*/
