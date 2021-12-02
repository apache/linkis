package com.alibaba.datax.plugin.writer.hdfswriter;

/**
 * Created by shf on 15/10/8.
 */
public class Key {
    /**
     * must have
     */
    static final String PATH = "path";
    /**
     * must have
     */
    final static String DEFAULT_FS = "defaultFS";
    /**
     * must have
     */
    final static String FILE_TYPE = "fileType";
    /**
     * must have
     */
    static final String FILE_NAME = "fileName";
    /**
     * must have for column
     */
    static final String COLUMN = "column";
    static final String NAME = "name";
    static final String TYPE = "type";
    public static final String DATE_FORMAT = "dateFormat";
    /**
     * must have
     */
    static final String WRITE_MODE = "writeMode";
    /**
     * must have
     */
    static final String FIELD_DELIMITER = "fieldDelimiter";
    /**
     * not must, default UTF-8
     */
    static final String ENCODING = "encoding";
    /**
     * not must, default no compress
     */
    static final String COMPRESS = "compress";
    /**
     * not must, not default \N
     */
    public static final String NULL_FORMAT = "nullFormat";
    /**
     * Kerberos
     */
    static final String HAVE_KERBEROS = "haveKerberos";
    static final String KERBEROS_KEYTAB_FILE_PATH = "kerberosKeytabFilePath";
    static final String KERBEROS_PRINCIPAL = "kerberosPrincipal";
    static final String LDAP_USERNAME="ldapUserName";
    static final String LDAP_USERPASSWORD="ldapUserPassword";
    static final String HIVE_KEBEROS_PRINCIPAL="hivePrincipal";
    /**
     * hadoop config
     */
    static final String HADOOP_CONFIG = "hadoopConfig";

    static final String HIVE_METASTORE_URIS="hiveMetastoreUris";
    static final String PARTITIONS_VALUES="partitionValues";
    static final String HIVE_TABLE="hiveTable";
    static final String HIVE_DATABASE="hiveDatabase";

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
    static final String HIVE_META_PARTITION = "partitionKeys";
}
