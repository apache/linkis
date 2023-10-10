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

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

  @Override
  public Map<String, Object> getErrorAttributes(
      ServerRequest request, ErrorAttributeOptions options) {
    Throwable throwable = this.getError(request);
    MergedAnnotation<ResponseStatus> responseStatusAnnotation =
        MergedAnnotations.from(
                throwable.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .get(ResponseStatus.class);
    HttpStatus errorStatus = determineHttpStatus(throwable, responseStatusAnnotation);
    Map<String, Object> map = new HashMap<>();
    map.put("method", request.path());
    map.put("status", errorStatus.value());
    String msg = errorStatus.getReasonPhrase();
    if (errorStatus.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
      msg = msg + ", with request path:" + request.path();
    }
    map.put("message", msg);
    map.put("data", Lists.newArrayList());

    return map;
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
}
