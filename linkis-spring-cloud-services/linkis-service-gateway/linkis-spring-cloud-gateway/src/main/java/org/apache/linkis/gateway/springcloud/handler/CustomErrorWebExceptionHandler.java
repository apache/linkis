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

package org.apache.linkis.gateway.springcloud.handler;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Lists;

public class CustomErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
  public CustomErrorWebExceptionHandler(
      ErrorAttributes errorAttributes,
      ResourceProperties resourceProperties,
      ErrorProperties errorProperties,
      ApplicationContext applicationContext) {
    super(errorAttributes, resourceProperties, errorProperties, applicationContext);
  }

  /**
   * Get Exception Properties
   *
   * @param request
   * @param options
   */
  @Override
  public Map<String, Object> getErrorAttributes(
      ServerRequest request, ErrorAttributeOptions options) {
    Map<String, Object> errorAttributes = new LinkedHashMap<>();
    errorAttributes.put("data", Lists.newArrayList());
    errorAttributes.put("method", request.path());
    Throwable error = getError(request);
    MergedAnnotation<ResponseStatus> responseStatusAnnotation =
        MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .get(ResponseStatus.class);
    HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
    errorAttributes.put("status", errorStatus.value());
    errorAttributes.put("message", errorStatus.getReasonPhrase());

    return errorAttributes;
  }

  private HttpStatus determineHttpStatus(
      Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
    if (error instanceof ResponseStatusException) {
      return ((ResponseStatusException) error).getStatus();
    }
    return responseStatusAnnotation
        .getValue("code", HttpStatus.class)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Specify the response processing method as JSON processing method
   *
   * @param errorAttributes
   */
  @Override
  public RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  /**
   * Obtain the corresponding HttpStatus based on the status
   *
   * @param errorAttributes
   */
  @Override
  public int getHttpStatus(Map<String, Object> errorAttributes) {
    return (int) errorAttributes.get("status");
  }
}
