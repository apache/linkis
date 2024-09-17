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

package org.apache.linkis.engineplugin.spark.utils

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.{
  BigIntVector,
  BitVector,
  FieldVector,
  Float8Vector,
  IntVector,
  VarCharVector,
  VectorSchemaRoot
}
import org.apache.arrow.vector.ipc.ArrowStreamWriter
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{BooleanType, DoubleType, IntegerType, LongType, StringType}

import java.io.ByteArrayOutputStream

object ArrowUtils {

  def toArrow(df: DataFrame): Array[Byte] = {
    val allocator = new RootAllocator(Long.MaxValue)
    val (root, fieldVectors) = createArrowVectors(df, allocator)
    val outStream = new ByteArrayOutputStream()
    val writer = new ArrowStreamWriter(root, null, outStream)

    writer.start()
    writer.writeBatch()
    writer.end()
    writer.close()

    val arrowBytes = outStream.toByteArray
    fieldVectors.foreach(_.close())
    allocator.close()
    arrowBytes
  }

  private def createArrowVectors(
      df: DataFrame,
      allocator: RootAllocator
  ): (VectorSchemaRoot, List[FieldVector]) = {
    val schema = df.schema
    val fieldVectors = schema.fields.map { field =>
      field.dataType match {
        case IntegerType =>
          val vector = new IntVector(field.name, allocator)
          vector.allocateNew(df.count().toInt)
          vector
        case LongType =>
          val vector = new BigIntVector(field.name, allocator)
          vector.allocateNew(df.count().toInt)
          vector
        case DoubleType =>
          val vector = new Float8Vector(field.name, allocator)
          vector.allocateNew(df.count().toInt)
          vector
        case BooleanType =>
          val vector = new BitVector(field.name, allocator)
          vector.allocateNew(df.count().toInt)
          vector
        case _ =>
          val vector: VarCharVector = new VarCharVector(field.name, allocator)
          vector.allocateNew(df.count().toInt)
          vector
      }
    }.toList

    df.collect().zipWithIndex.foreach { case (row, i) =>
      for (j <- fieldVectors.indices) {
        val vector = fieldVectors(j)
        row.schema.fields(j).dataType match {
          case IntegerType => vector.asInstanceOf[IntVector].setSafe(i, row.getInt(j))
          case LongType => vector.asInstanceOf[BigIntVector].setSafe(i, row.getLong(j))
          case DoubleType => vector.asInstanceOf[Float8Vector].setSafe(i, row.getDouble(j))
          case BooleanType =>
            vector.asInstanceOf[BitVector].setSafe(i, if (row.getBoolean(j)) 1 else 0)
          case StringType =>
            vector.asInstanceOf[VarCharVector].setSafe(i, row.getString(j).getBytes)
          case _ =>
            throw new IllegalArgumentException(
              s"Unsupported data type: ${row.schema.fields(j).dataType}"
            )
        }
      }
    }

    val javaFieldVectors: java.lang.Iterable[FieldVector] =
      fieldVectors
        .map(_.asInstanceOf[FieldVector])
        .iterator
        .asInstanceOf[java.lang.Iterable[FieldVector]]
    val root = new VectorSchemaRoot(javaFieldVectors)

    (root, fieldVectors)
  }

}
