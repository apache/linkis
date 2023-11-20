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

package org.apache.linkis.rpc.conf;

import org.apache.linkis.rpc.constant.RpcConstant;
import org.apache.linkis.server.security.SSOUtils$;
import org.apache.linkis.server.security.SecurityFilter$;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;

import scala.Tuple2;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class FeignConfig implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (null != attributes) {
      HttpServletRequest request = attributes.getRequest();
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        String value = request.getHeader(name);
        if (name.equalsIgnoreCase("content-length")) {
          continue;
        }
        requestTemplate.header(name, value);
      }
      requestTemplate.header(
          RpcConstant.LINKIS_LOAD_BALANCER_TYPE, RpcConstant.LINKIS_LOAD_BALANCER_TYPE_RPC);
      Tuple2<String, String> userTicketKV =
          SSOUtils$.MODULE$.getUserTicketKV(SecurityFilter$.MODULE$.OTHER_SYSTEM_IGNORE_UM_USER());
      requestTemplate.header(userTicketKV._1, userTicketKV._2);
    }
  }
}
