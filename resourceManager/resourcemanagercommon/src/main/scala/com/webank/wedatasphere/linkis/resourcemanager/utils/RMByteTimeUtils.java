/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.resourcemanager.utils;

import java.util.Locale;

public class RMByteTimeUtils {

    /**
     * Convert a quantity in bytes to a human-readable string such as "4.0 MB".
     */
    public static String bytesToString(long size) {
        long TB = 1L << 40;
        long GB = 1L << 30;
        long MB = 1L << 20;
        long KB = 1L << 10;

        double value;
        String unit;
        if (size >= 2*TB || -2*TB >= size) {
            value = size * 1f / TB;
            unit = "TB";
        } else if (size >= 2*GB || -2*GB >= size) {
            value = size * 1f / GB;
            unit = "GB";
        } else if (size >= 2*MB || -2*MB >= size) {
            value = size * 1f / MB;
            unit = "MB";
        } else if (size >= 2*KB || -2*KB >= size) {
            value = size * 1f / KB;
            unit = "KB";
        } else {
            value = size * 1f;
            unit = "B";
        }
        return String.format(Locale.US, "%.1f %s", value, unit);
    }
    
}
