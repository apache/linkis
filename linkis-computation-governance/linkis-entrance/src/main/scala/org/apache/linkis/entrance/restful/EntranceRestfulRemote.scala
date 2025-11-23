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

package org.apache.linkis.entrance.restful

import org.apache.linkis.server.Message

import org.springframework.web.bind.annotation.{
  PathVariable,
  RequestBody,
  RequestMapping,
  RequestMethod,
  RequestParam
}

import javax.servlet.http.HttpServletRequest

import java.util

import com.fasterxml.jackson.databind.JsonNode

trait EntranceRestfulRemote {

  @RequestMapping(value = Array("/entrance/execute"), method = Array(RequestMethod.POST))
  def execute(req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Message

  @RequestMapping(value = Array("/entrance/submit"), method = Array(RequestMethod.POST))
  def submit(req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Message

  @RequestMapping(value = Array("/entrance/{id}/status"), method = Array(RequestMethod.GET))
  def status(
      req: HttpServletRequest,
      @PathVariable("id") id: String,
      @RequestParam(value = "taskID", required = false) taskID: String
  ): Message

  @RequestMapping(value = Array("/entrance/{id}/progress"), method = Array(RequestMethod.GET))
  def progress(req: HttpServletRequest, @PathVariable("id") id: String): Message

  @RequestMapping(
    value = Array("/entrance/{id}/progressWithResource"),
    method = Array(RequestMethod.GET)
  )
  def progressWithResource(req: HttpServletRequest, @PathVariable("id") id: String): Message

  @RequestMapping(value = Array("/entrance/{id}/log"), method = Array(RequestMethod.GET))
  def log(req: HttpServletRequest, @PathVariable("id") id: String): Message

  @RequestMapping(value = Array("/entrance/{id}/killJobs"), method = Array(RequestMethod.POST))
  def killJobs(
      req: HttpServletRequest,
      @RequestBody jsonNode: JsonNode,
      @PathVariable("id") strongExecId: String
  ): Message

  @RequestMapping(value = Array("/entrance/{id}/kill"), method = Array(RequestMethod.GET))
  def kill(
      req: HttpServletRequest,
      @PathVariable("id") id: String,
      @RequestParam("taskID") taskID: java.lang.Long
  ): Message

  @RequestMapping(value = Array("/entrance/{id}/pause"), method = Array(RequestMethod.GET))
  def pause(req: HttpServletRequest, @PathVariable("id") id: String): Message

}
