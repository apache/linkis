package org.apache.linkis.metadata.query.service;

import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadata.query.service.conf.SqlParamsMapper;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.service.AbstractMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataConnection;
import org.apache.linkis.metadata.query.service.oracle.SqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleMetaService extends AbstractMetaService<SqlConnection> {
    private static final Logger LOG = LoggerFactory.getLogger(OracleMetaService.class);
    @Override
    public MetadataConnection<SqlConnection> getConnection(String operator, Map<String, Object> params) throws Exception {
        String host = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_HOST.getValue(), ""));
        //After deserialize, Integer will be Double, Why?
        Integer port = (Double.valueOf(String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PORT.getValue(), 0)))).intValue();
        String username = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_USERNAME.getValue(), ""));
        String password = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_PASSWORD.getValue(), ""));
        // 无法在当前数据库连接下直接切换到另外一个数据库，也没有像 MySQL 一样能 show tables from xxxx、select * from database.table ....
        String database = String.valueOf(params.getOrDefault(SqlParamsMapper.PARAM_SQL_DATABASE.getValue(), ""));
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
        LOG.info("oracle connection params:{}",params.toString());
        LOG.info("oracle connection host:{},port:{},username:{},password:{},database:{}",host,port,username,password,database);
        return new MetadataConnection<>(new SqlConnection(host, port, username, password, database, extraParams));
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
    public List<String> queryTables(SqlConnection connection, String schemaname) {
        try {
            return connection.getAllTables(schemaname);
        } catch (SQLException e) {
            throw new RuntimeException("Fail to get Sql tables(获取表列表失败)", e);
        }
    }

    @Override
    public List<MetaColumnInfo> queryColumns(SqlConnection connection, String schemaname, String table) {
        try {
            return connection.getColumns(schemaname, table);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Fail to get Sql columns(获取字段列表失败)", e);
        }
    }
}
