// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

namespace java org.apache.impala.thrift

include "ExecStats.thrift"
include "Status.thrift"
include "Types.thrift"
include "beeswax.thrift"
include "TCLIService.thrift"
include "RuntimeProfile.thrift"

// ImpalaService accepts query execution options through beeswax.Query.configuration in
// key:value form. For example, the list of strings could be:
//     "num_nodes:1", "abort_on_error:false"
// The valid keys are listed in this enum. They map to TQueryOptions.
// Note: If you add an option or change the default, you also need to update:
// - ImpalaInternalService.thrift: TQueryOptions
// - SetQueryOption(), SetQueryOptions()
// - TQueryOptionsToMap()
enum TImpalaQueryOptions {
  // if true, abort execution on the first error
  ABORT_ON_ERROR = 0

  // maximum # of errors to be reported; Unspecified or 0 indicates backend default
  MAX_ERRORS = 1

  // if true disable llvm codegen
  DISABLE_CODEGEN = 2

  // batch size to be used by backend; Unspecified or a size of 0 indicates backend
  // default
  BATCH_SIZE = 3

  // a per-machine approximate limit on the memory consumption of this query;
  // unspecified or a limit of 0 means no limit;
  // otherwise specified either as:
  // a) an int (= number of bytes);
  // b) a float followed by "M" (MB) or "G" (GB)
  MEM_LIMIT = 4

  // specifies the degree of parallelism with which to execute the query;
  // 1: single-node execution
  // NUM_NODES_ALL: executes on all nodes that contain relevant data
  // NUM_NODES_ALL_RACKS: executes on one node per rack that holds relevant data
  // > 1: executes on at most that many nodes at any point in time (ie, there can be
  //      more nodes than numNodes with plan fragments for this query, but at most
  //      numNodes would be active at any point in time)
  // Constants (NUM_NODES_ALL, NUM_NODES_ALL_RACKS) are defined in JavaConstants.thrift.
  NUM_NODES = 5

  // maximum length of the scan range; only applicable to HDFS scan range; Unspecified or
  // a length of 0 indicates backend default;
  MAX_SCAN_RANGE_LENGTH = 6

  MAX_IO_BUFFERS = 7 // Removed

  // Number of scanner threads.
  NUM_SCANNER_THREADS = 8

  ALLOW_UNSUPPORTED_FORMATS = 9 // Removed

  DEFAULT_ORDER_BY_LIMIT = 10 // Removed

  // DEBUG ONLY:
  // Accepted formats:
  // 1. ExecNode actions
  //  "[<instance idx>:]<node id>:<TExecNodePhase>:<TDebugAction>",
  //  the exec node with the given id will perform the specified action in the given
  //  phase. If the optional backend number (starting from 0) is specified, only that
  //  backend instance will perform the debug action, otherwise all backends will behave
  //  in that way.
  //  If the string doesn't have the required format or if any of its components is
  //  invalid, the option is ignored.
  //
  // 2. Global actions
  //  "<global label>:<command>@<param0>@<param1>@...<paramN>",
  //  global labels are marked in the code with DEBUG_ACTION*() macros.
  //  Available global actions:
  //  - SLEEP@<ms> will sleep for the 'ms' milliseconds.
  //  - JITTER@<ms>[@<probability>] will sleep for a random amount of time between 0
  //    and 'ms' milliseconds with the given probability. If <probability> is omitted,
  //    it is 1.0.
  //  - FAIL[@<probability>] returns an INTERNAL_ERROR status with the given
  //    probability. If <probability> is omitted, it is 1.0.
  //
  // Only a single ExecNode action is allowed, but multiple global actions can be
  // specified. To specify multiple actions, separate them with "|".
  DEBUG_ACTION = 11

  ABORT_ON_DEFAULT_LIMIT_EXCEEDED = 12 // Removed

  // Compression codec when inserting into tables.
  // Valid values are "snappy", "gzip", "bzip2" and "none"
  // Leave blank to use default.
  COMPRESSION_CODEC = 13

  SEQ_COMPRESSION_MODE = 14 // Removed

