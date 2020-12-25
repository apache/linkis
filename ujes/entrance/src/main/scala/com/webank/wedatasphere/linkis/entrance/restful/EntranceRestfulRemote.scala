/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.restful

import java.util

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.QueryParam
import javax.ws.rs.core.{Context, Response}
import org.springframework.web.bind.annotation.{PathVariable, RequestBody, RequestMapping, RequestMethod}

/**
  * Created by enjoyyin on 2018/9/3.
  */
trait EntranceRestfulRemote {

  @RequestMapping(value = Array("/entrance/execute"), method = Array(RequestMethod.POST))
  def execute(@Context req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Response

//  @RequestMapping(value = Array("/api/entrance/{id}"), method = Array(RequestMethod.GET))
//  def get(@PathVariable("id") id: String): Response

  @RequestMapping(value = Array("/entrance/{id}/status"), method = Array(RequestMethod.GET))
  def status(@PathVariable("id") id: String, @QueryParam("taskID") taskID:String): Response


  @RequestMapping(value = Array("/entrance/{id}/progress"), method = Array(RequestMethod.POST))
  def progress(@PathVariable("id") id: String): Response

  //TODO The resultSet interface is provided here, you need to think about it again, temporarily remove it.(resultSet这个接口是否在这里提供，还需再思考一下，先暂时去掉)
//  @RequestMapping(value = Array("/api/entrance/{id}/resultSet"), method = Array(RequestMethod.POST))
//  def resultSet(@PathVariable("id") id: String): Response

  @RequestMapping(value = Array("/entrance/{id}/log"), method = Array(RequestMethod.POST))
  def log(@Context req: HttpServletRequest, @PathVariable("id") id: String): Response

  @RequestMapping(value = Array("/entrance/{id}/kill"), method = Array(RequestMethod.POST))
  def kill(@PathVariable("id") id: String, @QueryParam("taskID") taskID:Long): Response

  @RequestMapping(value = Array("/entrance/{id}/pause"), method = Array(RequestMethod.POST))
  def pause(@PathVariable("id") id: String): Response

  @RequestMapping(value = Array("/entrance/backgroundservice"), method = Array(RequestMethod.POST))
  def backgroundservice(@Context req: HttpServletRequest, @RequestBody json: util.Map[String, Any]): Response


}