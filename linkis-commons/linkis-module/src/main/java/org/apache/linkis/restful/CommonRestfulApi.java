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

package org.apache.linkis.restful;

import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.netflix.discovery.DiscoveryManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "common api for all service")
@RestController
@RequestMapping(path = "/")
public class CommonRestfulApi {
  @Autowired private DiscoveryClient client;

  @ApiOperation(value = "Offline", notes = "offline this service", response = Message.class)
  @RequestMapping(path = "/offline", method = RequestMethod.GET)
  public Message offline(HttpServletRequest req) {
    DiscoveryManager.getInstance().shutdownComponent();
    return Message.ok().data("msg", "Offline successfully.");
  }
}
