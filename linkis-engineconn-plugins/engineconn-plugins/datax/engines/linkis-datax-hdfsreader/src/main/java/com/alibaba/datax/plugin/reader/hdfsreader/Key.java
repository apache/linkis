package com.alibaba.datax.plugin.reader.hdfsreader;

public final class Key {

    /**
     * plugin configuration
     */
    final static String PATH = "path";
    final static String DEFAULT_FS = "defaultFS";
    static final String FILETYPE = "fileType";
    static final String HADOOP_CONFIG = "hadoopConfig";
    static final String HAVE_KERBEROS = "haveKerberos";
    static final String KERBEROS_KEYTAB_FILE_PATH = "kerberosKeytabFilePath";
    static final String KERBEROS_PRINCIPAL = "kerberosPrincipal";
    static final String LDAP_USERNAME="ldapUserName";
    static final String LDAP_USERPASSWORD="ldapUserPassword";
    static final String PARTITIONS_VALUES="partitionValues";
    static final String HIVE_TABLE = "hiveTable";
    static final String HIVE_DATABASE="hiveDatabase";
    static final String HIVE_METASTORE_URIS="hiveMetastoreUris";
    static final String HIVE_KEBEROS_PRINCIPAL="hivePrincipal";

    /**
     * keys of meta schema information
     */
    static final String HIVE_META_SERDE_INFO = "serdeInfo";
    static final String HIVE_META_NUM_BUCKETS = "numBuckets";
    static final String HIVE_META_BUCKET_COLS = "bucketCols";
    static final String HIVE_META_SORT_COLS = "sortCols";
    static final String HIVE_META_INPUT_FORMAT = "inputFormat";
    static final String HIVE_META_OUTPUT_FORMAT = "outputFormat";
    static final String HIVE_META_PARAMETERS = "parameters";
    static final String HIVE_META_COMPRESSED = "compressed";

    /**
     * keys of HFile configuration
     */
    public static final String HFILE_PARSE_CONFIG = "hFileParseConfig";
    public static final String HFILE_PARSE_ROW_KEY = "rowKey";
    public static final String HFILE_PARSE_FAMILY = "family";
    public static final String HFIEL_PARSE_QUALIFIER = "qualifier";
    public static final String HFILE_PARSE_VALUE = "value";
    public static final String HFILE_TIMESTAMP = "timestamp";
}
