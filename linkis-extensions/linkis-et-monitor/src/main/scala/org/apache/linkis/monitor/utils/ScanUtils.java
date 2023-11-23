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

package org.apache.linkis.monitor.utils;


public class ScanUtils {
    public static int getNumOfLines(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        int lines = 1;
        int len = str.length();
        for (int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if (c == '\r') {
                lines++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            } else if (c == '\n') {
                lines++;
            }
        }
        return lines;
    }

    public static int getFirstIndexSkippingLines(String str, Integer lines) {
        if (str == null || str.length() == 0 || lines < 0) {
            return -1;
        }
        if (lines == 0) {
            return 0;
        }

        int curLineIdx = 0;
        int len = str.length();
        for (int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if (c == '\r') {
                curLineIdx++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            } else if (c == '\n') {
                curLineIdx++;
            } else {
                continue;
            }

            if (curLineIdx >= lines) {
                return pos + 1;
            }
        }
        return -1;
    }
}
