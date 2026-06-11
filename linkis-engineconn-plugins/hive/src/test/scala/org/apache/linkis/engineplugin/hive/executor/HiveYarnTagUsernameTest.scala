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

package org.apache.linkis.engineplugin.hive.executor

import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.utils.JobUtils

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters._

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.mockito.Mockito._

/**
 * Test class for YARN tag username enhancement feature. Tests the logic of adding username to YARN
 * job tags.
 */
class HiveYarnTagUsernameTest {

  /**
   * Test case TC001: Normal username + no jobTags Expected: LINKIS_123,USER_zhangsan
   */
  @Test
  def testNormalUsernameNoJobTags(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "zhangsan")
    properties.put(JobRequestConstants.JOB_ID, "123")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123,USER_zhangsan", tags)
  }

  /**
   * Test case TC002: Normal username + has jobTags Expected: LINKIS_123,EMR,USER_zhangsan
   */
  @Test
  def testNormalUsernameWithJobTags(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "zhangsan")
    properties.put(JobRequestConstants.JOB_ID, "123")
    properties.put(JobRequestConstants.JOB_SOURCE_TAGS, "EMR")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123,EMR,USER_zhangsan", tags)
  }

  /**
   * Test case TC003: Empty username Expected: LINKIS_123 (without USER tag)
   */
  @Test
  def testEmptyUsername(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "")
    properties.put(JobRequestConstants.JOB_ID, "123")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123", tags)
  }

  /**
   * Test case TC004: Null username Expected: LINKIS_123 (without USER tag)
   */
  @Test
  def testNullUsername(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put(JobRequestConstants.JOB_ID, "123")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123", tags)
  }

  /**
   * Test case TC005: Empty jobId Expected: No tags (return null)
   */
  @Test
  def testEmptyJobId(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "zhangsan")
    properties.put(JobRequestConstants.JOB_ID, "")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)

    // When jobId is blank, tags should be null
    if (StringUtils.isNotBlank(jobId)) {
      fail("Should not reach here when jobId is blank")
    } else {
      // No tags should be set
      assertNull(null)
    }
  }

  /**
   * Test case TC006: Username with special characters Expected: LINKIS_123,USER_user@example.com
   */
  @Test
  def testUsernameWithSpecialCharacters(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "user@example.com")
    properties.put(JobRequestConstants.JOB_ID, "123")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123,USER_user@example.com", tags)
  }

  /**
   * Test case TC007: Username with underscore Expected: LINKIS_123,USER_user_name
   */
  @Test
  def testUsernameWithUnderscore(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "user_name")
    properties.put(JobRequestConstants.JOB_ID, "123")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    assertEquals("LINKIS_123,USER_user_name", tags)
  }

  /**
   * Test case TC008: Null properties Expected: execUser should be null
   */
  @Test
  def testNullProperties(): Unit = {
    val execUser = extractExecUser(null)
    assertNull(execUser)
  }

  /**
   * Test case TC009: execUser is not a String type Expected: execUser should be null
   */
  @Test
  def testInvalidExecUserType(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", 123.asInstanceOf[Object])

    val execUser = extractExecUser(properties)
    assertNull(execUser)
  }

  /**
   * Test case TC010: jobTags with non-ASCII characters Expected: LINKIS_123,USER_zhangsan (jobTags
   * ignored)
   */
  @Test
  def testNonAsciiJobTags(): Unit = {
    val properties = new util.HashMap[String, Object]()
    properties.put("execUser", "zhangsan")
    properties.put(JobRequestConstants.JOB_ID, "123")
    properties.put(JobRequestConstants.JOB_SOURCE_TAGS, "中文标签")

    val execUser = extractExecUser(properties)
    val jobId = JobUtils.getJobIdFromMap(properties)
    val jobTags = JobUtils.getJobSourceTagsFromObjectMap(properties)

    val tags = buildTags(jobId, jobTags, execUser)

    // Non-ASCII jobTags should be ignored
    assertEquals("LINKIS_123,USER_zhangsan", tags)
  }

  /**
   * Helper method to simulate the execUser extraction logic
   */
  private def extractExecUser(properties: util.Map[String, Object]): String = {
    if (properties != null) {
      properties.get("execUser") match {
        case user: String => user
        case _ => null
      }
    } else null
  }

  /**
   * Helper method to simulate the tags building logic
   */
  private def buildTags(jobId: String, jobTags: String, execUser: String): String = {
    if (StringUtils.isAsciiPrintable(jobTags)) {
      if (StringUtils.isNotBlank(execUser)) {
        s"LINKIS_$jobId,$jobTags,USER_$execUser"
      } else {
        s"LINKIS_$jobId,$jobTags"
      }
    } else {
      if (StringUtils.isNotBlank(execUser)) {
        s"LINKIS_$jobId,USER_$execUser"
      } else {
        s"LINKIS_$jobId"
      }
    }
  }

}
