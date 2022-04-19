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
 
package org.apache.linkis.metadata.ddl

import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBO


trait SQLConst{
  val SPARK_SQL:String = "spark.sql"

  val LEFT_PARENTHESES:String = "("
  val RIGHT_PARENTHESES:String = ")"

  val COMMA:String = ","
  val SPACE:String = " "

  val COMMENT:String = "comment"

  val CREATE_TABLE = "create table if not exists "

  val PARTITIONED_BY = "partitioned by"

  val LINE_BREAK = "\n"

  val MARKS = """""""""

  val SINGLE_MARK:String = "'"

  val STORED_AS = "stored as"

  val SEMICOLON = """;"""


}


trait DDLCreator {


  val CODE:String = "code"
  val USER:String = "user"


  def createDDL(mdqTableInfo:MdqTableBO, user:String):String
}

