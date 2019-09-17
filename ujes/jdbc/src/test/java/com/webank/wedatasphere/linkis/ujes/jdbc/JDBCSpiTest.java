package com.webank.wedatasphere.linkis.ujes.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCSpiTest {
    private static UJESSQLConnection conn;
    public static UJESSQLConnection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver");
        conn = (UJESSQLConnection) DriverManager.getConnection("url","username","password");
        return conn;
    }
    @Test
    public void spiTest(){
        try {
            UJESSQLConnection conn = (UJESSQLConnection) DriverManager.getConnection("url","username","password");
            Assert.assertNotNull(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
