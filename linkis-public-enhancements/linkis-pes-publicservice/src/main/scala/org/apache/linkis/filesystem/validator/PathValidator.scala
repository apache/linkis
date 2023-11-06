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

package org.apache.linkis.filesystem.validator

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration._
import org.apache.linkis.filesystem.exception.WorkSpaceException
import org.apache.linkis.filesystem.util.WorkspaceUtil
import org.apache.linkis.server
import org.apache.linkis.server.{catchIt, Message}
import org.apache.linkis.server.security.SecurityFilter
import org.apache.linkis.server.utils.ModuleUserUtils
import org.apache.linkis.storage.utils.StorageUtils

import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import java.io.File

import com.fasterxml.jackson.databind.JsonNode
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}
import org.aspectj.lang.reflect.MethodSignature

@Aspect
@Component
class PathValidator extends Logging {

  @Pointcut(
    "@annotation(org.springframework.web.bind.annotation.RequestMapping) && within(org.apache.linkis.filesystem.restful.api.*)"
  )
  def restfulResponseCatch1(): Unit = {}

  def getPath(args: Array[Object], paramNames: Array[String]): String = {
    var path: String = null
    var index: Int = paramNames.indexOf("path")
    if (index != -1) {
      path = args(index).asInstanceOf[String]
    } else {
      index = paramNames.indexOf("json")
      if (index != -1) {
        args(index) match {
          case j: JsonNode if j.get("path") != null => path = j.get("path").textValue()
          case m: java.util.Map[String, Object] if m.get("path") != null =>
            path = m.get("path").asInstanceOf[String]
          case _ =>
        }
      }
    }
    path
  }

  def getUserName(args: Array[Object], paramNames: Array[String]): String = {
    var username: String = null
    paramNames.indexOf("req") match {
      case -1 =>
      case index: Int =>
        val proxyUser = paramNames.indexOf("proxyUser")
        if (proxyUser == -1 || StringUtils.isEmpty(args(proxyUser))) {
          username = ModuleUserUtils.getOperationUser(args(index).asInstanceOf[HttpServletRequest])
        } else {
          // 增加proxyuser的判断
          username = args(proxyUser).toString
        }
    }
    username
  }

  def checkPath(path: String, username: String): Unit = {
    // unchecked hdfs,oss,s3
    if (
        (path.contains(StorageUtils.HDFS_SCHEMA)) || (path
          .contains(StorageUtils.OSS_SCHEMA)) || (path.contains(StorageUtils.S3_SCHEMA))
    ) {
      return
    }

    // 校验path的逻辑
    val userLocalRootPath: String = WorkspaceUtil.suffixTuning(LOCAL_USER_ROOT_PATH.getValue) +
      username
    var userHdfsRootPath: String =
      WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue) +
        username + HDFS_USER_ROOT_PATH_SUFFIX.getValue
    if (!(path.contains(StorageUtils.FILE_SCHEMA))) {
      throw new WorkSpaceException(80025, "the path should contain schema")
    }
    userHdfsRootPath = StringUtils.trimTrailingCharacter(userHdfsRootPath, File.separatorChar)
    if (path.contains("../")) {
      throw new WorkSpaceException(80026, "Relative path not allowed")
    }
    if (!(path.contains(userLocalRootPath)) && !(path.contains(userHdfsRootPath))) {
      throw new WorkSpaceException(
        80027,
        "The path needs to be within the user's own workspace path"
      )
    }
  }

  def validate(args: Array[Object], paramNames: Array[String]): Unit = {
    // 获取path:String,json:JsonNode,json:Map中的path 参数
    val path: String = getPath(args, paramNames)
    val username: String = getUserName(args, paramNames)
    if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(username)) {
      logger.info(String.format("user:%s,path:%s", username, path))
      checkPath(path, username)
    }
  }

  @Around("restfulResponseCatch1()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val resp: Message = server.catchIt {
      val signature = proceedingJoinPoint.getSignature.asInstanceOf[MethodSignature]
      logger.info("enter the path validator,the method is {}", signature.getName)
      if (FILESYSTEM_PATH_CHECK_TRIGGER.getValue) {
        logger.info("path check trigger is open,now check the path")
        validate(proceedingJoinPoint.getArgs, signature.getParameterNames)
      }
      Message.ok()
    }
    if (Message.messageToHttpStatus(resp) != 200) resp else proceedingJoinPoint.proceed()
  }

  def getCurrentHttpResponse: HttpServletResponse = {
    val requestAttributes = RequestContextHolder.getRequestAttributes
    if (requestAttributes.isInstanceOf[ServletRequestAttributes]) {
      val response = requestAttributes.asInstanceOf[ServletRequestAttributes].getResponse
      return response
    }
    null
  }

}

object PathValidator {
  val validator = new PathValidator()

  def validate(path: String, username: String): Unit = {
    validator.checkPath(path, username)
  }

}
