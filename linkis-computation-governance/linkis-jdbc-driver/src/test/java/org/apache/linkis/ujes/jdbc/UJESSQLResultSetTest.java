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

import org.junit.*;

import java.sql.SQLException;

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

public class UJESSQLResultSetTest {

    private static UJESSQLConnection conn;
    private UJESSQLPreparedStatement preStatement;
    private UJESSQLResultSet resultSet;
    private UJESSQLResultSetMetaData metaData;

    @BeforeClass
    public static void getConnection() {
        try {
            conn = CreateConnection.getConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void closeConnection() {
        conn.close();
    }

    @Before
    public void getResultSet() {
        preStatement = conn.prepareStatement("show tables");
        preStatement.execute();
        resultSet = preStatement.getResultSet();
    }

    @After
    public void closeStatement() {
        preStatement.close();
    }

    @Test
    public void getObject() {
        while (resultSet.next()) {
            metaData = resultSet.getMetaData();
            int columnTypeFromVal = UJESSQLTypeParser.parserFromVal(resultSet.getObject(1));
            int columnTypeFromMetaData = metaData.getColumnType(1);
            Assert.assertTrue(columnTypeFromVal == columnTypeFromMetaData);
        }
    }

    @Test
    public void first() {
        resultSet.next();
        Object oldColumnVal = resultSet.getObject(1);
        while (resultSet.next()) {} // move to the end
        Assert.assertTrue(resultSet.first());
        Object newColumnVal = resultSet.getObject(1);
        Assert.assertTrue(oldColumnVal == newColumnVal);
    }

    @Test
    public void afterLast() {
        resultSet.next();
        resultSet.afterLast();
        Assert.assertTrue(resultSet.isAfterLast());
    }

    @Test
    public void beforeFirst() {
        resultSet.next();
        resultSet.beforeFirst();
        Assert.assertTrue(resultSet.isBeforeFirst());
    }

    @Test
    public void getMetaData() {
        resultSet.next();
        Assert.assertTrue(resultSet.getMetaData() != null);
    }

    @Test
    public void next() {
        while (resultSet.next()) {
            metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print(metaData.getColumnName(i) + ":" + resultSet.getObject(i) + "    ");
            }
            System.out.println();
        }
        Assert.assertTrue(resultSet.isAfterLast());
    }
}
