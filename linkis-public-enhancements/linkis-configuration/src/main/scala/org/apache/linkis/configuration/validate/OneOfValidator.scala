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

import com.google.gson.GsonBuilder

class OneOfValidator extends Validator {

  override def validate(value: String, range: String): Boolean = {
    val rangArray = new GsonBuilder().create().fromJson(range, classOf[Array[String]])
    rangArray.contains(value)
  }

  override var kind: String = "OFT" // one of them
}

/* object OneOfValidator{
  def main(args: Array[String]): Unit = {
    val range = "[\",\",\"\\t\"]"
    print(new OneOfValidator().validate(",,",range))
    /*val a = new Array[String](2)
    a(0) = ","
    a(0) = "\t"
    new GsonBuilder().create().toJson(a)
    print(new GsonBuilder().create().toJson(a))*/
  }
} */
