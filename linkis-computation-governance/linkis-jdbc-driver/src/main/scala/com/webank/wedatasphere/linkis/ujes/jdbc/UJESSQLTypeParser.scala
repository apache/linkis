package com.webank.wedatasphere.linkis.ujes.jdbc

/**
  * Created by leebai on 2019/8/23.
  */

import java.sql.{Timestamp, Types}


object UJESSQLTypeParser {
  def parserFromName(typeName: String): Int = {
    typeName match {
      case null => throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
      case "string" => Types.CHAR
      case "short" => Types.SMALLINT
      case "int" => Types.INTEGER
      case "long" => Types.BIGINT
      case "float" => Types.FLOAT
      case "double" => Types.DOUBLE
      case "boolean" => Types.BOOLEAN
      case "byte" => Types.TINYINT
      case "char" => Types.CHAR
      case "timestamp" => Types.TIMESTAMP
      case _ => throw new UJESSQLException(UJESSQLErrorCode.PREPARESTATEMENT_TYPEERROR)
    }
  }

  def parserFromVal(obj: Any): Int ={
    obj match {
      case _: String => Types.CHAR
      case _: Short => Types.SMALLINT
      case _: Int => Types.INTEGER
      case _: Long => Types.BIGINT
      case _: Float => Types.FLOAT
      case _: Double => Types.DOUBLE
      case _: Boolean => Types.BOOLEAN
      case _: Byte => Types.TINYINT
      case _: Char => Types.CHAR
      case _: Timestamp => Types.TIMESTAMP
      case _ => throw new UJESSQLException(UJESSQLErrorCode.PREPARESTATEMENT_TYPEERROR)
    }
  }
  def parserFromMetaData(dataType: Int): String = {
    dataType match {
      case Types.CHAR => "string"
      case Types.SMALLINT => "short"
      case Types.INTEGER => "int"
      case Types.BIGINT => "long"
      case Types.FLOAT => "float"
      case Types.DOUBLE => "double"
      case Types.BOOLEAN => "boolean"
      case Types.TINYINT => "byte"
      case Types.CHAR => "char"
      case Types.TIMESTAMP => "timestamp"
      case _ => throw new UJESSQLException(UJESSQLErrorCode.PREPARESTATEMENT_TYPEERROR)
    }
  }
}