  // HBase scan query option. If set and > 0, HBASE_CACHING is the value for
  // "hbase.client.Scan.setCaching()" when querying HBase table. Otherwise, use backend
  // default.
  // If the value is too high, then the hbase region server will have a hard time (GC
  // pressure and long response times). If the value is too small, then there will be
  // extra trips to the hbase region server.
  HBASE_CACHING = 15

  // HBase scan query option. If set, HBase scan will always set
  // "hbase.client.setCacheBlocks" to CACHE_BLOCKS. Default is false.
  // If the table is large and the query is doing big scan, set it to false to
  // avoid polluting the cache in the hbase region server.
  // If the table is small and the table is used several time, set it to true to improve
  // performance.
  HBASE_CACHE_BLOCKS = 16

  // Target file size for inserts into parquet tables. 0 uses the default.
  PARQUET_FILE_SIZE = 17

  // Level of detail for explain output (NORMAL, VERBOSE).
  EXPLAIN_LEVEL = 18

  // If true, waits for the result of all catalog operations to be processed by all
  // active impalad in the cluster before completing.
  SYNC_DDL = 19

  // Request pool this request should be submitted to. If not set
  // the pool is determined based on the user.
  REQUEST_POOL = 20

  V_CPU_CORES = 21 // Removed

  RESERVATION_REQUEST_TIMEOUT = 22 // Removed

  // if true, disables cached reads. This option has no effect if REPLICA_PREFERENCE is
  // configured.
  // TODO: IMPALA-4306: retire at compatibility-breaking version
  DISABLE_CACHED_READS = 23

  // Temporary testing flag
  DISABLE_OUTERMOST_TOPN = 24

  RM_INITIAL_MEM = 25 // Removed

  // Time, in s, before a query will be timed out if it is inactive. May not exceed
  // --idle_query_timeout if that flag > 0. If 0, falls back to --idle_query_timeout.
  QUERY_TIMEOUT_S = 26

  // Test hook for spill to disk operators
  BUFFER_POOL_LIMIT = 27

  // Transforms all count(distinct) aggregations into NDV()
  APPX_COUNT_DISTINCT = 28

  // If true, allows Impala to internally disable spilling for potentially
  // disastrous query plans. Impala will excercise this option if a query
  // has no plan hints, and at least one table is missing relevant stats.
  DISABLE_UNSAFE_SPILLS = 29

  // If the number of rows that are processed for a single query is below the
  // threshold, it will be executed on the coordinator only with codegen disabled
  EXEC_SINGLE_NODE_ROWS_THRESHOLD = 30

  // If true, use the table's metadata to produce the partition columns instead of table
  // scans whenever possible. This option is opt-in by default as this optimization may
  // produce different results than the scan based approach in some edge cases.
  OPTIMIZE_PARTITION_KEY_SCANS = 31

  // Prefered memory distance of replicas. This parameter determines the pool of replicas
  // among which scans will be scheduled in terms of the distance of the replica storage
  // from the impalad.
  REPLICA_PREFERENCE = 32

  // Enables random backend selection during scheduling.
  SCHEDULE_RANDOM_REPLICA = 33

  SCAN_NODE_CODEGEN_THRESHOLD = 34 // Removed

  // If true, the planner will not generate plans with streaming preaggregations.
  DISABLE_STREAMING_PREAGGREGATIONS = 35

  RUNTIME_FILTER_MODE = 36

  // Size (in bytes) of a runtime Bloom Filter. Will be rounded up to nearest power of
  // two.
  RUNTIME_BLOOM_FILTER_SIZE = 37

  // Time (in ms) to wait in scans for runtime filters to arrive.
  RUNTIME_FILTER_WAIT_TIME_MS = 38

  // If true, disable application of runtime filters to individual rows.
  DISABLE_ROW_RUNTIME_FILTERING = 39

  // Maximum number of bloom runtime filters allowed per query.
  MAX_NUM_RUNTIME_FILTERS = 40

  // If true, use UTF-8 annotation for string columns. Note that char and varchar columns
  // always use the annotation.
  PARQUET_ANNOTATE_STRINGS_UTF8 = 41

