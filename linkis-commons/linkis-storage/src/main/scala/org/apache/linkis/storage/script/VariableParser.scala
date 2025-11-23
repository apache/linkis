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

package org.apache.linkis.storage.script

import java.util

import scala.collection.mutable.ArrayBuffer

object VariableParser {

  val CONFIGURATION: String = "configuration"
  val VARIABLE: String = "variable"
  val RUNTIME: String = "runtime"
  val STARTUP: String = "startup"
  val SPECIAL: String = "special"

  def getVariables(params: util.Map[String, Object]): Array[Variable] = {
    import scala.collection.JavaConverters._
    val variables = new ArrayBuffer[Variable]
    params
      .getOrDefault(VARIABLE, new util.HashMap[String, Object])
      .asInstanceOf[util.Map[String, Object]]
      .asScala
      .foreach(f => variables += Variable(VARIABLE, null, f._1, f._2.toString))
    params
      .getOrDefault(CONFIGURATION, new util.HashMap[String, Object])
      .asInstanceOf[util.Map[String, Object]]
      .asScala
      .foreach { f =>
        f._2
          .asInstanceOf[util.Map[String, Object]]
          .asScala
          .filter(s => !isContextIDINFO(s._1))
          .foreach(p =>
            p._2 match {
              case e: util.Map[String, Object] =>
                e.asScala
                  .filter(s => !isContextIDINFO(s._1))
                  .foreach(s => variables += Variable(f._1, p._1, s._1, s._2.toString))
              case _ =>
                if (null == p._2) {
                  variables += Variable(CONFIGURATION, f._1, p._1, "")
                } else {
                  variables += Variable(CONFIGURATION, f._1, p._1, p._2.toString)
                }
            }
          )
      }
    variables.toArray
  }

  // TODO need delete
  def isContextIDINFO(key: String): Boolean = {
    "contextID".equalsIgnoreCase(key) || "nodeName".equalsIgnoreCase(key)
  }

  def getMap(variables: Array[Variable]): util.Map[String, Object] = {
    import scala.collection.JavaConverters._
    val vars = new util.HashMap[String, String]
    val confs = new util.HashMap[String, Object]
    variables.filter(_.sort == null).foreach(f => vars.put(f.key, f.value))
    variables.filter(_.sort != null).foreach { f =>
      f.sort match {
        case STARTUP | RUNTIME | SPECIAL =>
          if (confs.get(f.sort) == null) {
            confs.put(f.sort, createMap(f))
          } else {
            confs.get(f.sort).asInstanceOf[util.HashMap[String, Object]].put(f.key, f.value)
          }
        case _ =>
          if (confs.get(f.sortParent) == null) {
            confs.put(f.sortParent, new util.HashMap[String, Object])
            confs
              .get(f.sortParent)
              .asInstanceOf[util.HashMap[String, Object]]
              .put(f.sort, createMap(f))
          } else {
            val subMap = confs.get(f.sortParent).asInstanceOf[util.HashMap[String, Object]]
            if (subMap.get(f.sort) == null) {
              subMap.put(f.sort, createMap(f))
            } else {
              subMap
                .get(f.sort)
                .asInstanceOf[util.HashMap[String, Object]]
                .put(f.key, f.value)
            }
          }
      }
    }
    val params = new util.HashMap[String, Object]
    if (vars.size() > 0) params.asScala += VARIABLE -> vars
    if (confs.size() > 0) params.asScala += CONFIGURATION -> confs
    params
  }

  private def createMap(variable: Variable): util.Map[String, Object] = {
    val map = new util.HashMap[String, Object]
    map.put(variable.key, variable.value)
    map
  }

}
