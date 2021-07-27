/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.distcp.metastore.impl;


import com.webank.wedatasphere.linkis.engineplugin.distcp.exception.DistcpMysqlException;
import com.webank.wedatasphere.linkis.engineplugin.distcp.metastore.MysqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

public class MysqlServiceImpl implements MysqlService {
    Logger logger = LoggerFactory.getLogger(MysqlServiceImpl.class);
    Connection conn = null;
    Statement statement = null;
    public MysqlServiceImpl(String url,String user,String password){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new DistcpMysqlException(90096,"Mysql Init Error,"+e.getMessage());
        }
    }
    @Override
    public List<String> getTablePaths(String[] tables) {
        ResultSet resultSet = null;
        List<String> paths = new ArrayList<>();
        try {
            String querySql = convertSql(tables);
            statement = conn.createStatement();
            resultSet = statement.executeQuery(querySql);
            while (resultSet.next()){
                paths.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new DistcpMysqlException(90097,"Mysql Exec Error,"+e.getMessage());
        }finally {
            close();
        }
        return paths;
    }
    private void close(){
        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                throw new DistcpMysqlException(90098,"Mysql Close Statement Error,"+e.getMessage());
            }
        }
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                throw new DistcpMysqlException(90098,"Mysql Close Conn Error,"+e.getMessage());
            }
        }
    }
    private String convertSql(String[] tables) {
        Set<String> dbs = new HashSet<>();
        Set<Table> tabs = new HashSet<>();
        for (String table:tables) {
            String dbName = table.split("\\.")[0];
            String tabName = table.split("\\.")[1];
            if(tabName.equals("*")){
                dbs.add(dbName);
            }else {
                tabs.add(new Table(dbName,tabName));
            }
        }
        String sql = "";
        if(dbs.size()>0){
            StringBuilder in = new StringBuilder();
            dbs.stream().forEach(db->in.append("'"+db+"'"));
            if(in.length()>0) {
                sql = String.format("select db_location_uri from dbs where name in(%s)", in.toString());
            }
        }
        if(tabs.size()>0){
            StringBuilder in = new StringBuilder();
            tabs.stream().forEach(tab->{if(!dbs.contains(tab.dbName)) {in.append("'"+tab.toString()+"'");}});
            if(sql.length()>0){
                sql+=" union all ";
            }
            if(in.length()>0) {
                sql += String.format("select location from tbls t join sds s on t.sd_id=s.sd_id join dbs d on t.db_id=d.db_id where concat(d.name,'.',t.tbl_name) in(%s)", in);
            }
        }
        logger.info("QUERY MYSQL SQL:"+sql+"--dbs:"+dbs.toString()+"tabs:"+tabs.toString());
        return sql;
    }
    static class Table{
        public Table(String dbName, String tableName) {
            this.dbName = dbName;
            this.tableName = tableName;
        }
        String dbName;
        String tableName;
        @Override
        public String toString() {
            return dbName+"."+tableName;
        }
    }
}
