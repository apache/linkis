/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.utils

import java.lang.reflect

import scala.reflect.ClassTag


object ArrayUtils {

  def newArray[T](newLength: Int, clazz: Class[_ <: Array[T]]): Array[T] =
    reflect.Array.newInstance(clazz.getComponentType, newLength) match {
      case destArray: Array[T] => destArray
    }

  def copyArray[T](array: Array[T], newLength: Int): Array[T] = {
    val destArray = newArray[T](newLength, array.getClass)
    if(null == array || array.isEmpty) return destArray
    System.arraycopy(array, 0, destArray, 0,
      Math.min(array.length, newLength))
    destArray
  }

  def copyArray[T](array: Array[T]): Array[T] =
    copyArray[T](array, array.length)

  def copyArrayWithClass[T](array: Seq[T], clazz: Class[_ <:T]): Array[T] = {
    val destArray = reflect.Array.newInstance(clazz, array.length) match {
      case destArray: Array[T] => destArray
    }
    if(null == array || array.isEmpty) return destArray
    for(i <- array.indices)
      destArray(i) = array(i)
    destArray
  }

  def copyScalaArray[T: ClassTag](array: Seq[T], newLength: Int): Array[T] = {
    val fill: Int => T = index => if(index < array.length) array(index) else null.asInstanceOf[T]
    Array.tabulate(newLength)(fill)
  }

  def copyScalaArray[T: ClassTag](array: Seq[T]): Array[T] = copyScalaArray(array, array.length)

}
