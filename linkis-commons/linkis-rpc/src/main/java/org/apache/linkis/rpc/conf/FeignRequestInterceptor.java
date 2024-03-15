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

import org.apache.linkis.rpc.BaseRPCSender;
import org.apache.linkis.rpc.constant.RpcConstant;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SSOUtils$;
import org.apache.linkis.server.security.SecurityFilter$;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scala.Tuple2;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    Map<String, Collection<String>> headers = new HashMap<>(requestTemplate.headers());
    headers.put(
        RpcConstant.LINKIS_LOAD_BALANCER_TYPE,
        Arrays.asList(RpcConstant.LINKIS_LOAD_BALANCER_TYPE_RPC));
    Tuple2<String, String> userTicketKV =
        SSOUtils$.MODULE$.getUserTicketKV(SecurityFilter$.MODULE$.OTHER_SYSTEM_IGNORE_UM_USER());
    headers.put(userTicketKV._1, Arrays.asList(userTicketKV._2));
    try {
      String body =
          new String(
              requestTemplate.body(),
              org.apache.linkis.common.conf.Configuration.BDP_ENCODING().getValue());
      Message message = BDPJettyServerHelper.gson().fromJson(body, Message.class);
      headers.put(
          RpcConstant.FIXED_INSTANCE, Arrays.asList(BaseRPCSender.getFixedInstanceInfo(message)));
      requestTemplate.headers(headers);
    } catch (UnsupportedEncodingException e) {
    }
  }
}
