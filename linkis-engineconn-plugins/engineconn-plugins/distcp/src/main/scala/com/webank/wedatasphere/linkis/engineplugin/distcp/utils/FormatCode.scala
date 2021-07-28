/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.distcp.utils

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineplugin.distcp.exception.{DistcpFormatJsonException}
import com.webank.wedatasphere.linkis.engineplugin.distcp.metastore.impl.{MysqlServiceImpl}
import scala.collection.mutable.ArrayBuffer

case class Code(sourceTabs:Array[String],sourceDirs:Array[String],targetDir:String,ignore:Boolean,
                     log:String,extraInfo:Boolean,numMaps:Int,overwrite:Boolean,preserve:Array[String],update:Boolean,append:Boolean,
                     delete:Boolean,strategy:String,bandwidth:Int,async:Boolean,diff:Array[String],
                     rdiff:Array[String],numListstatusThreads:Int,skipcrccheck:Boolean,blocksperchunk:Int,
                     copybuffersize:String,xtrack:String,direct:Boolean,useiterator:Boolean,mysqlParams:java.util.HashMap[String,String])



class FormatCode(command: Code) extends Logging{
  val BLANK = " "
  val codes = ArrayBuffer[String]()
  trait ValueFormat[T,String]{def format(a:T,b:String):Unit}
  implicit val stringFormat:ValueFormat[String,String] ={new ValueFormat[String,String] {
    override def format(A:String,B:String) = {
      if(A != null) {
        if(B.length>0) codes+=B
        codes += A
      }
    }
  }}
  implicit val intFormat:ValueFormat[Int,String] ={new ValueFormat[Int,String] {
    override def format(A:Int,B:String)= {
      if(A != 0)
        codes += B += A
    }
  }}
  implicit val booleanFormat:ValueFormat[Boolean,String] ={new ValueFormat[Boolean,String] {
    override def format(A:Boolean,B:String)= {
      if(A)
        codes += B
    }
  }}
  implicit val arrayFormat:ValueFormat[Array[String],String] ={new ValueFormat[Array[String],String] {
    override def format(A:Array[String],B:String)= {
      if(A != null) {
        if(B.length>0)
          codes += B
        A.foreach(codes += _)
      }
    }
  }}
  implicit def covertIntToString(A:Int):String = A.toString

  object build {
    def format[A,String](V1: A,V2:String)(implicit w: ValueFormat[A,String]): Unit = w.format(V1,V2)
  }

  require(command.targetDir!=null,"not find any target dir")
  codes+="hadoop"+="distcp"
  build.format(command.ignore,"-i")
  build.format(command.log,"-log")
  build.format(command.extraInfo,"-v")
  build.format(command.numMaps,"-m")
  build.format(command.overwrite,"-overwrite")
  build.format(command.update,"-update")
  build.format(command.append,"-append")
  build.format(command.delete,"-delete")
  build.format(getPreserveInfo(command.preserve),"")
  build.format(command.diff,"-diff")
  build.format(command.rdiff,"-rdiff")
  build.format(command.strategy,"-strategy")
  build.format(command.bandwidth,"-bandwidth")
  build.format(command.async,"-async")
  build.format(command.numListstatusThreads,"-numListstatusThreads")
  build.format(command.skipcrccheck,"-skipcrccheck")
  build.format(command.blocksperchunk,"-blocksperchunk")
  build.format(command.copybuffersize,"-copybuffersize")
  build.format(command.xtrack,"-xtrack")
  build.format(command.direct,"-direct")
  build.format(command.useiterator,"-useiterator")
  build.format(getSourceDirs,"")
  build.format(command.targetDir,"")




  def getSourceDirs():Array[String]={
    var sourceDirs:Array[String] = null
    if(command.sourceTabs != null){
      require(command.mysqlParams != null,"mysql params is Null")
      val url = command.mysqlParams.get("url");
      val username = command.mysqlParams.get("username")
      val password = command.mysqlParams.get("password")
      require(url != null ,"mysql url must not be null")
      require(username != null ,"mysql username must not be null")
      require(password != null ,"mysql password must not be null")

      import  scala.collection.JavaConverters._
      sourceDirs = new MysqlServiceImpl(url,username,password).getTablePaths(command.sourceTabs).asScala.toArray
    }
    if(command.sourceDirs != null){
      if(sourceDirs == null){
        sourceDirs = command.sourceDirs
      }else{
        sourceDirs++command.sourceDirs
      }
    }
    if(sourceDirs.length <= 0){
      throw DistcpFormatJsonException(90203,"not find any source dirs")
    }
    sourceDirs
  }

  def getPreserveInfo(preserve:Array[String]): String ={
    if(preserve == null) return null
    var info = "-p"
    if(preserve.length == 0) return info
    preserve.foreach(_ match {
      case "replication" => info+="r"
      case "block" => info+="b"
      case "user" => info+="u"
      case "group" => info+="g"
      case "permission" => info+="p"
      case "checksum" => info+="c"
      case "acl" => info+="a"
      case "xattr" => info+="x"
      case "timestamp" => info+="t"
    })
    info
  }

}

object FormatCode{
  def apply(code:Code) = new FormatCode(code)
}
