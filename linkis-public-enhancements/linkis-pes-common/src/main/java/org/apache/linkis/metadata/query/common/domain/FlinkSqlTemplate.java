package org.apache.linkis.metadata.query.common.domain;

public class FlinkSqlTemplate {

    public static final String ES_DDL_SQL_TEMPLATE =
            "CREATE TABLE %s ( "
                    + "%s "
                    + ") "
                    + "WITH ("
                    + "'connector' = 'elasticsearch-7',"
                    + "'hosts' = 'http://localhost:9200',"
                    + "'index' = 'testIndex'"
                    + ")";

    public static final String JDBC_DDL_SQL_TEMPLATE =
            "CREATE TABLE %s ( "
                    + "%s "
                    + ") "
                    + "WITH ("
                    + "'connector' = 'jdbc',"
                    + "'url' = '%s',"
                    + "'username' = '%s',"
                    + "'password' = '%',"
                    + "'table-name' = '%s'"
                    + ")";

    public static final String KAFKA_DDL_SQL_TEMPLATE =
            "CREATE TABLE %s ( "
                    + "%s "
                    + ") "
                    + "WITH ("
                    + "'connector' = 'kafka',"
                    + "'properties.bootstrap.servers' = '%s',"
                    + "'properties.group.id' = 'testGroup',"
                    + "'topic' = 'testTopic'"
                    + "'format' = 'csv'"
                    + ")";

    public static final String DML_SQL_TEMPLATE = "INSERT INTO %s SELECT * FROM ${resultTable}";

    public static final String DQL_SQL_TEMPLATE = "SELECT %s FROM %s";

    public static String generateDqlSql(String columns, String table) {
        return String.format(FlinkSqlTemplate.DQL_SQL_TEMPLATE, columns, table);
    }

    public static String generateDmlSql(String table) {
        return String.format(FlinkSqlTemplate.DML_SQL_TEMPLATE, table);
    }
}