  // Determines how to resolve Parquet files' schemas in the absence of field IDs (which
  // is always, since fields IDs are NYI). Valid values are "position" and "name".
  PARQUET_FALLBACK_SCHEMA_RESOLUTION = 42

  // Multi-threaded execution: degree of parallelism = number of active threads per
  // backend
  MT_DOP = 43

  // If true, INSERT writes to S3 go directly to their final location rather than being
  // copied there by the coordinator. We cannot do this for INSERT OVERWRITES because for
  // those queries, the coordinator deletes all files in the final location before copying
  // the files there.
  // TODO: Find a way to get this working for INSERT OVERWRITEs too.
  S3_SKIP_INSERT_STAGING = 44

  // Maximum runtime bloom filter size, in bytes.
  RUNTIME_FILTER_MAX_SIZE = 45

  // Minimum runtime bloom filter size, in bytes.
  RUNTIME_FILTER_MIN_SIZE = 46

  // Prefetching behavior during hash tables' building and probing.
  PREFETCH_MODE = 47

  // Additional strict handling of invalid data parsing and type conversions.
  STRICT_MODE = 48

  // A limit on the amount of scratch directory space that can be used;
  // Unspecified or a limit of -1 means no limit;
  // Otherwise specified in the same way as MEM_LIMIT.
  SCRATCH_LIMIT = 49

  // Indicates whether the FE should rewrite Exprs for optimization purposes.
  // It's sometimes useful to disable rewrites for testing, e.g., expr-test.cc.
  ENABLE_EXPR_REWRITES = 50

  // Indicates whether to use the new decimal semantics, which includes better
  // rounding and output types for multiply / divide
  DECIMAL_V2 = 51

  // Indicates whether to use dictionary filtering for Parquet files
  PARQUET_DICTIONARY_FILTERING = 52

  // Policy for resolving nested array fields in Parquet files.
  // An Impala array type can have several different representations in
  // a Parquet schema (three, two, or one level). There is fundamental ambiguity
  // between the two and three level encodings with index-based field resolution.
  // The ambiguity can manually be resolved using this query option, or by using
  // PARQUET_FALLBACK_SCHEMA_RESOLUTION=name.
  PARQUET_ARRAY_RESOLUTION = 53

  // Indicates whether to read statistics from Parquet files and use them during query
  // processing. This includes skipping data based on the statistics and computing query
  // results like "select min()".
  PARQUET_READ_STATISTICS = 54

  // Join distribution mode that is used when the join inputs have an unknown
  // cardinality, e.g., because of missing table statistics.
  DEFAULT_JOIN_DISTRIBUTION_MODE = 55

  // If the number of rows processed per node is below the threshold and disable_codegen
  // is unset, codegen will be automatically be disabled by the planner.
  DISABLE_CODEGEN_ROWS_THRESHOLD = 56

  // The default spillable buffer size, in bytes.
  DEFAULT_SPILLABLE_BUFFER_SIZE = 57

  // The minimum spillable buffer size, in bytes.
  MIN_SPILLABLE_BUFFER_SIZE = 58

  // The maximum row size that memory is reserved for, in bytes.
  MAX_ROW_SIZE = 59

  // The time, in seconds, that a session may be idle for before it is closed (and all
  // running queries cancelled) by Impala. If 0, idle sessions never expire.
  IDLE_SESSION_TIMEOUT = 60

  // Minimum number of bytes that will be scanned in COMPUTE STATS TABLESAMPLE,
  // regardless of the user-supplied sampling percent.
  COMPUTE_STATS_MIN_SAMPLE_SIZE = 61

  // Time limit, in s, before a query will be timed out after it starts executing. Does
  // not include time spent in planning, scheduling or admission control. A value of 0
  // means no time limit.
  EXEC_TIME_LIMIT_S = 62

  // When a query has both grouping and distinct exprs, impala can optionally include the
  // distinct exprs in the hash exchange of the first aggregation phase to spread the data
  // among more nodes. However, this plan requires another hash exchange on the grouping
  // exprs in the second phase which is not required when omitting the distinct exprs in
  // the first phase. Shuffling by both is better if the grouping exprs have low NDVs.
  SHUFFLE_DISTINCT_EXPRS = 63

