package com.webank.wedatasphere.linkis.ujes.jdbc;

/**
 * Created by leebai on 2019/8/23.
 */
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateConnection {

    private static UJESSQLConnection conn;

    public static UJESSQLConnection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver");
        conn = (UJESSQLConnection) DriverManager.getConnection("jdbc:linkis://10.107.116.246:20817","johnnwang","Abcd1234");
        return conn;
    }
}

