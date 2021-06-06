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
package com.webank.wedatasphere.linkis.bml.util;

import com.webank.wedatasphere.linkis.common.utils.Utils$;
import org.apache.hadoop.record.meta.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.AbstractFunction1;

import java.security.MessageDigest;

public class MD5Utils {

    private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private static final Logger logger = LoggerFactory.getLogger(MD5Utils.class);

    public static String getMD5(String s) {
       return Utils$.MODULE$.tryCatch(Utils$.MODULE$.JFunction0(() -> {
            byte[] btInput = s.getBytes("utf-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
                str[k++] = HEX_DIGITS[byte0 & 0xf];
            }
            return new String(str);
        }), new AbstractFunction1<Throwable, String>() {
            @Override
            public String apply(Throwable v1) {
                logger.error("create MD5 for failed, reason:", v1);
                return null;
            }
        });

    }

    public static String getMD5(byte[] btInput) {
        return Utils$.MODULE$.tryCatch(Utils$.MODULE$.JFunction0(() -> {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
                str[k++] = HEX_DIGITS[byte0 & 0xf];
            }
            return new String(str);
        }),new AbstractFunction1<Throwable, String>() {

            @Override
            public String apply(Throwable v1) {
                logger.error("create MD5 for failed, reason:", v1);
                return null;
            }
        });
    }


}
