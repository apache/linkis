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

package org.apache.linkis.entrance.utils

import org.apache.linkis.common.utils.Logging

import java.util.regex.Pattern

object SafeUtils extends Logging {

  private val DANGEROUS_CODE_PATTERN = "/etc/passwd|" +
    "/etc/shadow|" +
    "/etc/group|" +
    "open\\(\\s*['\"]/etc/[^'\"]+['\"]\\s*,|" +
    "subprocess|" +
    "os\\.system|" +
    "os\\.popen|" +
    "shutil\\.execute|" +
    "eval|`.*?`|" +
    "import\\s+os\\.env|" +
    "import\\s+os\\.getlogin|" +
    "import\\s+os\\.getpid|" +
    "import\\s+os\\.getppid|" +
    "import\\s+os\\.getcwd|" +
    "import\\s+os\\.getexecname|" +
    "import\\s+os\\.startfile|" +
    "import\\s+os\\.fork|" +
    "import\\s+os\\.kill|" +
    "import\\s+os\\.wait|" +
    "import\\s+os\\.waitpid|" +
    "import\\s+os\\.wait3|" +
    "import\\s+os\\.wait4|" +
    "import\\s+os\\.confstr|" +
    "import\\s+os\\.sysconf|" +
    "import\\s+os\\.uname|" +
    "import\\s+os\\.urandom|" +
    "import\\s+os\\.chroot|" +
    "import\\s+os\\.setuid|" +
    "import\\s+os\\.setgid|" +
    "import\\s+os\\.setgroups|" +
    "import\\s+os\\.initgroups|" +
    "import\\s+os\\.getgrouplist|" +
    "import\\s+os\\.getlogin|" +
    "import\\s+os\\.getpgid|" +
    "import\\s+os\\.getpgrp|" +
    "import\\s+os\\.getsid|" +
    "import\\s+os\\.setpgid|" +
    "import\\s+os\\.setpgrp|" +
    "import\\s+os\\.setsid|" +
    "import\\s+os\\.forkpty|" +
    "import\\s+os\\.setreuid|" +
    "import\\s+os\\.setregid|" +
    "import\\s+os\\.getresuid|" +
    "import\\s+os\\.getresgid"

  private val ANNOTATION_PATTERN = "\\s*#.*$"

  private val SAFETY_PASS = "SAFETY_PASS"

  def isCodeSafe(code: String): Boolean = {
    var isSafe = true
    // 在匹配高危代码前，先移除注释
    val commentPattern = Pattern.compile(ANNOTATION_PATTERN, Pattern.MULTILINE)
    val cleanCode = commentPattern.matcher(code).replaceAll("")
    val code_pattern =
      Pattern.compile(DANGEROUS_CODE_PATTERN, Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
    val code_matcher = code_pattern.matcher(cleanCode)
    while (code_matcher.find) {
      isSafe = false
      val mather = commentPattern.matcher(code)
      while (mather.find)
        if (mather.group.toUpperCase().contains(SAFETY_PASS)) isSafe = true
    }
    isSafe
  }

}
