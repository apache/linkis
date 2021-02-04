package com.webank.wedatasphere.linkis.ujes.jdbc;

import java.sql.*;

/**
 * Created by johnnwang on 2019/12/11.
 */
public class JDBCTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        //1. 加载驱动类：com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver
        Class.forName("com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver");

        //2. 获得连接：jdbc:linkis://gatewayIP:gatewayPort   帐号和密码对应前端的帐号密码
        Connection connection =  DriverManager.getConnection("jdbc:linkis://127.0.0.1:9001","username","password");

        //3. 创建statement 和执行查询
        Statement st= connection.createStatement();
        ResultSet rs=st.executeQuery("show tables");
        //4.处理数据库的返回结果(使用ResultSet类)
        while (rs.next()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print(metaData.getColumnName(i) + ":" +metaData.getColumnTypeName(i)+": "+ rs.getObject(i) + "    ");
            }
            System.out.println();
        }
        //关闭资源
        rs.close();
        st.close();
        connection.close();
    }
}
