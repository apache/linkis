package com.webank.wedatasphere.linkis.ujes.jdbc;

/**
 * Created by leebai on 2019/8/23.
 */

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateConnection {

    private static UJESSQLConnection conn;

    public static UJESSQLConnection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver");
        conn = (UJESSQLConnection) DriverManager.getConnection("jdbc:linkis://hostname:port","username","password");
        return conn;
    }
}

