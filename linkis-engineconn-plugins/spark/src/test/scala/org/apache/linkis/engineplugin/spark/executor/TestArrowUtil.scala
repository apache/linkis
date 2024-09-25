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

package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.engineplugin.spark.utils.ArrowUtils

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.VectorSchemaRoot
import org.apache.arrow.vector.ipc.ArrowStreamReader
import org.apache.spark.sql.SparkSession

import java.io.ByteArrayInputStream

import org.junit.jupiter.api.{Assertions, Test}

class TestArrowUtil {

  @Test
  def testToArrow(): Unit = {
    val sparkSession = SparkSession
      .builder()
      .master("local[1]")
      .appName("testToArrow")
      .getOrCreate()
    val dataFrame = sparkSession
      .createDataFrame(
        Seq(("test1", 23, 552214221L), ("test2", 19, 41189877L), ("test3", 241, 1555223L))
      )
      .toDF("name", "age", "id")
    val arrowBytes = ArrowUtils.toArrow(dataFrame)

    // read arrow bytes for checking
    val allocator = new RootAllocator(Long.MaxValue)
    val byteArrayInputStream = new ByteArrayInputStream(arrowBytes)
    val streamReader = new ArrowStreamReader(byteArrayInputStream, allocator)

    try {
      val root: VectorSchemaRoot = streamReader.getVectorSchemaRoot
      val expectedData =
        Seq(("test1", 23, 552214221L), ("test2", 19, 41189877L), ("test3", 241, 1555223L))

      var rowIndex = 0
      while (streamReader.loadNextBatch()) {
        for (i <- 0 until root.getRowCount) {
          val name = root.getVector("name").getObject(i).toString
          val age = root.getVector("age").getObject(i).asInstanceOf[Int]
          val id = root.getVector("id").getObject(i).asInstanceOf[Long]

          val (expectedName, expectedAge, expectedId) = expectedData(rowIndex)
          Assertions.assertEquals(name, expectedName)
          Assertions.assertEquals(age, expectedAge)
          Assertions.assertEquals(id, expectedId)
          rowIndex += 1
        }
      }
      Assertions.assertEquals(rowIndex, expectedData.length)
    } finally {
      streamReader.close()
      allocator.close()
    }
  }

}
