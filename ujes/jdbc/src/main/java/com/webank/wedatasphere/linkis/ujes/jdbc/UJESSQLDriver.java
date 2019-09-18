package com.webank.wedatasphere.linkis.ujes.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by leebai on 2019/8/23
 * Modified by owenxu on 2019/8/29
 */
public class UJESSQLDriver extends UJESSQLDriverMain implements Driver {
    static {
        try {
            DriverManager.registerDriver(new UJESSQLDriver());
        } catch (SQLException e) {
            Logger logger = LoggerFactory.getLogger(UJESSQLDriver.class);
            logger.info("Load driver failed",e);
        }
    }

    static String URL_PREFIX = "jdbc:linkis://";
    static String URL_REGEX = "jdbc:linkis://([^:]+)(:\\d+)?(/[^\\?]+)?(\\?\\S+)?";

    static String HOST = "HOST";
    static String PORT = "PORT";
    static String DB_NAME = "DBNAME";
    static String PARAMS = "PARAMS";

    static String USER = "user";
    static String PASSWORD = "password";

    static String VERSION = "version";
    static int DEFAULT_VERSION = 1;
    static String MAX_CONNECTION_SIZE = "maxConnectionSize";
    static String READ_TIMEOUT = "readTimeout";
    static String ENABLE_DISCOVERY = "enableDiscovery";
    static String ENABLE_LOADBALANCER = "enableLoadBalancer";
    static String CREATOR = "creator";

    static String VARIABLE_HEADER = "var:";
    static String PARAM_SPLIT = "&";
    static String KV_SPLIT = "=";

}
