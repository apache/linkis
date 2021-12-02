package com.alibaba.datax.plugin.rdbms.reader;

/**
 * 编码，时区等配置，暂未定.
 */
public final class Key {
    public final static String JDBC_URL = "jdbcUrl";

    public final static String USERNAME = "username";

    public final static String PASSWORD = "password";

    public final static String TABLE = "table";

    public final static String MANDATORY_ENCODING = "mandatoryEncoding";

    // 是数组配置
    public final static String COLUMN = "column";

    public final static String COLUMN_LIST = "columnList";

    public final static String WHERE = "where";

    public final static String HINT = "hint";

    public final static String SPLIT_PK = "splitPk";

    public final static String SPLIT_MODE = "splitMode";

    public final static String SAMPLE_PERCENTAGE = "samplePercentage";

    public final static String QUERY_SQL = "querySql";

    public final static String SPLIT_PK_SQL = "splitPkSql";


    public final static String PRE_SQL = "preSql";

    public final static String POST_SQL = "postSql";

    public final static String CHECK_SLAVE = "checkSlave";

    public final static String SESSION = "session";

    public final static String DBNAME = "dbName";

    public final static String DRYRUN = "dryRun";

    public final static String CONNPARM = "connParams";

    public final static String HOST = "host";

    public final static String PROXY_HOST = "proxyHost";

    public final static String PORT = "port";

    public final static String PROXY_PORT = "proxyPort";

    public final static String DATABASE = "database";

    public final static String JDBCTEM = "jdbc:mysql://";

    public final static String JDBCORCL = "jdbc:oracle:thin:@";

    public final static String SID = "sid";

    public final static String SERVICENAME = "serviceName";

    public final static String GTID = "gtid";

    public final static String HEARTBEATINTERVAL = "heartBeatInterval";

    public final static String SCHEMA = "schema";

    public final static String SERVERID = "serverId";

}