  // This only has an effect if memory-estimate-based admission control is enabled, i.e.
  // max_mem_resources is set for the pool and, *contrary to best practices*, MEM_LIMIT
  // is not set. In that case, then min(MAX_MEM_ESTIMATE_FOR_ADMISSION,
  // planner memory estimate) is used for admission control purposes. This provides a
  // workaround if the planner's memory estimate is too high and prevents a runnable
  // query from being admitted. 0 or -1 means this has no effect. Defaults to 0.
  MAX_MEM_ESTIMATE_FOR_ADMISSION = 64

  // Admission control will reject queries when the number of reserved threads per backend
  // for the query exceeds this number. 0 or -1 means this has no effect.
  THREAD_RESERVATION_LIMIT = 65

  // Admission control will reject queries when the total number of reserved threads
  // across all backends for the query exceeds this number. 0 or -1 means this has no
  // effect.
  THREAD_RESERVATION_AGGREGATE_LIMIT = 66

  // Overrides the -kudu_read_mode flag to set the consistency level for Kudu scans.
  // Possible values are DEFAULT, READ_LATEST, and READ_AT_SNAPSHOT.
  KUDU_READ_MODE = 67

  // Allow reading of erasure coded files.
  ALLOW_ERASURE_CODED_FILES = 68

  // The timezone used in UTC<->localtime conversions. The default is the OS's timezone
  // at the coordinator, which can be overridden by environment variable $TZ.
  TIMEZONE = 69

  // Scan bytes limit, after which a query will be terminated with an error.
  SCAN_BYTES_LIMIT = 70

  // CPU time limit in seconds, after which a query will be terminated with an error.
  // Note that until IMPALA-7318 is fixed, CPU usage can be very stale and this may not
  // terminate queries soon enough.
  CPU_LIMIT_S = 71

  // The max number of estimated bytes a TopN operator is allowed to materialize, if the
  // planner thinks a TopN operator will exceed this limit, it falls back to a TotalSort
  // operator which is capable of spilling to disk (unlike the TopN operator which keeps
  // everything in memory). 0 or -1 means this has no effect.
  TOPN_BYTES_LIMIT = 72

  // An opaque string, not used by Impala itself, that can be used to identify
  // the client, like a User-Agent in HTTP. Drivers should set this to
  // their version number. May also be used by tests to help identify queries.
  CLIENT_IDENTIFIER = 73

  // Probability to enable tracing of resource usage consumption on all fragment instance
  // executors of a query. Must be between 0 and 1 inclusive, 0 means no query will be
  // traced, 1 means all queries will be traced.
  RESOURCE_TRACE_RATIO = 74

  // The maximum number of executor candidates to consider when scheduling remote
  // HDFS ranges. When non-zero, the scheduler generates a consistent set of
  // executor candidates based on the filename and offset. This algorithm is designed
  // to avoid changing file to node mappings when nodes come and go. It then picks from
  // among the candidates by the same method used for local scan ranges. Limiting the
  // number of nodes that can read a single file provides a type of simulated locality.
  // This increases the efficiency of file-related caches (e.g. the HDFS file handle
  // cache). If set to 0, the number of executor candidates is unlimited, and remote
  // ranges will be scheduled across all executors.
  NUM_REMOTE_EXECUTOR_CANDIDATES = 75

  // A limit on the number of rows produced by the query. The query will be
  // canceled if the query is still executing after this limit is hit. A value
  // of 0 means there is no limit on the number of rows produced.
  NUM_ROWS_PRODUCED_LIMIT = 76

  // Set when attempting to load a planner testcase. Typically used by developers for
  // debugging a testcase. Should not be set in user clusters. If set, a warning
  // is emitted in the query runtime profile.
  PLANNER_TESTCASE_MODE = 77

  // Specifies the default table file format.
  DEFAULT_FILE_FORMAT = 78
}

// The summary of a DML statement.
// TODO: Rename to reflect that this is for all DML.
struct TInsertResult {
  // Number of modified rows per partition. Only applies to HDFS and Kudu tables.
  // The keys represent partitions to create, coded as k1=v1/k2=v2/k3=v3..., with
  // the root in an unpartitioned table being the empty string.
  1: required map<string, i64> rows_modified

