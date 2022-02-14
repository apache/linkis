/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.ujes.jdbc;

/*
 * Notice:
 * if you want to test this module,you must rewrite default SQL we used for local test
 * */

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

public class UJESSQLPreparedStatementTest {
    private static UJESSQLConnection conn;
    private UJESSQLPreparedStatement preStatement;

    @BeforeClass
    public static void getConnection() {
        try {
            conn = JDBCSpiTest.getConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void crud() {
        preStatement = conn.prepareStatement("");
        preStatement.executeUpdate(
                "CREATE TABLE if not exists db.test1236 as select * from ai_fmi_ods.1000_10");
        preStatement.executeUpdate("insert into db.test1236 select * from ai_fmi_ods.1000_10");
        //  preStatement.executeUpdate("update db.test1236 set label=6 where label=1");
        preStatement.executeUpdate("select * from db.test1236");
        UJESSQLResultSet resultSet = preStatement.getResultSet();
        showResult(resultSet);
        preStatement.execute("drop table db.test1236");
        Assert.assertTrue(resultSet.isAfterLast());
    }

    @AfterClass
    public static void closeConnection() {
        conn.close();
    }

    @Test
    public void setObject() {
        preStatement = conn.prepareStatement("? ?");
        preStatement.setObject(1, "show");
        preStatement.setObject(2, "tables");
        Assert.assertTrue(preStatement.execute());
    }

    @Test
    public void execute() {
        preStatement = conn.prepareStatement("show tables");
        Assert.assertTrue(preStatement.execute());
    }

    @Test
    public void selectTest() {
        preStatement = conn.prepareStatement("select * from db.table limit 10");
        UJESSQLResultSet resultSet = preStatement.executeQuery();
        showResult(resultSet);
        Assert.assertTrue(resultSet.isAfterLast());
    }

    private void showResult(UJESSQLResultSet resultSet) {
        while (resultSet.next()) {
            UJESSQLResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print(
                        metaData.getColumnName(i)
                                + ":"
                                + metaData.getColumnTypeName(i)
                                + ": "
                                + resultSet.getObject(i)
                                + "    ");
            }
            System.out.println();
        }
    }

    @Test
    public void executeUpdate() {
        preStatement = conn.prepareStatement("show tables");
        Assert.assertEquals(preStatement.executeUpdate(), 0);
    }

    @Test
    public void executeQuery() {
        preStatement = conn.prepareStatement("show tables");
        Assert.assertTrue(preStatement.executeQuery() instanceof UJESSQLResultSet);
    }
}
