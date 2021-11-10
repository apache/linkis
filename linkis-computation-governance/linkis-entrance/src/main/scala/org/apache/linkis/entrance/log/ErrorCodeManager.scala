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
 
package org.apache.linkis.entrance.log

import java.io.{BufferedReader, InputStreamReader}
import java.util
import java.util.concurrent.TimeUnit

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.errorcode.client.handler.LinkisErrorCodeHandler
import org.apache.linkis.entrance.errorcode.EntranceErrorConstants
import org.apache.linkis.storage.FSFactory
import javax.annotation.PostConstruct

import scala.collection.mutable.ArrayBuffer


abstract class ErrorCodeManager {

  def getErrorCodes: Array[ErrorCode]

  def errorMatch(log: String): Option[(String, String)] = {
    getErrorCodes.foreach(e => if(e.regex.findFirstIn(log).isDefined) {
      val matched = e.regex.unapplySeq(log)
      if(matched.nonEmpty)
        return Some(e.code -> e.message.format(matched.get:_*))
      else Some(e.code -> e.message)
    })
    None
  }
}



/**
 * this error code is from errorcode server
 */
object FlexibleErrorCodeManager extends ErrorCodeManager{

  private val errorCodeHandler = LinkisErrorCodeHandler.getInstance()

  override def getErrorCodes: Array[ErrorCode] = Array.empty

  override def errorMatch(log: String): Option[(String, String)] = {
    val errorCodes = errorCodeHandler.handle(log)
    if (errorCodes != null && errorCodes.size() > 0){
      Some(errorCodes.get(0).getErrorCode, errorCodes.get(0).getErrorDesc)
    } else{
      None
    }
  }
}



object Main{
  def main(args: Array[String]): Unit = {
  }
}


/**
  * RefreshableErrorCodeManager corresponds to FixedErrorCodeManager, and refresheyeErrorCodeManager can update its own errorCodes through the query module.
 * The purpose is to enable users to update the error code at any time by modifying the database.
  * RefreshableErrorCodeManager 与 FixedErrorCodeManager 是对应的，refreshaleErrorCodeManager可以通过query模块进行更新自身的errorCodes
  * 目的是为了能够让用户通过修改数据库随时更新错误码
  */
//
//object RefreshableErrorCodeManager extends ErrorCodeManager{
//  private val sender:Sender =
//    Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue, 1000 * 60 * 60, 100)
//
//  private val logger:Logger = LoggerFactory.getLogger(getClass)
//  private var errorCodes:Array[ErrorCode] = _
//
//  class FetchErrorCodeThread extends Runnable{
//    override def run(): Unit = {
//      val requestErrorCode = new RequestErrorCode
//      val responseErrorCode = sender.send(requestErrorCode).asInstanceOf[ResponseErrorCode]
//      Utils.tryAndWarnMsg{
//        val responseErrorCode = sender.send(requestErrorCode).asInstanceOf[ResponseErrorCode]
//        val status = responseErrorCode.getStatus
//        val message = responseErrorCode.getMessage
//        if (status != 0){
//          logger.warn(s"Error encounters when retrieve errorCodes from query module, reason: $message")
//        }else{
//          val errorCodeList = responseErrorCode.getResult
//          val arrayBuffer = new ArrayBuffer[ErrorCode]()
//          import scala.collection.JavaConversions._
//          errorCodeList foreach { errorCode =>
//            val regex = errorCode.getErrorRegex.r.unanchored
//            val errorCode_ = errorCode.getErrorCode
//            val errorDesc = errorCode.getErrorDesc
//            arrayBuffer += ErrorCode(regex, errorCode_, errorDesc)
//          }
//          errorCodes = arrayBuffer.toArray
//        }
//      }("Query ErrorCodes failed. You may check the cause or just ignore it ")
//    }
//  }
//
//  override def getErrorCodes: Array[ErrorCode] = errorCodes
//}





