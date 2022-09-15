/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.errorcode

object EntranceErrorConstants {
  val QUEUE_NOT_EXIST = "10001"
  val USER_PERMISSION_NOT_ALLOW = "10001"
  val USER_RESOURCE_EXHAUSTION = "20001"
  val YARN_RESOURCE_EXHAUSTION = "20002"
  val JOB_COMMIT_EXCEED_MAX_TIME = "20003"
  val FILE_NOT_EXIST = "20003"
  val OUT_OF_MEMORY = "20083"
  val PERMISSION_DENIED = "30001"
  val DATABASE_NOT_FOUND = "40001"
  val TABLE_NOT_FOUND = "40002"
  val FIELD_NOT_FOUND = "40003"
  val PARTITION_FIELD_NOT_FOUND = "40004"
  val BRACKETS_NOT_MATCH = "50001"
  val GROUP_BY_ERROR = "50002"
  val FUNCTION_UNFOUND_ERROR = "50003"
  val FIELD_NAME_CONFLICT = "50004"
  val COLUMN_ERROR = "50005"
  val TABLE_EXIST = "50006"
  val FILE_NOT_FOUND_EXCEPTION = "64001"
  val EXPORT_PERMISSION_ERROR = "64002"
  val EXPORT_CREATE_DIR_ERROR = "64003"
  val IMPORT_ERROR = "50008"
  val NUMBER_NOT_MATCH = "50009"
  val MISSING_BRACKETS = "50010"
  val TYPE_NOT_MATCH = "50011"
  val FIELD_REF_ERROR = "50012"
  val FIELD_EXTRACT_ERROR = "50013"
  val INPUT_MISSING_MATCH = "50014"
  val GROUPBY_MISMATCH_ERROR = "50015"
  val NONE_TYPE_ERROR = "50016"
  val INDEX_OUT_OF_RANGE = "50017"
  val NUMBER_TYPE_DIFF_ERROR = "50018"
  val INVALID_TABLE = "50019"
  val UDF_ARG_ERROR = "50020"
  val AGG_FUNCTION_ERROR = "50021"
  val SYNTAX_ERROR = "50007"
  val USER_KILL_JOB = "50032"
  val PY_VAL_NOT_DEF = "60001"
  val PY_UDF_NOT_DEF = "60002"
  val SQL_ERROR = "50007"
  val PARSE_ERROR = "60003"
  val PERMISSION_DENIED_ERROR = "60010"
  val CANNOT_CONCAT = "61027"
  val PY4JJAVA_ERROR = "60020"
  val UNEXPECT_INDENT = "61028"
  val EXCEED = "60078"
  val UNEXPECT_CHARACTER = "69583"
  val INVALID_ROW_NUMBER = "60091"
  val PARQUET_DECODE_ERROR = "60092"
  val QUERY_YARN_ERROR = "60075"
  val MEM_EXHAUST = "11011"
  val CPU_EXHAUST = "11012"
  val SERVER_EXHAUST = "11013"
  val QUEUE_CPU_EXHAUST = "11014"
  val QUEUE_MEM_EXHAUST = "11015"
  val QUEUE_NUMBER_EXHAUST = "11016"
  val ENGINE_EXHAUST = "11017"

}
