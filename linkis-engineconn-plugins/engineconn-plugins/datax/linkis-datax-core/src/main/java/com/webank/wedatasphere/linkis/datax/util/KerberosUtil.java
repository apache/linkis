/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datax.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.CoreConstant;

import java.io.*;
import java.util.Properties;

/**
 * For kerberos connection
 * @author davidhua
 * 2020/4/23
 */
public class KerberosUtil {

    private static Properties properties;

    public static synchronized Properties getProperties(){
        if(null == properties && new File(CoreConstant.DATAX_KERBEROS_PATH).exists()){
            try (InputStream inputStream = new FileInputStream(CoreConstant.DATAX_LDAP_PATH)) {
                Properties props = new Properties();
                props.load(inputStream);
                properties = props;
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) {
                    //Do nothing, just return null properties
                    return properties;
                }
                throw DataXException.asDataXException(FrameworkErrorCode.SECRET_ERROR,
                        "Fail to read kerberos config file", e);
            }
            //Ignore
        }
        return properties;
    }


}
