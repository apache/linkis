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

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class ValidatorManagerImpl extends ValidatorManager with Logging {

  private var validators: Array[Validator] = _

  @PostConstruct
  def init(): Unit = {
    validators = Array(
      new NumericalValidator,
      new OneOfValidator,
      new FloatValidator,
      new NoneValidator,
      new RegexValidator
    )
  }

  override def getOrCreateValidator(kind: String): Validator = {
    logger.info(s"find a validator $kind")
    validators.find(_.kind.equalsIgnoreCase(kind)).get
    // TODO:  If it is custom, create a validator class with reflection(如果是自定义的，就用反射创建一个validator类)
  }

}
