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

package com.webank.wedatasphere.linkis.engine.spark.utils

import java.io.{IOException, InputStream, OutputStream}
import java.net.ServerSocket
import java.text.SimpleDateFormat
import java.util.{Date, HashMap}

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration._
import com.webank.wedatasphere.linkis.engine.spark.common.LineBufferedProcess
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.storage.{FSFactory, LineMetaData}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetReader
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils

import scala.util.Random

/**
  * Created by allenlliu on 2018/11/19.
  */
object EngineUtils {
  private val user:String = System.getProperty("user.name")
  private var sparkVersion: String = _
  private  var fileSystem : com.webank.wedatasphere.linkis.common.io.Fs = _


  def getName:String = Sender.getThisServiceInstance.getInstance

  def findAvailPort = {
    val socket = new ServerSocket(0)
    Utils.tryFinally(socket.getLocalPort){ Utils.tryQuietly(socket.close())}
  }

  def sparkSubmitVersion(): String = {
    if(sparkVersion != null) {
      return sparkVersion
    }
    val sparkSubmit = CommonVars("wds.linkis.server.spark-submit", "spark-submit").getValue
    val pb = new ProcessBuilder(sparkSubmit, "--version")
    pb.redirectErrorStream(true)
    pb.redirectInput(ProcessBuilder.Redirect.PIPE)

    val process = new LineBufferedProcess(pb.start())
    val exitCode = process.waitFor()
    val output = process.inputIterator.mkString("\n")

    val regex = """version (.*)""".r.unanchored

    sparkVersion = output match {
      case regex(version) => version
      case _ => throw new IOException(f"Unable to determing spark-submit version [$exitCode]:\n$output")
    }
    sparkVersion
  }

  def jarOfClass(cls: Class[_]): Option[String] = {
    val uri = cls.getResource("/" + cls.getName.replace('.', '/') + ".class")
    if (uri != null) {
      val uriStr = uri.toString
      if (uriStr.startsWith("jar:file:")) {
        Some(uriStr.substring("jar:file:".length, uriStr.indexOf("!")))
      } else {
        None
      }
    } else {
      None
    }
  }

  def getTmpHDFSPath():String={
    val format = new SimpleDateFormat("yyyyMMddHHmm")
    var path = SPARK_OUTPUT_RESULT_DIR.getValue
    path = path+user+"/"+format.format(new Date)+"_"+Random.nextInt(1000)+".out"
    return path
  }
  def createOutputStream(path:String): OutputStream = {
    if (fileSystem == null) this synchronized {
      if (fileSystem == null){
        fileSystem = FSFactory.getFs(StorageUtils.HDFS)
        fileSystem.init(new HashMap[String, String]())
      }
    }
    val outputStream:OutputStream = fileSystem.write(new FsPath(path),true)
    //val inputStream = new FileInputStream(logPath)
    outputStream
  }
  def createInputStream(path:String): InputStream = {
    if (fileSystem == null) this synchronized {
      if (fileSystem == null){
        fileSystem = FSFactory.getFs(StorageUtils.HDFS)
        fileSystem.init(new HashMap[String, String]())
      }
    }
    val inputStream:InputStream = fileSystem.read(new FsPath(path))
    //val inputStream = new FileInputStream(logPath)
    inputStream
  }

  def getResultStrByDolphinTextContent(dolphinContent:String):String = {
    val resultSetReader = ResultSetReader.getResultSetReader(dolphinContent)
    resultSetReader.getMetaData match {
      case metadata:LineMetaData =>
        val sb = new StringBuilder
        while (resultSetReader.hasNext){
          sb.append(resultSetReader.getRecord).append("\n")
        }
        sb.toString()
      case _ => dolphinContent
    }
  }

}
