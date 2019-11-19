package com.webank.wedatasphere.linkis.metadata.ddl

import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO


trait SQLConst{
  val SPARK_SQL:String = "spark.sql"

  val LEFT_PARENTHESES:String = "("
  val RIGHT_PARENTHESES:String = ")"

  val COMMA:String = ","
  val SPACE:String = " "

  val COMMENT:String = "comment"

  val CREATE_TABLE = "create table "

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

