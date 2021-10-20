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
 
package org.apache.linkis.protocol.mdq

import java.util
trait MDQProtocol {

}


case class DDLRequest(params:util.Map[String, Object]) extends MDQProtocol

/**
  * 返回DDL语句
  */
case class DDLResponse(code:String) extends MDQProtocol

abstract class DDLExecute(code:String) extends MDQProtocol

/**
  * sparkEngine返回执行是否成功
  * @param status true is 成功， false is 失败
  * @param code 返回的代码
  */
case class DDLExecuteResponse(status:Boolean, code:String, user:String) extends DDLExecute(code:String)


/**
  * 返回是否在MDQ收尾成功，包括数据库插入等操作
  * @param status
  */
case class DDLCompleteResponse(status:Boolean) extends MDQProtocol