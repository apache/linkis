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
include "Metrics.thrift"
include "Types.thrift"

// NOTE: This file and the includes above define the format of Impala query profiles. As
// newer versions of Impala should be able to read profiles written by older versions,
// some best practices must be followed when making changes to the structures below:
//
// - Only append new values at the end of enums.
// - Only add new fields at the end of structures, and always make them optional.
// - Don't remove fields.
// - Don't change the numbering of fields.

// Represents the different formats a runtime profile can be represented in.
enum TRuntimeProfileFormat {
  // Pretty printed.
  STRING = 0

  // The thrift profile, serialized, compressed, and encoded. Used for the query log.
  // See RuntimeProfile::SerializeToArchiveString.
  BASE64 = 1

  // TRuntimeProfileTree.
  THRIFT = 2
}

// Counter data
struct TCounter {
  1: required string name
  2: required Metrics.TUnit unit
  3: required i64 value
}

// Thrift version of RuntimeProfile::EventSequence - list of (label, timestamp) pairs
// which represent an ordered sequence of events.
struct TEventSequence {
  1: required string name
  2: required list<i64> timestamps
  3: required list<string> labels
}

// Struct to contain data sampled at even time intervals (e.g. ram usage every
// N seconds).
// values[0] represents the value when the counter stated (e.g. fragment started)
// values[1] is the value at period_ms (e.g. 500 ms later)
// values[2] is the value at 2 * period_ms (e.g. 1sec since start)
// This can be used to reconstruct a time line for a particular counter.
struct TTimeSeriesCounter {
  1: required string name
  2: required Metrics.TUnit unit

  // Period of intervals in ms
  3: required i32 period_ms

  // The sampled values.
  4: required list<i64> values

  // The index of the first value in this series (this is equal to the total number of
  // values contained in previous updates for this counter). Values > 0 mean that this
  // series contains an interval of a larger series. For values > 0, period_ms should be
  // ignored, as chunked counters don't resample their values.
  5: optional i64 start_index
}

// Thrift version of RuntimeProfile::SummaryStatsCounter.
struct TSummaryStatsCounter {
  1: required string name
  2: required Metrics.TUnit unit
  3: required i64 sum
  4: required i64 total_num_values
  5: required i64 min_value
  6: required i64 max_value
}

// Metadata to help identify what entity the profile node corresponds to.
union TRuntimeProfileNodeMetadata {
  // Set if this node corresponds to a plan node.
  1: Types.TPlanNodeId plan_node_id

  // Set if this node corresponds to a data sink.
  2: Types.TDataSinkId data_sink_id
}

// A single runtime profile
struct TRuntimeProfileNode {
  1: required string name
  2: required i32 num_children
  3: required list<TCounter> counters

  // Legacy field. May contain the node ID for plan nodes.
  // Replaced by node_metadata, which contains richer metadata.
  4: required i64 metadata

  // indicates whether the child will be printed with extra indentation;
  // corresponds to indent param of RuntimeProfile::AddChild()
  5: required bool indent

  // map of key,value info strings that capture any kind of additional information
  // about the profiled object
  6: required map<string, string> info_strings

  // Auxilliary structure to capture the info strings display order when printed
  7: required list<string> info_strings_display_order

  // map from parent counter name to child counter name
  8: required map<string, set<string>> child_counters_map

  // List of event sequences that capture ordered events in a query's lifetime
  9: optional list<TEventSequence> event_sequences

  // List of time series counters
  10: optional list<TTimeSeriesCounter> time_series_counters

  // List of summary stats counters
  11: optional list<TSummaryStatsCounter> summary_stats_counters

  // Metadata about the entity that this node refers to.
  12: optional TRuntimeProfileNodeMetadata node_metadata
}

// A flattened tree of runtime profiles, obtained by an
// pre-order traversal
struct TRuntimeProfileTree {
  1: required list<TRuntimeProfileNode> nodes
  2: optional ExecStats.TExecSummary exec_summary
}

// A list of TRuntimeProfileTree structures.
struct TRuntimeProfileForest {
  1: required list<TRuntimeProfileTree> profile_trees
  2: optional TRuntimeProfileTree host_profile
}
