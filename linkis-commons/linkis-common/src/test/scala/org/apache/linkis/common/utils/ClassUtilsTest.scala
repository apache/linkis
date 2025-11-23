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

package org.apache.linkis.common.utils

import org.apache.commons.lang3.StringUtils

import java.util.Hashtable

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ClassUtilsTest {

  @Test private[utils] def testJarOfClass(): Unit = {

    val someClass = ClassUtils.jarOfClass(classOf[StringUtils])
    val uri = classOf[StringUtils].getResource(
      "/" +
        classOf[StringUtils].getName.replace('.', '/') + ".class"
    )
    assertEquals(
      Some(uri.toString.substring("jar:file:".length, uri.toString.indexOf("!"))),
      someClass
    )
  }

  @Test private[utils] def testGetClassInstance(): Unit = {
    val hashTable = new Hashtable[String, String]()
    assertTrue(
      ClassUtils
        .getClassInstance(hashTable.getClass.getName)
        .isInstanceOf[Hashtable[String, String]]
    )
  }

  @Test private[utils] def testGetFieldVal(): Unit = {
    val hashTable = new Hashtable[String, String]()
    var modCount = ClassUtils.getFieldVal(hashTable, "modCount")
    assertEquals(0, modCount)
    hashTable.put("abc", "123")
    modCount = ClassUtils.getFieldVal(hashTable, "modCount")
    assertEquals(1, modCount)
  }

  @Test private[utils] def testSetFieldVal(): Unit = {
    val hashTable = new Hashtable[String, String]()
    ClassUtils.setFieldVal(hashTable, "modCount", 3)
    var modCount = ClassUtils.getFieldVal(hashTable, "modCount")
    assertEquals(3, modCount)
    ClassUtils.setFieldVal(hashTable, "modCount", 0)
    modCount = ClassUtils.getFieldVal(hashTable, "modCount")
    assertEquals(0, modCount)
  }

  @Test private[utils] def testIsInterfaceOrAbstract(): Unit = {
    assertTrue(ClassUtils.isInterfaceOrAbstract(classOf[Cloneable]))
    assertFalse(ClassUtils.isInterfaceOrAbstract(classOf[String]))
    assertTrue(ClassUtils.isInterfaceOrAbstract(Class.forName("java.util.Dictionary")))
  }

}
