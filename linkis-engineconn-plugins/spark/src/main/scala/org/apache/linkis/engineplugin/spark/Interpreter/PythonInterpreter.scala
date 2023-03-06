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

package org.apache.linkis.engineplugin.spark.Interpreter

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{ClassUtils, Logging, Utils}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.storage.FSFactory

import org.apache.commons.io.IOUtils
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.catalyst.expressions.Attribute

import java.io._
import java.nio.file.Files

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 */

object PythonInterpreter {

  def pythonPath: String = {
    val pythonPath = new ArrayBuffer[String]
    val pythonHomePath = new File(SparkConfiguration.SPARK_HOME.getValue, "python").getPath
    val pythonParentPath = new File(pythonHomePath, "lib")
    pythonPath += pythonHomePath
    pythonParentPath
      .listFiles(new FileFilter {
        override def accept(pathname: File): Boolean = pathname.getName.endsWith(".zip")
      })
      .foreach(f => pythonPath += f.getPath)
    ClassUtils.jarOfClass(classOf[SparkContext]).foreach(pythonPath += _)
    pythonPath.mkString(File.pathSeparator)
  }

  def createFakeShell(): File = createFakeShell("python/fake_shell.py")

  def createFakeShell(script: String, fileType: String = ".py"): File = {
    val source: InputStream = getClass.getClassLoader.getResourceAsStream(script)

    val file = Files.createTempFile("", fileType).toFile
    file.deleteOnExit()

    val sink = new FileOutputStream(file)
    val buf = new Array[Byte](1024)
    var n = source.read(buf)

    while (n > 0) {
      sink.write(buf, 0, n)
      n = source.read(buf)
    }

    source.close()
    sink.close()

    file
  }

}

object SQLSession extends Logging {

  def showDF(sc: SparkContext, jobGroup: String, df: Any, maxResult: Int = Int.MaxValue): String = {
    val startTime = System.currentTimeMillis()

    val iterator = Utils.tryThrow(df.asInstanceOf[DataFrame].toLocalIterator)(t => {
      sc.clearJobGroup()
      t
    })

    var columns: List[Attribute] = null
    // get field names
    Utils.tryThrow({
      val qe = df.getClass.getMethod("queryExecution").invoke(df)
      val a = qe.getClass.getMethod("analyzed").invoke(qe)
      val seq = a.getClass.getMethod("output").invoke(a).asInstanceOf[Seq[Any]]
      columns = seq.toList.asInstanceOf[List[Attribute]]
    })(t => {
      sc.clearJobGroup()
      t
    })
    var schema = new StringBuilder
    schema ++= "%TABLE\n"
    val nameSet = new mutable.HashSet[String]()
    for (col <- columns) {
      nameSet.add(col.name)
      schema ++= col.name ++ "\t"
    }
    val trim = if (nameSet.size < columns.length) {
      var schemaWithAlis = new StringBuilder
      schemaWithAlis ++= "%TABLE\n"
      for (col <- columns) {
        val colName = col.qualifiedName
        schemaWithAlis ++= colName ++ "\t"
      }
      logger.info("I AM IN LESS")
      logger.info(schemaWithAlis.toString.trim)
      schemaWithAlis.toString.trim
    } else {
      logger.info("I AM IN MORE")
      logger.info(schema.toString.trim)
      schema.toString.trim
    }
    val msg = FSFactory.getFs("").write(new FsPath(""), true)
    msg.write(trim.getBytes("utf-8"))

    var index = 0
    Utils.tryThrow({
      while (index < maxResult && iterator.hasNext) {
        msg.write("\n".getBytes("utf-8"))
        val row = iterator.next()
        columns.indices.foreach { i =>
          if (row.isNullAt(i)) msg.write("NULL".getBytes("utf-8"))
          else msg.write(row.apply(i).asInstanceOf[Object].toString.getBytes("utf-8"))
          if (i != columns.size - 1) {
            msg.write("\t".getBytes("utf-8"))
          }
        }
        index += 1
      }
    })(t => {
      sc.clearJobGroup()
      t
    })
    val colCount = if (columns != null) columns.size else 0
    logger.warn(s"Fetched $colCount col(s) :  $index row(s).")
    sc.clearJobGroup()
    Utils.tryFinally({
      msg.flush()
      msg.toString
    }) { () => IOUtils.closeQuietly(msg) }
  }

}
