package org.apache.linkis.metadata.query.service.hive;

import org.apache.commons.collections.MapUtils;
import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.service.AbstractSqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LWL
 * @date 2024/07/17
 */
public class SqlConnection extends AbstractSqlConnection {

    private static final Logger LOG = LoggerFactory.getLogger(SqlConnection.class);

    private static final CommonVars<String> SQL_DRIVER_CLASS =
            CommonVars.apply("wds.linkis.server.mdm.service.hive.driver", "org.apache.hive.jdbc.HiveDriver");

    private static final CommonVars<String> SQL_CONNECT_URL =
            CommonVars.apply(
                    "wds.linkis.server.mdm.service.hive.url", "jdbc:hive2://%s:%s/%s");

    public SqlConnection(
            String host,
            Integer port,
            String username,
            String password,
            String database,
            Map<String, Object> extraParams
    ) throws ClassNotFoundException, SQLException {
        super(host, port, username, password, database, extraParams);
    }

    public List<String> getAllDatabases() throws SQLException {
        List<String> dataBaseName = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW DATABASES");
            while (rs.next()) {
                dataBaseName.add(rs.getString(1));
            }
        } finally {
            closeResource(null, stmt, rs);
        }
        return dataBaseName;
    }

    public List<String> getAllTables(String database) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW TABLES FROM `" + database + "`");
            while (rs.next()) {
                tableNames.add(rs.getString(1));
            }
            return tableNames;
        } finally {
            closeResource(null, stmt, rs);
        }
    }

    public List<MetaColumnInfo> getColumns(String database, String table)
            throws SQLException, ClassNotFoundException {
        List<MetaColumnInfo> columns = new ArrayList<>();
        String columnSql = "SELECT * FROM `" + database + "`.`" + table + "` WHERE 1 = 2";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta = null;
        try {
            List<String> primaryKeys = getPrimaryKeys(database, table);
            ps = conn.prepareStatement(columnSql);
            rs = ps.executeQuery();
            meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                MetaColumnInfo info = new MetaColumnInfo();
                info.setIndex(i);
                info.setLength(meta.getColumnDisplaySize(i));
                info.setNullable((meta.isNullable(i) == ResultSetMetaData.columnNullable));
                info.setName(meta.getColumnName(i));
                info.setType(meta.getColumnTypeName(i));
                if (primaryKeys.contains(meta.getColumnName(i))) {
                    info.setPrimaryKey(true);
                }
                columns.add(info);
            }
        } finally {
            closeResource(null, ps, rs);
        }
        return columns;
    }

    public List<String> getPrimaryKeys(String database, String table) throws SQLException {
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {
            DatabaseMetaData dbMeta = conn.getMetaData();
            rs = dbMeta.getPrimaryKeys(null, database, table);
            while (rs.next()) {
                primaryKeys.add(rs.getString("column_name"));
            }
            return primaryKeys;
        } finally {
            if (null != rs) {
                rs.close();
            }
        }
    }

    /**
     * @param connectMessage
     * @param database
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Connection getDBConnection(
            ConnectMessage connectMessage, String database
    ) throws ClassNotFoundException, SQLException {
        Class.forName(SQL_DRIVER_CLASS.getValue());
        String url =
                String.format(
                        SQL_CONNECT_URL.getValue(), connectMessage.host, connectMessage.port, database);
        if (MapUtils.isNotEmpty(connectMessage.extraParams)) {
            String extraParamString =
                    connectMessage.extraParams.entrySet().stream()
                            .map(e -> String.join("=", e.getKey(), String.valueOf(e.getValue())))
                            .collect(Collectors.joining("&"));
            url += "?" + extraParamString;
        }
        LOG.info("jdbc connection url: {}", url);
        try {
            return DriverManager.getConnection(url, connectMessage.username, connectMessage.password);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getSqlConnectUrl() {
        return SQL_CONNECT_URL.getValue();
    }

    @Override
    public String generateJdbcDdlSql(String database, String table) {
        String columnSql = String.format("SHOW CREATE TABLE %s.%s", database, table);
        PreparedStatement ps = null;
        ResultSet rs = null;
        String ddl = "";
        try {
            ps = conn.prepareStatement(columnSql);
            rs = ps.executeQuery();
            if (rs.next()) {
                ddl = rs.getString("Create Table");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResource(null, ps, rs);
        }
        return ddl.replaceAll("\n", "\n\t");
    }
}
