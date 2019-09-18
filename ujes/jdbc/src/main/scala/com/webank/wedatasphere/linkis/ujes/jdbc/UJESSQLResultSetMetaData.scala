package com.webank.wedatasphere.linkis.ujes.jdbc

/**
  * Created by leebai on 2019/8/19.
  */

import java.sql.{ResultSetMetaData, Types}
import java.util

class UJESSQLResultSetMetaData extends ResultSetMetaData {
  private val columnNameProperties: util.HashMap[Int,String] = new util.HashMap[Int,String]()

  private var dataTypeProperties: util.HashMap[Int,String] = new util.HashMap[Int,String]()

  private var commentProperties: util.HashMap[Int,String] = new util.HashMap[Int,String]()

  private[jdbc] def setColumnNameProperties(column: Int,columnName: String): Unit ={
    if(column != null && columnName != null){
      columnNameProperties.put(column,columnName)
    }else throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
  }

  private[jdbc] def setDataTypeProperties(column: Int,columnName: String): Unit ={
    if(column != null && columnName != null){
      dataTypeProperties.put(column,columnName)
    }else throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
  }

  private[jdbc] def setCommentPropreties(column: Int,columnName: String): Unit ={
    if(column != null && columnName != null){
      commentProperties.put(column,columnName)
    }else throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
  }

  override def getColumnCount: Int = {
    columnNameProperties.size
  }

  override def isAutoIncrement(column: Int): Boolean = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)
  }

  override def isCaseSensitive(column: Int): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def isSearchable(column: Int): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def isCurrency(column: Int): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def isNullable(column: Int): Int = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def isSigned(column: Int): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getColumnDisplaySize(column: Int): Int = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getColumnLabel(column: Int): String = {
    if(columnNameProperties.get(column) == null) {
      throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
    }else columnNameProperties.get(column)
  }

  override def getColumnName(column: Int): String = {
    getColumnLabel(column)
  }

  override def getSchemaName(column: Int): String = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getPrecision(column: Int): Int = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getScale(column: Int): Int = {
    columnNameProperties.size
  }

  override def getTableName(column: Int): String = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getCatalogName(column: Int): String = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getColumnType(column: Int): Int = {
    UJESSQLTypeParser.parserFromName(getColumnTypeName(column))
  }

  override def getColumnTypeName(column: Int): String = {
    if(dataTypeProperties.get(column) == null) {
      throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
    }else dataTypeProperties.get(column)
  }

  override def isReadOnly(column: Int): Boolean = {
    true
  }

  override def isWritable(column: Int): Boolean = {
    false
  }

  override def isDefinitelyWritable(column: Int): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def getColumnClassName(column: Int): String = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def unwrap[T](iface: Class[T]): T = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }

  override def isWrapperFor(iface: Class[_]): Boolean = {     throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA)   }
}
