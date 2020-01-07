package com.webank.wedatasphere.linkis.engine.impala.client.util;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * 常量列表
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class Constant {
	public static final String REQUEST_POOL = "REQUEST_POOL";
	
	public static final int DEFAULT_ERROR_INDURANCE = 3;
	
	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final int DEFAULT_PARALLEL_LIMIT = 1;
	public static final int DEFAULT_HEART_BEAT_IN_SECOND = 3;
	
	public static final String DEFAULT_SSL_TYPE = "TLS";
	public static final String DEFAULT_TRUSTSTORE_TYPE = "JKS";
	
	public static final String DEFAULT_LOGIN_TICKET_BIN = "/opt/pems4cdh/gen_impala_ticket";
	public static final String DEFAULT_HIVERC_PATH = ".hiverc";
	
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
	
	public static final String DEFAULT_QUEUE_FORMAT = "root.%s";
	public static final Pattern HIVE_QUEUE_REGEX = Pattern.compile("mapreduce.job.queuename=([\\.\\w]+)");
}
