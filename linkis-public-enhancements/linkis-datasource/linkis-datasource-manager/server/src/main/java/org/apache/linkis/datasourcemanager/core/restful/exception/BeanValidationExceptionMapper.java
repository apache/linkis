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

package org.apache.linkis.datasourcemanager.core.restful.exception;

import org.apache.linkis.server.Message;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.ext.Provider;

/** Map bean validation exception to response */
@Provider
public class BeanValidationExceptionMapper {
  public Message toResponse(ValidationException exception) {
    StringBuilder stringBuilder = new StringBuilder();
    ((ConstraintViolationException) exception)
        .getConstraintViolations()
        .forEach(
            constraintViolation ->
                stringBuilder.append(constraintViolation.getMessage()).append(";"));
    return Message.error("Bean validation error[实例校验出错], detail:" + stringBuilder.toString());
  }
}
