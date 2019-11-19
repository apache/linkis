package com.webank.wedatasphere.linkis.protocol.mdq


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