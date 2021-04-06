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

// NOTE: The definitions in this file are part of the binary format of the Impala query
// profiles. They should preserve backwards compatibility and as such some rules apply
// when making changes. Please see RuntimeProfile.thrift for more details.

typedef i64 TTimestamp
typedef i32 TFragmentIdx
typedef i32 TPlanNodeId
typedef i32 TDataSinkId
typedef i32 TTupleId
typedef i32 TSlotId
typedef i32 TTableId
typedef i32 TJoinTableId

// TODO: Consider moving unrelated enums to better locations.

enum TPrimitiveType {
  INVALID_TYPE = 0
  NULL_TYPE = 1
  BOOLEAN = 2
  TINYINT = 3
  SMALLINT = 4
  INT = 5
  BIGINT = 6
  FLOAT = 7
  DOUBLE = 8
  DATE = 9
  DATETIME = 10
  TIMESTAMP = 11
  STRING = 12
  BINARY = 13
  DECIMAL = 14
  CHAR = 15
  VARCHAR = 16
  FIXED_UDA_INTERMEDIATE = 17
}

enum TTypeNodeType {
  SCALAR = 0
  ARRAY = 1
  MAP = 2
  STRUCT = 3
}

struct TScalarType {
  1: required TPrimitiveType type

  // Only set if type == CHAR or type == VARCHAR
  2: optional i32 len

  // Only set for DECIMAL
  3: optional i32 precision
  4: optional i32 scale
}

// Represents a field in a STRUCT type.
// TODO: Model column stats for struct fields.
struct TStructField {
  1: required string name
  2: optional string comment
}

struct TTypeNode {
  1: required TTypeNodeType type

  // only set for scalar types
  2: optional TScalarType scalar_type

  // only used for structs; has struct_fields.size() corresponding child types
  3: optional list<TStructField> struct_fields
}

// A flattened representation of a tree of column types obtained by depth-first
// traversal. Complex types such as map, array and struct have child types corresponding
// to the map key/value, array item type, and struct fields, respectively.
// For scalar types the list contains only a single node.
// Note: We cannot rename this to TType because it conflicts with Thrift's internal TType
// and the generated Python thrift files will not work.
struct TColumnType {
  1: list<TTypeNode> types
}

enum TStmtType {
  QUERY = 0
  DDL = 1
  DML = 2
  EXPLAIN = 3
  LOAD = 4
  SET = 5
  ADMIN_FN = 6
  TESTCASE = 7
}

// Level of verboseness for "explain" output.
enum TExplainLevel {
  MINIMAL = 0
  STANDARD = 1
  EXTENDED = 2
  VERBOSE = 3
}

enum TRuntimeFilterMode {
  // No filters are computed in the FE or the BE.
  OFF = 0

  // Only broadcast filters are computed in the BE, and are only published to the local
  // fragment.
  LOCAL = 1

  // All fiters are computed in the BE, and are published globally.
  GLOBAL = 2
}

enum TPrefetchMode {
  // No prefetching at all.
  NONE = 0

  // Prefetch the hash table buckets.
  HT_BUCKET = 1
}

// A TNetworkAddress is the standard host, port representation of a
// network address. The hostname field must be resolvable to an IPv4
// address.
struct TNetworkAddress {
  1: required string hostname
  2: required i32 port
}

// Wire format for UniqueId
struct TUniqueId {
  1: required i64 hi
  2: required i64 lo
}

enum TFunctionCategory {
  SCALAR = 0
  AGGREGATE = 1
  ANALYTIC = 2
}

enum TFunctionBinaryType {
  // Impala builtin. We can either run this interpreted or via codegen
  // depending on the query option.
  BUILTIN = 0

  // Java UDFs, loaded from *.jar
  JAVA = 1

  // Native-interface, precompiled UDFs loaded from *.so
  NATIVE = 2

  // Native-interface, precompiled to IR; loaded from *.ll
  IR = 3
}

// Represents a fully qualified function name.
struct TFunctionName {
  // Name of the function's parent database. Not set if in global
  // namespace (e.g. builtins)
  1: optional string db_name

  // Name of the function
  2: required string function_name
}

struct TScalarFunction {
  1: required string symbol;
  2: optional string prepare_fn_symbol
  3: optional string close_fn_symbol
}

struct TAggregateFunction {
  1: required TColumnType intermediate_type
  2: required bool is_analytic_only_fn
  3: required string update_fn_symbol
  4: required string init_fn_symbol
  5: optional string serialize_fn_symbol
  6: optional string merge_fn_symbol
  7: optional string finalize_fn_symbol
  8: optional string get_value_fn_symbol
  9: optional string remove_fn_symbol
  10: optional bool ignores_distinct
}

// Represents a function in the Catalog or a query plan, or may be used
// in a minimal form in order to simply specify a function (e.g. when
// included in a minimal catalog update or a TGetPartialCatalogInfo request).
//
// In the case of this latter 'specifier' use case, only the name must be
// set.
struct TFunction {
  // Fully qualified function name.
  1: required TFunctionName name

  // -------------------------------------------------------------------------
  // The following fields are always set, unless this TFunction is being used
  // as a name-only "specifier".
  // -------------------------------------------------------------------------

  // Type of the udf. e.g. hive, native, ir
  2: optional TFunctionBinaryType binary_type

  // The types of the arguments to the function
  3: optional list<TColumnType> arg_types

  // Return type for the function.
  4: optional TColumnType ret_type

  // If true, this function takes var args.
  5: optional bool has_var_args

  // -------------------------------------------------------------------------
  // The following fields are truly optional, even in "full" function objects.
  //
  // Note that TFunction objects are persisted in the user's metastore, so
  // in many cases these fields are optional because they have been added
  // incrementally across releases of Impala.
  // -------------------------------------------------------------------------

  // Optional comment to attach to the function
  6: optional string comment

  7: optional string signature

  // HDFS path for the function binary. This binary must exist at the time the
  // function is created.
  8: optional string hdfs_location

  // One of these should be set.
  9: optional TScalarFunction scalar_fn
  10: optional TAggregateFunction aggregate_fn

  // True for builtins or user-defined functions persisted by the catalog
  11: optional bool is_persistent

  // Last modified time of the 'hdfs_location'. Set by the coordinator to record
  // the mtime its aware of for the lib. Executors expect that the lib they use
  // has the same mtime as the coordinator's. An mtime of -1 makes the mtime check
  // a no-op.
  // Not set when stored in the catalog.
  12: optional i64 last_modified_time


  // NOTE: when adding fields to this struct, do not renumber the field IDs or
  // add new required fields. This struct is serialized into user metastores.
}
