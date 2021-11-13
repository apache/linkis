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
 
package org.apache.linkis.entrance.restful

import java.util

import com.fasterxml.jackson.databind.JsonNode
import org.apache.linkis.server.Message
import javax.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.{PathVariable, RequestBody, RequestMapping, RequestMethod, RequestParam}


trait EntranceRestfulRemote {

  @RequestMapping(value = Array("/entrance/execute"), method = Array(RequestMethod.POST))
  def execute(req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Message

  @RequestMapping(value = Array("/entrance/submit"), method = Array(RequestMethod.POST))
  def submit(req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Message

  //  @RequestMapping(value = Array("/api/entrance/{id}"), method = Array(RequestMethod.GET))
//  def get(@PathVariable("id") id: String): Response

  @RequestMapping(value = Array("/entrance/{id}/status"), method = Array(RequestMethod.GET))
  def status(@PathVariable("id") id: String, @RequestParam(value="taskID",required = false) taskID:String): Message

  @RequestMapping(value = Array("/entrance/{id}/progress"), method = Array(RequestMethod.POST))
  def progress(@PathVariable("id") id: String): Message

  //TODO The resultSet interface is provided here, you need to think about it again, temporarily remove it.(resultSet这个接口是否在这里提供，还需再思考一下，先暂时去掉)
//  @RequestMapping(value = Array("/api/entrance/{id}/resultSet"), method = Array(RequestMethod.POST))
//  def resultSet(@PathVariable("id") id: String): Message

  @RequestMapping(value = Array("/entrance/{id}/log"), method = Array(RequestMethod.POST))
  def log(req: HttpServletRequest, @PathVariable("id") id: String): Message

  @RequestMapping(value = Array("/entrance/killJobs"), method = Array(RequestMethod.POST))
  def killJobs(req: HttpServletRequest, @RequestBody jsonNode: JsonNode): Message

  @RequestMapping(value = Array("/entrance/{id}/kill"), method = Array(RequestMethod.POST))
  def kill(@PathVariable("id") id: String, @RequestParam("taskID") taskID:scala.Long): Message

  @RequestMapping(value = Array("/entrance/{id}/pause"), method = Array(RequestMethod.POST))
  def pause(@PathVariable("id") id: String): Message



}