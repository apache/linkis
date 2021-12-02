package com.alibaba.datax.core.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.core.util.container.CoreConstant;

import java.io.*;
import java.util.Properties;

/**
 * Created by jingxing on 14/12/15.
 */
public class LdapUtil {
    private static Properties properties;


    public static synchronized Properties getLdapProperties() {
        if (properties == null && new File(CoreConstant.DATAX_LDAP_PATH).exists()) {
            InputStream secretStream = null;
            try {
                secretStream = new FileInputStream(
                        CoreConstant.DATAX_LDAP_PATH);
            } catch (FileNotFoundException e) {
                throw DataXException.asDataXException(
                        FrameworkErrorCode.LDAP_ERROR,
                        "DataX LDAP配置错误");
            }

            properties = new Properties();
            try {
                properties.load(secretStream);
                secretStream.close();
            } catch (IOException e) {
                throw DataXException.asDataXException(
                        FrameworkErrorCode.SECRET_ERROR, "读取LDAP置文件出错", e);
            }
        }

        return properties;
    }
}
