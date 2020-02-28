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

package com.webank.wedatasphere.linkis.metadatamanager.service;

import com.webank.wedatasphere.linkis.metadatamanager.common.Json;
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaColumnInfo;
import com.webank.wedatasphere.linkis.metadatamanager.common.service.AbstractMetaService;
import com.webank.wedatasphere.linkis.metadatamanager.common.service.MetadataConnection;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author davidhua
 * 2020/02/14
 */
@Component
public class SqlMetaService extends AbstractMetaService<SqlConnection> {
    @Override
    public MetadataConnection<SqlConnection> getConnection(String operator, Map<String, Object> params) throws Exception {
        String host = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_HOST.getValue(), ""));
        //After deserialize, Integer will be Double, Why?
        Integer port = (Double.valueOf(String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PORT.getValue(), 0)))).intValue();
        String username = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_USERNAME.getValue(), ""));
        String password = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PASSWORD.getValue(), ""));
        Map<String, Object> extraParams = new HashMap<>();
        Object sqlParamObj =  params.get(SqlParamsMapper.PARAM_SQL_EXTRA_PARAMS.getValue());
        if(null != sqlParamObj){
            if(!(sqlParamObj instanceof Map)){
                extraParams = Json.fromJson(String.valueOf(sqlParamObj), Map.class, String.class, Object.class);
            }else{
                extraParams = (Map<String, Object>)sqlParamObj;
            }
        }
        assert extraParams != null;
        return new MetadataConnection<>(new SqlConnection(host, port, username, password, extraParams));
    }

    @Override
    public List<String> queryDatabases(SqlConnection connection) {
        try {
            return connection.getAllDatabases();
        } catch (SQLException e) {
            throw new RuntimeException("Fail to get Sql databases(获取数据库列表失败)", e);
        }
    }

    @Override
    public List<String> queryTables(SqlConnection connection, String database) {
        try {
            return connection.getAllTables(database);
        } catch (SQLException e) {
            throw new RuntimeException("Fail to get Sql tables(获取表列表失败)", e);
        }
    }

    @Override
    public List<MetaColumnInfo> queryColumns(SqlConnection connection, String database, String table) {
        try {
            return connection.getColumns(database, table);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Fail to get Sql columns(获取字段列表失败)", e);
        }
    }
}
