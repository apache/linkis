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

package org.apache.linkis.rpc

import org.apache.linkis.rpc.transform.{JavaCollectionSerializer, JavaMapSerializer}

import org.apache.commons.lang3.ClassUtils

import java.lang.reflect.ParameterizedType
import java.util

import org.json4s.{CustomSerializer, DefaultFormats, Extraction}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.jackson.Serialization
import org.json4s.reflect.ManifestFactory

object RPCFormatsTest {

  trait ResultResource
  class AvailableResource(val ticketId: String) extends ResultResource

  object ResultResourceSerializer
      extends CustomSerializer[ResultResource](implicit formats =>
        (
          { case JObject(List(("AvailableResource", JObject(List(("ticketId", ticketId)))))) =>
            new AvailableResource(ticketId.extract[String])
          },
          { case r: AvailableResource =>
            ("AvailableResource", ("ticketId", Extraction.decompose(r.ticketId)))
          }
        )
      )

  def testRPC1(args: Array[String]): Unit = {
    implicit val formats = DefaultFormats + ResultResourceSerializer
    val serializerClasses = formats.customSerializers
      .map(_.getClass.getGenericSuperclass match {
        case p: ParameterizedType =>
          val params = p.getActualTypeArguments
          if (params == null || params.isEmpty) null
          else params(0).asInstanceOf[Class[_]]
      })
      .filter(_ != null)
    val a = new AvailableResource("aaa")
    val str = Serialization.write(a)
    println(str)
    val clazz = classOf[AvailableResource]
    println(serializerClasses)
    val realClass1 = serializerClasses.find(ClassUtils.isAssignable(clazz, _))
    println(realClass1)
    val realClass = realClass1.getOrElse(clazz)
    val obj = Serialization.read(str)(formats, ManifestFactory.manifestOf(realClass))
    println(obj)
    println(classOf[Array[_]].getClass.getName)
  }

  case class TestCollection1(a: String, list: java.util.List[String])
  case class TestCollection2(a: String, list: java.util.Map[String, Integer])

  def testRPC2(args: Array[String]): Unit = {
    implicit val formats = DefaultFormats + JavaCollectionSerializer + JavaMapSerializer
    //    val a = TestCollection1("1", new util.ArrayList[String]())
    val a = TestCollection2("1", new util.HashMap[String, Integer]())
    //    a.list.add("1111")
    a.list.put("1111", 2)
    val str = Serialization.write(a)
    println(str)
    val realClass = classOf[TestCollection2]
    val obj = Serialization.read(str)(formats, ManifestFactory.manifestOf(realClass))
    println(obj)
  }

  def main(args: Array[String]): Unit = {
    testRPC2(args)
  }

}