  // Number of row operations attempted but not completed due to non-fatal errors
  // reported by the storage engine that Impala treats as warnings. Only applies to Kudu
  // tables. This includes errors due to duplicate/missing primary keys, nullability
  // constraint violations, and primary keys in uncovered partition ranges.
  // TODO: Provide a detailed breakdown of these counts by error. IMPALA-4416.
  2: optional i64 num_row_errors
}

// Response from a call to PingImpalaService
struct TPingImpalaServiceResp {
  // The Impala service's version string.
  1: string version

  // The Impalad's webserver address.
  2: string webserver_address
}

// Parameters for a ResetTable request which will invalidate a table's metadata.
// DEPRECATED.
struct TResetTableReq {
  // Name of the table's parent database.
  1: required string db_name

  // Name of the table.
  2: required string table_name
}

// For all rpc that return a TStatus as part of their result type,
// if the status_code field is set to anything other than OK, the contents
// of the remainder of the result type is undefined (typically not set)
service ImpalaService extends beeswax.BeeswaxService {
  // Cancel execution of query. Returns RUNTIME_ERROR if query_id
  // unknown.
  // This terminates all threads running on behalf of this query at
  // all nodes that were involved in the execution.
  // Throws BeeswaxException if the query handle is invalid (this doesn't
  // necessarily indicate an error: the query might have finished).
  Status.TStatus Cancel(1:beeswax.QueryHandle query_id)
      throws(1:beeswax.BeeswaxException error);

  // Invalidates all catalog metadata, forcing a reload
  // DEPRECATED; execute query "invalidate metadata" to refresh metadata
  Status.TStatus ResetCatalog();

  // Invalidates a specific table's catalog metadata, forcing a reload on the next access
  // DEPRECATED; execute query "refresh <table>" to refresh metadata
  Status.TStatus ResetTable(1:TResetTableReq request)

  // Returns the runtime profile string for the given query handle.
  string GetRuntimeProfile(1:beeswax.QueryHandle query_id)
      throws(1:beeswax.BeeswaxException error);

  // Closes the query handle and return the result summary of the insert.
  TInsertResult CloseInsert(1:beeswax.QueryHandle handle)
      throws(1:beeswax.QueryNotFoundException error, 2:beeswax.BeeswaxException error2);

  // Client calls this RPC to verify that the server is an ImpalaService. Returns the
  // server version.
  TPingImpalaServiceResp PingImpalaService();

  // Returns the summary of the current execution.
  ExecStats.TExecSummary GetExecSummary(1:beeswax.QueryHandle handle)
      throws(1:beeswax.QueryNotFoundException error, 2:beeswax.BeeswaxException error2);
}

// Impala HiveServer2 service

struct TGetExecSummaryReq {
  1: optional TCLIService.TOperationHandle operationHandle

  2: optional TCLIService.TSessionHandle sessionHandle
}

struct TGetExecSummaryResp {
  1: required TCLIService.TStatus status

  2: optional ExecStats.TExecSummary summary
}

struct TGetRuntimeProfileReq {
  1: optional TCLIService.TOperationHandle operationHandle

  2: optional TCLIService.TSessionHandle sessionHandle

  3: optional RuntimeProfile.TRuntimeProfileFormat format =
      RuntimeProfile.TRuntimeProfileFormat.STRING
}

struct TGetRuntimeProfileResp {
  1: required TCLIService.TStatus status

  // Will be set on success if TGetRuntimeProfileReq.format was STRING or BASE64.
  2: optional string profile

  // Will be set on success if TGetRuntimeProfileReq.format was THRIFT.
  3: optional RuntimeProfile.TRuntimeProfileTree thrift_profile
}

service ImpalaHiveServer2Service extends TCLIService.TCLIService {
  // Returns the exec summary for the given query
  TGetExecSummaryResp GetExecSummary(1:TGetExecSummaryReq req);

  // Returns the runtime profile string for the given query
  TGetRuntimeProfileResp GetRuntimeProfile(1:TGetRuntimeProfileReq req);
}
